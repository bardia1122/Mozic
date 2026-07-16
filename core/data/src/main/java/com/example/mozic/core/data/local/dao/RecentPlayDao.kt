package com.example.mozic.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.mozic.core.data.local.entity.RecentPlayEntity
import kotlinx.coroutines.flow.Flow

private const val MAX_RECENT_PLAYS = 100

@Dao
interface RecentPlayDao {
    @Query("SELECT songId FROM recent_plays ORDER BY playedAtEpochMs DESC")
    fun recentSongIds(): Flow<List<String>>

    /** Re-playing an existing song just bumps its timestamp via the primary-key conflict. */
    @Upsert
    suspend fun upsert(entry: RecentPlayEntity)

    @Query(
        """
        DELETE FROM recent_plays WHERE songId NOT IN (
            SELECT songId FROM recent_plays ORDER BY playedAtEpochMs DESC LIMIT $MAX_RECENT_PLAYS
        )
        """,
    )
    suspend fun trimToCap()

    @Query("DELETE FROM recent_plays WHERE songId = :songId")
    suspend fun remove(songId: String)

    /** Records a play and enforces the [MAX_RECENT_PLAYS] cap in one atomic step. */
    @Transaction
    suspend fun recordPlay(songId: String, playedAtEpochMs: Long) {
        upsert(RecentPlayEntity(songId = songId, playedAtEpochMs = playedAtEpochMs))
        trimToCap()
    }
}
