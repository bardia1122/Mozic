package com.example.mozic.core.data.fake

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.PlaylistRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class FakePlaylistRepository @Inject constructor() : PlaylistRepository {
    override fun playlists(category: PlaylistCategory): Flow<List<Playlist>> =
        flowOf(SampleData.playlists.filter { it.category == category })

    override fun playlistSongs(playlistId: String): Flow<PagingData<Song>> =
        flowOf(PagingData.from(SampleData.songs))
}
