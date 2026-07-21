package com.example.mozic.core.data.fake

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.repository.SocialRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeSocialRepository @Inject constructor() : SocialRepository {
    private val followedIds = MutableStateFlow(
        SampleData.users.filter { it.isFollowed }.map { it.id }.toSet(),
    )

    override fun searchUsers(query: String): Flow<PagingData<User>> {
        val q = query.trim().lowercase()
        return followedIds.map { followed ->
            val hits = SampleData.users.filter {
                it.username.lowercase().contains(q) || it.displayName.lowercase().contains(q)
            }
            PagingData.from(hits.map { it.copy(isFollowed = it.id in followed) })
        }
    }

    override fun following(): Flow<List<User>> = followedIds.map { followed ->
        SampleData.users.filter { it.id in followed }.map { it.copy(isFollowed = true) }
    }

    override suspend fun follow(userId: String) {
        followedIds.update { it + userId }
    }

    override suspend fun unfollow(userId: String) {
        followedIds.update { it - userId }
    }

    override fun publicPlaylistsOf(userId: String): Flow<List<Playlist>> =
        flowOf(SampleData.playlists.filter { it.ownerId == userId && it.isPublic })

    override fun userById(userId: String): Flow<User?> = followedIds.map { followed ->
        SampleData.users.find { it.id == userId }?.copy(isFollowed = userId in followed)
    }

    override fun followedUserIds(): Flow<Set<String>> = followedIds
}
