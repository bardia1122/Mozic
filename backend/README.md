# Mozic backend

Person C's **C1** phase (`doc/CLAUDE_PERSON_C.md` §4/§5) — the catalog,
social graph, and chat-history REST API.

**The API is Supabase**, not custom server code: a hosted Postgres project
with its auto-generated REST API (PostgREST) and built-in Auth. This is the
plan's own named fallback for hosting friction ("if hosting friction
threatens C1's deadline, Supabase for the REST part") — it also just solves
phone-reachability outright, since Supabase hosts everything, no deploy step
needed. `backend/`'s own Ktor project exists only for **C3**'s WebSocket
chat server (Supabase's Realtime is a generic Postgres change-feed, not a fit
for this project's custom send/ack/read/typing protocol — that needs a small
dedicated service, same as the plan anticipated). See `PROTOCOL.md` for the
full wire format.

## What's here

```
backend/
  supabase/
    schema.sql        — tables, RLS policies, grants, the search_catalog() RPC
    seed.py            — seeds songs/users/playlists/follows/messages via the REST + Auth Admin APIs
  src/…                — C3's WebSocket chat server, Kotlin/Ktor original implementation
  python/…             — the same server, rewritten in Python/FastAPI (see below) — pick one
  PROTOCOL.md          — the WS wire protocol, frozen contract with C5, identical for both implementations
  .env                 — Supabase credentials (gitignored, never commit), shared by both implementations
```

## C3 — WebSocket chat server

Two interchangeable implementations of the exact same `PROTOCOL.md` wire
format — the Android client (`ChatWebSocketClient`) can't tell which one it's
talking to. Run whichever one; don't run both at once (same port).

**Python/FastAPI** (`backend/python/`) — lighter to start, no JDK/Gradle needed:

```bash
cd backend/python
pip install -r requirements.txt     # once, ideally inside a venv
python main.py                      # reads backend/.env (one level up)
curl http://localhost:8080/health   # → "ok"
```

Single uvicorn worker only (the default `python main.py` invocation) — its
`ConnectionManager` keeps live WS sessions and a small participants cache in
plain process memory, not shared across workers/processes. `PORT` env var
overrides the default 8080.

**Kotlin/Ktor** (`backend/src/`) — the original implementation:

```bash
cd backend
./gradlew run                       # reads backend/.env
curl http://localhost:8080/health   # → "ok"
```

One endpoint: `wss://<host>/ws?token=<supabase-access-token>&since=<epochMs>`.
Full frame format, auth handshake, and a runnable test recipe (no Android
client needed) are in `PROTOCOL.md`. Persistence for chat messages goes
through the same Supabase Postgres project as C1/C2, via PostgREST calls
made with the secret/service_role key (bypasses RLS — the `messages` table's
policy deliberately has no client-side `UPDATE`, this server is the trusted
intermediary that assumes). Verified live (not just compiled) against the real
Supabase project, for **both** implementations: typing indicator, send → ack
→ push, read receipts (with the persisted row's status actually flipping to
`READ`), song-share payload round-trip, reconnect backfill (`since=`),
invalid/missing-token rejection (close code `1008`), and a spoofed-`senderId`
frame being silently dropped and never persisted — all against `alice`↔`bob`'s
seeded conversation (`conv-1`).

## Project

- URL: `https://ktwzmigxumrpblamerzw.supabase.co`
- Credentials are in `backend/.env` (gitignored) — `SUPABASE_PUBLISHABLE_KEY`
  is the one that goes in the Android app later (C2); `SUPABASE_SECRET_KEY`
  bypasses Row Level Security and must never leave the server side (seeding
  scripts, and eventually C3's WS server).
- Schema lives in `supabase/schema.sql`, applied by pasting it into the
  Supabase dashboard's SQL Editor (no direct DB connection string was
  shared, by design — fewer secrets in flight). Re-running it is safe
  (idempotent).
- Re-seed: `python backend/supabase/seed.py` (needs the `requests` package;
  bails out immediately if `songs` already has rows).

## Demo accounts

6 seeded users, all password `password123`, logging in by **email** (Supabase
Auth is email-based, not username-based — a deviation from the plan's
literal `username`/`password` wording):

| email | username | premium |
|---|---|---|
| alice@mozic.dev | alice | yes |
| bob@mozic.dev | bob | no |
| sara_m@mozic.dev | sara_m | yes |
| arman_k@mozic.dev | arman_k | no |
| lily_c@mozic.dev | lily_c | no |
| dj_reza@mozic.dev | dj_reza | yes |

`alice`↔`bob` and `alice`↔`sara_m` have seeded conversations (including a
song-share message) so chat/social screens won't be empty once C4 exists.

## Endpoints

Everything is native Supabase — no custom route layer:

```
POST /auth/v1/token?grant_type=password        { email, password } → { access_token, user, … }
GET  /rest/v1/songs?select=…&order=…            (filters/sort/embeds are PostgREST query params)
GET  /rest/v1/songs?id=eq.<id>&select=…
POST /rest/v1/rpc/search_catalog                { q, result_type }  → cross-song/artist/playlist search
GET  /rest/v1/playlists?category=eq.WORLD&select=…
GET  /rest/v1/playlist_songs?playlist_id=eq.<id>&select=position,songs(*)&order=position
GET  /rest/v1/profiles?select=…                 GET  /rest/v1/profiles?id=eq.<id>&select=…
POST /rest/v1/follows   { follower_id, followee_id }      DELETE with the same filter
GET  /rest/v1/conversations?select=…            (RLS: only rows where you're user_a/user_b)
GET  /rest/v1/messages?conversation_id=eq.<id>&select=…&order=sent_at.desc
```

Every request needs `apikey: <publishable-or-secret-key>`; authenticated
calls also need `Authorization: Bearer <access_token>` from login. Pagination
is native PostgREST: `Range: 0-19` request header +
`Prefer: count=exact` → response `Content-Range: 0-19/243` — **not** the
`?page=&size=` → `{items,page,totalPages}` envelope C1's original custom
server used; C2's `PagingSource`s should read `Content-Range` directly.

See `mozic-backend.http` for a runnable collection covering all of the above.

## Schema

`profiles` (the app's `User` — `auth.users` owns email/password, this table
owns everything the domain model needs, auto-populated by a trigger on
signup), `songs`, `playlists`, `playlist_songs`, `follows`, `conversations`,
`messages`. RLS is on for every table; `supabase/schema.sql` has the exact
policy per table (public catalog reads; follow/message/conversation access
scoped to the authenticated caller). No separate `artists` table — artist
search groups distinct `artist_name` off `songs`, matching the schema as
specified in the plan doc.

## Seed data

60 songs across 12 fictional artists (≥ the spec's 50-song minimum), audio
cycled across SoundHelix's 17 public royalty-free demo tracks, 15 playlists
(6 world, 6 local, 3 user-owned), 6 users, a handful of follows, 2 seeded
conversations. See `supabase/seed.py` for the exact data.
