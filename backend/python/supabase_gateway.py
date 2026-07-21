"""All of this server's talk to Supabase, over plain PostgREST/Auth HTTP calls
with the service_role secret key — same shape as C1's seed.py, no direct
Postgres connection. The secret key bypasses RLS, which is exactly right
here: this server is the trusted intermediary the `messages` table's RLS
policy expects for status writes (see supabase/schema.sql's comment on
`messages` having "no client UPDATE policy yet").
"""
from __future__ import annotations

import httpx

from wire import epoch_ms_to_iso, row_to_wire, wire_message_to_row


class SupabaseGateway:
    def __init__(self, client: httpx.AsyncClient, base_url: str, publishable_key: str, secret_key: str) -> None:
        self._client = client
        self._base_url = base_url.rstrip("/")
        self._publishable_key = publishable_key
        self._secret_key = secret_key

    def _service_headers(self, **extra: str) -> dict:
        return {
            "apikey": self._secret_key,
            "Authorization": f"Bearer {self._secret_key}",
            **extra,
        }

    async def authenticate(self, access_token: str) -> str | None:
        """Validates a Supabase Auth access token and returns the caller's user id, or None if invalid."""
        response = await self._client.get(
            f"{self._base_url}/auth/v1/user",
            headers={"apikey": self._publishable_key, "Authorization": f"Bearer {access_token}"},
        )
        if response.is_error:
            return None
        return response.json().get("id")

    async def participants_of(self, conversation_id: str) -> tuple[str, str] | None:
        response = await self._client.get(
            f"{self._base_url}/rest/v1/conversations",
            headers=self._service_headers(),
            params={"id": f"eq.{conversation_id}", "select": "id,user_a,user_b"},
        )
        response.raise_for_status()
        rows = response.json()
        if not rows:
            return None
        user_a, user_b = rows[0].get("user_a"), rows[0].get("user_b")
        if not user_a or not user_b:
            return None
        return user_a, user_b

    async def insert_message(self, message: dict) -> bool:
        """Persists a message as SENT. Returns False if the insert was rejected (bad conversation id, etc)."""
        row = wire_message_to_row(message, status="SENT")
        response = await self._client.post(
            f"{self._base_url}/rest/v1/messages",
            headers=self._service_headers(**{"Prefer": "return=minimal"}),
            json=row,
        )
        return not response.is_error

    async def mark_read(self, conversation_id: str, reader_id: str) -> None:
        """Marks every message the peer sent in this conversation as READ."""
        await self._client.patch(
            f"{self._base_url}/rest/v1/messages",
            headers=self._service_headers(**{"Prefer": "return=minimal"}),
            params={
                "conversation_id": f"eq.{conversation_id}",
                "sender_id": f"neq.{reader_id}",
                "status": "neq.READ",
            },
            json={"status": "READ"},
        )

    async def conversation_ids_for(self, user_id: str) -> list[str]:
        response = await self._client.get(
            f"{self._base_url}/rest/v1/conversations",
            headers=self._service_headers(),
            params={"or": f"(user_a.eq.{user_id},user_b.eq.{user_id})", "select": "id"},
        )
        response.raise_for_status()
        return [row["id"] for row in response.json()]

    async def messages_since(self, conversation_ids: list[str], since_epoch_ms: int) -> list[dict]:
        """Reconnect backfill: everything sent in the caller's conversations since since_epoch_ms."""
        if not conversation_ids:
            return []
        response = await self._client.get(
            f"{self._base_url}/rest/v1/messages",
            headers=self._service_headers(),
            params={
                "conversation_id": f"in.({','.join(conversation_ids)})",
                "sent_at": f"gt.{epoch_ms_to_iso(since_epoch_ms)}",
                "order": "sent_at.asc",
                "select": "*",
            },
        )
        response.raise_for_status()
        return [row_to_wire(row) for row in response.json()]
