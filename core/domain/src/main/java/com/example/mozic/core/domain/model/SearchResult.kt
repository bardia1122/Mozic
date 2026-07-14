package com.example.mozic.core.domain.model

/** A single hit in a mixed search feed; the UI renders each arm differently. */
sealed interface SearchResult {
    data class SongResult(val song: Song) : SearchResult
    data class ArtistResult(val artist: Artist) : SearchResult
    data class PlaylistResult(val playlist: Playlist) : SearchResult
}
