package com.example.mozic.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mozic.core.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE hiddenLocally = 0")
    fun conversations(): Flow<List<ConversationEntity>>

    /**
     * Only creates the row (default `hiddenLocally`/`forcedUnread` flags) — a
     * REST refresh must never overwrite an existing row's local flags, see
     * [updateProfile].
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(conversation: ConversationEntity)

    @Query(
        "UPDATE conversations SET peerUsername = :peerUsername, peerDisplayName = :peerDisplayName, " +
            "peerAvatarUrl = :peerAvatarUrl, peerIsPremium = :peerIsPremium WHERE id = :id",
    )
    suspend fun updateProfile(
        id: String,
        peerUsername: String,
        peerDisplayName: String,
        peerAvatarUrl: String?,
        peerIsPremium: Boolean,
    )

    @Query("UPDATE conversations SET hiddenLocally = 1 WHERE id = :id")
    suspend fun hide(id: String)

    @Query("UPDATE conversations SET forcedUnread = :forcedUnread WHERE id = :id")
    suspend fun setForcedUnread(id: String, forcedUnread: Boolean)
}
