package com.example.mozic.feature.playlists.navigation

import kotlinx.serialization.Serializable

/**
 * Carries the header fields the caller already has in hand at click time
 * (title/cover/count), so the detail header renders instantly with no
 * loading state of its own — only the paged song list below it loads.
 */
@Serializable
data class PlaylistDetailRoute(
    val playlistId: String,
    val title: String,
    val coverImageUrl: String?,
    val songCount: Int,
)
