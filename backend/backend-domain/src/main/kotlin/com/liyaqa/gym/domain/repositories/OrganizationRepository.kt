package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Organization
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Organization entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface OrganizationRepository {

    /**
     * Find an organization by its unique identifier.
     *
     * @param id The unique identifier of the organization
     * @return Optional containing the organization if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Organization>>

    /**
     * Find an organization by its name.
     *
     * @param name The organization name
     * @return Result containing Optional of organization if found
     */
    fun findByName(name: String): Result<Optional<Organization>>

    /**
     * Find all organizations.
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of organizations
     */
    fun findAll(page: Int = 0, size: Int = 20): Result<List<Organization>>

    /**
     * Save an organization (create or update).
     *
     * @param organization The organization to save
     * @return Result containing the saved organization
     */
    fun save(organization: Organization): Result<Organization>

    /**
     * Check if an organization with the given name already exists.
     *
     * @param name The organization name to check
     * @return Result containing true if exists, false otherwise
     */
    fun existsByName(name: String): Result<Boolean>
}
