package com.example.mozic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * One row of `POST /rpc/search_catalog` (backend/supabase/schema.sql). Exactly
 * one of [song]/[artist]/[playlist] is populated, discriminated by [type] —
 * mirrors the domain's sealed `SearchResult`. [song]/[playlist] are raw
 * `to_jsonb()` dumps of their tables (same shape as [SongDto]/[PlaylistDto]);
 * [artist] is a synthetic object the RPC builds since there's no `artists`
 * table, decoded via [SearchArtistDto].
 */
@Serializable
data class SearchCatalogRowDto(
    val type: String,
    val song: JsonObject? = null,
    val artist: JsonObject? = null,
    val playlist: JsonObject? = null,
)

@Serializable
data class SearchArtistDto(
    val id: String,
    val name: String,
    @SerialName("imageUrl") val imageUrl: String? = null,
)

/** Discriminator values `search_catalog` emits in [SearchCatalogRowDto.type]. */
object SearchResultType {
    const val SONG = "song"
    const val ARTIST = "artist"
    const val PLAYLIST = "playlist"
    const val ALL = "all"
}
