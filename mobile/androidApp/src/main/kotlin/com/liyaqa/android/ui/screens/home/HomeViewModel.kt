package com.liyaqa.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.domain.usecases.CheckInUseCase
import com.liyaqa.gym.domain.usecases.GetMemberProfileUseCase
import com.liyaqa.gym.domain.usecases.GetMyBookingsUseCase
import com.liyaqa.gym.network.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen
 * Manages home dashboard state and data loading
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMemberProfileUseCase: GetMemberProfileUseCase,
    private val getMyBookingsUseCase: GetMyBookingsUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val scheduleRepository: ScheduleRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadHomeData()
    }

    /**
     * Load all home screen data concurrently
     */
    fun loadHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                // Show loading only on initial load, not on refresh
                if (!forceRefresh) {
                    _homeState.value = HomeState.Loading
                }

                // Get current member ID
                val memberId = tokenStorage.getMemberId()
                if (memberId == null) {
                    _homeState.value = HomeState.Error("User not authenticated")
                    _isRefreshing.value = false
                    return@launch
                }

                // Load all data concurrently using async
                val memberDeferred = async { getMemberProfileUseCase(memberId, forceRefresh) }
                val bookingsDeferred = async { getMyBookingsUseCase.getUpcoming(memberId, forceRefresh) }

                // Await all results
                val memberResult = memberDeferred.await()
                val bookingsResult = bookingsDeferred.await()

                // Check if any result failed
                if (memberResult.isFailure) {
                    _homeState.value = HomeState.Error(
                        memberResult.exceptionOrNull()?.message ?: "Failed to load member profile"
                    )
                    _isRefreshing.value = false
                    return@launch
                }

                if (bookingsResult.isFailure) {
                    _homeState.value = HomeState.Error(
                        bookingsResult.exceptionOrNull()?.message ?: "Failed to load bookings"
                    )
                    _isRefreshing.value = false
                    return@launch
                }

                // Get the data
                val member = memberResult.getOrThrow()
                val bookings = bookingsResult.getOrThrow()

                // Load schedule details for each booking concurrently
                val bookingsWithSchedule = bookings.map { booking ->
                    async {
                        val scheduleResult = scheduleRepository.getScheduleById(booking.scheduleId)
                        BookingWithSchedule(
                            booking = booking,
                            schedule = scheduleResult.getOrNull()
                        )
                    }
                }.map { it.await() }

                // TODO: Implement activity stats API when available
                // For now, use placeholder data
                val activityStats = ActivityStats(
                    visitsThisWeek = 0,
                    classesAttended = 0,
                    totalVisits = 0,
                    currentStreak = 0
                )

                // TODO: Implement notifications API when available
                // For now, use placeholder data
                val notifications = NotificationData(
                    unreadCount = 0,
                    latestNotifications = emptyList()
                )

                // Update state with success
                _homeState.value = HomeState.Success(
                    member = member,
                    upcomingBookings = bookingsWithSchedule.take(3), // Show top 3
                    activityStats = activityStats,
                    notifications = notifications
                )

                _isRefreshing.value = false

            } catch (e: Exception) {
                _homeState.value = HomeState.Error(
                    e.message ?: "An unexpected error occurred"
                )
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Refresh home screen data
     */
    fun refresh() {
        _isRefreshing.value = true
        loadHomeData(forceRefresh = true)
    }

    /**
     * Check in to the gym
     */
    fun checkIn() {
        viewModelScope.launch {
            try {
                val memberId = tokenStorage.getMemberId() ?: return@launch
                // TODO: Implement check-in functionality when API is ready
                // For now, just refresh the data
                refresh()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
