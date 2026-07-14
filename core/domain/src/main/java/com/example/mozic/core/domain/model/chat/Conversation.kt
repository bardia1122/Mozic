package com.example.mozic.core.domain.model.chat

import com.example.mozic.core.domain.model.User

data class Conversation(
    val id: String,
    val peer: User,
    val lastMessage: Message?,
    val unreadCount: Int,
)
