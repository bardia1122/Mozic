package com.example.mozic.core.domain.model

/**
 * Real Supabase Auth identity (C5) — replaces the old `SampleData.CURRENT_USER_ID`
 * fake "me". [LoggedIn.accessToken] is only ever read by `:core:network`'s WS
 * client and REST calls; nothing above `:core:data` needs it.
 */
sealed interface AuthState {
    data object LoggedOut : AuthState

    data class LoggedIn(val userId: String, val email: String, val accessToken: String) : AuthState
}
