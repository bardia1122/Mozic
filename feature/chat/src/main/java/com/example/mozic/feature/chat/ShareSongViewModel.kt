package com.example.mozic.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.ChatRepository
import com.example.mozic.core.domain.repository.SocialRepository
import com.example.mozic.feature.chat.navigation.ShareSongRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ShareSongViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    socialRepository: SocialRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val songId: String = savedStateHandle.toRoute<ShareSongRoute>().songId

    private val sendingPeerId = MutableStateFlow<String?>(null)

    private val _effects = Channel<ShareSongEffect>(Channel.BUFFERED)
    val effects: Flow<ShareSongEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<ShareSongUiState> = combine(
        authRepository.authState,
        socialRepository.following(),
        sendingPeerId,
    ) { auth, friends, sending ->
        when (auth) {
            is AuthState.LoggedIn -> ShareSongUiState.Content(friends, sending)
            AuthState.LoggedOut -> ShareSongUiState.LoggedOut
            AuthState.Unknown -> ShareSongUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShareSongUiState.Loading)

    fun onFriendClick(peerId: String) {
        if (sendingPeerId.value != null) return
        sendingPeerId.value = peerId
        viewModelScope.launch {
            val conversationId = chatRepository.conversationWith(peerId)
            if (conversationId != null) {
                chatRepository.sendSongShare(conversationId, songId)
                _effects.send(ShareSongEffect.Sent)
            }
            sendingPeerId.value = null
        }
    }
}
