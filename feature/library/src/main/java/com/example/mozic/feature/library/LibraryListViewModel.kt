package com.example.mozic.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.DownloadRepository
import com.example.mozic.core.domain.repository.LibraryRepository
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import com.example.mozic.feature.library.navigation.LibraryListKind
import com.example.mozic.feature.library.navigation.LibraryListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val libraryRepository: LibraryRepository,
    private val playerController: PlayerController,
    private val downloadRepository: DownloadRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val kind: LibraryListKind = savedStateHandle.toRoute<LibraryListRoute>().kind

    private val songs = when (kind) {
        LibraryListKind.LIKED -> libraryRepository.likedSongs()
        LibraryListKind.RECENTLY_PLAYED -> libraryRepository.recentlyPlayed()
    }

    private val _effects = Channel<LibraryListEffect>(Channel.BUFFERED)
    val effects: Flow<LibraryListEffect> = _effects.receiveAsFlow()

    /**
     * One [DownloadRepository.downloadState] subscription per currently-loaded
     * song, re-combined whenever the song list itself changes.
     */
    private val downloadStates: Flow<Map<String, DownloadState>> = songs.flatMapLatest { list ->
        if (list.isEmpty()) {
            flowOf(emptyMap())
        } else {
            val stateFlows = list.map { song ->
                downloadRepository.downloadState(song.id).map { state -> song.id to state }
            }
            combine(stateFlows) { pairs -> pairs.toMap() }
        }
    }

    val uiState: StateFlow<LibraryListUiState> = combine(
        songs,
        downloadStates,
        userPreferencesRepository.preferences,
    ) { list, states, prefs ->
        LibraryListUiState.Content(list, states, prefs.isPremium) as LibraryListUiState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryListUiState.Loading)

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
                    LibraryListKind.LIKED -> libraryRepository.unlike(event.songId)
                    LibraryListKind.RECENTLY_PLAYED -> libraryRepository.removeRecent(event.songId)
                }
            }

            is LibraryListEvent.RequestDownload -> viewModelScope.launch {
                downloadRepository.enqueue(event.songId)
            }

            is LibraryListEvent.RequestRemoveDownload -> viewModelScope.launch {
                downloadRepository.remove(event.songId)
            }

            LibraryListEvent.UpgradeRequired -> viewModelScope.launch {
                _effects.send(LibraryListEffect.ShowUpgradePrompt)
            }
        }
    }
}
