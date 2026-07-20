package com.example.mozic.feature.chat

import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.model.chat.ConnectionState

data class ChatThreadUiState(
    val peer: User? = null,
    val connectionState: ConnectionState = ConnectionState.CONNECTED,
    val isPeerTyping: Boolean = false,
    val inputText: String = "",
)

sealed interface ChatThreadEvent {
    data class InputChanged(val text: String) : ChatThreadEvent

    data object Send : ChatThreadEvent

    data class SongShareClick(val songId: String) : ChatThreadEvent
}
