package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Class
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Class entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface ClassRepository {

    /**
     * Find a class by its unique identifier.
     *
     * @param id The unique identifier of the class
     * @return Optional containing the class if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Class>>

    /**
     * Find all classes for a specific branch.
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of classes
     */
    fun findByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<Class>>

    /**
     * Find all active classes.
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of active classes
     */
    fun findAllActive(page: Int = 0, size: Int = 20): Result<List<Class>>

    /**
     * Save a class (create or update).
     *
     * @param gymClass The class to save
     * @return Result containing the saved class
     */
    fun save(gymClass: Class): Result<Class>
}
