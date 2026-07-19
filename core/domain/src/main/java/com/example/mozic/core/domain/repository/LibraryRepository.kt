package com.example.mozic.core.domain.repository

import com.example.mozic.core.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun likedSongs(): Flow<List<Song>>

    fun recentlyPlayed(): Flow<List<Song>>

    suspend fun toggleLike(songId: String)

    suspend fun unlike(songId: String)

    suspend fun removeRecent(songId: String)

    suspend fun recordPlayed(songId: String)
}
