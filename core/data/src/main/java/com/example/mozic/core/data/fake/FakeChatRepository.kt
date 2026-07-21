package com.example.mozic.core.data.fake

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.chat.ConnectionState
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.model.chat.MessagePayload
import com.example.mozic.core.domain.model.chat.MessageStatus
import com.example.mozic.core.domain.repository.ChatRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * How long a fake send sits in [MessageStatus.SENDING] before flipping to
 * [MessageStatus.SENT] — long enough for the clock icon to actually be
 * visible in the UI.
 */
private const val FAKE_SEND_ACK_DELAY_MS = 500L

/**
 * Fake chat backed by an in-memory message map (the real one is Room-backed).
 * Sends append locally so the UI updates live, mirroring the optimistic-send flow.
 */
@Singleton
class FakeChatRepository @Inject constructor() : ChatRepository {
    private val messagesByConvo = MutableStateFlow(SampleData.messagesByConversation)
    private val unread = MutableStateFlow(
        SampleData.conversations.associate { it.id to it.unreadCount },
    )
    private val typingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val connection = MutableStateFlow(ConnectionState.CONNECTED)
    private val deletedConversationIds = MutableStateFlow<Set<String>>(emptySet())

    /** I1's friend picker can start a DM with anyone followed, not just the two pre-seeded [SampleData] ones. */
    private val createdConversations = MutableStateFlow<List<Conversation>>(emptyList())

    override val connectionState: StateFlow<ConnectionState> = connection.asStateFlow()

    override fun conversations(): Flow<List<Conversation>> = combine(
        messagesByConvo,
        unread,
        deletedConversationIds,
        createdConversations,
    ) { byConvo, unreadCounts, deleted, created ->
        (SampleData.conversations + created)
            .filterNot { it.id in deleted }
            .map { convo ->
                convo.copy(
                    lastMessage = byConvo[convo.id].orEmpty().maxByOrNull { it.sentAtEpochMs },
                    unreadCount = unreadCounts[convo.id] ?: 0,
                )
            }
    }

    override suspend fun conversationWith(peerId: String): String? {
        val allConversations = SampleData.conversations + createdConversations.value
        allConversations.find { it.peer.id == peerId }?.let { return it.id }
        val peer = SampleData.users.find { it.id == peerId } ?: return null
        val id = "fake-conv-$peerId"
        createdConversations.update { it + Conversation(id = id, peer = peer, lastMessage = null, unreadCount = 0) }
        return id
    }

    /** Newest-first, matching the `reverseLayout = true` chat `LazyColumn` (and Room's real `PagingSource` order). */
    override fun messages(conversationId: String): Flow<PagingData<Message>> =
        messagesByConvo.map { byConvo ->
            PagingData.from(byConvo[conversationId].orEmpty().sortedByDescending { it.sentAtEpochMs })
        }

    override suspend fun sendText(conversationId: String, text: String) {
        send(conversationId, MessagePayload.Text(text))
    }

    override suspend fun sendSongShare(conversationId: String, songId: String) {
        val song = SampleData.songs.find { it.id == songId } ?: return
        send(
            conversationId,
            MessagePayload.SongShare(
                songId = song.id,
                title = song.title,
                artistName = song.artistName,
                coverImageUrl = song.coverImageUrl,
            ),
        )
    }

    override suspend fun markConversationRead(conversationId: String) {
        unread.update { it + (conversationId to 0) }
        messagesByConvo.update { byConvo ->
            val read = byConvo[conversationId].orEmpty().map { it.copy(status = MessageStatus.READ) }
            byConvo + (conversationId to read)
        }
    }

    override suspend fun markConversationUnread(conversationId: String) {
        unread.update { counts ->
            val current = counts[conversationId] ?: 0
            counts + (conversationId to if (current > 0) current else 1)
        }
    }

    override suspend fun deleteConversation(conversationId: String) {
        deletedConversationIds.update { it + conversationId }
    }

    override fun peerIsTyping(conversationId: String): Flow<Boolean> =
        typingState.map { it[conversationId] ?: false }

    override suspend fun setTyping(conversationId: String, typing: Boolean) {
        typingState.update { it + (conversationId to typing) }
    }

    /**
     * Mirrors the real optimistic-send flow (SENDING appears instantly, then
     * flips to SENT) so the clock/check UI is actually exercised on fakes.
     */
    private suspend fun send(conversationId: String, payload: MessagePayload) {
        val id = UUID.randomUUID().toString()
        val message = Message(
            id = id,
            conversationId = conversationId,
            senderId = SampleData.CURRENT_USER_ID,
            sentAtEpochMs = System.currentTimeMillis(),
            status = MessageStatus.SENDING,
            payload = payload,
        )
        messagesByConvo.update { byConvo ->
            byConvo + (conversationId to (byConvo[conversationId].orEmpty() + message))
        }
        delay(FAKE_SEND_ACK_DELAY_MS)
        messagesByConvo.update { byConvo ->
            val updated = byConvo[conversationId].orEmpty().map {
                if (it.id == id) it.copy(status = MessageStatus.SENT) else it
            }
            byConvo + (conversationId to updated)
        }
    }
}
