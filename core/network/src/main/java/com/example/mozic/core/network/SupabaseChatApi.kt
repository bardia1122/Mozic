package com.example.mozic.core.network

import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.network.dto.ConversationRowDto
import com.example.mozic.core.network.dto.MessageRowDto
import com.example.mozic.core.network.dto.ProfileRowDto
import com.example.mozic.core.network.mapper.peerId
import com.example.mozic.core.network.mapper.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

private const val REST_PATH = "/rest/v1/"

/**
 * REST half of chat (C5) — conversation list + message-history backfill.
 * Real-time send/ack/read/typing goes over [ChatWebSocketClient] instead;
 * this class only ever reads. `conversations`/`messages` require the
 * caller's own JWT as `Authorization: Bearer` (not the anon key) so RLS scopes
 * rows to the authenticated participant — `profiles` is public, anon is fine.
 */
@Singleton
class SupabaseChatApi @Inject constructor(
    private val client: HttpClient,
) {
    /** Conversation shells (no last message/unread count — see `ChatMappers.toDomain`'s kdoc). */
    suspend fun conversations(accessToken: String, myUserId: String): List<Conversation> {
        val rows: List<ConversationRowDto> = client.get(restUrl("conversations")) {
            bearerAuth(accessToken)
            parameter("select", "*")
        }.body()
        if (rows.isEmpty()) return emptyList()

        val peerIds = rows.map { it.peerId(myUserId) }.distinct()
        val profiles = profiles(peerIds).associateBy(ProfileRowDto::id)
        return rows.mapNotNull { row ->
            profiles[row.peerId(myUserId)]?.let { row.toDomain(it) }
        }
    }

    suspend fun profiles(ids: List<String>): List<ProfileRowDto> {
        if (ids.isEmpty()) return emptyList()
        return client.get(restUrl("profiles")) {
            parameter("id", "in.(${ids.joinToString(",")})")
            parameter("select", "*")
        }.body()
    }

    /**
     * Most-recent-first, capped at [limit] — no infinite ancient-history
     * paging is a deliberate cut for this session, see PROGRESS.md.
     */
    suspend fun recentMessages(accessToken: String, conversationId: String, limit: Int): List<Message> {
        val rows: List<MessageRowDto> = client.get(restUrl("messages")) {
            bearerAuth(accessToken)
            parameter("conversation_id", "eq.$conversationId")
            parameter("select", "*")
            parameter("order", "sent_at.desc")
            parameter("limit", limit)
        }.body()
        return rows.map { it.toDomain() }
    }

    private fun restUrl(path: String) = "${BuildConfig.SUPABASE_URL}$REST_PATH$path"
}
