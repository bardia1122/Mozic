package com.example.mozic.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.common.result.Result
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.ChatRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<ConversationListUiState> = authRepository.authState
        .flatMapLatest { auth ->
            if (auth is AuthState.LoggedIn) {
                chatRepository.conversations().map { ConversationListUiState.Content(it) as ConversationListUiState }
            } else {
                flowOf(ConversationListUiState.LoggedOut)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationListUiState.Loading)

    private val _loginState = MutableStateFlow(LoginFormState())
    val loginState: StateFlow<LoginFormState> = _loginState.asStateFlow()

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
            is LoginEvent.EmailChanged -> _loginState.update { it.copy(email = event.value, hasError = false) }

            is LoginEvent.PasswordChanged -> _loginState.update { it.copy(password = event.value, hasError = false) }

            LoginEvent.Submit -> submitLogin()
        }
    }

    private fun submitLogin() {
        val email = _loginState.value.email.trim()
        val password = _loginState.value.password
        if (email.isEmpty() || password.isEmpty()) return

        _loginState.update { it.copy(isLoading = true, hasError = false) }
        viewModelScope.launch {
            when (authRepository.login(email, password)) {
                is Result.Success -> _loginState.value = LoginFormState()
                is Result.Error -> _loginState.update { it.copy(isLoading = false, hasError = true) }
            }
        }
    }
}
