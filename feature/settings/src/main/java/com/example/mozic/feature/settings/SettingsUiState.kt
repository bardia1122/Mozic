package com.example.mozic.feature.settings

import com.example.mozic.core.domain.model.AppLanguage
import com.example.mozic.core.domain.model.ThemeSetting
import com.example.mozic.core.domain.model.UserPreferences

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Ready(val preferences: UserPreferences) : SettingsUiState
}

sealed interface SettingsEvent {
    data class SetTheme(val theme: ThemeSetting) : SettingsEvent

    data class SetLanguage(val language: AppLanguage) : SettingsEvent

    data object Logout : SettingsEvent
}

sealed interface SettingsEffect {
    data object LoggedOut : SettingsEffect
}
