package com.example.mozic.feature.downloads

import com.example.mozic.core.domain.model.Song

/** "By date" is deliberately not offered — see `doc/PROGRESS.md`'s B6 notes on why. */
enum class DownloadSortOrder { NAME, ARTIST }

sealed interface DownloadsUiState {
    data object Loading : DownloadsUiState

    data class Content(val songs: List<Song>, val sortOrder: DownloadSortOrder) : DownloadsUiState
}

sealed interface DownloadsEvent {
    data class SetSortOrder(val order: DownloadSortOrder) : DownloadsEvent

    /** [queueIds] is the currently loaded (sorted) order, so the tapped song's position is honored. */
    data class SongClick(val song: Song, val queueIds: List<String>) : DownloadsEvent

    data class RemoveDownload(val songId: String) : DownloadsEvent
}
