package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.presentation.dto.auth.*
import com.liyaqa.gym.presentation.service.AuthService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Authentication Controller
 * Handles user authentication, registration, and token management
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * Login endpoint
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        logger.info("Login request received for email: ${request.email}")

        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Login failed for email: ${request.email}", e)
            throw e
        }
    }

    /**
     * Register endpoint (public member registration)
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        logger.info("Registration request received for email: ${request.email}")

        return try {
            val response = authService.register(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            logger.error("Registration failed for email: ${request.email}", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during registration for email: ${request.email}", e)
            throw e
        }
    }

    /**
     * Refresh token endpoint
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        logger.debug("Token refresh request received")

        return try {
            val response = authService.refreshToken(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.error("Token refresh failed", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during token refresh", e)
            throw e
        }
    }

    /**
     * Logout endpoint
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    fun logout(): ResponseEntity<MessageResponse> {
        logger.info("Logout request received")

        return try {
            val response = authService.logout()
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Logout failed", e)
            throw e
        }
    }

    /**
     * Forgot password endpoint
     * POST /api/v1/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        logger.info("Forgot password request received for email: ${request.email}")

        return try {
            val response = authService.forgotPassword(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Forgot password request failed", e)
            // Don't expose error details for security
            ResponseEntity.ok(
                MessageResponse(message = "If the email exists, a password reset link has been sent")
            )
        }
    }

    /**
     * Reset password endpoint
     * POST /api/v1/auth/reset-password
     */
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        logger.info("Password reset request received")

        return try {
            val response = authService.resetPassword(request)
            ResponseEntity.ok(response)
        } catch (e: NotImplementedError) {
            logger.warn("Reset password endpoint called but not implemented yet")
            ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(MessageResponse(message = "Password reset functionality is not yet implemented"))
        } catch (e: Exception) {
            logger.error("Password reset failed", e)
            throw e
        }
    }

    /**
     * Change password endpoint (requires authentication)
     * POST /api/v1/auth/change-password
     */
    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest): ResponseEntity<MessageResponse> {
        logger.info("Password change request received")

        return try {
            val response = authService.changePassword(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.error("Password change failed", e)
            throw e
        } catch (e: IllegalStateException) {
            logger.error("Password change failed - unauthorized", e)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse(message = e.message ?: "Unauthorized"))
        } catch (e: Exception) {
            logger.error("Unexpected error during password change", e)
            throw e
        }
    }

    /**
     * Exception handler for IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                error = "Bad Request",
                message = e.message ?: "Invalid request"
            )
        )
    }

    /**
     * Exception handler for general exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception in AuthController", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                error = "Internal Server Error",
                message = "An unexpected error occurred"
            )
        )
    }
}

/**
 * Error response DTO
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: java.time.Instant = java.time.Instant.now()
)
