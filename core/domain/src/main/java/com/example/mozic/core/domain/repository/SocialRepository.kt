package com.example.mozic.core.domain.repository

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun searchUsers(query: String): Flow<PagingData<User>>

    fun following(): Flow<List<User>>

    suspend fun follow(userId: String)

    suspend fun unfollow(userId: String)

    fun publicPlaylistsOf(userId: String): Flow<List<Playlist>>

    /** C6 addition — a single user's profile (with a live [User.isFollowed]), for the profile screen. */
    fun userById(userId: String): Flow<User?>
}
