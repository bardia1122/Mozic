package com.example.mozic.core.domain.repository

import com.example.mozic.core.common.result.Result
import com.example.mozic.core.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

/**
 * C5's real identity, gating chat (and, later, C6's social graph). There is no
 * app-wide login wall — every other tab works unauthenticated exactly as
 * before; only the chat feature reads [authState] and shows a login screen
 * when it's [AuthState.LoggedOut].
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun logout()
}
