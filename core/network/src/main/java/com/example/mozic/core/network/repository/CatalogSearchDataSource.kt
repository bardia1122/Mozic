package com.example.mozic.core.network.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mozic.core.domain.model.SearchFilter
import com.example.mozic.core.domain.model.SearchResult
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.dto.SearchResultType
import com.example.mozic.core.network.paging.SearchCatalogPagingSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

private const val SEARCH_PAGE_SIZE = 20

/**
 * Real catalog-match search, backed by the `search_catalog` RPC (C2). Injected
 * into `:core:data`'s `SearchRepositoryImpl`, which keeps owning Room-backed
 * history — this class only ever answers "what matches this query", nothing
 * else in the frozen `SearchRepository` contract.
 */
@Singleton
class CatalogSearchDataSource @Inject constructor(
    private val api: SupabaseCatalogApi,
    private val json: Json,
) {
    fun search(query: String, filter: SearchFilter): Flow<PagingData<SearchResult>> {
        val resultType = when (filter) {
            SearchFilter.ALL -> SearchResultType.ALL
            SearchFilter.SONG -> SearchResultType.SONG
            SearchFilter.ARTIST -> SearchResultType.ARTIST
            SearchFilter.PLAYLIST -> SearchResultType.PLAYLIST
        }
        return Pager(PagingConfig(pageSize = SEARCH_PAGE_SIZE)) {
            SearchCatalogPagingSource(api, json, query, resultType)
        }.flow
    }
}
