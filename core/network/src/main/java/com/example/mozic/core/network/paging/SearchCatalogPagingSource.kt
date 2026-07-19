package com.example.mozic.core.network.paging

import com.example.mozic.core.domain.model.SearchResult
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.dto.PlaylistDto
import com.example.mozic.core.network.dto.SearchResultType
import com.example.mozic.core.network.mapper.toDomain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class SearchCatalogPagingSource(
    private val api: SupabaseCatalogApi,
    private val json: Json,
    private val query: String,
    private val resultType: String,
) : OffsetPagingSource<SearchResult>() {

    override suspend fun fetch(range: IntRange): RangePage<SearchResult> {
        if (query.isBlank()) return RangePage(emptyList(), 0)

        val page = api.searchCatalog(query, resultType, range)
        val playlistIds = page.items.mapNotNull { row ->
            if (row.type == SearchResultType.PLAYLIST) {
                row.playlist?.let { json.decodeFromJsonElement<PlaylistDto>(it).id }
            } else {
                null
            }
        }
        val playlistCounts = api.playlistSongCounts(playlistIds)
        val results = page.items.mapNotNull { it.toDomain(json) { id -> playlistCounts[id] ?: 0 } }
        return RangePage(results, page.total)
    }
}
