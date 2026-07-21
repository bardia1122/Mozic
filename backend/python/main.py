"""C3: the WebSocket chat server, rewritten from Kotlin/Ktor to Python/FastAPI.
Catalog/social/auth REST (C1/C2) is served directly by Supabase (PostgREST +
Auth) — this module exists only for the send/ack/read/typing protocol
Supabase's Realtime (a generic Postgres change-feed) can't provide. See
PROTOCOL.md for the wire format this implements; it is unchanged from the
Kotlin version, so the Android client needs zero changes to talk to this one.

Run with: uvicorn main:app --host 0.0.0.0 --port 8080  (see README.md)
"""
from __future__ import annotations

import json
import logging
import time
from contextlib import asynccontextmanager

import httpx
import uvicorn
from fastapi import FastAPI, Header, HTTPException, Request, WebSocket, WebSocketDisconnect
from fastapi.responses import PlainTextResponse, Response

import env
from connection_manager import ConnectionManager
from supabase_gateway import SupabaseGateway

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
log = logging.getLogger("mozic.chat")

connections = ConnectionManager()


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with httpx.AsyncClient() as client:
        app.state.gateway = SupabaseGateway(
            client=client,
            base_url=env.require("SUPABASE_URL"),
            publishable_key=env.require("SUPABASE_PUBLISHABLE_KEY"),
            secret_key=env.require("SUPABASE_SECRET_KEY"),
        )
        yield


app = FastAPI(lifespan=lifespan)


@app.get("/health")
async def health() -> PlainTextResponse:
    return PlainTextResponse("ok")


async def _authenticated_user_id(gateway: SupabaseGateway, authorization: str | None) -> str:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=401, detail="missing bearer token")
    user_id = await gateway.authenticate(authorization.split(" ", 1)[1])
    if user_id is None:
        raise HTTPException(status_code=401, detail="invalid token")
    return user_id


@app.post("/avatar")
async def upload_avatar(request: Request, authorization: str | None = Header(default=None)) -> dict:
    """
    Proxies the avatar upload with the secret key instead of the Android app
    calling Supabase Storage directly — see `SupabaseGateway.upload_avatar`'s
    docstring for why direct-from-client uploads don't work on this project.
    """
    gateway: SupabaseGateway = request.app.state.gateway
    user_id = await _authenticated_user_id(gateway, authorization)
    image_bytes = await request.body()
    content_type = request.headers.get("content-type", "image/jpeg")
    url = await gateway.upload_avatar(user_id, image_bytes, content_type)
    return {"avatarUrl": url}


@app.delete("/avatar", status_code=204)
async def delete_avatar(request: Request, authorization: str | None = Header(default=None)) -> Response:
    gateway: SupabaseGateway = request.app.state.gateway
    user_id = await _authenticated_user_id(gateway, authorization)
    await gateway.remove_avatar(user_id)
    return Response(status_code=204)


@app.websocket("/ws")
async def chat_websocket(websocket: WebSocket) -> None:
    # Ktor's routing DSL accepts the handshake before the handler body runs,
    # so this mirrors that: accept unconditionally, validate after, close
    # with 1008 if the token doesn't check out — same observable behavior as
    # the Kotlin version (an immediate open+close, not a rejected handshake).
    await websocket.accept()
    gateway: SupabaseGateway = websocket.app.state.gateway

    token = websocket.query_params.get("token")
    user_id = await gateway.authenticate(token) if token else None
    if user_id is None:
        await websocket.close(code=1008, reason="invalid or missing token")
        return

    connections.register(user_id, websocket)
    log.info("user %s connected", user_id)

    try:
        raw_since = websocket.query_params.get("since")
        since_epoch_ms = int(raw_since) if raw_since and raw_since.lstrip("-").isdigit() else None
        if since_epoch_ms is not None:
            conversation_ids = await gateway.conversation_ids_for(user_id)
            for message in await gateway.messages_since(conversation_ids, since_epoch_ms):
                await websocket.send_json({"type": "message", "message": message})

        while True:
            text = await websocket.receive_text()
            try:
                root = json.loads(text)
            except json.JSONDecodeError:
                log.warning("malformed frame from %s: %s", user_id, text)
                continue
            frame_type = root.get("type")
            if frame_type == "send":
                await _on_send(user_id, root, gateway)
            elif frame_type == "read":
                await _on_read(user_id, root, gateway)
            elif frame_type == "typing":
                await _on_typing(user_id, root, gateway)
            else:
                log.warning("unknown frame type from %s: %s", user_id, root)
    except WebSocketDisconnect:
        pass
    finally:
        connections.unregister(user_id, websocket)
        log.info("user %s disconnected", user_id)


async def _resolve_participants(conversation_id: str, gateway: SupabaseGateway) -> tuple[str, str] | None:
    cached = connections.participants_of(conversation_id)
    if cached is not None:
        return cached
    fetched = await gateway.participants_of(conversation_id)
    if fetched is None:
        return None
    connections.cache_participants(conversation_id, *fetched)
    return fetched


async def _on_send(sender_id: str, frame: dict, gateway: SupabaseGateway) -> None:
    message = frame.get("message")
    if not message or message.get("senderId") != sender_id:
        log.warning("dropped send: claimed senderId != authenticated user %s", sender_id)
        return

    conversation_id = message.get("conversationId")
    participants = conversation_id and await _resolve_participants(conversation_id, gateway)
    if not participants or sender_id not in participants:
        log.warning("dropped send: %s is not a participant of %s", sender_id, conversation_id)
        return

    persisted = {**message, "status": "SENT"}
    if not await gateway.insert_message(persisted):
        log.warning("failed to persist message %s in conversation %s", message.get("id"), conversation_id)
        return

    await connections.send_to(sender_id, {"type": "ack", "messageId": message["id"]})
    peer_id = participants[1] if sender_id == participants[0] else participants[0]
    await connections.send_to(peer_id, {"type": "message", "message": persisted})


async def _on_read(reader_id: str, frame: dict, gateway: SupabaseGateway) -> None:
    conversation_id = frame.get("conversationId")
    participants = conversation_id and await _resolve_participants(conversation_id, gateway)
    if not participants:
        return

    await gateway.mark_read(conversation_id, reader_id)
    peer_id = participants[1] if reader_id == participants[0] else participants[0]
    await connections.send_to(
        peer_id,
        {"type": "read", "conversationId": conversation_id, "upToMs": int(time.time() * 1000)},
    )


async def _on_typing(user_id: str, frame: dict, gateway: SupabaseGateway) -> None:
    conversation_id = frame.get("conversationId")
    participants = conversation_id and await _resolve_participants(conversation_id, gateway)
    if not participants:
        return

    peer_id = participants[1] if user_id == participants[0] else participants[0]
    await connections.send_to(
        peer_id,
        {"type": "typing", "conversationId": conversation_id, "isTyping": bool(frame.get("isTyping"))},
    )


if __name__ == "__main__":
    import os

    port = int(os.environ.get("PORT", "8080"))
    uvicorn.run(app, host="0.0.0.0", port=port)
