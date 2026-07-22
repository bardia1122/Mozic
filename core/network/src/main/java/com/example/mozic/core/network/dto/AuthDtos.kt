package com.example.mozic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val email: String, val password: String)

/**
 * `POST /auth/v1/signup` request — [data] lands in `auth.users.raw_user_meta_data`,
 * which `handle_new_user()` (`backend/supabase/schema.sql`) copies into the new
 * `profiles` row it creates, so `display_name`/`username` arrive pre-populated
 * instead of both falling back to the email's local part.
 */
@Serializable
data class SignUpRequestDto(val email: String, val password: String, val data: SignUpMetadataDto)

@Serializable
data class SignUpMetadataDto(
    @SerialName("display_name") val displayName: String,
    val username: String,
)

@Serializable
data class RefreshRequestDto(@SerialName("refresh_token") val refreshToken: String)

/** `POST /auth/v1/token` response (both `grant_type=password` and `=refresh_token`). */
@Serializable
data class AuthTokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    val user: AuthUserDto,
)

@Serializable
data class AuthUserDto(val id: String, val email: String)

/**
 * `POST /auth/v1/signup` response. The project has "Confirm email" turned
 * off, so this always carries a session, identical in shape to
 * [AuthTokenResponseDto] — [accessToken]/[user] are still nullable so a
 * malformed/unexpected response surfaces as a clear parse failure ([SupabaseAuthApi.signUp]
 * throws) rather than a silent wrong state, not because a sessionless shape is expected.
 */
@Serializable
data class SignUpResponseDto(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: AuthUserDto? = null,
)
