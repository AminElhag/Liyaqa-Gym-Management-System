package com.liyaqa.gym.database.dao

import com.liyaqa.gym.domain.ClassSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * Data Access Object for ClassSchedule operations
 * Implements cache-aside pattern: check cache first, then network
 */
interface ScheduleDao {
    /**
     * Get all class schedules from cache
     */
    suspend fun getAll(): List<ClassSchedule>

    /**
     * Get class schedule by ID
     */
    suspend fun getById(id: String): ClassSchedule?

    /**
     * Get schedules by class ID
     */
    suspend fun getByClassId(classId: String): List<ClassSchedule>

    /**
     * Get schedules by date range
     */
    suspend fun getByDateRange(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): List<ClassSchedule>

    /**
     * Get upcoming schedules (limit results)
     */
    suspend fun getUpcoming(limit: Long): List<ClassSchedule>

    /**
     * Get schedules by instructor
     */
    suspend fun getByInstructor(instructorId: String): List<ClassSchedule>

    /**
     * Get available schedules (not full, not cancelled)
     */
    suspend fun getAvailable(limit: Long): List<ClassSchedule>

    /**
     * Observe all schedules as a flow
     */
    fun observeAll(): Flow<List<ClassSchedule>>

    /**
     * Observe schedule by ID as a flow
     */
    fun observeById(id: String): Flow<ClassSchedule?>

    /**
     * Observe schedules by date range as a flow
     */
    fun observeByDateRange(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Flow<List<ClassSchedule>>

    /**
     * Save a single schedule to cache
     */
    suspend fun save(schedule: ClassSchedule)

    /**
     * Save multiple schedules to cache
     */
    suspend fun saveAll(schedules: List<ClassSchedule>)

    /**
     * Update booked count for a schedule
     */
    suspend fun updateBookedCount(
        id: String,
        bookedCount: Int,
        waitlistCount: Int
    )

    /**
     * Cancel a schedule
     */
    suspend fun cancelSchedule(
        id: String,
        cancellationReason: String
    )

    /**
     * Delete schedule by ID
     */
    suspend fun deleteById(id: String)

    /**
     * Clear old schedules (ended before specified date)
     */
    suspend fun clearOldSchedules(endDateTime: LocalDateTime)

    /**
     * Clear all cached schedules
     */
    suspend fun clearAll()

    /**
     * Get count of cached schedules
     */
    suspend fun count(): Long
}
