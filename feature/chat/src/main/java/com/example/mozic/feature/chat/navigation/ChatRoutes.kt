package com.example.mozic.feature.chat.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ConversationListRoute

@Serializable
data class ChatThreadRoute(val conversationId: String)
