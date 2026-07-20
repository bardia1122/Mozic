package com.example.mozicbackend

import java.io.File

/**
 * Reads `backend/.env` (gitignored, same file C1's seed.py uses) merged with
 * real process env vars — env vars win, so a real deployment can set them
 * without needing a `.env` file on disk at all.
 */
object Env {
    private val dotenv: Map<String, String> by lazy {
        val file = File(".env")
        if (!file.exists()) {
            emptyMap()
        } else {
            file.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
                .associate { line ->
                    val (key, value) = line.split("=", limit = 2)
                    key.trim() to value.trim()
                }
        }
    }

    fun require(key: String): String =
        System.getenv(key) ?: dotenv[key] ?: error("Missing required env var: $key (checked process env and .env)")
}
