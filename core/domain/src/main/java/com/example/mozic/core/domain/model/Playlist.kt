package com.example.mozic.core.domain.model

data class Playlist(
    val id: String,
    val title: String,
    val coverImageUrl: String?,
    val ownerId: String?,
    val isPublic: Boolean,
    val category: PlaylistCategory,
    val songCount: Int,
    /**
     * Up to the first 4 member songs' cover URLs, by position — only
     * populated when [coverImageUrl] is null (a user-created playlist with
     * no curated cover of its own). Used to render a collage in place of a
     * single cover: 1 song fills the whole cover, 2 split left/right, 3 are
     * two on top/one on bottom, 4+ are a 2x2 grid of the first 4.
     */
    val coverImageUrls: List<String> = emptyList(),
)

/** WORLD = editorial/global, LOCAL = region, USER = user-created. */
enum class PlaylistCategory { WORLD, LOCAL, USER }
