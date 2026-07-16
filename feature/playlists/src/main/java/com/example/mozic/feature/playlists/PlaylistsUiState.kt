package com.example.mozic.feature.playlists

import com.example.mozic.core.domain.model.Playlist

sealed interface PlaylistsUiState {
    data object Loading : PlaylistsUiState

    data class Content(
        val world: List<Playlist>,
        val local: List<Playlist>,
        val mine: List<Playlist>,
    ) : PlaylistsUiState

    data object Error : PlaylistsUiState
}

sealed interface PlaylistsEvent {
    data class PlaylistClick(val playlist: Playlist) : PlaylistsEvent
}

sealed interface PlaylistsEffect {
    data class NavigateToDetail(val playlist: Playlist) : PlaylistsEffect
}
