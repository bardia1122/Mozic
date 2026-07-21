package com.example.mozic.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.ProfileRepository
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Fake 2-second "processing" step before the mock purchase resolves — a
 * demo-friendly stand-in for a real billing flow.
 */
private const val MOCK_PURCHASE_DELAY_MS = 2_000L

/**
 * Enhanced, not gated: every other tab (including this one) stays usable
 * unauthenticated per C5's own scoping — logged-out shows exactly what this
 * screen showed before login existed (local DataStore avatar/premium, no
 * name to show). Logged-in swaps in the real `profiles` row via
 * [ProfileRepository.myProfile] for name/avatar/premium, and both write paths
 * ([ProfileEvent.UploadAvatar]/[ProfileEvent.PurchasePremium]) go to the
 * server instead of local prefs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val isPurchasing = MutableStateFlow(false)

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    private val myProfile = authRepository.authState.flatMapLatest { auth ->
        if (auth is AuthState.LoggedIn) profileRepository.myProfile() else flowOf(null)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        myProfile,
        userPreferencesRepository.preferences,
        isPurchasing,
    ) { profile, prefs, purchasing ->
        ProfileUiState.Content(
            displayName = profile?.displayName,
            avatarUri = profile?.avatarUrl ?: prefs.avatarUri,
            isPremium = profile?.isPremium ?: prefs.isPremium,
            isLoggedIn = profile != null,
            isPurchasing = purchasing,
        ) as ProfileUiState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState.Loading)

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SetLocalAvatar -> viewModelScope.launch {
                userPreferencesRepository.setAvatarUri(event.uri)
            }

            is ProfileEvent.UploadAvatar -> viewModelScope.launch {
                try {
                    profileRepository.updateAvatar(event.bytes, event.mimeType)
                } catch (e: Exception) {
                    _effects.send(ProfileEffect.AvatarUpdateFailed)
                }
            }

            ProfileEvent.RemoveAvatar -> viewModelScope.launch {
                if (authRepository.authState.value is AuthState.LoggedIn) {
                    try {
                        profileRepository.removeAvatar()
                    } catch (e: Exception) {
                        _effects.send(ProfileEffect.AvatarUpdateFailed)
                    }
                } else {
                    userPreferencesRepository.setAvatarUri(null)
                }
            }

            ProfileEvent.PurchasePremium -> viewModelScope.launch {
                isPurchasing.value = true
                delay(MOCK_PURCHASE_DELAY_MS)
                val isLoggedIn = authRepository.authState.value is AuthState.LoggedIn
                if (isLoggedIn) {
                    runCatching { profileRepository.setPremium(true) }
                } else {
                    userPreferencesRepository.setPremium(true)
                }
                isPurchasing.value = false
                _effects.send(ProfileEffect.PurchaseCompleted)
            }
        }
    }
}
