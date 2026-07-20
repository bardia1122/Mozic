package com.example.mozicbackend

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets

/**
 * C3: the WebSocket chat server. Catalog/social/auth REST (C1/C2) is served
 * directly by Supabase (PostgREST + Auth) — this module exists only for the
 * send/ack/read/typing protocol Supabase's Realtime (a generic Postgres
 * change-feed) can't provide. See PROTOCOL.md for the wire format and
 * ChatWebSocket.kt for the "persist first, then ack, then push" handling.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val gateway = SupabaseGateway(
        baseUrl = Env.require("SUPABASE_URL"),
        publishableKey = Env.require("SUPABASE_PUBLISHABLE_KEY"),
        secretKey = Env.require("SUPABASE_SECRET_KEY"),
    )
    val connections = ConnectionManager()

    install(WebSockets) {
        pingPeriodMillis = 15_000
        timeoutMillis = 30_000
    }

    routing {
        get("/health") {
            call.respondText("ok")
        }
        chatWebSocketRoute(gateway, connections)
    }
}
