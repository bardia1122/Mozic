package com.example.mozic.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mozic.core.data.fake.SampleData
import com.example.mozic.core.data.local.dao.SearchHistoryDao
import com.example.mozic.core.data.local.entity.SearchHistoryEntity
import com.example.mozic.core.domain.model.SearchFilter
import com.example.mozic.core.domain.model.SearchResult
import com.example.mozic.core.domain.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

private const val SEARCH_PAGE_SIZE = 20

/** Simulated network latency so the results list's shimmer skeleton is actually visible. */
private const val FAKE_SEARCH_LOAD_DELAY_MS = 500L

/**
 * History is real (Room, via [SearchHistoryDao]). Match results still come
 * from [SampleData] pending Person C's real catalog search (C2) — swapping
 * that in later is a one-line `@Binds` change, per the frozen-contract seam.
 */
@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao,
) : SearchRepository {

    override fun search(query: String, filter: SearchFilter): Flow<PagingData<SearchResult>> {
        val normalized = query.trim().lowercase()
        val results = when (filter) {
            SearchFilter.ALL -> songHits(normalized) + artistHits(normalized) + playlistHits(normalized)
            SearchFilter.SONG -> songHits(normalized)
            SearchFilter.ARTIST -> artistHits(normalized)
            SearchFilter.PLAYLIST -> playlistHits(normalized)
        }
        return Pager(PagingConfig(pageSize = SEARCH_PAGE_SIZE)) {
            SearchResultsPagingSource(results)
        }.flow
    }

    override fun history(): Flow<List<String>> = searchHistoryDao.recentQueries()

    override suspend fun addToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        searchHistoryDao.upsert(SearchHistoryEntity(query = trimmed, searchedAtEpochMs = System.currentTimeMillis()))
    }

    override suspend fun removeFromHistory(query: String) {
        searchHistoryDao.delete(query)
    }

    private fun songHits(query: String): List<SearchResult> =
        SampleData.songs
            .filter { it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query) }
            .map { SearchResult.SongResult(it) }

    private fun artistHits(query: String): List<SearchResult> =
        SampleData.artists
            .filter { it.name.lowercase().contains(query) }
            .map { SearchResult.ArtistResult(it) }

    private fun playlistHits(query: String): List<SearchResult> =
        SampleData.playlists
            .filter { it.title.lowercase().contains(query) }
            .map { SearchResult.PlaylistResult(it) }
}

/**
 * Pages a pre-computed in-memory match list. `PagingData.from(list)` (the
 * static-list shortcut) was tried first but its `LoadState` signaling proved
 * unreliable under the rapid `flatMapLatest` cancellation that debounced
 * search produces — a real [PagingSource] goes through Paging's normal,
 * well-tested load pipeline instead.
 */
private class SearchResultsPagingSource(
    private val results: List<SearchResult>,
) : PagingSource<Int, SearchResult>() {

    override fun getRefreshKey(state: PagingState<Int, SearchResult>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        delay(FAKE_SEARCH_LOAD_DELAY_MS)
        val startIndex = params.key ?: 0
        val endIndex = (startIndex + params.loadSize).coerceAtMost(results.size)
        val page = if (startIndex < results.size) results.subList(startIndex, endIndex) else emptyList()
        return LoadResult.Page(
            data = page,
            prevKey = if (startIndex == 0) null else (startIndex - params.loadSize).coerceAtLeast(0),
            nextKey = if (endIndex >= results.size) null else endIndex,
        )
    }
}
