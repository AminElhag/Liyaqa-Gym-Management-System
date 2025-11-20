package com.liyaqa.gym.presentation.dto.subscription

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Response DTO for subscription details
 */
@Schema(description = "Subscription details response")
data class SubscriptionResponse(
    @Schema(description = "Subscription ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val memberId: UUID,

    @Schema(description = "Membership plan ID", example = "123e4567-e89b-12d3-a456-426614174002")
    val planId: UUID,

    @Schema(description = "Start date", example = "2025-11-20")
    val startDate: LocalDate,

    @Schema(description = "End date", example = "2025-12-20")
    val endDate: LocalDate?,

    @Schema(description = "Subscription status", example = "ACTIVE", allowableValues = ["ACTIVE", "EXPIRED", "CANCELLED", "SUSPENDED", "PAUSED"])
    val status: String,

    @Schema(description = "Auto-renew enabled", example = "true")
    val autoRenew: Boolean,

    @Schema(description = "Remaining visits (for visit-based plans)", example = "10")
    val remainingVisits: Int?,

    @Schema(description = "Date subscription was paused", example = "2025-11-25")
    val pausedAt: LocalDate?,

    @Schema(description = "Date subscription pause ends", example = "2025-12-09")
    val pausedUntil: LocalDate?,

    @Schema(description = "Date subscription was cancelled")
    val cancelledAt: Instant?,

    @Schema(description = "Cancellation reason")
    val cancellationReason: String?,

    @Schema(description = "Is subscription currently active", example = "true")
    val isActive: Boolean,

    @Schema(description = "Is subscription expired", example = "false")
    val isExpired: Boolean,

    @Schema(description = "Can subscription be used for access", example = "true")
    val canUse: Boolean,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant,

    @Schema(description = "Last update timestamp")
    val updatedAt: Instant
)

/**
 * Summary response for subscription lists
 */
@Schema(description = "Subscription summary for list views")
data class SubscriptionSummaryResponse(
    @Schema(description = "Subscription ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Membership plan ID")
    val planId: UUID,

    @Schema(description = "Start date")
    val startDate: LocalDate,

    @Schema(description = "End date")
    val endDate: LocalDate?,

    @Schema(description = "Subscription status")
    val status: String,

    @Schema(description = "Auto-renew enabled")
    val autoRenew: Boolean,

    @Schema(description = "Can be used for access")
    val canUse: Boolean
)

/**
 * Response for freeze operation
 */
@Schema(description = "Subscription freeze confirmation")
data class FreezeConfirmationResponse(
    @Schema(description = "Subscription ID")
    val subscriptionId: UUID,

    @Schema(description = "Date subscription is frozen until")
    val frozenUntil: LocalDate,

    @Schema(description = "New end date after extension")
    val newEndDate: LocalDate,

    @Schema(description = "Number of days frozen")
    val freezeDays: Long,

    @Schema(description = "Success message")
    val message: String = "Subscription frozen successfully"
)

/**
 * Response for cancellation operation
 */
@Schema(description = "Subscription cancellation confirmation")
data class CancellationConfirmationResponse(
    @Schema(description = "Subscription ID")
    val subscriptionId: UUID,

    @Schema(description = "Cancellation timestamp")
    val cancelledAt: Instant,

    @Schema(description = "Whether cancellation is immediate")
    val immediate: Boolean,

    @Schema(description = "End date (for non-immediate cancellations)")
    val endDate: LocalDate?,

    @Schema(description = "Success message")
    val message: String
)

/**
 * Response for renewal operation
 */
@Schema(description = "Subscription renewal confirmation")
data class RenewalConfirmationResponse(
    @Schema(description = "Subscription ID")
    val subscriptionId: UUID,

    @Schema(description = "New end date")
    val newEndDate: LocalDate,

    @Schema(description = "Payment ID")
    val paymentId: UUID,

    @Schema(description = "Success message")
    val message: String = "Subscription renewed successfully"
)

/**
 * Response for upgrade operation
 */
@Schema(description = "Subscription upgrade confirmation")
data class UpgradeConfirmationResponse(
    @Schema(description = "Subscription ID")
    val subscriptionId: UUID,

    @Schema(description = "New plan ID")
    val newPlanId: UUID,

    @Schema(description = "Payment ID for prorated amount")
    val paymentId: UUID?,

    @Schema(description = "New end date")
    val newEndDate: LocalDate?,

    @Schema(description = "Success message")
    val message: String = "Subscription upgraded successfully"
)

/**
 * Paginated response for subscription lists
 */
@Schema(description = "Paginated subscription list")
data class PagedSubscriptionResponse(
    @Schema(description = "Subscription list")
    val content: List<SubscriptionSummaryResponse>,

    @Schema(description = "Current page number")
    val page: Int,

    @Schema(description = "Page size")
    val size: Int,

    @Schema(description = "Total elements")
    val totalElements: Long,

    @Schema(description = "Total pages")
    val totalPages: Int,

    @Schema(description = "Has next page")
    val hasNext: Boolean,

    @Schema(description = "Has previous page")
    val hasPrevious: Boolean
)
