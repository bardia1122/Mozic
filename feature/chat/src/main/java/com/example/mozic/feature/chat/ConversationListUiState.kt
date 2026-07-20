package com.example.mozic.feature.chat

import com.example.mozic.core.domain.model.chat.Conversation

sealed interface ConversationListUiState {
    data object Loading : ConversationListUiState

    /** C5: no session yet — [LoginFormState] drives the inline login form the screen renders instead of the list. */
    data object LoggedOut : ConversationListUiState

    data class Content(val conversations: List<Conversation>) : ConversationListUiState
}

sealed interface ConversationListEvent {
    data class DeleteConversation(val conversationId: String) : ConversationListEvent

    data class MarkUnread(val conversationId: String) : ConversationListEvent

    data object LogOut : ConversationListEvent
}

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    /** Resolved to `R.string.chat_login_error` by the screen — keeps the ViewModel resource-agnostic. */
    val hasError: Boolean = false,
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent

    data class PasswordChanged(val value: String) : LoginEvent

    data object Submit : LoginEvent
}
