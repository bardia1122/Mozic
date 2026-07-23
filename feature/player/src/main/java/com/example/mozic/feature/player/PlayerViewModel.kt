package com.example.mozic.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.model.PlayerState
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.DownloadRepository
import com.example.mozic.core.domain.repository.LibraryRepository
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Thin by design: forwards [PlayerController.state] as-is and proxies every
 * user action straight to the controller — no business logic lives here.
 * Shared by both [MiniPlayerBar] and `NowPlayingScreen`; each composable gets
 * its own instance via `hiltViewModel()` (different `ViewModelStoreOwner`s),
 * both observing the same singleton [PlayerController].
 *
 * [extras] is the one piece of actual view-model logic: like/download/premium
 * status for the current song isn't part of [PlayerState] (that's
 * [PlayerController]'s contract, not B's repositories), so it's combined here
 * the same way `LibraryListViewModel` combines its own per-song download
 * states — see A6 in `doc/CLAUDE_PERSON_A.md`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val state: StateFlow<PlayerState> = playerController.state

    private val _effects = Channel<PlayerEffect>(Channel.BUFFERED)
    val effects: Flow<PlayerEffect> = _effects.receiveAsFlow()

    private val currentSongId: Flow<String?> = state.map { it.currentSong?.id }.distinctUntilChanged()

    private val isCurrentSongLiked: Flow<Boolean> = combine(
        currentSongId,
        libraryRepository.likedSongs(),
    ) { songId, liked -> songId != null && liked.any { it.id == songId } }

    private val currentDownloadState: Flow<DownloadState> = currentSongId.flatMapLatest { songId ->
        if (songId == null) flowOf(DownloadState.NotDownloaded) else downloadRepository.downloadState(songId)
    }

    val extras: StateFlow<PlayerExtrasUiState> = combine(
        isCurrentSongLiked,
        currentDownloadState,
        userPreferencesRepository.preferences,
    ) { isLiked, downloadState, prefs ->
        PlayerExtrasUiState(isLiked = isLiked, downloadState = downloadState, isPremium = prefs.isPremium)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerExtrasUiState())

    fun togglePlayPause() = playerController.togglePlayPause()

    fun next() = playerController.next()

    fun previous() = playerController.previous()

    fun stop() = playerController.stop()

    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)

    fun setSpeed(speed: Float) = playerController.setSpeed(speed)

    fun setSleepTimer(duration: Duration?) = playerController.setSleepTimer(duration)

    fun toggleShuffle() = playerController.toggleShuffle()

    fun cycleRepeatMode() = playerController.cycleRepeatMode()

    fun toggleLike() {
        val songId = state.value.currentSong?.id ?: return
        viewModelScope.launch { libraryRepository.toggleLike(songId) }
    }

    fun requestDownload() {
        val songId = state.value.currentSong?.id ?: return
        viewModelScope.launch { downloadRepository.enqueue(songId) }
    }

    fun removeDownload() {
        val songId = state.value.currentSong?.id ?: return
        viewModelScope.launch { downloadRepository.remove(songId) }
    }

    fun requestUpgrade() {
        viewModelScope.launch { _effects.send(PlayerEffect.ShowUpgradePrompt) }
    }
}
