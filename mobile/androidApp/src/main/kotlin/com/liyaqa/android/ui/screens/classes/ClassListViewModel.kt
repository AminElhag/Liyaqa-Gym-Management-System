package com.liyaqa.android.ui.screens.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.domain.Booking
import com.liyaqa.gym.domain.ClassSchedule
import com.liyaqa.gym.domain.ClassType
import com.liyaqa.gym.domain.usecases.BookClassUseCase
import com.liyaqa.gym.domain.usecases.CancelBookingUseCase
import com.liyaqa.gym.domain.usecases.GetSchedulesUseCase
import com.liyaqa.gym.network.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * ViewModel for the class list screen
 * Manages schedule loading, filtering, and booking operations
 */
@HiltViewModel
class ClassListViewModel @Inject constructor(
    private val getSchedulesUseCase: GetSchedulesUseCase,
    private val bookClassUseCase: BookClassUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val scheduleRepository: ScheduleRepository,
    private val bookingRepository: BookingRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _classListState = MutableStateFlow<ClassListState>(ClassListState.Loading)
    val classListState: StateFlow<ClassListState> = _classListState.asStateFlow()

    private val _bookingEvent = MutableStateFlow<BookingEvent>(BookingEvent.Idle)
    val bookingEvent: StateFlow<BookingEvent> = _bookingEvent.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private var currentFilters: ClassFilters = ClassFilters()
    private var capacityUpdateJob: Job? = null

    init {
        loadSchedules()
        startCapacityUpdates()
    }

    /**
     * Load schedules for the selected date with filters
     */
    fun loadSchedules(
        date: LocalDate = currentDate,
        filters: ClassFilters = currentFilters,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if (!forceRefresh) {
                    _classListState.value = ClassListState.Loading
                }

                currentDate = date
                currentFilters = filters

                // Get member ID
                val memberId = tokenStorage.getMemberId()
                if (memberId == null) {
                    _classListState.value = ClassListState.Error("User not authenticated")
                    _isRefreshing.value = false
                    return@launch
                }

                // Load schedules
                val schedulesResult = getSchedulesUseCase.invoke(date, date, forceRefresh)
                if (schedulesResult.isFailure) {
                    _classListState.value = ClassListState.Error(
                        schedulesResult.exceptionOrNull()?.message ?: "Failed to load schedules"
                    )
                    _isRefreshing.value = false
                    return@launch
                }

                var schedules = schedulesResult.getOrThrow()

                // Load user bookings
                val bookingsResult = bookingRepository.getMyBookings(memberId)
                val bookings = bookingsResult.getOrNull() ?: emptyList()
                val bookingsByScheduleId = bookings.associateBy { it.scheduleId }
                val bookedScheduleIds = bookings
                    .filter { it.isConfirmed() || it.isWaitlisted() }
                    .map { it.scheduleId }
                    .toSet()

                // Apply filters
                schedules = applyFilters(schedules, filters)

                // Map to ScheduleWithClass
                val schedulesWithClass = schedules.map { schedule ->
                    val isBooked = bookedScheduleIds.contains(schedule.id)
                    val booking = bookingsByScheduleId[schedule.id]
                    ScheduleWithClass(
                        schedule = schedule,
                        gymClass = null, // We have class info in schedule already
                        isBooked = isBooked,
                        booking = booking
                    )
                }

                _classListState.value = ClassListState.Success(
                    schedules = schedulesWithClass,
                    selectedDate = date,
                    filters = filters,
                    userBookings = bookedScheduleIds
                )

                _isRefreshing.value = false

            } catch (e: Exception) {
                _classListState.value = ClassListState.Error(
                    e.message ?: "An unexpected error occurred"
                )
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Apply filters to schedules
     */
    private fun applyFilters(
        schedules: List<ClassSchedule>,
        filters: ClassFilters
    ): List<ClassSchedule> {
        var filtered = schedules

        // Filter by class type
        filters.classType?.let { type ->
            // Note: We can't filter by type directly on ClassSchedule
            // This would require class info from the API
            // For now, we'll skip this filter or implement it when we have class data
        }

        // Filter by trainer
        filters.trainerId?.let { trainerId ->
            filtered = filtered.filter { it.instructorId == trainerId }
        }

        // Filter by availability
        if (filters.showOnlyAvailable) {
            filtered = filtered.filter { it.canBook() }
        }

        return filtered
    }

    /**
     * Update filters
     */
    fun updateFilters(filters: ClassFilters) {
        currentFilters = filters
        loadSchedules(currentDate, filters, forceRefresh = false)
    }

    /**
     * Change selected date
     */
    fun selectDate(date: LocalDate) {
        currentDate = date
        loadSchedules(date, currentFilters, forceRefresh = false)
    }

    /**
     * Refresh schedules
     */
    fun refresh() {
        _isRefreshing.value = true
        loadSchedules(currentDate, currentFilters, forceRefresh = true)
    }

    /**
     * Book a class
     */
    fun bookClass(scheduleId: String) {
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
                    // Reload schedules to update UI
                    loadSchedules(currentDate, currentFilters, forceRefresh = true)
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
     * Cancel a booking
     */
    fun cancelBooking(bookingId: String, reason: String? = null) {
        viewModelScope.launch {
            try {
                _bookingEvent.value = BookingEvent.Loading

                val result = cancelBookingUseCase(bookingId, reason)

                if (result.isSuccess) {
                    _bookingEvent.value = BookingEvent.Success("Booking cancelled successfully!")
                    // Reload schedules to update UI
                    loadSchedules(currentDate, currentFilters, forceRefresh = true)
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
     * In production, this should use WebSocket for better performance
     */
    private fun startCapacityUpdates() {
        capacityUpdateJob?.cancel()
        capacityUpdateJob = viewModelScope.launch {
            while (true) {
                delay(30_000) // Poll every 30 seconds

                // Only update if we're showing schedules (not loading or error)
                val currentState = _classListState.value
                if (currentState is ClassListState.Success) {
                    // Silently refresh in background
                    updateCapacity()
                }
            }
        }
    }

    /**
     * Update capacity for current schedules
     */
    private suspend fun updateCapacity() {
        try {
            val currentState = _classListState.value
            if (currentState !is ClassListState.Success) return

            // Fetch updated schedules from network
            val schedulesResult = getSchedulesUseCase.invoke(
                currentDate,
                currentDate,
                forceRefresh = true
            )

            if (schedulesResult.isSuccess) {
                val updatedSchedules = schedulesResult.getOrThrow()
                val updatedScheduleMap = updatedSchedules.associateBy { it.id }

                // Update existing state with new capacity info
                val updatedSchedulesWithClass = currentState.schedules.map { scheduleWithClass ->
                    val updatedSchedule = updatedScheduleMap[scheduleWithClass.schedule.id]
                    if (updatedSchedule != null) {
                        scheduleWithClass.copy(schedule = updatedSchedule)
                    } else {
                        scheduleWithClass
                    }
                }

                _classListState.value = currentState.copy(
                    schedules = updatedSchedulesWithClass
                )
            }
        } catch (e: Exception) {
            // Silently fail - don't disrupt user experience
        }
    }

    override fun onCleared() {
        super.onCleared()
        capacityUpdateJob?.cancel()
    }
}
