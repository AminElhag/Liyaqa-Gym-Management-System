package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Branch
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Branch entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface BranchRepository {

    /**
     * Find a branch by its unique identifier.
     *
     * @param id The unique identifier of the branch
     * @return Optional containing the branch if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Branch>>

    /**
     * Find all branches belonging to a specific organization.
     *
     * @param organizationId The organization identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of branches
     */
    fun findByOrganization(organizationId: UUID, page: Int = 0, size: Int = 20): Result<List<Branch>>

    /**
     * Find all active branches.
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of active branches
     */
    fun findActive(page: Int = 0, size: Int = 20): Result<List<Branch>>

    /**
     * Find branches by name (partial match).
     *
     * @param name The branch name to search for
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of matching branches
     */
    fun findByName(name: String, page: Int = 0, size: Int = 20): Result<List<Branch>>

    /**
     * Save a branch (create or update).
     *
     * @param branch The branch to save
     * @return Result containing the saved branch
     */
    fun save(branch: Branch): Result<Branch>

    /**
     * Check if a branch with the given name exists in an organization.
     *
     * @param organizationId The organization identifier
     * @param name The branch name to check
     * @return Result containing true if exists, false otherwise
     */
    fun existsByOrganizationAndName(organizationId: UUID, name: String): Result<Boolean>

    /**
     * Count branches by organization (tenant).
     *
     * @param organizationId The organization identifier
     * @return Result containing count of branches for the organization
     */
    fun countByOrganization(organizationId: UUID): Result<Long>
}
