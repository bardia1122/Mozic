package com.example.mozic.feature.social

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mozic.core.domain.model.NotLoggedInException
import com.example.mozic.core.domain.repository.ChatRepository
import com.example.mozic.core.domain.repository.SocialRepository
import com.example.mozic.feature.social.navigation.UserProfileRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId: String = savedStateHandle.toRoute<UserProfileRoute>().userId

    private val _effects = Channel<SocialActionEffect>(Channel.BUFFERED)
    val effects: Flow<SocialActionEffect> = _effects.receiveAsFlow()

    /** Separate from [effects] — a conversation id to navigate to, not a follow/unfollow outcome. */
    private val _navigateToChat = Channel<String>(Channel.BUFFERED)
    val navigateToChatEffect: Flow<String> = _navigateToChat.receiveAsFlow()

    // Both driven by the repository's own followedIds StateFlow (see
    // NetworkSocialRepository's kdoc) — a follow/unfollow tap here updates
    // this screen live, including an automatic revert if the network call
    // fails, with no extra local state needed.
    val uiState: StateFlow<UserProfileUiState> = combine(
        socialRepository.userById(userId),
        socialRepository.publicPlaylistsOf(userId),
    ) { user, playlists ->
        if (user == null) UserProfileUiState.NotFound else UserProfileUiState.Content(user, playlists)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfileUiState.Loading)

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onFollowToggle(currentlyFollowed: Boolean) {
        viewModelScope.launch {
            try {
                if (currentlyFollowed) {
                    socialRepository.unfollow(userId)
                    _effects.trySend(SocialActionEffect.Unfollowed)
                } else {
                    socialRepository.follow(userId)
                    _effects.trySend(SocialActionEffect.Followed)
                }
            } catch (e: NotLoggedInException) {
                _effects.trySend(SocialActionEffect.LoginRequired)
            } catch (e: Exception) {
                _effects.trySend(SocialActionEffect.ActionFailed)
            }
        }
    }

    /** Resolves/creates the DM the same way I1's song-share friend picker does, then navigates straight to it. */
    fun onMessageClick() {
        viewModelScope.launch {
            val conversationId = chatRepository.conversationWith(userId)
            if (conversationId != null) {
                _navigateToChat.send(conversationId)
            } else {
                _effects.trySend(SocialActionEffect.LoginRequired)
            }
        }
    }
}
