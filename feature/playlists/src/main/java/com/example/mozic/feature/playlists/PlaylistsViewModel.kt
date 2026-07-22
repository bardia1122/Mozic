package com.example.mozic.feature.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _effects = Channel<PlaylistsEffect>(Channel.BUFFERED)
    val effects: Flow<PlaylistsEffect> = _effects.receiveAsFlow()

    private val _createPlaylistState = MutableStateFlow<CreatePlaylistUiState>(CreatePlaylistUiState.Hidden)
    val createPlaylistState: StateFlow<CreatePlaylistUiState> = _createPlaylistState.asStateFlow()

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
            PlaylistsEvent.CreatePlaylistClick -> onCreatePlaylistClick()
            is PlaylistsEvent.CreatePlaylistTitleChange -> onCreatePlaylistTitleChange(event.title)
            PlaylistsEvent.CreatePlaylistConfirm -> onCreatePlaylistConfirm()
            PlaylistsEvent.CreatePlaylistDismiss -> _createPlaylistState.value = CreatePlaylistUiState.Hidden
        }
    }

    private fun onCreatePlaylistClick() {
        viewModelScope.launch {
            val loggedIn = authRepository.authState.first { it !is AuthState.Unknown } is AuthState.LoggedIn
            if (loggedIn) {
                _createPlaylistState.value = CreatePlaylistUiState.Visible()
            } else {
                _effects.trySend(PlaylistsEffect.LoginRequiredForCreate)
            }
        }
    }

    private fun onCreatePlaylistTitleChange(title: String) {
        val current = _createPlaylistState.value as? CreatePlaylistUiState.Visible ?: return
        _createPlaylistState.value = current.copy(title = title, showError = false)
    }

    private fun onCreatePlaylistConfirm() {
        val current = _createPlaylistState.value as? CreatePlaylistUiState.Visible ?: return
        val title = current.title.trim()
        if (title.isEmpty() || current.isSubmitting) return

        _createPlaylistState.value = current.copy(isSubmitting = true, showError = false)
        viewModelScope.launch {
            runCatching { playlistRepository.createPlaylist(title) }
                .onSuccess {
                    _createPlaylistState.value = CreatePlaylistUiState.Hidden
                    _effects.trySend(PlaylistsEffect.PlaylistCreated)
                }
                .onFailure {
                    _createPlaylistState.value = current.copy(isSubmitting = false, showError = true)
                }
        }
    }
}
