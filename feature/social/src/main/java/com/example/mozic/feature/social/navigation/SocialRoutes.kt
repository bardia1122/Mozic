package com.example.mozic.feature.social.navigation

import kotlinx.serialization.Serializable

@Serializable
data object UserSearchRoute

@Serializable
data class UserProfileRoute(val userId: String)

@Serializable
data object FollowingListRoute
