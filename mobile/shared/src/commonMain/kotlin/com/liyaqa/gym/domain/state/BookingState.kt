package com.liyaqa.gym.domain.state

import com.liyaqa.gym.domain.Booking

/**
 * UI state for bookings screen
 */
sealed class BookingState {
    /**
     * Initial state
     */
    data object Idle : BookingState()

    /**
     * Loading bookings
     */
    data object Loading : BookingState()

    /**
     * Successfully loaded bookings
     */
    data class Success(
        val upcomingBookings: List<Booking>,
        val pastBookings: List<Booking>,
        val isRefreshing: Boolean = false
    ) : BookingState()

    /**
     * Error loading bookings
     */
    data class Error(
        val message: String,
        val error: Throwable? = null
    ) : BookingState()

    /**
     * Booking a class
     */
    data class Booking(
        val scheduleId: String
    ) : BookingState()

    /**
     * Booking successful
     */
    data class BookingSuccess(
        val booking: Booking
    ) : BookingState()

    /**
     * Booking failed
     */
    data class BookingError(
        val message: String,
        val error: Throwable? = null
    ) : BookingState()

    /**
     * Cancelling a booking
     */
    data class Cancelling(
        val bookingId: String
    ) : BookingState()

    /**
     * Cancellation successful
     */
    data class CancellationSuccess(
        val bookingId: String
    ) : BookingState()

    /**
     * Cancellation failed
     */
    data class CancellationError(
        val bookingId: String,
        val message: String,
        val error: Throwable? = null
    ) : BookingState()
}
