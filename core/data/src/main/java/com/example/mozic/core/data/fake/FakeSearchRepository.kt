package com.example.mozic.core.data.fake

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.SearchFilter
import com.example.mozic.core.domain.model.SearchResult
import com.example.mozic.core.domain.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

@Singleton
class FakeSearchRepository @Inject constructor() : SearchRepository {
    private val historyState = MutableStateFlow(listOf("lofi", "focus"))

    override fun search(query: String, filter: SearchFilter): Flow<PagingData<SearchResult>> {
        val q = query.trim().lowercase()
        val results = when (filter) {
            SearchFilter.ALL -> songHits(q) + artistHits(q) + playlistHits(q)
            SearchFilter.SONG -> songHits(q)
            SearchFilter.ARTIST -> artistHits(q)
            SearchFilter.PLAYLIST -> playlistHits(q)
        }
        return flowOf(PagingData.from(results))
    }

    override fun history(): Flow<List<String>> = historyState.asStateFlow()

    override suspend fun addToHistory(query: String) {
        historyState.update { (listOf(query) + it).distinct().take(10) }
    }

    override suspend fun removeFromHistory(query: String) {
        historyState.update { it - query }
    }

    private fun songHits(q: String): List<SearchResult> =
        SampleData.songs
            .filter { it.title.lowercase().contains(q) || it.artistName.lowercase().contains(q) }
            .map { SearchResult.SongResult(it) }

    private fun artistHits(q: String): List<SearchResult> =
        SampleData.artists
            .filter { it.name.lowercase().contains(q) }
            .map { SearchResult.ArtistResult(it) }

    private fun playlistHits(q: String): List<SearchResult> =
        SampleData.playlists
            .filter { it.title.lowercase().contains(q) }
            .map { SearchResult.PlaylistResult(it) }
}
