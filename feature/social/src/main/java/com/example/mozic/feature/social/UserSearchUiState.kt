package com.example.mozic.feature.social

data class UserSearchUiState(val query: String = "")

sealed interface UserSearchEvent {
    data class QueryChanged(val query: String) : UserSearchEvent

    data class FollowToggle(val userId: String, val currentlyFollowed: Boolean) : UserSearchEvent
}
