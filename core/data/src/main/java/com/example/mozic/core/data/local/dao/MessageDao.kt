package com.example.mozic.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.mozic.core.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    /** Newest-first, matching `reverseLayout = true` chat `LazyColumn` (C4). */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY sentAtEpochMs DESC")
    fun pagingSource(conversationId: String): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY sentAtEpochMs DESC LIMIT 1")
    fun lastMessage(conversationId: String): Flow<MessageEntity?>

    @Query(
        "SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId " +
            "AND senderId != :myUserId AND status != 'READ'",
    )
    fun unreadCount(conversationId: String, myUserId: String): Flow<Int>

    /** Rows never acked yet — resent on every reconnect (`RealChatRepository`'s offline queue). */
    @Query("SELECT * FROM messages WHERE status = 'SENDING' ORDER BY sentAtEpochMs")
    suspend fun pendingSends(): List<MessageEntity>

    @Query("SELECT MAX(sentAtEpochMs) FROM messages")
    suspend fun maxSentAtEpochMs(): Long?

    @Upsert
    suspend fun upsert(message: MessageEntity)

    @Upsert
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    /** I opened the thread: flips the *peer's* messages to READ. */
    @Query(
        "UPDATE messages SET status = 'READ' WHERE conversationId = :conversationId " +
            "AND senderId != :myUserId AND status != 'READ'",
    )
    suspend fun markPeerMessagesRead(conversationId: String, myUserId: String)

    /** The peer's `read` frame arrived: flips *my own* sent messages up to their reported `upToMs`. */
    @Query(
        "UPDATE messages SET status = 'READ' WHERE conversationId = :conversationId " +
            "AND senderId = :myUserId AND sentAtEpochMs <= :upToMs AND status != 'READ'",
    )
    suspend fun markMyMessagesReadUpTo(conversationId: String, myUserId: String, upToMs: Long)

    /** See [ConversationDao.clearAll] — same "cache belongs to whoever's logged in" reasoning. */
    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
