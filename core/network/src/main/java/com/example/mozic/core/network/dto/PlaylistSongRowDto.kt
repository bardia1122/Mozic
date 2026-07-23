package com.example.mozic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of `GET /playlist_songs?select=position,songs(*)` — PostgREST's
 * to-one embed nests the related `songs` row under the target table's name.
 */
@Serializable
data class PlaylistSongRowDto(
    val position: Int,
    val songs: SongDto,
)

/** POST body for the Now Playing screen's "Add to playlist" flow — appended at [position]. */
@Serializable
data class PlaylistSongInsertDto(
    @SerialName("playlist_id") val playlistId: String,
    @SerialName("song_id") val songId: String,
    val position: Int,
)
