package com.liyaqa.infrastructure.cache

import com.liyaqa.gym.domain.entities.ClassSchedule
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Cache service for ClassSchedule entities.
 * Implements cache-aside pattern with get, put, and invalidate operations.
 * Schedules are cached with 5-minute TTL as they change frequently (bookings, cancellations).
 */
@Service
class ScheduleCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(ScheduleCacheService::class.java)

    /**
     * Get schedules for a specific branch and date from cache.
     *
     * @param branchId The branch ID
     * @param date The schedule date
     * @return List of cached schedules or null if not found
     */
    @Cacheable(
        value = ["schedule"],
        key = "#branchId + ':' + #date.toString()",
        unless = "#result == null"
    )
    fun getSchedules(branchId: UUID, date: LocalDate): List<ClassSchedule>? {
        val key = RedisConfig.scheduleKey(branchId.toString(), date.toString())
        logger.debug("Cache miss for schedule key: {}", key)
        return null
    }

    /**
     * Get schedules from cache using manual Redis operations.
     *
     * @param branchId The branch ID
     * @param date The schedule date
     * @return List of cached schedules or null if not found
     */
    @Suppress("UNCHECKED_CAST")
    fun getSchedulesDirect(branchId: UUID, date: LocalDate): List<ClassSchedule>? {
        return try {
            val key = RedisConfig.scheduleKey(branchId.toString(), date.toString())
            val cached = redisTemplate.opsForValue().get(key)
            if (cached != null) {
                logger.debug("Cache hit for schedule key: {}", key)
                cached as List<ClassSchedule>
            } else {
                logger.debug("Cache miss for schedule key: {}", key)
                null
            }
        } catch (e: Exception) {
            logger.error("Error retrieving schedules from cache", e)
            null
        }
    }

    /**
     * Put schedules into cache.
     *
     * @param branchId The branch ID
     * @param date The schedule date
     * @param schedules List of schedules to cache
     * @return The cached schedules
     */
    @CachePut(
        value = ["schedule"],
        key = "#branchId + ':' + #date.toString()"
    )
    fun putSchedules(branchId: UUID, date: LocalDate, schedules: List<ClassSchedule>): List<ClassSchedule> {
        val key = RedisConfig.scheduleKey(branchId.toString(), date.toString())
        logger.debug("Caching {} schedules with key: {}", schedules.size, key)
        return schedules
    }

    /**
     * Put schedules into cache using manual Redis operations.
     * Uses 5-minute TTL for frequently changing schedule data.
     *
     * @param branchId The branch ID
     * @param date The schedule date
     * @param schedules List of schedules to cache
     * @param ttlMinutes TTL in minutes (default: 5 minutes)
     */
    fun putSchedulesDirect(
        branchId: UUID,
        date: LocalDate,
        schedules: List<ClassSchedule>,
        ttlMinutes: Long = 5
    ) {
        try {
            val key = RedisConfig.scheduleKey(branchId.toString(), date.toString())
            redisTemplate.opsForValue().set(key, schedules, ttlMinutes, TimeUnit.MINUTES)
            logger.debug("Cached {} schedules with key: {} (TTL: {} minutes)", schedules.size, key, ttlMinutes)
        } catch (e: Exception) {
            logger.error("Error caching schedules", e)
        }
    }

    /**
     * Invalidate schedule cache for a specific branch and date.
     * Called when schedules are updated (bookings, cancellations, modifications).
     *
     * @param branchId The branch ID
     * @param date The schedule date
     */
    @CacheEvict(
        value = ["schedule"],
        key = "#branchId + ':' + #date.toString()"
    )
    fun invalidateSchedules(branchId: UUID, date: LocalDate) {
        val key = RedisConfig.scheduleKey(branchId.toString(), date.toString())
        logger.debug("Invalidated schedule cache with key: {}", key)
    }

    /**
     * Invalidate schedule cache using manual Redis operations.
     *
     * @param branchId The branch ID
     * @param date The schedule date
     */
    fun invalidateSchedulesDirect(branchId: UUID, date: LocalDate) {
        try {
            val key = RedisConfig.scheduleKey(branchId.toString(), date.toString())
            redisTemplate.delete(key)
            logger.debug("Invalidated schedule cache with key: {}", key)
        } catch (e: Exception) {
            logger.error("Error invalidating schedule cache", e)
        }
    }

    /**
     * Invalidate all schedules for a specific branch.
     * Useful when bulk updates or migrations occur.
     *
     * @param branchId The branch ID
     */
    fun invalidateBranchSchedules(branchId: UUID) {
        try {
            val pattern = "schedule:$branchId:*"
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
                logger.debug("Invalidated {} schedule cache entries for branch: {}", keys.size, branchId)
            }
        } catch (e: Exception) {
            logger.error("Error invalidating branch schedules cache", e)
        }
    }

    /**
     * Invalidate schedules for a date range.
     * Useful when schedule changes affect multiple days.
     *
     * @param branchId The branch ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     */
    fun invalidateDateRange(branchId: UUID, startDate: LocalDate, endDate: LocalDate) {
        try {
            var currentDate = startDate
            val keysToDelete = mutableListOf<String>()

            while (!currentDate.isAfter(endDate)) {
                val key = RedisConfig.scheduleKey(branchId.toString(), currentDate.toString())
                keysToDelete.add(key)
                currentDate = currentDate.plusDays(1)
            }

            if (keysToDelete.isNotEmpty()) {
                redisTemplate.delete(keysToDelete)
                logger.debug("Invalidated {} schedule cache entries for date range: {} to {}",
                    keysToDelete.size, startDate, endDate)
            }
        } catch (e: Exception) {
            logger.error("Error invalidating schedule cache for date range", e)
        }
    }

    /**
     * Check if schedules are cached for a specific date.
     *
     * @param branchId The branch ID
     * @param date The schedule date
     * @return true if schedules are cached, false otherwise
     */
    fun areSchedulesCached(branchId: UUID, date: LocalDate): Boolean {
        return try {
            val key = RedisConfig.scheduleKey(branchId.toString(), date.toString())
            redisTemplate.hasKey(key) ?: false
        } catch (e: Exception) {
            logger.error("Error checking schedule cache existence", e)
            false
        }
    }

    /**
     * Get or fetch schedules using cache-aside pattern.
     * If schedules are in cache, returns them. Otherwise, fetches from DB.
     *
     * @param branchId The branch ID
     * @param date The schedule date
     * @param fetchFromDb Function to fetch schedules from database
     * @return List of schedules from cache or database
     */
    fun getOrFetch(
        branchId: UUID,
        date: LocalDate,
        fetchFromDb: () -> List<ClassSchedule>
    ): List<ClassSchedule> {
        // Check cache first
        val cached = getSchedulesDirect(branchId, date)
        if (cached != null) {
            return cached
        }

        // Cache miss - fetch from DB
        val schedules = fetchFromDb()

        // Store in cache (even if empty list, to avoid repeated DB queries)
        if (schedules.isNotEmpty()) {
            putSchedulesDirect(branchId, date, schedules)
        }

        return schedules
    }

    /**
     * Invalidate cache when a schedule is modified.
     * This should be called after any update, booking, or cancellation.
     *
     * @param schedule The modified schedule
     */
    fun invalidateOnUpdate(schedule: ClassSchedule) {
        val date = schedule.startDate.toLocalDate()
        invalidateSchedulesDirect(
            UUID.fromString(schedule.classId.toString()),
            date
        )
    }

    /**
     * Warm up cache with schedules for the next N days.
     * Useful for preloading frequently accessed data.
     *
     * @param branchId The branch ID
     * @param days Number of days to warm up (default: 7)
     * @param fetchSchedules Function to fetch schedules for a specific date
     */
    fun warmUpCache(
        branchId: UUID,
        days: Int = 7,
        fetchSchedules: (LocalDate) -> List<ClassSchedule>
    ) {
        try {
            val startDate = LocalDate.now()
            for (i in 0 until days) {
                val date = startDate.plusDays(i.toLong())
                val schedules = fetchSchedules(date)
                if (schedules.isNotEmpty()) {
                    putSchedulesDirect(branchId, date, schedules)
                }
            }
            logger.info("Warmed up schedule cache for branch {} with {} days", branchId, days)
        } catch (e: Exception) {
            logger.error("Error warming up schedule cache", e)
        }
    }
}
