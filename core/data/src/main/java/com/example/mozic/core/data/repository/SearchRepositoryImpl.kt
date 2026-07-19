package com.example.mozic.core.data.repository

import androidx.paging.PagingData
import com.example.mozic.core.data.local.dao.SearchHistoryDao
import com.example.mozic.core.data.local.entity.SearchHistoryEntity
import com.example.mozic.core.domain.model.SearchFilter
import com.example.mozic.core.domain.model.SearchResult
import com.example.mozic.core.domain.repository.SearchRepository
import com.example.mozic.core.network.repository.CatalogSearchDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * History is real (Room, via [SearchHistoryDao]). Match results are the real
 * catalog (C2, via [CatalogSearchDataSource] → Supabase's `search_catalog`
 * RPC) — replaces the `SampleData`-backed match lookup this class used before
 * C2, per the frozen-contract seam B3 called out.
 */
@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao,
    private val catalogSearchDataSource: CatalogSearchDataSource,
) : SearchRepository {

    override fun search(query: String, filter: SearchFilter): Flow<PagingData<SearchResult>> =
        catalogSearchDataSource.search(query, filter)

    override fun history(): Flow<List<String>> = searchHistoryDao.recentQueries()

    override suspend fun addToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        searchHistoryDao.upsert(SearchHistoryEntity(query = trimmed, searchedAtEpochMs = System.currentTimeMillis()))
    }

    override suspend fun removeFromHistory(query: String) {
        searchHistoryDao.delete(query)
    }
}
