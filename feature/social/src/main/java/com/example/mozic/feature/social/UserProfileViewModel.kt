package com.example.mozic.feature.social

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mozic.core.domain.model.NotLoggedInException
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
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId: String = savedStateHandle.toRoute<UserProfileRoute>().userId

    private val _effects = Channel<SocialActionEffect>(Channel.BUFFERED)
    val effects: Flow<SocialActionEffect> = _effects.receiveAsFlow()

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
}
