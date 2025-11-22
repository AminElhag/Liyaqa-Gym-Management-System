package com.liyaqa.android.ui.screens.classes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.domain.GymClass
import com.liyaqa.gym.domain.usecases.BookClassUseCase
import com.liyaqa.gym.domain.usecases.CancelBookingUseCase
import com.liyaqa.gym.network.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject

/**
 * ViewModel for the class detail screen
 * Manages detailed class information and booking operations
 */
@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val bookingRepository: BookingRepository,
    private val bookClassUseCase: BookClassUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val tokenStorage: TokenStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scheduleId: String = checkNotNull(savedStateHandle["scheduleId"])

    private val _classDetailState = MutableStateFlow<ClassDetailState>(ClassDetailState.Loading)
    val classDetailState: StateFlow<ClassDetailState> = _classDetailState.asStateFlow()

    private val _bookingEvent = MutableStateFlow<BookingEvent>(BookingEvent.Idle)
    val bookingEvent: StateFlow<BookingEvent> = _bookingEvent.asStateFlow()

    private var capacityUpdateJob: Job? = null

    init {
        loadClassDetail()
        startCapacityUpdates()
    }

    /**
     * Load class detail
     */
    fun loadClassDetail(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!forceRefresh) {
                    _classDetailState.value = ClassDetailState.Loading
                }

                val memberId = tokenStorage.getMemberId()
                if (memberId == null) {
                    _classDetailState.value = ClassDetailState.Error("User not authenticated")
                    return@launch
                }

                // Load schedule
                val scheduleResult = scheduleRepository.getScheduleById(scheduleId)
                if (scheduleResult.isFailure) {
                    _classDetailState.value = ClassDetailState.Error(
                        scheduleResult.exceptionOrNull()?.message ?: "Failed to load class details"
                    )
                    return@launch
                }

                val schedule = scheduleResult.getOrThrow()

                // Check if user has booked this class
                val bookingsResult = bookingRepository.getMyBookings(memberId)
                val bookings = bookingsResult.getOrNull() ?: emptyList()
                val booking = bookings.find { it.scheduleId == scheduleId }
                val isBooked = booking != null && (booking.isConfirmed() || booking.isWaitlisted())

                // Create a GymClass from schedule data
                // Note: We don't have full class details, but we have basic info from schedule
                val gymClass = GymClass(
                    id = schedule.classId,
                    branchId = "", // Not available in schedule
                    name = schedule.instructorName ?: "Class",
                    description = schedule.notes,
                    type = com.liyaqa.gym.domain.ClassType.OTHER, // Not available in current schedule
                    level = com.liyaqa.gym.domain.ClassLevel.ALL_LEVELS, // Not available
                    capacity = schedule.capacity,
                    durationMinutes = calculateDuration(schedule.startDateTime, schedule.endDateTime),
                    imageUrl = null,
                    isActive = !schedule.isCancelled,
                    createdAt = schedule.createdAt,
                    updatedAt = schedule.updatedAt
                )

                _classDetailState.value = ClassDetailState.Success(
                    schedule = schedule,
                    gymClass = gymClass,
                    isBooked = isBooked,
                    booking = booking
                )

            } catch (e: Exception) {
                _classDetailState.value = ClassDetailState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Calculate duration in minutes between two datetime
     */
    private fun calculateDuration(
        start: kotlinx.datetime.LocalDateTime,
        end: kotlinx.datetime.LocalDateTime
    ): Int {
        val startInstant = start.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())
        val endInstant = end.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())
        val duration = endInstant - startInstant
        return duration.inWholeMinutes.toInt()
    }

    /**
     * Book this class
     */
    fun bookClass() {
        viewModelScope.launch {
            try {
                _bookingEvent.value = BookingEvent.Loading

                val memberId = tokenStorage.getMemberId()
                if (memberId == null) {
                    _bookingEvent.value = BookingEvent.Error("User not authenticated")
                    return@launch
                }

                val result = bookClassUseCase(scheduleId, memberId)

                if (result.isSuccess) {
                    _bookingEvent.value = BookingEvent.Success("Class booked successfully!")
                    // Reload details to update UI
                    loadClassDetail(forceRefresh = true)
                } else {
                    _bookingEvent.value = BookingEvent.Error(
                        result.exceptionOrNull()?.message ?: "Failed to book class"
                    )
                }

            } catch (e: Exception) {
                _bookingEvent.value = BookingEvent.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Cancel booking
     */
    fun cancelBooking(bookingId: String, reason: String? = null) {
        viewModelScope.launch {
            try {
                _bookingEvent.value = BookingEvent.Loading

                val result = cancelBookingUseCase(bookingId, reason)

                if (result.isSuccess) {
                    _bookingEvent.value = BookingEvent.Success("Booking cancelled successfully!")
                    // Reload details to update UI
                    loadClassDetail(forceRefresh = true)
                } else {
                    _bookingEvent.value = BookingEvent.Error(
                        result.exceptionOrNull()?.message ?: "Failed to cancel booking"
                    )
                }

            } catch (e: Exception) {
                _bookingEvent.value = BookingEvent.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Clear booking event
     */
    fun clearBookingEvent() {
        _bookingEvent.value = BookingEvent.Idle
    }

    /**
     * Start real-time capacity updates using polling
     */
    private fun startCapacityUpdates() {
        capacityUpdateJob?.cancel()
        capacityUpdateJob = viewModelScope.launch {
            while (true) {
                delay(30_000) // Poll every 30 seconds

                // Only update if we're showing details
                val currentState = _classDetailState.value
                if (currentState is ClassDetailState.Success) {
                    updateCapacity()
                }
            }
        }
    }

    /**
     * Update capacity
     */
    private suspend fun updateCapacity() {
        try {
            val currentState = _classDetailState.value
            if (currentState !is ClassDetailState.Success) return

            // Fetch updated schedule
            val scheduleResult = scheduleRepository.getScheduleById(scheduleId)
            if (scheduleResult.isSuccess) {
                val updatedSchedule = scheduleResult.getOrThrow()
                _classDetailState.value = currentState.copy(
                    schedule = updatedSchedule
                )
            }
        } catch (e: Exception) {
            // Silently fail
        }
    }

    override fun onCleared() {
        super.onCleared()
        capacityUpdateJob?.cancel()
    }
}
