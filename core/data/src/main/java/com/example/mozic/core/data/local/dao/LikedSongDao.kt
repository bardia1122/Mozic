package com.example.mozic.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.mozic.core.data.local.entity.LikedSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {
    @Query("SELECT songId FROM liked_songs ORDER BY likedAtEpochMs DESC")
    fun likedSongIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE songId = :songId)")
    suspend fun isLiked(songId: String): Boolean

    @Upsert
    suspend fun like(entry: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE songId = :songId")
    suspend fun unlike(songId: String)
}
