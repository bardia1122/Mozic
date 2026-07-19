package com.example.mozic.core.network.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * Item-index-offset [PagingSource] for PostgREST's `Range`/`Content-Range`
 * pagination — same "cursor is an item index, not a page number" shape this
 * codebase's other hand-rolled sources use (`SearchResultsPagingSource`,
 * `PlaylistSongsPagingSource` in `:core:data`'s fakes), just backed by a real
 * network fetch instead of slicing an in-memory list.
 */
abstract class OffsetPagingSource<T : Any> : PagingSource<Int, T>() {

    final override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    // Paging's own contract wants every failure (IOException, Ktor's
    // ClientRequestException/ServerResponseException, a SerializationException
    // on a malformed response, …) turned into LoadResult.Error, never thrown —
    // the doc's own PagingSource snippet (CLAUDE_PERSON_C.md §5) catches
    // Exception for the same reason.
    @Suppress("TooGenericExceptionCaught")
    final override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val start = params.key ?: 0
        val end = start + params.loadSize - 1
        return try {
            val page = fetch(start..end)
            val nextStart = start + page.items.size
            val hasMore = page.items.isNotEmpty() && (page.total == null || nextStart < page.total)
            LoadResult.Page(
                data = page.items,
                prevKey = if (start == 0) null else (start - params.loadSize).coerceAtLeast(0),
                nextKey = if (hasMore) nextStart else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    protected abstract suspend fun fetch(range: IntRange): RangePage<T>
}
