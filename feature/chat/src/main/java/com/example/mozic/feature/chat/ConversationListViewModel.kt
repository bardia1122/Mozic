package com.example.mozic.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    val uiState: StateFlow<ConversationListUiState> = chatRepository.conversations()
        .map { conversations -> ConversationListUiState.Content(conversations) as ConversationListUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationListUiState.Loading)

    fun onEvent(event: ConversationListEvent) {
        when (event) {
            is ConversationListEvent.DeleteConversation -> viewModelScope.launch {
                chatRepository.deleteConversation(event.conversationId)
            }

            is ConversationListEvent.MarkUnread -> viewModelScope.launch {
                chatRepository.markConversationUnread(event.conversationId)
            }
        }
    }
}
