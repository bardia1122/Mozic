package com.example.mozic.core.network.dto

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
