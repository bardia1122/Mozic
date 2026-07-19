package com.example.mozic.core.network.paging

import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.network.SupabaseCatalogApi
import com.example.mozic.core.network.mapper.toDomain

internal class PlaylistSongsPagingSource(
    private val api: SupabaseCatalogApi,
    private val playlistId: String,
) : OffsetPagingSource<Song>() {
    override suspend fun fetch(range: IntRange): RangePage<Song> {
        val page = api.playlistSongs(playlistId, range)
        return RangePage(page.items.map { it.songs.toDomain() }, page.total)
    }
}
