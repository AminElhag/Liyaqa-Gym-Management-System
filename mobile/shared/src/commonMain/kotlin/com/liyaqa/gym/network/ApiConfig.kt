package com.liyaqa.gym.network

/**
 * API configuration constants
 */
object ApiConfig {
    const val BASE_URL = "http://localhost:8080/api/v1"
    const val TIMEOUT_MILLIS = 30_000L

    object Endpoints {
        // Member endpoints
        const val MEMBERS = "/members"
        const val MEMBER_BY_ID = "/members/{id}"
        const val MEMBER_SEARCH = "/members/search"

        // Subscription endpoints
        const val SUBSCRIPTIONS = "/subscriptions"
        const val SUBSCRIPTION_BY_ID = "/subscriptions/{id}"
        const val MEMBER_SUBSCRIPTIONS = "/members/{memberId}/subscriptions"

        // Class endpoints
        const val CLASSES = "/classes"
        const val CLASS_BY_ID = "/classes/{id}"
        const val CLASS_SCHEDULES = "/class-schedules"
        const val CLASS_SCHEDULE_BY_ID = "/class-schedules/{id}"

        // Booking endpoints
        const val BOOKINGS = "/bookings"
        const val BOOKING_BY_ID = "/bookings/{id}"
        const val MEMBER_BOOKINGS = "/members/{memberId}/bookings"
        const val SCHEDULE_BOOKINGS = "/class-schedules/{scheduleId}/bookings"

        // Auth endpoints
        const val LOGIN = "/auth/login"
        const val LOGOUT = "/auth/logout"
        const val REFRESH_TOKEN = "/auth/refresh"
    }
}
