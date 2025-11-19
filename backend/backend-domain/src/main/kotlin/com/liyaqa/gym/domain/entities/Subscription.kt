package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Subscription entity linking a Member to a MembershipPlan.
 * Tracks the active membership period and status.
 */
data class Subscription(
    val id: UUID,
    val memberId: UUID,
    val planId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val status: SubscriptionStatus,
    val autoRenew: Boolean,
    val remainingVisits: Int?,
    val pausedAt: LocalDate?,
    val pausedUntil: LocalDate?,
    val cancelledAt: Instant?,
    val cancellationReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        endDate?.let {
            require(!it.isBefore(startDate)) {
                "End date cannot be before start date"
            }
        }
        pausedUntil?.let { until ->
            require(pausedAt != null) { "Cannot have pausedUntil without pausedAt" }
            require(!until.isBefore(pausedAt)) {
                "Pause end date cannot be before pause start date"
            }
        }
    }

    fun isActive(): Boolean = status == SubscriptionStatus.ACTIVE

    fun isExpired(): Boolean {
        return endDate?.isBefore(LocalDate.now()) == true
    }

    fun isPaused(): Boolean = status == SubscriptionStatus.PAUSED

    fun canUse(): Boolean {
        return isActive() && !isExpired() && !isPaused()
    }

    fun activate(): Subscription {
        return copy(status = SubscriptionStatus.ACTIVE, updatedAt = Instant.now())
    }

    fun suspend(): Subscription {
        return copy(status = SubscriptionStatus.SUSPENDED, updatedAt = Instant.now())
    }

    fun cancel(reason: String? = null): Subscription {
        val now = Instant.now()
        return copy(
            status = SubscriptionStatus.CANCELLED,
            cancelledAt = now,
            cancellationReason = reason,
            autoRenew = false,
            updatedAt = now
        )
    }

    fun pause(until: LocalDate): Subscription {
        val now = LocalDate.now()
        require(!until.isBefore(now)) { "Cannot pause until a past date" }
        return copy(
            status = SubscriptionStatus.PAUSED,
            pausedAt = now,
            pausedUntil = until,
            updatedAt = Instant.now()
        )
    }

    fun resume(): Subscription {
        return copy(
            status = SubscriptionStatus.ACTIVE,
            pausedAt = null,
            pausedUntil = null,
            updatedAt = Instant.now()
        )
    }

    fun decrementVisit(): Subscription {
        require(remainingVisits != null && remainingVisits > 0) {
            "No remaining visits to decrement"
        }
        return copy(
            remainingVisits = remainingVisits - 1,
            updatedAt = Instant.now()
        )
    }

    fun enableAutoRenew(): Subscription {
        return copy(autoRenew = true, updatedAt = Instant.now())
    }

    fun disableAutoRenew(): Subscription {
        return copy(autoRenew = false, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            memberId: UUID,
            planId: UUID,
            startDate: LocalDate,
            endDate: LocalDate?,
            autoRenew: Boolean = false,
            remainingVisits: Int? = null
        ): Subscription {
            val now = Instant.now()
            return Subscription(
                id = UUID.randomUUID(),
                memberId = memberId,
                planId = planId,
                startDate = startDate,
                endDate = endDate,
                status = SubscriptionStatus.ACTIVE,
                autoRenew = autoRenew,
                remainingVisits = remainingVisits,
                pausedAt = null,
                pausedUntil = null,
                cancelledAt = null,
                cancellationReason = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Subscription status enumeration
 */
enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED,
    SUSPENDED,
    PAUSED
}
