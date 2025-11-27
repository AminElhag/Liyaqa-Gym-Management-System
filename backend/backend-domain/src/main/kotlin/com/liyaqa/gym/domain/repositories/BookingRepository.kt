package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Booking
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Booking entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface BookingRepository {

    /**
     * Find a booking by its unique identifier.
     *
     * @param id The unique identifier of the booking
     * @return Optional containing the booking if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Booking>>

    /**
     * Find all bookings for a specific member.
     *
     * @param memberId The member identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of bookings
     */
    fun findByMember(memberId: UUID, page: Int = 0, size: Int = 20): Result<List<Booking>>

    /**
     * Find all bookings for a specific class schedule.
     *
     * @param scheduleId The class schedule identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of bookings
     */
    fun findBySchedule(scheduleId: UUID, page: Int = 0, size: Int = 20): Result<List<Booking>>

    /**
     * Count the number of bookings for a specific class schedule.
     *
     * @param scheduleId The class schedule identifier
     * @return Result containing the count of bookings
     */
    fun countBySchedule(scheduleId: UUID): Result<Long>

    /**
     * Save a booking (create or update).
     *
     * @param booking The booking to save
     * @return Result containing the saved booking
     */
    fun save(booking: Booking): Result<Booking>

    /**
     * Delete a booking.
     *
     * @param id The unique identifier of the booking to delete
     * @return Result indicating success or failure
     */
    fun delete(id: UUID): Result<Unit>

    /**
     * Count all bookings across all tenants (platform-wide).
     *
     * @return Result containing total count of bookings
     */
    fun countAll(): Result<Long>
}
