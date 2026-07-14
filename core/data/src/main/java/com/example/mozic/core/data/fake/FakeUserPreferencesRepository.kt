package com.example.mozic.core.data.fake

import com.example.mozic.core.domain.model.AppLanguage
import com.example.mozic.core.domain.model.ThemeSetting
import com.example.mozic.core.domain.model.UserPreferences
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakeUserPreferencesRepository @Inject constructor() : UserPreferencesRepository {
    private val prefs = MutableStateFlow(
        UserPreferences(
            theme = ThemeSetting.SYSTEM,
            language = AppLanguage.EN,
            fontScale = 1f,
            isPremium = false,
        ),
    )

    override val preferences: Flow<UserPreferences> = prefs.asStateFlow()

    override suspend fun setTheme(theme: ThemeSetting) {
        prefs.update { it.copy(theme = theme) }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        prefs.update { it.copy(language = language) }
    }

    override suspend fun setFontScale(scale: Float) {
        prefs.update { it.copy(fontScale = scale) }
    }

    override suspend fun setPremium(premium: Boolean) {
        prefs.update { it.copy(isPremium = premium) }
    }
}
