package com.example.mozic.core.network.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.PlaylistRepository
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.mapper.playlistsWithCounts
import com.example.mozic.core.network.paging.PlaylistSongsPagingSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val PLAYLIST_SONGS_PAGE_SIZE = 50

/** Real, Supabase/PostgREST-backed [PlaylistRepository] (C2). */
@Singleton
class NetworkPlaylistRepository @Inject constructor(
    private val api: SupabaseCatalogApi,
) : PlaylistRepository {

    override fun playlists(category: PlaylistCategory): Flow<List<Playlist>> = flow {
        emit(api.playlistsWithCounts(category))
    }

    override fun playlistSongs(playlistId: String): Flow<PagingData<Song>> =
        Pager(PagingConfig(pageSize = PLAYLIST_SONGS_PAGE_SIZE)) {
            PlaylistSongsPagingSource(api, playlistId)
        }.flow
}
