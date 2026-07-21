package com.example.mozic.core.domain.model

/**
 * Real Supabase Auth identity (C5) — replaces the old `SampleData.CURRENT_USER_ID`
 * fake "me". [LoggedIn.accessToken] is only ever read by `:core:network`'s WS
 * client and REST calls; nothing above `:core:data` needs it.
 *
 * [Unknown] is the transient value while a persisted session (if any) is
 * still being restored from disk on cold start — added in C6 after a real
 * bug: any login-gated action taken in the first moments after launch could
 * read the old two-state model's default of [LoggedOut] and wrongly report
 * "not logged in" even though a valid session was about to be restored.
 * Consumers that gate on `is AuthState.LoggedIn` already treat [Unknown] the
 * same as [LoggedOut] (safe: don't act yet); anything that must tell "not
 * logged in" apart from "not sure yet" — like C6's follow/unfollow — should
 * await the first non-[Unknown] value instead of reading `.value` directly.
 */
sealed interface AuthState {
    data object Unknown : AuthState

    data object LoggedOut : AuthState

    data class LoggedIn(val userId: String, val email: String, val accessToken: String) : AuthState
}
