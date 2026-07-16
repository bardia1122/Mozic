package com.example.mozic.core.domain.repository

import com.example.mozic.core.domain.model.AppLanguage
import com.example.mozic.core.domain.model.ThemeSetting
import com.example.mozic.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/** Person B's seam and the single source of premium truth — nobody duplicates it. */
interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setTheme(theme: ThemeSetting)

    suspend fun setLanguage(language: AppLanguage)

    suspend fun setFontScale(scale: Float)

    suspend fun setPremium(premium: Boolean)

    suspend fun setAvatarUri(uri: String?)
}
