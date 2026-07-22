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

enum class AuthFormMode { LOGIN, SIGN_UP }

/**
 * A client-side validation failure caught before the request ever reaches
 * Supabase — resolved to a `chat_signup_error_*`/`chat_login_error_*` string
 * by the screen, same "ViewModel stays resource-agnostic" rule as [LoginFormState.hasError].
 */
enum class LoginFieldError { MISSING_FIELDS, INVALID_EMAIL, WEAK_PASSWORD, PASSWORD_MISMATCH, INVALID_USERNAME }

data class LoginFormState(
    val mode: AuthFormMode = AuthFormMode.LOGIN,
    val email: String = "",
    val password: String = "",
    /** Sign-up only. */
    val confirmPassword: String = "",
    /** Sign-up only — becomes the new account's `profiles.display_name`. */
    val displayName: String = "",
    /** Sign-up only — becomes the new account's `profiles.username`. */
    val username: String = "",
    /** Sign-up only — a locally-picked preview; the bytes behind it live in the ViewModel until submit. */
    val avatarPreviewUri: String? = null,
    val isLoading: Boolean = false,
    val fieldError: LoginFieldError? = null,
    /**
     * Resolved to `R.string.chat_login_error`/`chat_signup_error` by the screen — keeps the ViewModel
     * resource-agnostic.
     */
    val hasError: Boolean = false,
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent

    data class PasswordChanged(val value: String) : LoginEvent

    data class ConfirmPasswordChanged(val value: String) : LoginEvent

    data class DisplayNameChanged(val value: String) : LoginEvent

    data class UsernameChanged(val value: String) : LoginEvent

    // Plain class, not data class: a ByteArray property would get a
    // content-blind, misleading generated equals()/hashCode() — and this is a
    // one-shot event, never compared for equality anyway.
    class AvatarPicked(val previewUri: String, val bytes: ByteArray, val mimeType: String) : LoginEvent

    data object AvatarCleared : LoginEvent

    data object ToggleMode : LoginEvent

    data object Submit : LoginEvent
}
