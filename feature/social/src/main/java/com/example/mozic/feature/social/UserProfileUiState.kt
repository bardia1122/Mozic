package com.example.mozic.feature.social

import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.User

sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState

    data class Content(val user: User, val publicPlaylists: List<Playlist>) : UserProfileUiState

    data object NotFound : UserProfileUiState
}
