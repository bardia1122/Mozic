package com.example.mozic.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mozic.core.domain.model.AppLanguage
import com.example.mozic.core.domain.model.ThemeSetting
import com.example.mozic.core.domain.model.UserPreferences
import com.example.mozic.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object Keys {
    val THEME = stringPreferencesKey("theme")
    val LANGUAGE = stringPreferencesKey("language")
    val FONT_SCALE = floatPreferencesKey("font_scale")
    val IS_PREMIUM = booleanPreferencesKey("is_premium")
    val AVATAR_URI = stringPreferencesKey("avatar_uri")
}

/**
 * Replaces `FakeUserPreferencesRepository`. DataStore's `data`/`edit` are
 * already non-blocking and internally sequenced onto their own dispatcher, so
 * (unlike [com.example.mozic.core.data.worker.DownloadWorker]'s genuinely
 * blocking OkHttp stream) there's no `@IoDispatcher` to inject here.
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            theme = prefs[Keys.THEME]?.let(ThemeSetting::valueOf) ?: ThemeSetting.SYSTEM,
            language = prefs[Keys.LANGUAGE]?.let(AppLanguage::valueOf) ?: AppLanguage.EN,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1f,
            isPremium = prefs[Keys.IS_PREMIUM] ?: false,
            avatarUri = prefs[Keys.AVATAR_URI],
        )
    }

    override suspend fun setTheme(theme: ThemeSetting) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    override suspend fun setFontScale(scale: Float) {
        dataStore.edit { it[Keys.FONT_SCALE] = scale }
    }

    override suspend fun setPremium(premium: Boolean) {
        dataStore.edit { it[Keys.IS_PREMIUM] = premium }
    }

    override suspend fun setAvatarUri(uri: String?) {
        dataStore.edit {
            if (uri != null) it[Keys.AVATAR_URI] = uri else it.remove(Keys.AVATAR_URI)
        }
    }
}
