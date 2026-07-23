package com.example.mozic.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.PlaylistRepository
import com.example.mozic.feature.player.navigation.AddToPlaylistRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Only lists playlists the caller actually owns — `playlist_songs_insert_owner`
 * (schema.sql) rejects an insert into anyone else's, so there's no point
 * showing playlists that would just fail on tap.
 */
@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val songId: String = savedStateHandle.toRoute<AddToPlaylistRoute>().songId

    private val addingPlaylistId = MutableStateFlow<String?>(null)

    private val _effects = Channel<AddToPlaylistEffect>(Channel.BUFFERED)
    val effects: Flow<AddToPlaylistEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<AddToPlaylistUiState> = combine(
        authRepository.authState,
        playlistRepository.playlists(PlaylistCategory.USER),
        addingPlaylistId,
    ) { auth, playlists, adding ->
        when (auth) {
            is AuthState.LoggedIn -> AddToPlaylistUiState.Content(
                playlists = playlists.filter { it.ownerId == auth.userId },
                addingPlaylistId = adding,
            )
            AuthState.LoggedOut -> AddToPlaylistUiState.LoggedOut
            AuthState.Unknown -> AddToPlaylistUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddToPlaylistUiState.Loading)

    fun onPlaylistClick(playlistId: String) {
        if (addingPlaylistId.value != null) return
        addingPlaylistId.value = playlistId
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
            addingPlaylistId.value = null
            _effects.send(AddToPlaylistEffect.Added)
        }
    }
}
