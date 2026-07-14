package com.example.mozic.core.domain.model

/** Single source of truth for user settings, persisted in DataStore (Person B). */
data class UserPreferences(
    val theme: ThemeSetting,
    val language: AppLanguage,
    val fontScale: Float,
    val isPremium: Boolean,
)

enum class ThemeSetting { LIGHT, DARK, SYSTEM }

enum class AppLanguage { EN, FA }
