package com.liyaqa.gym.presentation.dto.platform

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

/**
 * Platform admin login request
 */
data class PlatformAdminLoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * Platform admin login response
 */
data class LoginResponse(
    val token: String,
    val user: PlatformAdminResponse,
    val role: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 86400 // 24 hours in seconds
)

/**
 * Platform admin response DTO
 */
data class PlatformAdminResponse(
    val id: UUID,
    val email: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val lastLoginAt: Instant?
)

/**
 * Simplified Platform Admin model for authentication
 * TODO: Replace with actual PlatformAdmin entity when available
 */
data class PlatformAdmin(
    val id: UUID,
    val email: String,
    val name: String,
    val passwordHash: String,
    val role: PlatformAdminRole = PlatformAdminRole.PLATFORM_ADMIN,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val lastLoginAt: Instant? = null
)

/**
 * Platform admin roles
 */
enum class PlatformAdminRole {
    PLATFORM_ADMIN,
    SUPER_ADMIN
}

/**
 * Extension function to convert PlatformAdmin to response DTO
 */
fun PlatformAdmin.toResponse(): PlatformAdminResponse {
    return PlatformAdminResponse(
        id = this.id,
        email = this.email,
        name = this.name,
        role = this.role.name,
        isActive = this.isActive,
        createdAt = this.createdAt,
        lastLoginAt = this.lastLoginAt
    )
}
