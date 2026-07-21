package com.example.mozic.feature.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.NotLoggedInException
import com.example.mozic.core.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FollowingListViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
) : ViewModel() {

    private val _effects = Channel<SocialActionEffect>(Channel.BUFFERED)
    val effects: Flow<SocialActionEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<FollowingListUiState> = socialRepository.following()
        .map { users -> FollowingListUiState.Content(users) as FollowingListUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FollowingListUiState.Loading)

    // Unfollow is the only toggle direction reachable from this screen — every
    // row here is, by construction, someone already followed.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onUnfollowClick(userId: String) {
        viewModelScope.launch {
            try {
                socialRepository.unfollow(userId)
                _effects.trySend(SocialActionEffect.Unfollowed)
            } catch (e: NotLoggedInException) {
                _effects.trySend(SocialActionEffect.LoginRequired)
            } catch (e: Exception) {
                _effects.trySend(SocialActionEffect.ActionFailed)
            }
        }
    }
}
