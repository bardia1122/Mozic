"""Timestamp/shape conversions between PostgREST's snake_case `messages` rows
(timestamptz as an ISO-8601 string) and the WS protocol's camelCase message
object (epoch milliseconds) — see backend/PROTOCOL.md. Kept separate from
`supabase_gateway.py` for the same reason the Kotlin version split
`SupabaseDtos.kt`/`WireFrames.kt`: a DB column rename should never leak into
the client-facing wire format.
"""
from __future__ import annotations

from datetime import datetime, timezone


def epoch_ms_to_iso(epoch_ms: int) -> str:
    dt = datetime.fromtimestamp(epoch_ms / 1000, tz=timezone.utc)
    return dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{epoch_ms % 1000:03d}Z"


def iso_to_epoch_ms(iso: str) -> int:
    # PostgREST returns a numeric UTC offset (e.g. "+00:00"); Python's
    # fromisoformat also accepts a bare "Z" from 3.11 on, but normalizing it
    # here keeps this working on older interpreters too.
    normalized = iso.replace("Z", "+00:00")
    dt = datetime.fromisoformat(normalized)
    return int(dt.timestamp() * 1000)


def row_to_wire(row: dict) -> dict:
    """A `messages` REST row (snake_case, ISO timestamp) -> the WS wire message shape."""
    return {
        "id": row["id"],
        "conversationId": row["conversation_id"],
        "senderId": row["sender_id"],
        "sentAtEpochMs": iso_to_epoch_ms(row["sent_at"]),
        "status": row["status"],
        "payloadType": row["payload_type"],
        "text": row.get("text"),
        "songId": row.get("song_id"),
        "songTitle": row.get("song_title"),
        "songArtist": row.get("song_artist"),
        "songCover": row.get("song_cover"),
    }


def wire_message_to_row(message: dict, status: str) -> dict:
    """A wire message (camelCase, epoch millis, as received in a `send` frame) -> a `messages` insert row."""
    return {
        "id": message["id"],
        "conversation_id": message["conversationId"],
        "sender_id": message["senderId"],
        "sent_at": epoch_ms_to_iso(message["sentAtEpochMs"]),
        "status": status,
        "payload_type": message["payloadType"],
        "text": message.get("text"),
        "song_id": message.get("songId"),
        "song_title": message.get("songTitle"),
        "song_artist": message.get("songArtist"),
        "song_cover": message.get("songCover"),
    }
