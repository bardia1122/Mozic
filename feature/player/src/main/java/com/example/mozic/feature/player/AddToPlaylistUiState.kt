package com.example.mozic.feature.player

import com.example.mozic.core.domain.model.Playlist

sealed interface AddToPlaylistUiState {
    data object Loading : AddToPlaylistUiState

    data object LoggedOut : AddToPlaylistUiState

    data class Content(val playlists: List<Playlist>, val addingPlaylistId: String? = null) : AddToPlaylistUiState
}

sealed interface AddToPlaylistEffect {
    data object Added : AddToPlaylistEffect
}
