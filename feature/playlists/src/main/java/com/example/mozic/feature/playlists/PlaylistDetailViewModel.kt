package com.example.mozic.feature.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.PlaylistRepository
import com.example.mozic.feature.playlists.navigation.PlaylistDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    playlistRepository: PlaylistRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PlaylistDetailRoute>()

    // Passed in via the route from the grid tap, so the header renders with no loading state.
    val title: String = route.title
    val coverImageUrl: String? = route.coverImageUrl
    val songCount: Int = route.songCount

    val songs: Flow<PagingData<Song>> = playlistRepository.playlistSongs(route.playlistId).cachedIn(viewModelScope)

    fun onEvent(event: PlaylistDetailEvent) {
        when (event) {
            is PlaylistDetailEvent.SongClick -> playerController.playQueue(
                songIds = event.queueIds,
                startIndex = event.queueIds.indexOf(event.song.id).coerceAtLeast(0),
            )

            is PlaylistDetailEvent.PlayAll -> playerController.playQueue(
                songIds = event.queueIds,
                shuffle = event.shuffle,
            )
        }
    }
}
