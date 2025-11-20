package com.liyaqa.gym.database.dao

import com.liyaqa.gym.domain.Booking
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Booking operations
 * Implements cache-aside pattern: check cache first, then network
 */
interface BookingDao {
    /**
     * Get all bookings from cache
     */
    suspend fun getAll(): List<Booking>

    /**
     * Get booking by ID
     */
    suspend fun getById(id: String): Booking?

    /**
     * Get bookings by member ID
     */
    suspend fun getByMemberId(memberId: String): List<Booking>

    /**
     * Get bookings by schedule ID
     */
    suspend fun getByScheduleId(scheduleId: String): List<Booking>

    /**
     * Get booking by member and schedule
     */
    suspend fun getByMemberAndSchedule(
        memberId: String,
        scheduleId: String
    ): Booking?

    /**
     * Get upcoming bookings for a member
     */
    suspend fun getUpcomingByMember(memberId: String): List<Booking>

    /**
     * Get bookings by status
     */
    suspend fun getByStatus(status: String): List<Booking>

    /**
     * Get waitlisted bookings for a schedule
     */
    suspend fun getWaitlistedBySchedule(scheduleId: String): List<Booking>

    /**
     * Observe all bookings as a flow
     */
    fun observeAll(): Flow<List<Booking>>

    /**
     * Observe booking by ID as a flow
     */
    fun observeById(id: String): Flow<Booking?>

    /**
     * Observe bookings by member ID as a flow
     */
    fun observeByMemberId(memberId: String): Flow<List<Booking>>

    /**
     * Save a single booking to cache
     */
    suspend fun save(booking: Booking)

    /**
     * Save multiple bookings to cache
     */
    suspend fun saveAll(bookings: List<Booking>)

    /**
     * Update booking status
     */
    suspend fun updateStatus(id: String, status: String)

    /**
     * Cancel a booking
     */
    suspend fun cancel(
        id: String,
        cancellationReason: String
    )

    /**
     * Check in a booking
     */
    suspend fun checkIn(id: String)

    /**
     * Delete booking by ID
     */
    suspend fun deleteById(id: String)

    /**
     * Delete bookings by member ID
     */
    suspend fun deleteByMemberId(memberId: String)

    /**
     * Delete bookings by schedule ID
     */
    suspend fun deleteByScheduleId(scheduleId: String)

    /**
     * Clear old bookings (for schedules that ended before specified time)
     */
    suspend fun clearOldBookings(endDateTime: String)

    /**
     * Clear all cached bookings
     */
    suspend fun clearAll()

    /**
     * Get count of cached bookings
     */
    suspend fun count(): Long
}
