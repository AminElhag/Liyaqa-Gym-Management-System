package com.liyaqa.gym.presentation.service

import com.liyaqa.gym.presentation.dto.platform.PlatformAdmin
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * Platform Admin Authentication Service
 * TODO: Replace with proper implementation when PlatformAdmin domain entity is available
 * This is a simplified stub for now
 */
@Service
class PlatformAdminAuthService {

    private val logger = LoggerFactory.getLogger(PlatformAdminAuthService::class.java)

    // TODO: Replace with database lookup
    private val mockAdmins = mutableMapOf<String, PlatformAdmin>()

    init {
        // Add a default admin for testing
        val defaultAdmin = PlatformAdmin(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            email = "admin@liyaqa.com",
            name = "Platform Administrator",
            passwordHash = hashPassword("admin123"), // TODO: Replace with actual hashing
            isActive = true,
            createdAt = Instant.now()
        )
        mockAdmins[defaultAdmin.email] = defaultAdmin
    }

    /**
     * Authenticate a platform admin by email and password
     */
    fun authenticate(email: String, password: String): PlatformAdmin {
        logger.info("Attempting to authenticate platform admin: $email")

        val admin = mockAdmins[email]
            ?: throw BadCredentialsException("Invalid email or password")

        if (!admin.isActive) {
            throw BadCredentialsException("Admin account is not active")
        }

        val passwordHash = hashPassword(password)
        if (admin.passwordHash != passwordHash) {
            throw BadCredentialsException("Invalid email or password")
        }

        // Update last login
        val updatedAdmin = admin.copy(lastLoginAt = Instant.now())
        mockAdmins[email] = updatedAdmin

        logger.info("Platform admin authenticated successfully: $email")
        return updatedAdmin
    }

    /**
     * Get platform admin by ID
     */
    fun getAdminById(id: UUID): PlatformAdmin? {
        return mockAdmins.values.find { it.id == id }
    }

    /**
     * Get platform admin by email
     */
    fun getAdminByEmail(email: String): PlatformAdmin? {
        return mockAdmins[email]
    }

    /**
     * Simple password hashing
     * TODO: Replace with BCrypt or Argon2
     */
    private fun hashPassword(password: String): String {
        // This is NOT secure - just for stub implementation
        return "hashed_$password"
    }
}
