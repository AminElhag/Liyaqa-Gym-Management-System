package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.gym.domain.entities.Trainer
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Trainer entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface TrainerRepository {

    /**
     * Find a trainer by their unique identifier.
     *
     * @param id The unique identifier of the trainer
     * @return Optional containing the trainer if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Trainer>>

    /**
     * Find all trainers belonging to a specific branch.
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of trainers
     */
    fun findByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<Trainer>>

    /**
     * Find available trainers for a specific time range.
     * Available means trainers who are active and not scheduled during the given time.
     *
     * @param branchId The branch identifier
     * @param startTime The start time to check availability
     * @param endTime The end time to check availability
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of available trainers
     */
    fun findAvailable(
        branchId: UUID,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Trainer>>

    /**
     * Find trainers with a specific specialization.
     *
     * @param specialization The class type specialization
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of trainers with the specialization
     */
    fun findBySpecialization(
        specialization: ClassType,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Trainer>>

    /**
     * Save a trainer (create or update).
     *
     * @param trainer The trainer to save
     * @return Result containing the saved trainer
     */
    fun save(trainer: Trainer): Result<Trainer>
}
