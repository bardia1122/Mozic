"""Reads backend/.env (gitignored, same file the Kotlin version and C1's
seed.py both use) merged with real process env vars — env vars win, same
precedence as the Kotlin `Env.require`, so a real deployment can set them
without needing a `.env` file on disk at all.
"""
from __future__ import annotations

import os
from pathlib import Path

from dotenv import load_dotenv

# .env lives at backend/.env, one level up from this file — load it
# regardless of the working directory `uvicorn` happens to be started from.
_ENV_PATH = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(_ENV_PATH, override=False)


def require(key: str) -> str:
    value = os.environ.get(key)
    if not value:
        raise RuntimeError(f"Missing required env var: {key} (checked process env and {_ENV_PATH})")
    return value
