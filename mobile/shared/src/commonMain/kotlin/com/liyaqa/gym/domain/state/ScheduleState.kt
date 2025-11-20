package com.liyaqa.gym.domain.state

import com.liyaqa.gym.domain.ClassSchedule
import kotlinx.datetime.LocalDate

/**
 * UI state for class schedules screen
 */
sealed class ScheduleState {
    /**
     * Initial state
     */
    data object Idle : ScheduleState()

    /**
     * Loading schedules
     */
    data class Loading(
        val startDate: LocalDate,
        val endDate: LocalDate
    ) : ScheduleState()

    /**
     * Successfully loaded schedules
     */
    data class Success(
        val schedules: List<ClassSchedule>,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val isRefreshing: Boolean = false
    ) : ScheduleState()

    /**
     * Error loading schedules
     */
    data class Error(
        val message: String,
        val error: Throwable? = null
    ) : ScheduleState()

    /**
     * Empty state (no schedules found)
     */
    data class Empty(
        val startDate: LocalDate,
        val endDate: LocalDate
    ) : ScheduleState()
}

/**
 * Filter options for schedule view
 */
enum class ScheduleViewFilter {
    TODAY,
    THIS_WEEK,
    NEXT_7_DAYS,
    THIS_MONTH,
    CUSTOM
}
