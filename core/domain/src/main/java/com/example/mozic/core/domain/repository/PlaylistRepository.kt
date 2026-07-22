package com.example.mozic.core.domain.repository

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun playlists(category: PlaylistCategory): Flow<List<Playlist>>

    fun playlistSongs(playlistId: String): Flow<PagingData<Song>>

    /**
     * Creates a public [PlaylistCategory.USER] playlist owned by the caller.
     * Throws [com.example.mozic.core.domain.model.NotLoggedInException] if not logged in.
     */
    suspend fun createPlaylist(title: String): Playlist
}
