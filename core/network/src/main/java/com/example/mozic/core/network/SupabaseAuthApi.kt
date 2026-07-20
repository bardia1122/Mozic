package com.example.mozic.core.network

import com.example.mozic.core.network.dto.AuthTokenResponseDto
import com.example.mozic.core.network.dto.LoginRequestDto
import com.example.mozic.core.network.dto.RefreshRequestDto
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
