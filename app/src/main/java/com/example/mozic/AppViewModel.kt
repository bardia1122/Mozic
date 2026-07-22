package com.example.mozic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.model.UserPreferences
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.ProfileRepository
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AppUiState {
    data object Loading : AppUiState

    data class Ready(val preferences: UserPreferences) : AppUiState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    profileRepository: ProfileRepository,
    authRepository: AuthRepository,
) : ViewModel() {
    val uiState: StateFlow<AppUiState> = userPreferencesRepository.preferences
        .map { AppUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState.Loading)

    /** Backs [com.example.mozic.ui.MozicTopBar]'s leading avatar — `null` while logged out/unset. */
    val avatarUrl: StateFlow<String?> = profileRepository.myProfile()
        .map { it?.avatarUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Gates [com.example.mozic.ui.MozicTopBar]'s avatar tap — logged-out taps go to sign-in instead of Profile. */
    val isLoggedIn: StateFlow<Boolean> = authRepository.authState
        .map { it is AuthState.LoggedIn }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
