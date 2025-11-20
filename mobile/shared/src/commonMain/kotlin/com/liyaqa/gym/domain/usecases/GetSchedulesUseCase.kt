package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.domain.ClassSchedule
import kotlinx.datetime.*

/**
 * Use case to get class schedules
 * Provides different views of schedules (today, week, month)
 */
class GetSchedulesUseCase(
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * Execute the use case
     * @param startDate Start date for filtering schedules
     * @param endDate End date for filtering schedules
     * @param forceRefresh Force refresh from network
     * @return Result containing list of ClassSchedule or error
     */
    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate,
        forceRefresh: Boolean = false
    ): Result<List<ClassSchedule>> {
        return try {
            val result = scheduleRepository.getSchedules(startDate, endDate, forceRefresh)

            if (result.isFailure) {
                return result
            }

            val schedules = result.getOrThrow()

            // Filter out cancelled schedules and sort by start time
            val activeSchedules = schedules
                .filter { !it.isCancelled }
                .sortedBy { it.startDateTime }

            Result.success(activeSchedules)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get today's schedules
     */
    suspend fun getToday(forceRefresh: Boolean = false): Result<List<ClassSchedule>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return invoke(today, today, forceRefresh)
    }

    /**
     * Get this week's schedules (Monday to Sunday)
     */
    suspend fun getThisWeek(forceRefresh: Boolean = false): Result<List<ClassSchedule>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dayOfWeek = today.dayOfWeek
        val daysFromMonday = dayOfWeek.ordinal // Monday = 0, Sunday = 6

        val startOfWeek = today.minus(daysFromMonday, DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)

        return invoke(startOfWeek, endOfWeek, forceRefresh)
    }

    /**
     * Get next 7 days of schedules
     */
    suspend fun getNext7Days(forceRefresh: Boolean = false): Result<List<ClassSchedule>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val endDate = today.plus(7, DateTimeUnit.DAY)

        return invoke(today, endDate, forceRefresh)
    }

    /**
     * Get this month's schedules
     */
    suspend fun getThisMonth(forceRefresh: Boolean = false): Result<List<ClassSchedule>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startOfMonth = LocalDate(today.year, today.month, 1)
        val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

        return invoke(startOfMonth, endOfMonth, forceRefresh)
    }

    /**
     * Get available schedules only (not full, not cancelled)
     */
    suspend fun getAvailable(
        startDate: LocalDate,
        endDate: LocalDate,
        forceRefresh: Boolean = false
    ): Result<List<ClassSchedule>> {
        return try {
            val result = invoke(startDate, endDate, forceRefresh)

            if (result.isFailure) {
                return result
            }

            val schedules = result.getOrThrow()

            // Filter available schedules
            val availableSchedules = schedules.filter { it.canBook() }

            Result.success(availableSchedules)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
