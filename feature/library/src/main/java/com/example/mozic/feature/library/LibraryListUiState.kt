package com.example.mozic.feature.library

import com.example.mozic.core.domain.model.Song

sealed interface LibraryListUiState {
    data object Loading : LibraryListUiState

    data class Content(val songs: List<Song>) : LibraryListUiState
}

sealed interface LibraryListEvent {
    /** [queueIds] is the currently loaded song order, so the tapped song's position is honored. */
    data class SongClick(val song: Song, val queueIds: List<String>) : LibraryListEvent

    data class PlayAll(val queueIds: List<String>, val shuffle: Boolean) : LibraryListEvent

    data class RemoveSong(val songId: String) : LibraryListEvent
}
