package com.liyaqa.gym.presentation.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Login request DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String,

    val organizationId: UUID? = null // Optional: for multi-tenant login
)

/**
 * Register request DTO (public member registration)
 */
data class RegisterRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    val nameArabic: String? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    @field:NotBlank(message = "Phone is required")
    val phone: String,

    val nationalId: String? = null,

    @field:NotBlank(message = "Gender is required")
    val gender: String, // MALE or FEMALE

    val dateOfBirth: String? = null, // ISO format: yyyy-MM-dd

    @field:NotBlank(message = "Branch ID is required")
    val branchId: String // UUID as string
)

/**
 * Refresh token request DTO
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

/**
 * Forgot password request DTO
 */
data class ForgotPasswordRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    val organizationId: UUID? = null
)

/**
 * Reset password request DTO
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "Token is required")
    val token: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)

/**
 * Change password request DTO
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)
