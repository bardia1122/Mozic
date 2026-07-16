package com.example.mozic.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.LibraryRepository
import com.example.mozic.feature.library.navigation.LibraryListKind
import com.example.mozic.feature.library.navigation.LibraryListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val libraryRepository: LibraryRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    val kind: LibraryListKind = savedStateHandle.toRoute<LibraryListRoute>().kind

    private val songs = when (kind) {
        LibraryListKind.LIKED -> libraryRepository.likedSongs()
        LibraryListKind.RECENTLY_PLAYED -> libraryRepository.recentlyPlayed()
    }

    val uiState: StateFlow<LibraryListUiState> = songs
        .map { LibraryListUiState.Content(it) as LibraryListUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryListUiState.Loading)

    fun onEvent(event: LibraryListEvent) {
        when (event) {
            is LibraryListEvent.SongClick -> playerController.playQueue(
                songIds = event.queueIds,
                startIndex = event.queueIds.indexOf(event.song.id).coerceAtLeast(0),
            )

            is LibraryListEvent.PlayAll -> playerController.playQueue(
                songIds = event.queueIds,
                shuffle = event.shuffle,
            )

            is LibraryListEvent.RemoveSong -> viewModelScope.launch {
                when (kind) {
                    LibraryListKind.LIKED -> libraryRepository.toggleLike(event.songId)
                    LibraryListKind.RECENTLY_PLAYED -> libraryRepository.removeRecent(event.songId)
                }
            }
        }
    }
}
