package com.example.mozic.feature.library

import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.model.Song

sealed interface LibraryListUiState {
    data object Loading : LibraryListUiState

    data class Content(
        val songs: List<Song>,
        val downloadStates: Map<String, DownloadState>,
        val isPremium: Boolean,
    ) : LibraryListUiState
}

sealed interface LibraryListEvent {
    /** [queueIds] is the currently loaded song order, so the tapped song's position is honored. */
    data class SongClick(val song: Song, val queueIds: List<String>) : LibraryListEvent

    data class PlayAll(val queueIds: List<String>, val shuffle: Boolean) : LibraryListEvent

    data class RemoveSong(val songId: String) : LibraryListEvent

    data class RequestDownload(val songId: String) : LibraryListEvent

    data class RequestRemoveDownload(val songId: String) : LibraryListEvent

    data object UpgradeRequired : LibraryListEvent
}

sealed interface LibraryListEffect {
    data object ShowUpgradePrompt : LibraryListEffect
}
