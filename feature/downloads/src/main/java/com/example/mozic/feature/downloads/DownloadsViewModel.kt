package com.example.mozic.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    private val sortOrder = MutableStateFlow(DownloadSortOrder.NAME)

    val uiState: StateFlow<DownloadsUiState> = combine(
        downloadRepository.allDownloads(),
        sortOrder,
    ) { songs, order ->
        DownloadsUiState.Content(songs.sortedFor(order), order) as DownloadsUiState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DownloadsUiState.Loading)

    fun onEvent(event: DownloadsEvent) {
        when (event) {
            is DownloadsEvent.SetSortOrder -> sortOrder.value = event.order

            is DownloadsEvent.SongClick -> playerController.playQueue(
                songIds = event.queueIds,
                startIndex = event.queueIds.indexOf(event.song.id).coerceAtLeast(0),
            )

            is DownloadsEvent.RemoveDownload -> viewModelScope.launch {
                downloadRepository.remove(event.songId)
            }
        }
    }
}

private fun List<Song>.sortedFor(order: DownloadSortOrder): List<Song> = when (order) {
    DownloadSortOrder.NAME -> sortedBy { it.title }
    DownloadSortOrder.ARTIST -> sortedBy { it.artistName }
}
