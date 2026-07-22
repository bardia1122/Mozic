package com.example.mozic.core.network.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.model.NotLoggedInException
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.PlaylistRepository
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.dto.PlaylistInsertDto
import com.example.mozic.core.network.mapper.playlistsWithCounts
import com.example.mozic.core.network.paging.PlaylistSongsPagingSource
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

private const val PLAYLIST_SONGS_PAGE_SIZE = 50

/**
 * Real, Supabase/PostgREST-backed [PlaylistRepository] (C2; [createPlaylist]
 * added later). [refreshTrigger] exists solely for [createPlaylist] — a plain
 * `flow { emit(...) }` per [playlists] call, like C2 shipped it, never
 * notices its own writes, same reasoning as `NetworkProfileRepository`'s
 * identical trigger.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NetworkPlaylistRepository @Inject constructor(
    private val api: SupabaseCatalogApi,
    private val authRepository: AuthRepository,
) : PlaylistRepository {

    private val refreshTrigger = MutableStateFlow(0)

    override fun playlists(category: PlaylistCategory): Flow<List<Playlist>> =
        refreshTrigger.flatMapLatest { flow { emit(api.playlistsWithCounts(category)) } }

    override fun playlistSongs(playlistId: String): Flow<PagingData<Song>> =
        Pager(PagingConfig(pageSize = PLAYLIST_SONGS_PAGE_SIZE)) {
            PlaylistSongsPagingSource(api, playlistId)
        }.flow

    override suspend fun createPlaylist(title: String): Playlist {
        val auth = requireLoggedIn()
        val id = UUID.randomUUID().toString()
        api.createPlaylist(
            accessToken = auth.accessToken,
            playlist = PlaylistInsertDto(
                id = id,
                title = title,
                ownerId = auth.userId,
                isPublic = true,
                category = "USER",
            ),
        )
        refreshTrigger.update { it + 1 }
        return Playlist(
            id = id,
            title = title,
            coverImageUrl = null,
            ownerId = auth.userId,
            isPublic = true,
            category = PlaylistCategory.USER,
            songCount = 0,
        )
    }

    // Same "await past Unknown" reasoning as NetworkSocialRepository.requireLoggedIn.
    private suspend fun requireLoggedIn(): AuthState.LoggedIn =
        authRepository.authState.first { it !is AuthState.Unknown } as? AuthState.LoggedIn
            ?: throw NotLoggedInException()
}
