package com.example.mozic.core.network.repository

import com.example.mozic.core.domain.model.AuthState
import com.example.mozic.core.domain.model.NotLoggedInException
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.domain.repository.AuthRepository
import com.example.mozic.core.domain.repository.ProfileRepository
import com.example.mozic.core.network.BackendProfileApi
import com.example.mozic.core.network.SupabaseSocialApi
import com.example.mozic.core.network.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * The caller's own account (not the social graph — see [SocialRepository]).
 * Lives in `:core:network`, same placement/reasoning as
 * [NetworkSocialRepository]: no Room needed, only REST + [AuthRepository].
 *
 * [myProfile] re-fetches whenever [refreshTrigger] bumps, which
 * [updateAvatar]/[setPremium] do on success — a plain `authState`-keyed fetch
 * alone wouldn't notice its *own* writes, since neither the access token nor
 * the user id change when only the avatar/premium flag does.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NetworkProfileRepository @Inject constructor(
    private val socialApi: SupabaseSocialApi,
    private val backendProfileApi: BackendProfileApi,
    private val authRepository: AuthRepository,
) : ProfileRepository {

    private val refreshTrigger = MutableStateFlow(0)

    override fun myProfile(): Flow<User?> = combine(authRepository.authState, refreshTrigger) { auth, _ -> auth }
        .flatMapLatest { auth ->
            val userId = (auth as? AuthState.LoggedIn)?.userId
            if (userId == null) {
                flowOf(null)
            } else {
                flow { emit(socialApi.profileById(userId)?.toDomain(isFollowed = false)) }
            }
        }

    override suspend fun updateAvatar(imageBytes: ByteArray, mimeType: String): String {
        val auth = requireLoggedIn()
        val url = backendProfileApi.uploadAvatar(auth.accessToken, imageBytes, mimeType)
        refreshTrigger.value += 1
        return url
    }

    override suspend fun removeAvatar() {
        val auth = requireLoggedIn()
        backendProfileApi.removeAvatar(auth.accessToken)
        refreshTrigger.value += 1
    }

    override suspend fun setPremium(isPremium: Boolean) {
        val auth = requireLoggedIn()
        socialApi.updateProfile(auth.accessToken, auth.userId, isPremium = isPremium)
        refreshTrigger.value += 1
    }

    // Same "await past Unknown" reasoning as NetworkSocialRepository.requireLoggedIn.
    private suspend fun requireLoggedIn(): AuthState.LoggedIn =
        authRepository.authState.first { it !is AuthState.Unknown } as? AuthState.LoggedIn
            ?: throw NotLoggedInException()
}
