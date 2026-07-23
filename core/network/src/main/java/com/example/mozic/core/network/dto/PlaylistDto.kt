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
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("is_public") val isPublic: Boolean,
    val category: String,
)

/** One row of a `select=playlist_id` count-only query — see `playlistSongCounts`. */
@Serializable
data class PlaylistSongCountRowDto(
    @SerialName("playlist_id") val playlistId: String,
)

/** One row of `select=playlist_id,position,songs(cover_image_url)` — see `playlistCoverImageUrls`. */
@Serializable
data class PlaylistSongCoverRowDto(
    @SerialName("playlist_id") val playlistId: String,
    val position: Int,
    val songs: SongCoverDto,
)

/** Minimal embed — only the one column the collage cover actually needs, not a full [SongDto]. */
@Serializable
data class SongCoverDto(
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
)

/**
 * POST body for the "Create playlist" flow. The client generates [id] itself
 * — the column has no server-side default — and [category] is always
 * `"USER"`; `playlists_insert_own`'s RLS check (`backend/supabase/schema.sql`)
 * rejects anything else. `cover_image_url` is omitted (nullable column, no
 * cover at creation time).
 *
 * [isPublic]/[category] deliberately have no default value even though every
 * caller passes the same constant: the shared `Json`'s `encodeDefaults` is
 * unset (defaults to `false`), so a property left at its Kotlin default is
 * silently dropped from the request body — harmless for `is_public` (the
 * `playlists` table itself defaults it to `true`) but fatal for `category`,
 * which has no DB-side default and is `not null`; PostgREST then rejects the
 * insert with a null-constraint violation. Making both required forces
 * kotlinx.serialization to always encode them regardless of that setting.
 */
@Serializable
data class PlaylistInsertDto(
    val id: String,
    val title: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("is_public") val isPublic: Boolean,
    val category: String,
)
