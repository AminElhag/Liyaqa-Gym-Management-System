package com.liyaqa.gym.application.subscription.dto

import com.liyaqa.gym.domain.entities.SubscriptionStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Data Transfer Object for Subscription entity.
 * Complete representation for detailed views.
 */
data class SubscriptionDTO(
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
    val isActive: Boolean,
    val isExpired: Boolean,
    val canUse: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Summary DTO for list views and compact representations.
 */
data class SubscriptionSummaryDTO(
    val id: UUID,
    val memberId: UUID,
    val planId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val status: SubscriptionStatus,
    val autoRenew: Boolean,
    val canUse: Boolean
)

/**
 * DTO for freeze confirmation response.
 */
data class FreezeConfirmationDTO(
    val subscriptionId: UUID,
    val frozenUntil: LocalDate,
    val newEndDate: LocalDate,
    val freezeDays: Long
)
