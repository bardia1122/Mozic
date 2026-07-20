# Mozic chat WebSocket protocol (C3)

One endpoint, JSON text frames, a `type` discriminator on every frame. This
document is the frozen contract between this server and **C5**'s Android
client — treat it the same way `:core:domain`'s frozen models are treated.

```
wss://<host>/ws?token=<supabase-access-token>&since=<epochMs>
```

- `token` (required) — a Supabase Auth **access token**, the same
  `access_token` returned by `POST /auth/v1/token?grant_type=password`
  against the Supabase project (see `backend/README.md`). The server calls
  `GET {SUPABASE_URL}/auth/v1/user` with it to resolve the caller's user id;
  an invalid/expired/missing token closes the connection immediately with
  close code `1008` (policy violation) and a human-readable reason.
- `since` (optional, epoch milliseconds) — if present, the server replays
  every message in the caller's conversations sent after this timestamp as
  `message` frames, oldest first, before the live loop starts. This is the
  reconnect backfill: the client should persist the timestamp of the last
  message it has seen and pass it back on the next connect. Omit it to skip
  backfill entirely (e.g. first-ever connect, or if the client already
  re-fetches history over REST on reconnect).

Only one session per user is tracked at a time — a second connection for the
same user id replaces the first in the server's session map (no multi-device
fan-out; not a spec requirement for this project).

## Message object

Every `message` field below (in `send` and in the `message` push) is this
shape — camelCase, mirrors the frozen `Message`/`MessagePayload` domain model
in `doc/CLAUDE_PERSON_C.md` §3 exactly so C5's mapping is closer to a no-op:

```jsonc
{
  "id": "<client-generated UUID>",
  "conversationId": "conv-1",
  "senderId": "<uuid>",
  "sentAtEpochMs": 1784499456139,
  "status": "SENDING" | "SENT" | "READ",
  "payloadType": "text" | "song",
  // payloadType == "text":
  "text": "hello",
  // payloadType == "song" (denormalized so history renders offline):
  "songId": "song-3",
  "songTitle": "Test Song",
  "songArtist": "Test Artist",
  "songCover": "https://…"
}
```

## Client → server frames

```jsonc
{ "type": "send",   "message": { …see above, status is ignored and always persisted as SENT… } }
{ "type": "read",   "conversationId": "conv-1" }
{ "type": "typing", "conversationId": "conv-1", "isTyping": true }
```

- `send.message.senderId` **must** equal the token's resolved user id, and
  that user must be one of the conversation's two participants — otherwise
  the frame is silently dropped (no ack, no persistence, no push). This is
  the one authorization check the server does itself, since the secret-key
  PostgREST calls it makes to persist chat bypass RLS.
- `send.message.id` is client-generated (UUID) specifically so the client
  can insert it locally as `SENDING` before the round trip, then flip it to
  `SENT` on `ack` — see CLAUDE_PERSON_C.md §3's "why" note. Retried sends
  reuse the same id; the server's insert is a plain `INSERT`, not an
  upsert, so a genuine retry (same id, same conversation) will get a
  Postgres primary-key conflict and no ack — C5 should only retry an id
  that never got an `ack` in the first place.

## Server → client frames

```jsonc
{ "type": "ack",     "messageId": "<uuid>" }                    // to the sender only
{ "type": "message", "message": { …full message object, status="SENT"… } }  // to the peer, or replayed on `since` backfill
{ "type": "read",    "conversationId": "conv-1", "upToMs": 1784499457257 }  // to the original sender, when the peer reads
{ "type": "typing",  "conversationId": "conv-1", "isTyping": true }         // forwarded to the peer only
```

- Server behavior is "persist first, then ack, then push": a `send` frame is
  written to Postgres (via PostgREST, service-role key) before anything is
  sent back, so a crash between persist and push only delays delivery — the
  recipient's next reconnect (`since=`) backfill still finds it.
- `read` marks every message the *other* participant sent in that
  conversation as `READ` (a single batched `PATCH`, not per-message) and
  forwards a `read` frame carrying the current server time as `upToMs` — the
  client's own semantics are "everything I sent with `sentAtEpochMs <=
  upToMs` is now read".
- `typing` is fire-and-forget: never persisted, just relayed to the peer if
  they're currently connected. If the peer is offline the frame is dropped
  silently (there is nothing meaningful to backfill for typing state).
- Frames from an unknown/malformed `type` are logged server-side and
  otherwise ignored (no error frame sent back) — keeps the client's error
  handling simple, since a client speaking this exact protocol should never
  produce one.

## Trying it without the Android client

Get an access token for one of C1's seeded demo users (any of the six in
`backend/README.md`, password `password123` for all), then connect with any
WebSocket CLI (`websocat`, `wscat`, or a two-line Python `websockets`
script):

```bash
curl -s "$SUPABASE_URL/auth/v1/token?grant_type=password" \
  -H "apikey: $SUPABASE_PUBLISHABLE_KEY" -H "Content-Type: application/json" \
  -d '{"email":"alice@mozic.dev","password":"password123"}' | jq -r .access_token
```

`alice`↔`bob` (conversation `conv-1`) already has seed history, so it's the
easiest pair to test receipts/typing with two connections open at once —
exactly what this server itself was verified with this session (see
`doc/PROGRESS.md`'s C3 entry): typing indicator, send → ack → push, read
receipt, Postgres persistence, song-share payload round-trip, spoofed-sender
rejection, and reconnect backfill were all confirmed live against the
project, not just compiled.

## Running the server

```bash
cd backend
./gradlew run           # reads backend/.env for SUPABASE_URL / _PUBLISHABLE_KEY / _SECRET_KEY
curl http://localhost:8080/health   # → "ok"
```

`PORT` env var overrides the default `8080` for deployment. No direct
Postgres connection is used — all persistence goes through PostgREST with
the `SUPABASE_SECRET_KEY` (service_role, bypasses RLS by design: see
`supabase/schema.sql`'s comment on `messages` having "no client UPDATE
policy yet" — this server is the trusted intermediary that policy assumes).
