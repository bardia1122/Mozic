package com.example.mozic.core.domain.repository

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.chat.ConnectionState
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    fun conversations(): Flow<List<Conversation>>

    /** Room-backed, so history renders identically live or offline. */
    fun messages(conversationId: String): Flow<PagingData<Message>>

    suspend fun sendText(conversationId: String, text: String)

    suspend fun sendSongShare(conversationId: String, songId: String)

    /**
     * Resolves the DM conversation with [peerId] for I1's song-share friend
     * picker, creating it if none exists yet — C1's seed data pre-created every
     * conversation, so nothing needed this before. Returns `null` if the
     * caller isn't authenticated (share entry points gate on login already,
     * same as C6's follow gate).
     */
    suspend fun conversationWith(peerId: String): String?

    suspend fun markConversationRead(conversationId: String)

    /**
     * Flips the badge back on without touching any message's stored status —
     * a local "remind me" flag, not a receipt.
     */
    suspend fun markConversationUnread(conversationId: String)

    suspend fun deleteConversation(conversationId: String)

    fun peerIsTyping(conversationId: String): Flow<Boolean>

    suspend fun setTyping(conversationId: String, typing: Boolean)

    val connectionState: StateFlow<ConnectionState>
}
