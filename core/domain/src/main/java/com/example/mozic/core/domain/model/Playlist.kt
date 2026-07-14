package com.example.mozic.core.domain.model

data class Playlist(
    val id: String,
    val title: String,
    val coverImageUrl: String?,
    val ownerId: String?,
    val isPublic: Boolean,
    val category: PlaylistCategory,
    val songCount: Int,
)

/** WORLD = editorial/global, LOCAL = region, USER = user-created. */
enum class PlaylistCategory { WORLD, LOCAL, USER }
