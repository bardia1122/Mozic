package com.example.mozic.feature.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _effects = Channel<PlaylistsEffect>(Channel.BUFFERED)
    val effects: Flow<PlaylistsEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<PlaylistsUiState> = combine(
        playlistRepository.playlists(PlaylistCategory.WORLD),
        playlistRepository.playlists(PlaylistCategory.LOCAL),
        playlistRepository.playlists(PlaylistCategory.USER),
    ) { world, local, mine -> PlaylistsUiState.Content(world, local, mine) as PlaylistsUiState }
        .catch { emit(PlaylistsUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaylistsUiState.Loading)

    fun onEvent(event: PlaylistsEvent) {
        when (event) {
            is PlaylistsEvent.PlaylistClick -> _effects.trySend(PlaylistsEffect.NavigateToDetail(event.playlist))
        }
    }
}
