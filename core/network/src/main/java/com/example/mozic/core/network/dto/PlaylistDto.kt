package com.example.mozic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors `public.playlists` (backend/supabase/schema.sql). `songCount` isn't
 * a column — PostgREST doesn't expose a reliable cross-version aggregate embed,
 * so it's fetched separately via a batched `playlist_songs` count query and
 * folded in at the mapper boundary (see `CatalogMappers.playlistsWithCounts`).
 */
@Serializable
data class PlaylistDto(
    val id: String,
    val title: String,
    @SerialName("cover_image_url") val coverImageUrl: String,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("is_public") val isPublic: Boolean,
    val category: String,
)

/** One row of a `select=playlist_id` count-only query — see `playlistSongCounts`. */
@Serializable
data class PlaylistSongCountRowDto(
    @SerialName("playlist_id") val playlistId: String,
)
