package com.example.mozic.core.network

import com.example.mozic.core.network.dto.AuthTokenResponseDto
import com.example.mozic.core.network.dto.LoginRequestDto
import com.example.mozic.core.network.dto.RefreshRequestDto
import com.example.mozic.core.network.dto.SignUpMetadataDto
import com.example.mozic.core.network.dto.SignUpRequestDto
import com.example.mozic.core.network.dto.SignUpResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

private const val AUTH_TOKEN_PATH = "/auth/v1/token"
private const val AUTH_SIGNUP_PATH = "/auth/v1/signup"

/**
 * A resolved Supabase Auth session — [refreshToken] is a persistence-layer
 * concern (`:core:data`'s `AuthRepositoryImpl` stores it for a background
 * refresh), never surfaced through the domain `AuthRepository`/`AuthState`
 * contract itself.
 */
data class AuthSession(val userId: String, val email: String, val accessToken: String, val refreshToken: String)

/**
 * Thin wrapper over Supabase Auth's password-grant endpoint (C5). Mirrors
 * [SupabaseCatalogApi]'s "DTOs never leave this class" rule — callers only
 * ever see [AuthSession].
 */
@Singleton
class SupabaseAuthApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun login(email: String, password: String): AuthSession =
        tokenRequest("password") { setBody(LoginRequestDto(email, password)) }

    suspend fun refresh(refreshToken: String): AuthSession =
        tokenRequest("refresh_token") { setBody(RefreshRequestDto(refreshToken)) }

    // "Confirm email" is off for this project (C5), so this always returns a
    // session, same as login — the nullable DTO fields are only there to
    // turn an unexpected sessionless response into a clear thrown failure.
    suspend fun signUp(email: String, password: String, displayName: String, username: String): AuthSession {
        val response: SignUpResponseDto = client.post("${BuildConfig.SUPABASE_URL}$AUTH_SIGNUP_PATH") {
            contentType(ContentType.Application.Json)
            setBody(SignUpRequestDto(email, password, SignUpMetadataDto(displayName, username)))
        }.body()
        val user = requireNotNull(response.user) { "signup did not return a session (check Confirm email is off)" }
        val accessToken = requireNotNull(response.accessToken) { "signup response missing access_token" }
        return AuthSession(
            userId = user.id,
            email = user.email,
            accessToken = accessToken,
            refreshToken = response.refreshToken.orEmpty(),
        )
    }

    private suspend fun tokenRequest(
        grantType: String,
        configureBody: HttpRequestBuilder.() -> Unit,
    ): AuthSession {
        val response: AuthTokenResponseDto = client.post("${BuildConfig.SUPABASE_URL}$AUTH_TOKEN_PATH") {
            parameter("grant_type", grantType)
            contentType(ContentType.Application.Json)
            configureBody()
        }.body()
        return AuthSession(
            userId = response.user.id,
            email = response.user.email,
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
        )
    }
}
