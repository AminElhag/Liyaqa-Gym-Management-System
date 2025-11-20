package com.liyaqa.gym.network.models

import kotlinx.serialization.Serializable

/**
 * Authentication request/response models
 */

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long, // seconds
    val tokenType: String = "Bearer",
    val member: MemberResponse
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)
