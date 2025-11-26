package com.liyaqa.gym.presentation.controller.platform

import com.liyaqa.gym.presentation.dto.platform.LoginResponse
import com.liyaqa.gym.presentation.dto.platform.PlatformAdmin
import com.liyaqa.gym.presentation.dto.platform.PlatformAdminLoginRequest
import com.liyaqa.gym.presentation.dto.platform.PlatformAdminResponse
import com.liyaqa.gym.presentation.dto.platform.toResponse
import com.liyaqa.gym.presentation.security.JwtTokenProvider
import com.liyaqa.gym.presentation.service.PlatformAdminAuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * Platform Admin Authentication Controller
 * Handles platform administrator authentication and account management
 */
@RestController
@RequestMapping("/api/v1/platform/auth")
@Tag(name = "Platform Admin Authentication", description = "Platform administrator authentication endpoints")
class PlatformAuthController(
    private val authService: PlatformAdminAuthService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(PlatformAuthController::class.java)

    /**
     * Platform admin login
     */
    @PostMapping("/login")
    @Operation(summary = "Platform admin login", description = "Authenticate platform administrator")
    fun login(@RequestBody @Valid request: PlatformAdminLoginRequest): ResponseEntity<LoginResponse> {
        logger.info("Platform admin login attempt: ${request.email}")

        val admin = authService.authenticate(request.email, request.password)
        val token = jwtTokenProvider.generatePlatformAdminToken(admin)

        val response = LoginResponse(
            token = token,
            user = admin.toResponse(),
            role = "PLATFORM_ADMIN"
        )

        logger.info("Platform admin logged in successfully: ${request.email}")
        return ResponseEntity.ok(response)
    }

    /**
     * Platform admin logout
     */
    @PostMapping("/logout")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Platform admin logout", description = "Logout platform administrator")
    fun logout(): ResponseEntity<Unit> {
        // TODO: Implement token invalidation when token blacklist is available
        logger.info("Platform admin logout")
        return ResponseEntity.ok().build()
    }

    /**
     * Get current platform admin details
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get current admin", description = "Get current authenticated platform administrator details")
    fun getCurrentAdmin(@AuthenticationPrincipal adminId: String?): ResponseEntity<PlatformAdminResponse> {
        logger.info("Get current platform admin details")

        // TODO: Extract admin from security context when available
        // For now, return a mock admin
        val mockAdmin = PlatformAdmin(
            id = java.util.UUID.randomUUID(),
            email = "admin@liyaqa.com",
            name = "Platform Administrator",
            passwordHash = "hashed",
            isActive = true,
            createdAt = java.time.Instant.now()
        )

        return ResponseEntity.ok(mockAdmin.toResponse())
    }
}
