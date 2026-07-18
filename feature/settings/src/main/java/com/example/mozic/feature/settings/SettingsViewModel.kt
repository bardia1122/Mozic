package com.example.mozic.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozic.core.domain.repository.UserPreferencesRepository
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
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = userPreferencesRepository.preferences
        .map { SettingsUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetTheme -> viewModelScope.launch {
                userPreferencesRepository.setTheme(event.theme)
            }
            is SettingsEvent.SetLanguage -> viewModelScope.launch {
                userPreferencesRepository.setLanguage(event.language)
            }
            is SettingsEvent.SetCrossfadeEnabled -> viewModelScope.launch {
                userPreferencesRepository.setCrossfadeEnabled(event.enabled)
            }
            SettingsEvent.Logout -> viewModelScope.launch {
                _effects.send(SettingsEffect.LoggedOut)
            }
        }
    }
}
