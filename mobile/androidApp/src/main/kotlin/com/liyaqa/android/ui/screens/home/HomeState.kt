package com.liyaqa.android.ui.screens.home

import com.liyaqa.gym.domain.Booking
import com.liyaqa.gym.domain.ClassSchedule
import com.liyaqa.gym.domain.Member

/**
 * State for the home screen
 */
sealed class HomeState {
    /**
     * Initial loading state
     */
    data object Loading : HomeState()

    /**
     * Home data loaded successfully
     */
    data class Success(
        val member: Member,
        val upcomingBookings: List<BookingWithSchedule>,
        val activityStats: ActivityStats,
        val notifications: NotificationData
    ) : HomeState()

    /**
     * Error state
     */
    data class Error(val message: String) : HomeState()
}

/**
 * Booking with associated schedule information
 */
data class BookingWithSchedule(
    val booking: Booking,
    val schedule: ClassSchedule?
)

/**
 * Activity statistics for the member
 */
data class ActivityStats(
    val visitsThisWeek: Int,
    val classesAttended: Int,
    val totalVisits: Int,
    val currentStreak: Int
)

/**
 * Notification data
 */
data class NotificationData(
    val unreadCount: Int,
    val latestNotifications: List<Notification>
)

/**
 * Notification item
 */
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: kotlinx.datetime.Instant,
    val isRead: Boolean
)
