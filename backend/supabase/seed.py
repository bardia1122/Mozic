#!/usr/bin/env python3
"""Seeds the Mozic Supabase project via its REST + Auth Admin APIs.

Run after schema.sql has been applied (Supabase SQL Editor). Reads
credentials from backend/.env (gitignored, never committed). Idempotent:
bails out immediately if `songs` already has rows.

Same catalog/playlist/follow/conversation shape as the retired local
Ktor+SQLite Seed.kt — 60 songs (12 artists, SoundHelix audio cycled the same
way), 15 playlists, 6 demo users, a few follows, two seeded conversations.
The one structural difference: users are real Supabase Auth accounts
(email + password, not username + password), so ids are server-issued
UUIDs rather than "user-1".."user-6" — this script creates the auth users
first and then substitutes their real UUIDs everywhere a seed row below
references a demo user.
"""

import os
import sys
from datetime import datetime, timedelta, timezone

import requests

ROOT = os.path.dirname(os.path.abspath(__file__))
ENV_PATH = os.path.join(ROOT, "..", ".env")
DEMO_PASSWORD = "password123"


def load_env(path):
    env = {}
    with open(path) as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            env[key.strip()] = value.strip()
    return env


env = load_env(ENV_PATH)
SUPABASE_URL = env["SUPABASE_URL"].rstrip("/")
SECRET_KEY = env["SUPABASE_SECRET_KEY"]

REST = f"{SUPABASE_URL}/rest/v1"
ADMIN_USERS = f"{SUPABASE_URL}/auth/v1/admin/users"

HEADERS = {
    "apikey": SECRET_KEY,
    "Authorization": f"Bearer {SECRET_KEY}",
    "Content-Type": "application/json",
    "Prefer": "return=representation",
}


def rest_post(table, rows):
    resp = requests.post(f"{REST}/{table}", headers=HEADERS, json=rows, timeout=30)
    if not resp.ok:
        print(f"FAILED inserting into {table}: {resp.status_code} {resp.text}", file=sys.stderr)
        resp.raise_for_status()
    return resp.json()


def rest_get(table, params):
    resp = requests.get(f"{REST}/{table}", headers=HEADERS, params=params, timeout=30)
    resp.raise_for_status()
    return resp.json()


def create_auth_user(email, metadata):
    resp = requests.post(
        ADMIN_USERS,
        headers=HEADERS,
        json={
            "email": email,
            "password": DEMO_PASSWORD,
            "email_confirm": True,
            "user_metadata": metadata,
        },
        timeout=30,
    )
    if not resp.ok:
        print(f"FAILED creating auth user {email}: {resp.status_code} {resp.text}", file=sys.stderr)
        resp.raise_for_status()
    return resp.json()["id"]


SONG_SEEDS = [
    # (title, artist, duration_ms)
    ("Neon Skyline", "Nova Cascade", 214_000), ("Static Bloom", "Nova Cascade", 205_800),
    ("Glass Horizon", "Nova Cascade", 198_000), ("Chrome Tide", "Nova Cascade", 221_500),
    ("Electric Bloom", "Nova Cascade", 189_900),
    ("Slow Tide", "The Midnight Echo", 198_500), ("Rooftops", "The Midnight Echo", 189_300),
    ("Paper Lanterns", "The Midnight Echo", 203_100), ("Hollow Streets", "The Midnight Echo", 176_400),
    ("After Hours", "The Midnight Echo", 231_000),
    ("Paper Planes", "Solaris", 176_000), ("Undertow", "Solaris", 242_100),
    ("Drift Away", "Solaris", 210_600), ("Quiet Orbit", "Solaris", 195_200),
    ("Faded Light", "Solaris", 188_700),
    ("Gold Hour", "Kavana", 231_200), ("Midnight Drive", "Kavana", 217_600),
    ("Sugar Rush", "Kavana", 179_400), ("City Lights", "Kavana", 202_800),
    ("Heartbeat Radio", "Kavana", 196_500),
    ("Coffee & Rain", "Lo-Fi Collective", 163_400), ("Afterglow", "Lo-Fi Collective", 172_900),
    ("Sunday Static", "Lo-Fi Collective", 158_200), ("Warm Vinyl", "Lo-Fi Collective", 167_100),
    ("Attic Window", "Lo-Fi Collective", 181_300),
    ("Broken Amps", "Velvet Static", 224_700), ("Wildfire", "Velvet Static", 209_300),
    ("Concrete Jungle", "Velvet Static", 233_600), ("Loud Silence", "Velvet Static", 199_800),
    ("Rearview", "Velvet Static", 214_900),
    ("Northern Sky", "Aurora Drift", 251_000), ("Glacier Dreams", "Aurora Drift", 238_400),
    ("Silent Peaks", "Aurora Drift", 227_700), ("Polar Light", "Aurora Drift", 219_100),
    ("Frostbound", "Aurora Drift", 243_800),
    ("Tehran Nights", "DJ Reza", 256_000), ("Bassline Fever", "DJ Reza", 241_300),
    ("Neon Bazaar", "DJ Reza", 233_900), ("Midnight Market", "DJ Reza", 247_600),
    ("Pulse Drive", "DJ Reza", 229_200),
    ("Blue Note Avenue", "Northern Lights Trio", 264_000), ("Late Set", "Northern Lights Trio", 258_300),
    ("Brass & Rain", "Northern Lights Trio", 249_700), ("Smoky Room", "Northern Lights Trio", 271_400),
    ("Velvet Sax", "Northern Lights Trio", 253_800),
    ("Wildflower Road", "Marble Sky", 192_600), ("Autumn Letters", "Marble Sky", 204_100),
    ("Quiet Hills", "Marble Sky", 187_500), ("Homebound", "Marble Sky", 199_900),
    ("Wooden Porch", "Marble Sky", 178_300),
    ("Static Heart", "Crimson Radio", 216_800), ("Radio Silence", "Crimson Radio", 208_200),
    ("Burnout", "Crimson Radio", 194_700), ("Skyline Riot", "Crimson Radio", 225_500),
    ("Fault Lines", "Crimson Radio", 211_900),
    ("Moonlit Sonata", "Echo Valley", 278_000), ("Glass Cathedral", "Echo Valley", 291_400),
    ("Autumn Waltz", "Echo Valley", 246_200), ("Reverie", "Echo Valley", 262_700),
    ("Solitude in Blue", "Echo Valley", 254_400),
]

PLAYLIST_SEEDS = [
    # (title, category, owner_username, song_index_range)
    ("Global Top 50", "WORLD", None, range(0, 15)),
    ("Fresh Finds", "WORLD", None, range(5, 20)),
    ("Electronic Essentials", "WORLD", None, range(0, 5)),
    ("Indie Rotation", "WORLD", None, range(5, 15)),
    ("Chill Focus", "WORLD", None, range(10, 25)),
    ("Rock Revival", "WORLD", None, range(25, 35)),
    ("Tehran Nights", "LOCAL", None, range(35, 40)),
    ("Local Heroes", "LOCAL", None, range(35, 50)),
    ("Downtown Sessions", "LOCAL", None, range(40, 45)),
    ("Homegrown Beats", "LOCAL", None, range(35, 45)),
    ("Late Night Local", "LOCAL", None, range(45, 50)),
    ("Weekend Local Mix", "LOCAL", None, range(35, 55)),
    ("My Focus Mix", "USER", "alice", range(20, 30)),
    ("Late Night", "USER", "bob", range(50, 60)),
    ("Workout Energy", "USER", "sara_m", range(0, 10)),
]

USER_SEEDS = [
    # (username, display_name, is_premium)
    ("alice", "Alice Morgan", True),
    ("bob", "Bob Chen", False),
    ("sara_m", "Sara Moradi", True),
    ("arman_k", "Arman K.", False),
    ("lily_c", "Lily Chen", False),
    ("dj_reza", "DJ Reza", True),
]

FOLLOWS = [
    ("alice", "bob"), ("alice", "sara_m"), ("alice", "dj_reza"),
    ("bob", "alice"), ("sara_m", "dj_reza"),
]

SOUNDHELIX_TRACK_COUNT = 17


def song_id(index):
    return f"song-{index + 1}"


def cover_url(entity_id):
    return f"https://picsum.photos/seed/mozic-backend-{entity_id}/500/500"


def main():
    existing = rest_get("songs", {"select": "id", "limit": 1})
    if existing:
        print("songs table already has rows — seed already ran, nothing to do.")
        return

    print("Inserting songs...")
    now = datetime.now(timezone.utc)
    song_rows = []
    for index, (title, artist, duration_ms) in enumerate(SONG_SEEDS):
        sid = song_id(index)
        track_number = (index % SOUNDHELIX_TRACK_COUNT) + 1
        song_rows.append({
            "id": sid,
            "title": title,
            "artist_name": artist,
            "cover_image_url": cover_url(sid),
            "audio_url": f"https://www.soundhelix.com/examples/mp3/SoundHelix-Song-{track_number}.mp3",
            "duration_ms": duration_ms,
            "created_at": (now - timedelta(minutes=(len(SONG_SEEDS) - index))).isoformat(),
            "popularity": (len(SONG_SEEDS) - index) * 3,
        })
    rest_post("songs", song_rows)
    print(f"  {len(song_rows)} songs inserted.")

    print("Creating demo users (Supabase Auth)...")
    user_ids = {}
    for username, display_name, is_premium in USER_SEEDS:
        uid = f"user-{username}"
        metadata = {
            "username": username,
            "display_name": display_name,
            "avatar_url": cover_url(uid),
            "is_premium": is_premium,
        }
        user_ids[username] = create_auth_user(f"{username}@mozic.dev", metadata)
        print(f"  {username} -> {user_ids[username]}")

    print("Inserting playlists...")
    playlist_rows = []
    for index, (title, category, owner_username, _range) in enumerate(PLAYLIST_SEEDS):
        pid = f"playlist-{index + 1}"
        playlist_rows.append({
            "id": pid,
            "title": title,
            "cover_image_url": cover_url(pid),
            "owner_id": user_ids[owner_username] if owner_username else None,
            "is_public": True,
            "category": category,
        })
    rest_post("playlists", playlist_rows)
    print(f"  {len(playlist_rows)} playlists inserted.")

    print("Inserting playlist_songs...")
    playlist_song_rows = []
    for index, (_title, _category, _owner, song_range) in enumerate(PLAYLIST_SEEDS):
        pid = f"playlist-{index + 1}"
        for position, song_index in enumerate(song_range):
            playlist_song_rows.append({
                "playlist_id": pid,
                "song_id": song_id(song_index % len(SONG_SEEDS)),
                "position": position,
            })
    rest_post("playlist_songs", playlist_song_rows)
    print(f"  {len(playlist_song_rows)} playlist_songs rows inserted.")

    print("Inserting follows...")
    follow_rows = [
        {"follower_id": user_ids[a], "followee_id": user_ids[b]}
        for a, b in FOLLOWS
    ]
    rest_post("follows", follow_rows)
    print(f"  {len(follow_rows)} follows inserted.")

    print("Inserting conversations + messages...")
    rest_post("conversations", [
        {"id": "conv-1", "user_a": user_ids["alice"], "user_b": user_ids["bob"]},
        {"id": "conv-2", "user_a": user_ids["alice"], "user_b": user_ids["sara_m"]},
    ])

    def song_ref(index):
        title, artist, _ = SONG_SEEDS[index]
        return song_id(index), title, artist

    base = now - timedelta(hours=6)
    demo_messages = [
        ("conv-1", "bob", 0, "READ", "text", "hey! found something for you", None),
        ("conv-1", "bob", 60, "READ", "song", None, 2),
        ("conv-1", "alice", 120, "SENT", "text", "on repeat already \U0001F525", None),
        ("conv-2", "alice", 0, "READ", "text", "playlist for the drive?", None),
        ("conv-2", "sara_m", 45, "SENT", "song", None, 7),
    ]
    message_rows = []
    for conv_id, sender, offset_sec, status, payload_type, text, song_index in demo_messages:
        sid, title, artist = song_ref(song_index) if song_index is not None else (None, None, None)
        message_rows.append({
            "conversation_id": conv_id,
            "sender_id": user_ids[sender],
            "sent_at": (base + timedelta(seconds=offset_sec)).isoformat(),
            "status": status,
            "payload_type": payload_type,
            "text": text,
            "song_id": sid,
            "song_title": title,
            "song_artist": artist,
            "song_cover": cover_url(sid) if sid else None,
        })
    rest_post("messages", message_rows)
    print(f"  {len(message_rows)} messages inserted.")

    print("Done.")


if __name__ == "__main__":
    main()
