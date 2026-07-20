package com.example.mozicbackend

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * All of this server's talk to Supabase, over plain PostgREST/Auth HTTP
 * calls with the service_role secret key — same shape as C1's seed.py, no
 * JDBC driver or direct DB connection needed. The secret key bypasses RLS,
 * which is exactly right here: this server is the trusted intermediary the
 * `messages` table's RLS policy expects for status writes (see schema.sql,
 * "no client UPDATE policy on messages yet").
 */
class SupabaseGateway(
    private val baseUrl: String,
    private val publishableKey: String,
    private val secretKey: String,
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    /** Validates a Supabase Auth access token and returns the caller's user id, or null if invalid. */
    suspend fun authenticate(accessToken: String): String? {
        val response = client.get("$baseUrl/auth/v1/user") {
            header("apikey", publishableKey)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (!response.status.isSuccess()) return null
        return response.body<SupabaseAuthUser>().id
    }

    suspend fun participantsOf(conversationId: String): Pair<String, String>? {
        val response = client.get("$baseUrl/rest/v1/conversations") {
            authAsService()
            parameter("id", "eq.$conversationId")
            parameter("select", "id,user_a,user_b")
        }
        val row = response.body<List<ConversationRow>>().firstOrNull() ?: return null
        val userA = row.userA ?: return null
        val userB = row.userB ?: return null
        return userA to userB
    }

    /** Persists a message as SENT. Returns false if the insert was rejected (bad conversation id, etc). */
    suspend fun insertMessage(message: WireMessage): Boolean {
        val row = MessageRow(
            id = message.id,
            conversationId = message.conversationId,
            senderId = message.senderId,
            sentAt = Instant.ofEpochMilli(message.sentAtEpochMs).toString(),
            status = "SENT",
            payloadType = message.payloadType,
            text = message.text,
            songId = message.songId,
            songTitle = message.songTitle,
            songArtist = message.songArtist,
            songCover = message.songCover,
        )
        val response = client.post("$baseUrl/rest/v1/messages") {
            authAsService()
            header("Prefer", "return=minimal")
            contentType(ContentType.Application.Json)
            setBody(row)
        }
        return response.status.isSuccess()
    }

    /** Marks every message the peer sent in this conversation as READ. */
    suspend fun markRead(conversationId: String, readerId: String) {
        client.patch("$baseUrl/rest/v1/messages") {
            authAsService()
            header("Prefer", "return=minimal")
            parameter("conversation_id", "eq.$conversationId")
            parameter("sender_id", "neq.$readerId")
            parameter("status", "neq.READ")
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to "READ"))
        }
    }

    suspend fun conversationIdsFor(userId: String): List<String> {
        val response = client.get("$baseUrl/rest/v1/conversations") {
            authAsService()
            parameter("or", "(user_a.eq.$userId,user_b.eq.$userId)")
            parameter("select", "id")
        }
        return response.body<List<ConversationRow>>().map { it.id }
    }

    /** Reconnect backfill: everything sent in the caller's conversations since [sinceEpochMs]. */
    suspend fun messagesSince(conversationIds: List<String>, sinceEpochMs: Long): List<WireMessage> {
        if (conversationIds.isEmpty()) return emptyList()
        val response = client.get("$baseUrl/rest/v1/messages") {
            authAsService()
            parameter("conversation_id", "in.(${conversationIds.joinToString(",")})")
            parameter("sent_at", "gt.${Instant.ofEpochMilli(sinceEpochMs)}")
            parameter("order", "sent_at.asc")
            parameter("select", "*")
        }
        return response.body<List<MessageRow>>().map { it.toWire() }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.authAsService() {
        header("apikey", secretKey)
        header(HttpHeaders.Authorization, "Bearer $secretKey")
    }
}
