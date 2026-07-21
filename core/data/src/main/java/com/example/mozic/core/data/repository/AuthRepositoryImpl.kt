package com.example.mozic.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mozic.core.common.dispatcher.IoDispatcher
import com.example.mozic.core.common.result.Result
import com.example.mozic.core.data.di.AuthSessionDataStore
import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.network.AuthSession
import com.example.mozic.core.network.SupabaseAuthApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private object AuthKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USER_ID = stringPreferencesKey("user_id")
    val EMAIL = stringPreferencesKey("email")
}

/**
 * C5's real identity, replacing the old `SampleData.CURRENT_USER_ID` fake
 * "me". Persists the Supabase Auth session in its own DataStore (separate
 * from device-local [com.example.mozic.core.domain.model.UserPreferences] —
 * this is a real account, not a device setting) so a login survives a
 * process kill. There's no proactive access-token refresh timer — a token
 * that expires mid-session just fails the WS handshake until the next
 * explicit login; a deliberate cut, same tier as C5's own "reconnect
 * sophistication" cut-list allowance (PLAN_PERSON_C.md).
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @AuthSessionDataStore private val dataStore: DataStore<Preferences>,
    private val api: SupabaseAuthApi,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        scope.launch { restoreSession() }
    }

    // Login is this repository's one-shot Result-wrapped boundary (same
    // reasoning as OffsetPagingSource.load/NetworkSongRepository.song) — any
    // network/auth failure needs to land as Result.Error, never propagate.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun login(email: String, password: String): Result<Unit> = try {
        persist(api.login(email, password))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun logout() {
        dataStore.edit { it.clear() }
        _authState.value = AuthState.LoggedOut
    }

    private suspend fun restoreSession() {
        val prefs = dataStore.data.first()
        val accessToken = prefs[AuthKeys.ACCESS_TOKEN]
        val userId = prefs[AuthKeys.USER_ID]
        val email = prefs[AuthKeys.EMAIL]
        val refreshToken = prefs[AuthKeys.REFRESH_TOKEN]
        if (accessToken == null || userId == null || email == null) {
            _authState.value = AuthState.LoggedOut
            return
        }

        // Show the last-known session immediately (offline-friendly), then
        // best-effort refresh it in the background for a token less likely
        // to already be stale.
        _authState.value = AuthState.LoggedIn(userId, email, accessToken)
        if (refreshToken != null) {
            runCatching { persist(api.refresh(refreshToken)) }
        }
    }

    private suspend fun persist(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[AuthKeys.ACCESS_TOKEN] = session.accessToken
            prefs[AuthKeys.REFRESH_TOKEN] = session.refreshToken
            prefs[AuthKeys.USER_ID] = session.userId
            prefs[AuthKeys.EMAIL] = session.email
        }
        _authState.value = AuthState.LoggedIn(session.userId, session.email, session.accessToken)
    }
}
