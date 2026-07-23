package com.example.mozic.core.data.fake

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.repository.PlaylistRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private const val PLAYLIST_SONGS_PAGE_SIZE = 50

/** Simulated network latency so the song list's shimmer skeleton is actually visible. */
private const val FAKE_PLAYLIST_SONGS_LOAD_DELAY_MS = 400L

@Singleton
class FakePlaylistRepository @Inject constructor() : PlaylistRepository {

    private val allPlaylists = MutableStateFlow(SampleData.playlists)

    override fun playlists(category: PlaylistCategory): Flow<List<Playlist>> =
        allPlaylists.map { list -> list.filter { it.category == category } }

    override suspend fun createPlaylist(title: String): Playlist {
        val playlist = Playlist(
            id = "fake-playlist-${UUID.randomUUID()}",
            title = title,
            coverImageUrl = null,
            ownerId = "fake-user",
            isPublic = true,
            category = PlaylistCategory.USER,
            songCount = 0,
        )
        allPlaylists.update { it + playlist }
        return playlist
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        allPlaylists.update { list ->
            list.map { if (it.id == playlistId) it.copy(songCount = it.songCount + 1) else it }
        }
    }

    override fun playlistSongs(playlistId: String): Flow<PagingData<Song>> {
        val songCount = SampleData.playlists.find { it.id == playlistId }?.songCount ?: SampleData.songs.size
        // Only 10 sample songs exist but playlists claim up to 50 — cycle the
        // catalog to reach the declared count. Real ids are kept (no synthetic
        // suffixes), so PlayerController.playQueue still resolves every entry.
        val songs = List(songCount) { index -> SampleData.songs[index % SampleData.songs.size] }
        return Pager(PagingConfig(pageSize = PLAYLIST_SONGS_PAGE_SIZE)) {
            PlaylistSongsPagingSource(songs)
        }.flow
    }
}

/**
 * Pages a pre-computed in-memory song list. Uses a real [PagingSource] instead
 * of the `PagingData.from(list)` shortcut, whose `LoadState` signaling proved
 * unreliable under rapid flow cancellation (see `SearchRepositoryImpl`'s
 * `SearchResultsPagingSource` for the bug this avoids).
 */
private class PlaylistSongsPagingSource(
    private val songs: List<Song>,
) : PagingSource<Int, Song>() {

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        delay(FAKE_PLAYLIST_SONGS_LOAD_DELAY_MS)
        val startIndex = params.key ?: 0
        val endIndex = (startIndex + params.loadSize).coerceAtMost(songs.size)
        val page = if (startIndex < songs.size) songs.subList(startIndex, endIndex) else emptyList()
        return LoadResult.Page(
            data = page,
            prevKey = if (startIndex == 0) null else (startIndex - params.loadSize).coerceAtLeast(0),
            nextKey = if (endIndex >= songs.size) null else endIndex,
        )
    }
}
