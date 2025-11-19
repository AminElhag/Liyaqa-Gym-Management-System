package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.ClassSchedule
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for ClassSchedule entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface ClassScheduleRepository {

    /**
     * Find a class schedule by its unique identifier.
     *
     * @param id The unique identifier of the class schedule
     * @return Optional containing the class schedule if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<ClassSchedule>>

    /**
     * Find all class schedules for a specific branch.
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of class schedules
     */
    fun findByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<ClassSchedule>>

    /**
     * Find class schedules within a date range.
     *
     * @param startDate The start date/time of the range
     * @param endDate The end date/time of the range
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of class schedules in the range
     */
    fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        page: Int = 0,
        size: Int = 20
    ): Result<List<ClassSchedule>>

    /**
     * Find class schedules assigned to a specific trainer.
     *
     * @param trainerId The trainer identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of class schedules
     */
    fun findByTrainer(trainerId: UUID, page: Int = 0, size: Int = 20): Result<List<ClassSchedule>>

    /**
     * Save a class schedule (create or update).
     *
     * @param schedule The class schedule to save
     * @return Result containing the saved class schedule
     */
    fun save(schedule: ClassSchedule): Result<ClassSchedule>
}
