package com.example.mozic.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.player.PlayerController
import com.example.mozic.core.domain.repository.ChatRepository
import com.example.mozic.feature.chat.navigation.ChatThreadRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatThreadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    private val conversationId: String = savedStateHandle.toRoute<ChatThreadRoute>().conversationId

    private val inputText = MutableStateFlow("")

    val messages: Flow<PagingData<Message>> =
        chatRepository.messages(conversationId).cachedIn(viewModelScope)

    val uiState: StateFlow<ChatThreadUiState> = combine(
        chatRepository.conversations(),
        chatRepository.peerIsTyping(conversationId),
        chatRepository.connectionState,
        inputText,
    ) { conversations, isPeerTyping, connectionState, text ->
        ChatThreadUiState(
            peer = conversations.find { it.id == conversationId }?.peer,
            connectionState = connectionState,
            isPeerTyping = isPeerTyping,
            inputText = text,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatThreadUiState())

    init {
        viewModelScope.launch { chatRepository.markConversationRead(conversationId) }
    }

    fun onEvent(event: ChatThreadEvent) {
        when (event) {
            is ChatThreadEvent.InputChanged -> {
                inputText.value = event.text
                viewModelScope.launch { chatRepository.setTyping(conversationId, event.text.isNotBlank()) }
            }

            ChatThreadEvent.Send -> {
                val text = inputText.value.trim()
                if (text.isEmpty()) return
                inputText.value = ""
                viewModelScope.launch {
                    chatRepository.setTyping(conversationId, false)
                    chatRepository.sendText(conversationId, text)
                }
            }

            is ChatThreadEvent.SongShareClick -> playerController.play(event.songId)
        }
    }
}
