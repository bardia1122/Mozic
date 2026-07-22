package com.example.mozic.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.common.result.Result
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.ChatRepository
import com.example.mozic.core.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** RFC 5322-ish, good enough to catch typos before a request round-trip — not a full validator. */
private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}\$")

/** Matches Supabase Auth's own minimum (6 chars) plus a letter+digit requirement so seeded/new passwords agree. */
private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d).{6,}\$")

/** `profiles.username` is unique/not-null — kept to URL/mention-safe characters, matching the seeded demo usernames. */
private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,20}\$")

private data class PendingAvatar(val bytes: ByteArray, val mimeType: String)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<ConversationListUiState> = authRepository.authState
        .flatMapLatest { auth ->
            when (auth) {
                is AuthState.LoggedIn -> chatRepository.conversations()
                    .map { ConversationListUiState.Content(it) as ConversationListUiState }
                AuthState.LoggedOut -> flowOf(ConversationListUiState.LoggedOut)
                // Cold-start session restore still in flight — stay on Loading
                // rather than flashing the login form for an already-logged-in
                // user (same root cause as C6's follow/unfollow race).
                AuthState.Unknown -> flowOf(ConversationListUiState.Loading)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationListUiState.Loading)

    private val _loginState = MutableStateFlow(LoginFormState())
    val loginState: StateFlow<LoginFormState> = _loginState.asStateFlow()

    // Held outside LoginFormState (never rendered): the account doesn't exist
    // yet to upload an avatar to, so the picked bytes just wait here until
    // signUp() resolves — only used once, then discarded either way.
    private var pendingAvatar: PendingAvatar? = null

    fun onEvent(event: ConversationListEvent) {
        when (event) {
            is ConversationListEvent.DeleteConversation -> viewModelScope.launch {
                chatRepository.deleteConversation(event.conversationId)
            }

            is ConversationListEvent.MarkUnread -> viewModelScope.launch {
                chatRepository.markConversationUnread(event.conversationId)
            }

            // No UI in the C5 plan calls for this — added so one physical
            // phone can switch between the seeded demo accounts (alice/bob)
            // to exercise two-way chat without a second device.
            ConversationListEvent.LogOut -> viewModelScope.launch { authRepository.logout() }
        }
    }

    fun onLoginEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _loginState.update {
                it.copy(email = event.value, hasError = false, fieldError = null)
            }

            is LoginEvent.PasswordChanged -> _loginState.update {
                it.copy(password = event.value, hasError = false, fieldError = null)
            }

            is LoginEvent.ConfirmPasswordChanged -> _loginState.update {
                it.copy(confirmPassword = event.value, hasError = false, fieldError = null)
            }

            is LoginEvent.DisplayNameChanged -> _loginState.update {
                it.copy(displayName = event.value, hasError = false, fieldError = null)
            }

            is LoginEvent.UsernameChanged -> _loginState.update {
                it.copy(username = event.value, hasError = false, fieldError = null)
            }

            is LoginEvent.AvatarPicked -> {
                pendingAvatar = PendingAvatar(event.bytes, event.mimeType)
                _loginState.update { it.copy(avatarPreviewUri = event.previewUri) }
            }

            LoginEvent.AvatarCleared -> {
                pendingAvatar = null
                _loginState.update { it.copy(avatarPreviewUri = null) }
            }

            LoginEvent.ToggleMode -> {
                pendingAvatar = null
                _loginState.update {
                    LoginFormState(
                        mode = if (it.mode == AuthFormMode.LOGIN) AuthFormMode.SIGN_UP else AuthFormMode.LOGIN,
                        email = it.email,
                    )
                }
            }

            LoginEvent.Submit -> submitAuth()
        }
    }

    // Always runs on Submit, even with every field blank — a disabled button
    // gives no feedback when tapped, so validation (not button-enablement) is
    // what tells the user what's wrong, same "or" the request asked for.
    private fun submitAuth() {
        val state = _loginState.value
        val email = state.email.trim()
        val password = state.password
        val isSignUp = state.mode == AuthFormMode.SIGN_UP

        val fieldError = validate(email, password, state, isSignUp)
        if (fieldError != null) {
            _loginState.update { it.copy(fieldError = fieldError, hasError = false) }
            return
        }

        _loginState.update { it.copy(isLoading = true, hasError = false, fieldError = null) }
        viewModelScope.launch {
            val result = if (isSignUp) {
                authRepository.signUp(email, password, state.displayName.trim(), state.username.trim())
            } else {
                authRepository.login(email, password)
            }
            when (result) {
                is Result.Success -> {
                    if (isSignUp) uploadPendingAvatar()
                    _loginState.value = LoginFormState()
                }
                is Result.Error -> _loginState.update { it.copy(isLoading = false, hasError = true) }
            }
        }
    }

    private fun validate(email: String, password: String, state: LoginFormState, isSignUp: Boolean): LoginFieldError? {
        val missingRequired = email.isEmpty() || password.isEmpty() ||
            (isSignUp && (state.confirmPassword.isEmpty() || state.displayName.isBlank() || state.username.isBlank()))
        return when {
            missingRequired -> LoginFieldError.MISSING_FIELDS
            !EMAIL_REGEX.matches(email) -> LoginFieldError.INVALID_EMAIL
            isSignUp -> validateSignUpOnly(password, state)
            else -> null
        }
    }

    private fun validateSignUpOnly(password: String, state: LoginFormState): LoginFieldError? = when {
        !PASSWORD_REGEX.matches(password) -> LoginFieldError.WEAK_PASSWORD
        password != state.confirmPassword -> LoginFieldError.PASSWORD_MISMATCH
        !USERNAME_REGEX.matches(state.username.trim()) -> LoginFieldError.INVALID_USERNAME
        else -> null
    }

    // Best-effort: the account itself is already created at this point, so a
    // failed avatar upload isn't worth surfacing as a sign-up error — the
    // user can just set it later from the profile tab.
    private suspend fun uploadPendingAvatar() {
        val avatar = pendingAvatar ?: return
        pendingAvatar = null
        runCatching { profileRepository.updateAvatar(avatar.bytes, avatar.mimeType) }
    }
}
