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
    data object CreatePlaylistClick : PlaylistsEvent
    data class CreatePlaylistTitleChange(val title: String) : PlaylistsEvent
    data object CreatePlaylistConfirm : PlaylistsEvent
    data object CreatePlaylistDismiss : PlaylistsEvent
}

sealed interface PlaylistsEffect {
    data class NavigateToDetail(val playlist: Playlist) : PlaylistsEffect
    data object LoginRequiredForCreate : PlaylistsEffect
    data object PlaylistCreated : PlaylistsEffect
}

/**
 * Local UI state for the "Create playlist" dialog — separate from
 * [PlaylistsUiState] since it's transient chrome, not fetched content.
 */
sealed interface CreatePlaylistUiState {
    data object Hidden : CreatePlaylistUiState

    data class Visible(
        val title: String = "",
        val isSubmitting: Boolean = false,
        val showError: Boolean = false,
    ) : CreatePlaylistUiState
}
