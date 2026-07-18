package com.example.mozic.feature.player

/**
 * Shared `Modifier.sharedElement` key for a song's cover art, used by both
 * [MiniPlayerBar] and `NowPlayingScreen` so the mini-player thumbnail morphs
 * into the Now Playing disc (A5). Keyed on song id, not a fixed string, so a
 * queue transition mid-animation never mismatches two different covers.
 */
internal fun playerCoverSharedElementKey(songId: String) = "player-cover-$songId"
