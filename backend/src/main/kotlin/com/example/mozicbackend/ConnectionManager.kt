package com.example.mozicbackend

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import java.util.concurrent.ConcurrentHashMap

/**
 * One live WS session per user id (last connection wins — this project has
 * no multi-device requirement) plus a small cache of conversation ->
 * (userA, userB) so `send`/`read`/`typing` don't each need their own round
 * trip to Supabase just to find out who the peer is.
 */
class ConnectionManager {
    private val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val participants = ConcurrentHashMap<String, Pair<String, String>>()

    fun register(userId: String, session: DefaultWebSocketServerSession) {
        sessions[userId] = session
    }

    fun unregister(userId: String, session: DefaultWebSocketServerSession) {
        sessions.remove(userId, session)
    }

    suspend fun sendTo(userId: String, text: String) {
        sessions[userId]?.send(Frame.Text(text))
    }

    fun participantsOf(conversationId: String): Pair<String, String>? = participants[conversationId]

    fun cacheParticipants(conversationId: String, userA: String, userB: String) {
        participants[conversationId] = userA to userB
    }
}
