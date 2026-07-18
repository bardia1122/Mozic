package com.example.mozic.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Fake 2-second "processing" step before the mock purchase resolves — a
 * demo-friendly stand-in for a real billing flow.
 */
private const val MOCK_PURCHASE_DELAY_MS = 2_000L

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val isPurchasing = MutableStateFlow(false)

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<ProfileUiState> = combine(
        userPreferencesRepository.preferences,
        isPurchasing,
    ) { prefs, purchasing ->
        ProfileUiState.Content(
            avatarUri = prefs.avatarUri,
            isPremium = prefs.isPremium,
            isPurchasing = purchasing,
        ) as ProfileUiState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState.Loading)

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SetAvatar -> viewModelScope.launch {
                userPreferencesRepository.setAvatarUri(event.uri)
            }

            ProfileEvent.PurchasePremium -> viewModelScope.launch {
                isPurchasing.value = true
                delay(MOCK_PURCHASE_DELAY_MS)
                userPreferencesRepository.setPremium(true)
                isPurchasing.value = false
                _effects.send(ProfileEffect.PurchaseCompleted)
            }
        }
    }
}
