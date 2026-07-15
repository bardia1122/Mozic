package com.example.mozic.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.mozic.core.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

private const val MAX_HISTORY_ENTRIES = 10

@Dao
interface SearchHistoryDao {
    @Query("SELECT query FROM search_history ORDER BY searchedAtEpochMs DESC LIMIT $MAX_HISTORY_ENTRIES")
    fun recentQueries(): Flow<List<String>>

    /** Re-searching an existing query just bumps its timestamp via the primary-key conflict. */
    @Upsert
    suspend fun upsert(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun delete(query: String)
}
