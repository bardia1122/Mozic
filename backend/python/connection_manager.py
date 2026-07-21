"""One live WS connection per user id (last connection wins — no multi-device
fan-out, not a spec requirement) plus a small cache of conversation ->
(userA, userB) so send/read/typing don't each need their own round trip to
Supabase just to find out who the peer is.

Plain dicts, not a lock-protected structure: uvicorn runs a single asyncio
event loop per worker, and every mutation here is a synchronous dict
assignment with no `await` in between, so it's already atomic with respect to
other coroutines. This mirrors the Kotlin version's `ConcurrentHashMap`
safety guarantee for this single-worker deployment model — see README for
why running more than one uvicorn worker would break this.
"""
from __future__ import annotations

import logging

from fastapi import WebSocket

log = logging.getLogger("mozic.connections")


class ConnectionManager:
    def __init__(self) -> None:
        self._sessions: dict[str, WebSocket] = {}
        self._participants: dict[str, tuple[str, str]] = {}

    def register(self, user_id: str, websocket: WebSocket) -> None:
        self._sessions[user_id] = websocket

    def unregister(self, user_id: str, websocket: WebSocket) -> None:
        if self._sessions.get(user_id) is websocket:
            del self._sessions[user_id]

    async def send_to(self, user_id: str, payload: dict) -> None:
        session = self._sessions.get(user_id)
        if session is None:
            return
        # A stale/half-closed peer socket must never take down the sender's
        # own request handling — the Kotlin version doesn't guard this (a
        # failed push to the peer would propagate up through the sender's own
        # incoming-frame loop), a deliberate small improvement here, not a
        # protocol change.
        try:
            await session.send_json(payload)
        except Exception:  # noqa: BLE001 - any send failure means "peer is gone", nothing more specific to do
            log.warning("failed to push to %s, dropping", user_id)

    def participants_of(self, conversation_id: str) -> tuple[str, str] | None:
        return self._participants.get(conversation_id)

    def cache_participants(self, conversation_id: str, user_a: str, user_b: str) -> None:
        self._participants[conversation_id] = (user_a, user_b)
