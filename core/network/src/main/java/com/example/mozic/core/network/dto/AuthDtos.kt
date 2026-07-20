package com.example.mozic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val email: String, val password: String)

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
