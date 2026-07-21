package com.example.mozic.feature.chat

import com.example.mozic.core.domain.model.User

sealed interface ShareSongUiState {
    data object Loading : ShareSongUiState

    data object LoggedOut : ShareSongUiState

    data class Content(val friends: List<User>, val sendingPeerId: String? = null) : ShareSongUiState
}

sealed interface ShareSongEffect {
    data object Sent : ShareSongEffect
}
