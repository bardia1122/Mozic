package com.example.mozic.feature.social

import com.example.mozic.core.domain.model.User

sealed interface FollowingListUiState {
    data object Loading : FollowingListUiState

    data class Content(val users: List<User>) : FollowingListUiState
}
