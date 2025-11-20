package com.liyaqa.gym.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Subscription entity linking a Member to a MembershipPlan.
 * Tracks the active membership period and status.
 */
@Serializable
data class Subscription(
    val id: String,
    val memberId: String,
    val planId: String,
    val planName: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val status: SubscriptionStatus,
    val autoRenew: Boolean,
    val remainingVisits: Int? = null,
    val pausedAt: LocalDate? = null,
    val pausedUntil: LocalDate? = null,
    val cancelledAt: Instant? = null,
    val cancellationReason: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun isActive(): Boolean = status == SubscriptionStatus.ACTIVE

    fun isExpired(currentDate: LocalDate): Boolean {
        return endDate?.let { it < currentDate } ?: false
    }

    fun isPaused(): Boolean = status == SubscriptionStatus.PAUSED

    fun canUse(currentDate: LocalDate): Boolean {
        return isActive() && !isExpired(currentDate) && !isPaused()
    }

    fun daysRemaining(currentDate: LocalDate): Int? {
        return endDate?.let { end ->
            if (end < currentDate) 0 else (end.toEpochDays() - currentDate.toEpochDays())
        }
    }

    fun hasUnlimitedVisits(): Boolean = remainingVisits == null

    fun isVisitBased(): Boolean = remainingVisits != null
}
