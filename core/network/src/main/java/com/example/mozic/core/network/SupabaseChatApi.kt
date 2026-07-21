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
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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

    /**
     * `resolution=ignore-duplicates` (keyed on the `id` primary key), not
     * `merge-duplicates` — PostgREST implements `merge-duplicates` as
     * `ON CONFLICT DO UPDATE`, which requires an `UPDATE` grant/policy on the
     * table; `schema.sql` only grants `SELECT`/`INSERT` on `conversations`
     * (rows never change after creation), so `merge-duplicates` was silently
     * rejected with a 403 every time — caught here by a swallowing
     * `runCatching` in `RealChatRepository.conversationWith`, so the failure
     * never surfaced. `ignore-duplicates` (`ON CONFLICT DO NOTHING`) needs
     * only `INSERT` and is the semantically correct choice anyway: the
     * caller always computes the same deterministic id for a given pair, so
     * a re-share to someone already messaged is a safe no-op insert, nothing
     * to merge.
     */
    suspend fun createConversation(accessToken: String, id: String, userA: String, userB: String) {
        client.post(restUrl("conversations")) {
            bearerAuth(accessToken)
            header("Prefer", "resolution=ignore-duplicates,return=minimal")
            contentType(ContentType.Application.Json)
            setBody(ConversationRowDto(id, userA, userB))
        }
    }

    /**
     * Looks up an existing conversation between two users regardless of which
     * one is `user_a`/`user_b` — used by [conversationWith]'s callers *before*
     * attempting [createConversation], since local Room might not have synced
     * an already-existing conversation yet (e.g. right after login, before the
     * first `refreshConversations()` pass): fabricating a fresh id for a pair
     * that already has one would hit `conversations`' own
     * `unique(user_a, user_b)` constraint as a 409, since that constraint
     * isn't the `id` primary key `createConversation`'s own `ON CONFLICT`
     * target covers.
     */
    suspend fun conversationBetween(accessToken: String, userA: String, userB: String): ConversationRowDto? {
        val rows: List<ConversationRowDto> = client.get(restUrl("conversations")) {
            bearerAuth(accessToken)
            parameter("or", "(and(user_a.eq.$userA,user_b.eq.$userB),and(user_a.eq.$userB,user_b.eq.$userA))")
            parameter("select", "id,user_a,user_b")
        }.body()
        return rows.firstOrNull()
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
