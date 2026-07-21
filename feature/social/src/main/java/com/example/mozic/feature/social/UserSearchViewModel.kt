package com.example.mozic.feature.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mozic.core.domain.model.NotLoggedInException
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 400L

/**
 * Same debounce/flatMapLatest shape as `:feature:search`'s `SearchViewModel`
 * — B's own operator chain pattern, reused per PLAN_PERSON_C.md's C6 note.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserSearchViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
) : ViewModel() {

    private val queryState = MutableStateFlow("")

    private val _effects = Channel<SocialActionEffect>(Channel.BUFFERED)
    val effects: Flow<SocialActionEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<UserSearchUiState> = queryState
        .map { UserSearchUiState(query = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSearchUiState())

    val results: Flow<PagingData<User>> = queryState
        .map { it.trim() }
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .filter { it.isNotEmpty() }
        .flatMapLatest { query -> socialRepository.searchUsers(query) }
        .cachedIn(viewModelScope)

    // Each paged User's own isFollowed is a load-time snapshot that never
    // updates in place (see UserSearchPagingSource's kdoc) — the screen reads
    // this live set instead to decide each row's actual current state.
    val followedIds: StateFlow<Set<String>> = socialRepository.followedUserIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun onEvent(event: UserSearchEvent) {
        when (event) {
            is UserSearchEvent.QueryChanged -> queryState.value = event.query
            is UserSearchEvent.FollowToggle -> toggleFollow(event.userId, event.currentlyFollowed)
        }
    }

    // Distinguishes "not logged in" (SocialRepository's own dedicated
    // NotLoggedInException, see NetworkSocialRepository.requireLoggedIn) from
    // any other failure (network, RLS) so the UI can show a more useful
    // message than a generic retry prompt for the login case — both branches
    // reduce to "which snackbar", nothing more specific to do with either.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun toggleFollow(userId: String, currentlyFollowed: Boolean) {
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
