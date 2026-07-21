package com.example.mozic.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    // `homeContent()` is a one-shot Flow that completes after its single
    // emission — a plain `.catch { emit(Error) }` chained after it would
    // catch a network failure once, then the whole flow (and this StateFlow's
    // only source of future updates) completes for good, with no way to
    // recover short of the ViewModel being torn down and recreated. That's
    // the bug: reconnecting Wi-Fi while stuck on the Home tab never recovers,
    // because nothing ever re-subscribes. The `.catch` has to sit *inside*
    // the `flatMapLatest` lambda so only the per-attempt inner flow dies on
    // error, never the outer chain that's listening for the next retry.
    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            songRepository.homeContent()
                .map { content -> HomeUiState.Content(content.carousel, content.rows) as HomeUiState }
                .catch { emit(HomeUiState.Error) }
        }
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

            HomeEvent.Retry -> retryTrigger.tryEmit(Unit)
        }
    }
}
