package com.example.mozic.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> = songRepository.homeContent()
        .map { content -> HomeUiState.Content(content.carousel, content.rows) as HomeUiState }
        .catch { emit(HomeUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SongClick -> playerController.playQueue(
                songIds = event.queue.map(Song::id),
                startIndex = event.queue.indexOf(event.song).coerceAtLeast(0),
            )

            is HomeEvent.PlaylistClick -> _effects.trySend(HomeEffect.ShowComingSoon)

            is HomeEvent.QuickActionClick -> when (event.action) {
                QuickAction.MY_PLAYLISTS -> _effects.trySend(HomeEffect.NavigateToPlaylists)
                QuickAction.LIKED -> _effects.trySend(HomeEffect.NavigateToLiked)
                QuickAction.RECENTLY_PLAYED -> _effects.trySend(HomeEffect.NavigateToRecentlyPlayed)
                QuickAction.TOP_ARTISTS -> _effects.trySend(HomeEffect.ShowComingSoon)
            }
        }
    }
}
