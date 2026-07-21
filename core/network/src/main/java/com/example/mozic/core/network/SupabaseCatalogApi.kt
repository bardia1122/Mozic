package com.example.mozic.core.network

import com.example.mozic.core.network.dto.PlaylistDto
import com.example.mozic.core.network.dto.PlaylistSongCountRowDto
import com.example.mozic.core.network.dto.PlaylistSongRowDto
import com.example.mozic.core.network.dto.SearchCatalogRowDto
import com.example.mozic.core.network.dto.SongDto
import com.example.mozic.core.network.paging.RangePage
import com.example.mozic.core.network.paging.applyRange
import com.example.mozic.core.network.paging.toRangePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

private const val REST_PATH = "/rest/v1/"

/**
 * Thin wrapper over PostgREST's raw HTTP surface (`backend/supabase/schema.sql`
 * + `backend/README.md` §Endpoints). Returns DTOs only — mapping to domain
 * models happens in `mapper/CatalogMappers.kt`, never here.
 */
@Singleton
class SupabaseCatalogApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun songs(order: String, range: IntRange): RangePage<SongDto> {
        val response = client.get(restUrl("songs")) {
            parameter("select", "*")
            parameter("order", order)
            applyRange(range)
        }
        return response.toRangePage()
    }

    suspend fun songById(id: String): SongDto? {
        val response = client.get(restUrl("songs")) {
            parameter("id", "eq.$id")
            parameter("select", "*")
        }
        return response.body<List<SongDto>>().firstOrNull()
    }

    suspend fun playlists(category: String): List<PlaylistDto> {
        val response = client.get(restUrl("playlists")) {
            parameter("category", "eq.$category")
            parameter("select", "*")
        }
        return response.body()
    }

    suspend fun playlistSongs(playlistId: String, range: IntRange): RangePage<PlaylistSongRowDto> {
        val response = client.get(restUrl("playlist_songs")) {
            parameter("playlist_id", "eq.$playlistId")
            parameter("select", "position,songs(*)")
            parameter("order", "position")
            applyRange(range)
        }
        return response.toRangePage()
    }

    /** One batched query instead of N+1 per-playlist count lookups. */
    suspend fun playlistSongCounts(playlistIds: List<String>): Map<String, Int> {
        if (playlistIds.isEmpty()) return emptyMap()
        val response = client.get(restUrl("playlist_songs")) {
            parameter("playlist_id", "in.(${playlistIds.joinToString(",")})")
            parameter("select", "playlist_id")
        }
        val rows: List<PlaylistSongCountRowDto> = response.body()
        return rows.groupingBy { it.playlistId }.eachCount()
    }

    /**
     * Called as `GET .../rpc/search_catalog?q=...&result_type=...`, not POST +
     * JSON body — confirmed empirically that PostgREST only honors the `Range`
     * header (i.e. actually paginates) on GET-invoked RPCs; a POST call with
     * the same params returns the *entire* result set every time regardless
     * of the requested range.
     */
    suspend fun searchCatalog(query: String, resultType: String, range: IntRange): RangePage<SearchCatalogRowDto> {
        val response = client.get(restUrl("rpc/search_catalog")) {
            parameter("q", query)
            parameter("result_type", resultType)
            applyRange(range)
        }
        return response.toRangePage()
    }

    private fun restUrl(path: String) = "${BuildConfig.SUPABASE_URL}$REST_PATH$path"
}
