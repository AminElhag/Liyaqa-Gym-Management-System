package com.liyaqa.gym.domain

import kotlinx.serialization.Serializable

/**
 * Gender enumeration
 */
@Serializable
enum class Gender {
    MALE,
    FEMALE
}

/**
 * Member status enumeration
 */
@Serializable
enum class MemberStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_APPROVAL
}

/**
 * Subscription status enumeration
 */
@Serializable
enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED,
    SUSPENDED,
    PAUSED
}

/**
 * Booking status enumeration
 */
@Serializable
enum class BookingStatus {
    CONFIRMED,
    WAITLISTED,
    ATTENDED,
    CANCELLED,
    NO_SHOW
}

/**
 * Class type enumeration
 */
@Serializable
enum class ClassType {
    YOGA,
    PILATES,
    CARDIO,
    STRENGTH_TRAINING,
    HIIT,
    SPINNING,
    ZUMBA,
    CROSSFIT,
    BOXING,
    SWIMMING,
    FUNCTIONAL_TRAINING,
    STRETCHING,
    MARTIAL_ARTS,
    DANCE,
    OTHER
}

/**
 * Class difficulty level
 */
@Serializable
enum class ClassLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ALL_LEVELS
}
