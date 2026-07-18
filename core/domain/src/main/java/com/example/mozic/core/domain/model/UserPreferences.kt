package com.example.mozic.core.domain.model

/** Single source of truth for user settings, persisted in DataStore (Person B). */
data class UserPreferences(
    val theme: ThemeSetting,
    val language: AppLanguage,
    val fontScale: Float,
    val isPremium: Boolean,
    /** Photo-picker `content://` Uri, persisted as a string. `null` = default avatar. */
    val avatarUri: String? = null,
    /** Volume-ramp crossfade between tracks (A6) — on by default, toggleable in Settings. */
    val crossfadeEnabled: Boolean = true,
)

enum class ThemeSetting { LIGHT, DARK, SYSTEM }

enum class AppLanguage { EN, FA }
