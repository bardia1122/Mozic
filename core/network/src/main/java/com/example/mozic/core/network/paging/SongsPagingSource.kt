package com.example.mozic.core.network.paging

import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.mapper.toDomain

internal class SongsPagingSource(
    private val api: SupabaseCatalogApi,
    private val order: String,
) : OffsetPagingSource<Song>() {
    override suspend fun fetch(range: IntRange): RangePage<Song> {
        val page = api.songs(order, range)
        return RangePage(page.items.map { it.toDomain() }, page.total)
    }
}
