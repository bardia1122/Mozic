package com.example.mozic.feature.playlists

import com.example.mozic.core.domain.model.Song

sealed interface PlaylistDetailEvent {
    /** [queueIds] is the currently loaded song order, so the tapped song's position is honored. */
    data class SongClick(val song: Song, val queueIds: List<String>) : PlaylistDetailEvent

    data class PlayAll(val queueIds: List<String>, val shuffle: Boolean) : PlaylistDetailEvent
}
