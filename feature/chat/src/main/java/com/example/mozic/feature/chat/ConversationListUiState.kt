package com.example.mozic.feature.chat

import com.example.mozic.core.domain.model.chat.Conversation

sealed interface ConversationListUiState {
    data object Loading : ConversationListUiState

    data class Content(val conversations: List<Conversation>) : ConversationListUiState
}

sealed interface ConversationListEvent {
    data class DeleteConversation(val conversationId: String) : ConversationListEvent

    data class MarkUnread(val conversationId: String) : ConversationListEvent
}
