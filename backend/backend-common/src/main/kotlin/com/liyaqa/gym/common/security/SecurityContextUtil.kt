package com.liyaqa.gym.common.security

import java.util.UUID

/**
 * Utility class for accessing Security Context information.
 * Provides methods to get current user information from the security context.
 */
object SecurityContextUtil {

    /**
     * Get the current authenticated user ID from the security context.
     *
     * @return The UUID of the current authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    fun getCurrentUserId(): UUID {
        // TODO: Implement actual SecurityContextHolder access
        // This is a placeholder implementation
        // In actual implementation:
        // val authentication = SecurityContextHolder.getContext().authentication
        // val userDetails = authentication.principal as GymUserDetails
        // return userDetails.getUserId()
        throw NotImplementedError("Security context integration not yet implemented")
    }

    /**
     * Get the current authenticated user ID, or null if not authenticated.
     *
     * @return The UUID of the current authenticated user, or null
     */
    fun getCurrentUserIdOrNull(): UUID? {
        return try {
            getCurrentUserId()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the current authenticated organization ID from the security context.
     *
     * @return The UUID of the organization the current user belongs to
     * @throws IllegalStateException if no user is authenticated
     */
    fun getCurrentOrganizationId(): UUID {
        // TODO: Implement actual SecurityContextHolder access
        throw NotImplementedError("Security context integration not yet implemented")
    }

    /**
     * Get the current authenticated organization ID, or null if not authenticated.
     *
     * @return The UUID of the organization, or null
     */
    fun getCurrentOrganizationIdOrNull(): UUID? {
        return try {
            getCurrentOrganizationId()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the current authenticated member ID from the security context (if user is a member).
     *
     * @return The UUID of the member, or null if not a member
     */
    fun getCurrentMemberIdOrNull(): UUID? {
        // TODO: Implement actual SecurityContextHolder access
        return null
    }

    /**
     * Get the current authenticated branch ID from the security context.
     *
     * @return The UUID of the branch, or null
     */
    fun getCurrentBranchIdOrNull(): UUID? {
        // TODO: Implement actual SecurityContextHolder access
        return null
    }

    /**
     * Check if a user is currently authenticated.
     *
     * @return true if a user is authenticated, false otherwise
     */
    fun isAuthenticated(): Boolean {
        // TODO: Implement actual SecurityContextHolder access
        return false
    }
}
