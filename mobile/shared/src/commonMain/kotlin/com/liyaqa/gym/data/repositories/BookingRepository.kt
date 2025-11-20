package com.liyaqa.gym.data.repositories

import com.liyaqa.gym.database.dao.BookingDao
import com.liyaqa.gym.domain.Booking
import com.liyaqa.gym.domain.BookingStatus
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.ConnectivityMonitor
import com.liyaqa.gym.network.mappers.toDomain
import com.liyaqa.gym.network.models.CancelBookingRequest
import com.liyaqa.gym.network.models.CreateBookingRequest
import com.liyaqa.gym.network.services.ClassApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

/**
 * Repository interface for Booking operations
 */
interface BookingRepository {
    /**
     * Book a class
     * @param scheduleId The schedule ID to book
     * @param memberId The member ID making the booking
     * @param notes Optional notes for the booking
     * @return Result containing Booking or error
     */
    suspend fun bookClass(
        scheduleId: String,
        memberId: String,
        notes: String? = null
    ): Result<Booking>

    /**
     * Cancel a booking
     * @param bookingId The booking ID to cancel
     * @param reason Optional cancellation reason
     * @return Result indicating success or error
     */
    suspend fun cancelBooking(bookingId: String, reason: String? = null): Result<Unit>

    /**
     * Get member's bookings
     * @param memberId The member ID
     * @param forceRefresh Force fetching from network
     * @return Result containing list of Booking or error
     */
    suspend fun getMyBookings(memberId: String, forceRefresh: Boolean = false): Result<List<Booking>>

    /**
     * Get booking by ID
     * @param bookingId The booking ID
     * @return Result containing Booking or error
     */
    suspend fun getBookingById(bookingId: String): Result<Booking>

    /**
     * Observe member's bookings as a flow
     * @param memberId The member ID
     * @return Flow of list of Booking
     */
    fun observeMyBookings(memberId: String): Flow<List<Booking>>

    /**
     * Clear all cached booking data
     */
    suspend fun clearCache()
}

/**
 * Implementation of BookingRepository with optimistic UI updates
 */
class BookingRepositoryImpl(
    private val apiService: ClassApiService,
    private val bookingDao: BookingDao,
    private val connectivityMonitor: ConnectivityMonitor
) : BookingRepository {

    override suspend fun bookClass(
        scheduleId: String,
        memberId: String,
        notes: String?
    ): Result<Booking> {
        // Check connectivity first
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection. Booking requires online access."))
        }

        val request = CreateBookingRequest(
            scheduleId = scheduleId,
            notes = notes
        )

        // Make the booking
        return when (val result = apiService.bookClass(memberId, request)) {
            is ApiResult.Success -> {
                val booking = result.data.toDomain()
                // Save to cache
                bookingDao.save(booking)
                Result.success(booking)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override suspend fun cancelBooking(bookingId: String, reason: String?): Result<Unit> {
        // Get the booking from cache first
        val booking = bookingDao.getById(bookingId)

        // Optimistic update: mark as cancelled in local cache immediately
        if (booking != null) {
            val cancelledBooking = booking.copy(
                status = BookingStatus.CANCELLED,
                cancelledAt = Clock.System.now(),
                cancellationReason = reason
            )
            bookingDao.save(cancelledBooking)
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: keep the optimistic update
            // In production, queue this for sync later
            return Result.success(Unit)
        }

        val request = CancelBookingRequest(reason = reason)

        // Cancel on server
        return when (val result = apiService.cancelBooking(bookingId, request)) {
            is ApiResult.Success -> {
                val updatedBooking = result.data.toDomain()
                // Update cache with server response
                bookingDao.save(updatedBooking)
                Result.success(Unit)
            }
            is ApiResult.Error -> {
                // Rollback the optimistic update
                if (booking != null) {
                    bookingDao.save(booking)
                }
                Result.failure(result.error)
            }
        }
    }

    override suspend fun getMyBookings(memberId: String, forceRefresh: Boolean): Result<List<Booking>> {
        // Try to get cached data first (offline-first approach)
        if (!forceRefresh) {
            val cached = bookingDao.getByMemberId(memberId)
            if (cached.isNotEmpty()) {
                return Result.success(cached)
            }
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: return cached data
            val cached = bookingDao.getByMemberId(memberId)
            return if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No internet connection and no cached data available"))
            }
        }

        // Fetch from network
        return when (val result = apiService.getMemberBookings(memberId, size = 100)) {
            is ApiResult.Success -> {
                val bookings = result.data.content.map { it.toDomain() }
                // Save to cache
                bookingDao.saveAll(bookings)
                Result.success(bookings)
            }
            is ApiResult.Error -> {
                // Network error: try to return cached data as fallback
                val cached = bookingDao.getByMemberId(memberId)
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(result.error)
                }
            }
        }
    }

    override suspend fun getBookingById(bookingId: String): Result<Booking> {
        // Check cache first
        val cached = bookingDao.getById(bookingId)
        if (cached != null) {
            return Result.success(cached)
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            return Result.failure(Exception("No internet connection and no cached data available"))
        }

        // Fetch from network
        return when (val result = apiService.getBookingById(bookingId)) {
            is ApiResult.Success -> {
                val booking = result.data.toDomain()
                // Save to cache
                bookingDao.save(booking)
                Result.success(booking)
            }
            is ApiResult.Error -> {
                Result.failure(result.error)
            }
        }
    }

    override fun observeMyBookings(memberId: String): Flow<List<Booking>> {
        return bookingDao.observeByMemberId(memberId)
    }

    override suspend fun clearCache() {
        bookingDao.clearAll()
    }
}
