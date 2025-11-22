package com.liyaqa.gym.data.repositories

import com.liyaqa.gym.database.dao.ScheduleDao
import com.liyaqa.gym.domain.ClassSchedule
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.ConnectivityMonitor
import com.liyaqa.gym.network.mappers.toDomain
import com.liyaqa.gym.network.services.ClassApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlin.time.Duration.Companion.minutes

/**
 * Repository interface for ClassSchedule operations
 */
interface ScheduleRepository {
    /**
     * Get schedules for a date range
     * @param startDate Start date for filtering schedules
     * @param endDate End date for filtering schedules
     * @param forceRefresh Force fetching from network, bypassing cache
     * @return Result containing list of ClassSchedule or error
     */
    suspend fun getSchedules(
        startDate: LocalDate,
        endDate: LocalDate,
        forceRefresh: Boolean = false
    ): Result<List<ClassSchedule>>

    /**
     * Get schedule by ID
     * @param id The schedule ID
     * @return Result containing ClassSchedule or error
     */
    suspend fun getScheduleById(id: String): Result<ClassSchedule>

    /**
     * Observe schedules for a date range as a flow
     * @param startDate Start date for filtering schedules
     * @param endDate End date for filtering schedules
     * @return Flow of list of ClassSchedule
     */
    fun observeSchedules(startDate: LocalDate, endDate: LocalDate): Flow<List<ClassSchedule>>

    /**
     * Sync schedules in background
     * @param startDate Start date for syncing schedules
     * @param endDate End date for syncing schedules
     */
    suspend fun syncSchedules(startDate: LocalDate, endDate: LocalDate)

    /**
     * Clear all cached schedule data
     */
    suspend fun clearCache()
}

/**
 * Implementation of ScheduleRepository with offline-first capabilities
 */
class ScheduleRepositoryImpl(
    private val apiService: ClassApiService,
    private val scheduleDao: ScheduleDao,
    private val connectivityMonitor: ConnectivityMonitor
) : ScheduleRepository {

    private val cacheMaxAge = 30.minutes

    /**
     * Convert LocalDate to LocalDateTime at start of day (00:00:00)
     */
    private fun LocalDate.atStartOfDay(): LocalDateTime = this.atTime(0, 0, 0)

    /**
     * Convert LocalDate to LocalDateTime at end of day (23:59:59)
     */
    private fun LocalDate.atEndOfDay(): LocalDateTime = this.atTime(23, 59, 59)

    override suspend fun getSchedules(
        startDate: LocalDate,
        endDate: LocalDate,
        forceRefresh: Boolean
    ): Result<List<ClassSchedule>> {
        // Try to get cached data first (offline-first approach)
        if (!forceRefresh) {
            val cached = scheduleDao.getByDateRange(startDate.atStartOfDay(), endDate.atEndOfDay())
            if (cached.isNotEmpty()) {
                // Return cached data immediately
                // Background sync will happen separately
                return Result.success(cached)
            }
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: return cached data
            val cached = scheduleDao.getByDateRange(startDate.atStartOfDay(), endDate.atEndOfDay())
            return if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No internet connection and no cached data available"))
            }
        }

        // Fetch from network
        return when (val result = apiService.getSchedules(
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            size = 100 // Get more results for better caching
        )) {
            is ApiResult.Success -> {
                val schedules = result.data.content.map { it.toDomain() }
                // Save to cache
                scheduleDao.saveAll(schedules)
                Result.success(schedules)
            }
            is ApiResult.Error -> {
                // Network error: try to return cached data as fallback
                val cached = scheduleDao.getByDateRange(startDate.atStartOfDay(), endDate.atEndOfDay())
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(result.error)
                }
            }
        }
    }

    override suspend fun getScheduleById(id: String): Result<ClassSchedule> {
        // Check cache first
        val cached = scheduleDao.getById(id)
        if (cached != null) {
            return Result.success(cached)
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection and no cached data available"))
        }

        // Fetch from network
        return when (val result = apiService.getScheduleById(id)) {
            is ApiResult.Success -> {
                val schedule = result.data.toDomain()
                // Save to cache
                scheduleDao.save(schedule)
                Result.success(schedule)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override fun observeSchedules(startDate: LocalDate, endDate: LocalDate): Flow<List<ClassSchedule>> {
        return scheduleDao.observeByDateRange(startDate.atStartOfDay(), endDate.atEndOfDay())
    }

    override suspend fun syncSchedules(startDate: LocalDate, endDate: LocalDate) {
        // Only sync if online
        if (!connectivityMonitor.isConnected()) {
            return
        }

        // Fetch from network in background
        when (val result = apiService.getSchedules(
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            size = 100
        )) {
            is ApiResult.Success -> {
                val schedules = result.data.content.map { it.toDomain() }
                // Update cache
                scheduleDao.saveAll(schedules)
            }
            is ApiResult.Error -> {
                // Ignore errors during background sync
            }
        }
    }

    override suspend fun clearCache() {
        scheduleDao.clearAll()
    }
}
