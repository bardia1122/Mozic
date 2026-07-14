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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

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

    override val connectionState: StateFlow<ConnectionState> = connection.asStateFlow()

    override fun conversations(): Flow<List<Conversation>> =
        combine(messagesByConvo, unread) { byConvo, unreadCounts ->
            SampleData.conversations.map { convo ->
                convo.copy(
                    lastMessage = byConvo[convo.id].orEmpty().maxByOrNull { it.sentAtEpochMs },
                    unreadCount = unreadCounts[convo.id] ?: 0,
                )
            }
        }

    override fun messages(conversationId: String): Flow<PagingData<Message>> =
        messagesByConvo.map { byConvo ->
            PagingData.from(byConvo[conversationId].orEmpty().sortedBy { it.sentAtEpochMs })
        }

    override suspend fun sendText(conversationId: String, text: String) {
        append(conversationId, MessagePayload.Text(text))
    }

    override suspend fun sendSongShare(conversationId: String, songId: String) {
        val song = SampleData.songs.find { it.id == songId } ?: return
        append(
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

    override fun peerIsTyping(conversationId: String): Flow<Boolean> =
        typingState.map { it[conversationId] ?: false }

    override suspend fun setTyping(conversationId: String, typing: Boolean) {
        typingState.update { it + (conversationId to typing) }
    }

    private fun append(conversationId: String, payload: MessagePayload) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = SampleData.CURRENT_USER_ID,
            sentAtEpochMs = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            payload = payload,
        )
        messagesByConvo.update { byConvo ->
            byConvo + (conversationId to (byConvo[conversationId].orEmpty() + message))
        }
    }
}
