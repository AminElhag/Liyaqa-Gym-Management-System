package com.liyaqa.gym.presentation.security

import com.liyaqa.gym.domain.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Custom UserDetailsService implementation for loading user-specific data
 */
@Service
class GymUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val logger = LoggerFactory.getLogger(GymUserDetailsService::class.java)

    /**
     * Load user by username (email)
     */
    override fun loadUserByUsername(username: String): UserDetails {
        logger.debug("Loading user by username: $username")

        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found with email: $username")

        if (!user.isActive) {
            throw UsernameNotFoundException("User account is not active: $username")
        }

        return GymUserDetails.create(user)
    }

    /**
     * Load user by ID
     */
    fun loadUserById(userId: UUID): GymUserDetails {
        logger.debug("Loading user by ID: $userId")

        val user = userRepository.findById(userId)
            ?: throw UsernameNotFoundException("User not found with ID: $userId")

        if (!user.isActive) {
            throw UsernameNotFoundException("User account is not active with ID: $userId")
        }

        return GymUserDetails.create(user)
    }

    /**
     * Load user by email and organization ID (for multi-tenant scenarios)
     */
    fun loadUserByEmailAndOrganization(email: String, organizationId: UUID): GymUserDetails {
        logger.debug("Loading user by email and organization: $email, $organizationId")

        val user = userRepository.findByEmailAndOrganizationId(email, organizationId)
            ?: throw UsernameNotFoundException("User not found with email $email in organization $organizationId")

        if (!user.isActive) {
            throw UsernameNotFoundException("User account is not active: $email")
        }

        return GymUserDetails.create(user)
    }
}
