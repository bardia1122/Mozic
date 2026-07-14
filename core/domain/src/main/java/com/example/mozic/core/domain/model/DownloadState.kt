package com.example.mozic.core.domain.model

/**
 * Per-song download status. Owned by Person B's [DownloadRepository]
 * implementation but consumed everywhere (Person A's "smart play" streams the
 * remote URL unless state is [Downloaded], then it uses [Downloaded.localFilePath]).
 */
sealed interface DownloadState {
    data object NotDownloaded : DownloadState
    data object Queued : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    data class Downloaded(val localFilePath: String) : DownloadState
    data class Failed(val reason: String?) : DownloadState
}
