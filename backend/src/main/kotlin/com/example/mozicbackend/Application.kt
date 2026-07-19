package com.example.mozicbackend

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * C1's catalog/social/auth REST API is now served directly by Supabase
 * (PostgREST + Auth — see backend/supabase/). This module is a placeholder
 * for C3's WebSocket chat server, which Supabase can't provide (its
 * Realtime is a generic Postgres change-feed, not a fit for this project's
 * send/ack/read/typing protocol) — build that out here when C3 starts.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    routing {
        get("/health") {
            call.respondText("ok")
        }
    }
}
