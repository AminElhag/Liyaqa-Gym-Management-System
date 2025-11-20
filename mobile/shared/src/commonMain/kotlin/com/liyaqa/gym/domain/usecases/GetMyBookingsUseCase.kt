package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.domain.Booking
import com.liyaqa.gym.domain.BookingStatus

/**
 * Use case to get member's bookings
 * Can filter by status and sort results
 */
class GetMyBookingsUseCase(
    private val bookingRepository: BookingRepository
) {
    /**
     * Execute the use case
     * @param memberId The member ID
     * @param forceRefresh Force refresh from network
     * @param filterStatus Optional status filter
     * @return Result containing list of Booking or error
     */
    suspend operator fun invoke(
        memberId: String,
        forceRefresh: Boolean = false,
        filterStatus: BookingStatus? = null
    ): Result<List<Booking>> {
        return try {
            val result = bookingRepository.getMyBookings(memberId, forceRefresh)

            if (result.isFailure) {
                return result
            }

            val bookings = result.getOrThrow()

            // Apply filter if specified
            val filteredBookings = if (filterStatus != null) {
                bookings.filter { it.status == filterStatus }
            } else {
                bookings
            }

            // Sort by booking date (most recent first)
            val sortedBookings = filteredBookings.sortedByDescending { it.bookedAt }

            Result.success(sortedBookings)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get upcoming bookings (confirmed or waitlisted)
     */
    suspend fun getUpcoming(
        memberId: String,
        forceRefresh: Boolean = false
    ): Result<List<Booking>> {
        return try {
            val result = bookingRepository.getMyBookings(memberId, forceRefresh)

            if (result.isFailure) {
                return result
            }

            val bookings = result.getOrThrow()

            // Filter upcoming bookings
            val upcomingBookings = bookings
                .filter { it.isConfirmed() || it.isWaitlisted() }
                .sortedBy { it.bookedAt }

            Result.success(upcomingBookings)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get past bookings (attended, cancelled, or no-show)
     */
    suspend fun getPast(
        memberId: String,
        forceRefresh: Boolean = false
    ): Result<List<Booking>> {
        return try {
            val result = bookingRepository.getMyBookings(memberId, forceRefresh)

            if (result.isFailure) {
                return result
            }

            val bookings = result.getOrThrow()

            // Filter past bookings
            val pastBookings = bookings
                .filter { it.isAttended() || it.isCancelled() || it.isNoShow() }
                .sortedByDescending { it.bookedAt }

            Result.success(pastBookings)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
