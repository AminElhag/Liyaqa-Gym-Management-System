package com.liyaqa.gym.presentation.dto.subscription

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.util.UUID

/**
 * Request DTO for creating a new subscription
 */
@Schema(description = "Request to create a new subscription")
data class CreateSubscriptionRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @field:NotNull(message = "Plan ID is required")
    @Schema(description = "Membership plan ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val planId: UUID,

    @Schema(description = "Start date (defaults to today)", example = "2025-11-20")
    val startDate: LocalDate? = null,

    @Schema(description = "Auto-renew subscription", example = "true")
    val autoRenew: Boolean = false,

    @field:NotBlank(message = "Payment method is required")
    @Schema(
        description = "Payment method",
        example = "CREDIT_CARD",
        allowableValues = ["CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "APPLE_PAY", "STCPAY", "MADA", "ONLINE"]
    )
    val paymentMethod: String,

    @Schema(description = "Payment metadata (e.g., card token, transaction reference)")
    val paymentMetadata: Map<String, Any> = emptyMap()
)

/**
 * Request DTO for freezing a subscription
 */
@Schema(description = "Request to freeze a subscription")
data class FreezeSubscriptionRequest(
    @field:NotNull(message = "Freeze duration in days is required")
    @field:Min(value = 1, message = "Freeze duration must be at least 1 day")
    @field:Max(value = 90, message = "Freeze duration cannot exceed 90 days")
    @Schema(description = "Freeze duration in days", example = "14")
    val duration: Int,

    @field:Size(max = 500, message = "Reason must not exceed 500 characters")
    @Schema(description = "Reason for freezing", example = "Medical reasons")
    val reason: String? = null
)

/**
 * Request DTO for cancelling a subscription
 */
@Schema(description = "Request to cancel a subscription")
data class CancelSubscriptionRequest(
    @field:Size(max = 500, message = "Reason must not exceed 500 characters")
    @Schema(description = "Cancellation reason", example = "Moving to another city")
    val reason: String? = null,

    @Schema(description = "Cancel immediately or at end of period", example = "false")
    val immediate: Boolean = false
)

/**
 * Request DTO for upgrading a subscription
 */
@Schema(description = "Request to upgrade subscription to a different plan")
data class UpgradeSubscriptionRequest(
    @field:NotNull(message = "New plan ID is required")
    @Schema(description = "New membership plan ID", example = "123e4567-e89b-12d3-a456-426614174002")
    val planId: UUID,

    @field:NotBlank(message = "Payment method is required")
    @Schema(
        description = "Payment method for prorated amount",
        example = "CREDIT_CARD",
        allowableValues = ["CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "APPLE_PAY", "STCPAY", "MADA", "ONLINE"]
    )
    val paymentMethod: String,

    @Schema(description = "Payment metadata")
    val paymentMetadata: Map<String, Any> = emptyMap()
)

/**
 * Request DTO for renewing a subscription
 */
@Schema(description = "Request to renew a subscription")
data class RenewSubscriptionRequest(
    @field:NotBlank(message = "Payment method is required")
    @Schema(
        description = "Payment method",
        example = "CREDIT_CARD",
        allowableValues = ["CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "APPLE_PAY", "STCPAY", "MADA", "ONLINE"]
    )
    val paymentMethod: String,

    @Schema(description = "Payment metadata")
    val paymentMetadata: Map<String, Any> = emptyMap()
)
