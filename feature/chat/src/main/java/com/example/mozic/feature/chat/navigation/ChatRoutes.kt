package com.example.mozic.feature.chat.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ConversationListRoute

@Serializable
data class ChatThreadRoute(val conversationId: String)

/** I1's friend-picker bottom sheet — reachable from any song row/Now Playing, not just from within chat. */
@Serializable
data class ShareSongRoute(val songId: String)
