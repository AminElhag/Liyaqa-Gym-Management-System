package com.liyaqa.android.ui.screens.classes

import com.liyaqa.gym.domain.Booking
import com.liyaqa.gym.domain.ClassSchedule
import com.liyaqa.gym.domain.ClassType
import com.liyaqa.gym.domain.GymClass
import kotlinx.datetime.LocalDate

/**
 * State for class list screen
 */
sealed class ClassListState {
    object Loading : ClassListState()
    data class Success(
        val schedules: List<ScheduleWithClass>,
        val selectedDate: LocalDate,
        val filters: ClassFilters,
        val userBookings: Set<String> // Set of schedule IDs the user has booked
    ) : ClassListState()
    data class Error(val message: String) : ClassListState()
}

/**
 * Combined schedule and class information
 */
data class ScheduleWithClass(
    val schedule: ClassSchedule,
    val gymClass: GymClass?,
    val isBooked: Boolean = false,
    val booking: Booking? = null
)

/**
 * Filter options for class list
 */
data class ClassFilters(
    val classType: ClassType? = null,
    val trainerId: String? = null,
    val showOnlyAvailable: Boolean = false
)

/**
 * State for class detail screen
 */
sealed class ClassDetailState {
    object Loading : ClassDetailState()
    data class Success(
        val schedule: ClassSchedule,
        val gymClass: GymClass,
        val isBooked: Boolean,
        val booking: Booking?
    ) : ClassDetailState()
    data class Error(val message: String) : ClassDetailState()
}

/**
 * UI event for booking operations
 */
sealed class BookingEvent {
    object Idle : BookingEvent()
    object Loading : BookingEvent()
    data class Success(val message: String) : BookingEvent()
    data class Error(val message: String) : BookingEvent()
}
