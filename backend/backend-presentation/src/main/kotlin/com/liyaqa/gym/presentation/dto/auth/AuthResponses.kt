package com.liyaqa.gym.presentation.dto.auth

import java.time.Instant
import java.util.UUID

/**
 * Authentication response DTO
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long, // seconds
    val user: UserInfo
)

/**
 * User information DTO
 */
data class UserInfo(
    val id: UUID,
    val email: String,
    val role: String,
    val organizationId: UUID,
    val branchId: UUID?,
    val memberId: UUID?,
    val staffId: UUID?,
    val isEmailVerified: Boolean,
    val mustChangePassword: Boolean
)

/**
 * Refresh token response DTO
 */
data class RefreshTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long // seconds
)

/**
 * Generic success response
 */
data class MessageResponse(
    val message: String,
    val timestamp: Instant = Instant.now()
)

/**
 * Registration response DTO
 */
data class RegisterResponse(
    val message: String,
    val userId: UUID,
    val memberId: UUID,
    val requiresEmailVerification: Boolean = true
)
