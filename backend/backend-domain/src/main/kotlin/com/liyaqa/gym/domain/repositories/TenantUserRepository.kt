package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.TenantUser
import com.liyaqa.gym.domain.entities.tenant.TenantUserRole
import com.liyaqa.gym.domain.entities.tenant.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for TenantUser entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface TenantUserRepository {

    /**
     * Find a tenant user by their unique identifier.
     *
     * @param id The unique identifier of the tenant user
     * @return Optional containing the tenant user if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<TenantUser>>

    /**
     * Find a tenant user by email.
     *
     * @param email The email address to search for
     * @return Optional containing the tenant user if found, empty otherwise
     */
    fun findByEmail(email: String): Result<Optional<TenantUser>>

    /**
     * Find all users belonging to a tenant.
     *
     * @param tenantId The tenant identifier
     * @param pageable Pagination and sorting parameters
     * @return Page of tenant users
     */
    fun findByTenant(tenantId: UUID, pageable: Pageable): Result<Page<TenantUser>>

    /**
     * Find tenant users by role.
     *
     * @param tenantId The tenant identifier
     * @param role The role to filter by
     * @param pageable Pagination and sorting parameters
     * @return Page of tenant users
     */
    fun findByTenantAndRole(
        tenantId: UUID,
        role: TenantUserRole,
        pageable: Pageable
    ): Result<Page<TenantUser>>

    /**
     * Find the owner of a tenant.
     *
     * @param tenantId The tenant identifier
     * @return Optional containing the owner if found
     */
    fun findOwnerByTenant(tenantId: UUID): Result<Optional<TenantUser>>

    /**
     * Save a tenant user (create or update).
     *
     * @param tenantUser The tenant user to save
     * @return The saved tenant user
     */
    fun save(tenantUser: TenantUser): Result<TenantUser>

    /**
     * Check if a tenant user with the given email exists.
     *
     * @param email The email to check
     * @return true if exists, false otherwise
     */
    fun existsByEmail(email: String): Result<Boolean>

    /**
     * Check if a tenant user with the given email exists in a specific tenant.
     *
     * @param tenantId The tenant identifier
     * @param email The email to check
     * @return true if exists, false otherwise
     */
    fun existsByTenantAndEmail(tenantId: UUID, email: String): Result<Boolean>

    /**
     * Count users in a tenant.
     *
     * @param tenantId The tenant identifier
     * @return Count of users
     */
    fun countByTenant(tenantId: UUID): Result<Long>

    /**
     * Soft delete a tenant user.
     *
     * @param tenantUserId The tenant user ID to delete
     * @return Result indicating success
     */
    fun softDelete(tenantUserId: UUID): Result<Unit>
}
