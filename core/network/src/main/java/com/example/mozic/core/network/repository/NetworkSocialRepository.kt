package com.example.mozic.core.network.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.SocialRepository
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.SupabaseSocialApi
import com.example.mozic.core.network.mapper.toDomain
import com.example.mozic.core.network.paging.UserSearchPagingSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val USER_SEARCH_PAGE_SIZE = 20

/**
 * Real, Supabase/PostgREST-backed [SocialRepository] (C6). Lives in
 * `:core:network` (not `:core:data`) — same placement as C2's
 * `NetworkSongRepository`/`NetworkPlaylistRepository` — since it needs no
 * Room, only REST calls plus [AuthRepository] for "who am I" (the same
 * domain interface `:core:network` already depends on for the WS auth
 * handshake's token, injectable here regardless of which module actually
 * binds the real impl — Hilt resolves the whole app-wide graph, not per-module).
 *
 * [followedIds] is an in-memory cache of "who I follow," refreshed whenever
 * [AuthRepository.authState] changes and optimistically updated (with a
 * revert-on-failure) by [follow]/[unfollow] — mirrors `RealChatRepository`'s
 * "one background scope reacting to authState" shape, just without Room: no
 * offline requirement was named for the social graph, unlike chat's Room
 * cache. [following] and [userById] both derive from this same StateFlow, so
 * a follow/unfollow toggle updates both live with zero extra plumbing. The
 * one place this doesn't reach is [searchUsers]'s already-loaded Paging
 * pages — see `UserSearchPagingSource`'s own kdoc for that documented gap.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NetworkSocialRepository @Inject constructor(
    private val socialApi: SupabaseSocialApi,
    private val catalogApi: SupabaseCatalogApi,
    private val authRepository: AuthRepository,
) : SocialRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val followedIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        scope.launch {
            authRepository.authState.collectLatest { state ->
                followedIds.value = when (state) {
                    is AuthState.LoggedIn ->
                        runCatching { socialApi.followedIds(state.userId).toSet() }.getOrDefault(emptySet())
                    AuthState.LoggedOut -> emptySet()
                }
            }
        }
    }

    override fun searchUsers(query: String): Flow<PagingData<User>> {
        val myUserId = currentUserId()
        val trimmed = query.trim()
        return Pager(PagingConfig(pageSize = USER_SEARCH_PAGE_SIZE)) {
            UserSearchPagingSource(socialApi, trimmed, myUserId) { followedIds.value }
        }.flow
    }

    override fun following(): Flow<List<User>> = followedIds.flatMapLatest { ids ->
        if (ids.isEmpty()) {
            flowOf(emptyList())
        } else {
            flow { emit(socialApi.profiles(ids.toList()).map { it.toDomain(isFollowed = true) }) }
        }
    }

    override fun userById(userId: String): Flow<User?> = followedIds.flatMapLatest { ids ->
        flow { emit(socialApi.profileById(userId)?.toDomain(isFollowed = userId in ids)) }
    }

    // Optimistic toggle + revert-on-failure (PLAN_PERSON_C.md's own C6 wording)
    // — any failure (not logged in, network error) must revert and propagate
    // so the UI layer's own try/catch can show a snackbar.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun follow(userId: String) {
        val auth = requireLoggedIn()
        followedIds.update { it + userId }
        try {
            socialApi.follow(auth.accessToken, auth.userId, userId)
        } catch (e: Exception) {
            followedIds.update { it - userId }
            throw e
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun unfollow(userId: String) {
        val auth = requireLoggedIn()
        followedIds.update { it - userId }
        try {
            socialApi.unfollow(auth.accessToken, auth.userId, userId)
        } catch (e: Exception) {
            followedIds.update { it + userId }
            throw e
        }
    }

    override fun publicPlaylistsOf(userId: String): Flow<List<Playlist>> = flow {
        val dtos = socialApi.playlistsByOwner(userId)
        val counts = catalogApi.playlistSongCounts(dtos.map { it.id })
        emit(dtos.map { it.toDomain(counts[it.id] ?: 0) })
    }

    private fun currentUserId(): String? = (authRepository.authState.value as? AuthState.LoggedIn)?.userId

    private fun requireLoggedIn(): AuthState.LoggedIn =
        authRepository.authState.value as? AuthState.LoggedIn
            ?: error("Must be logged in to follow/unfollow users")
}
