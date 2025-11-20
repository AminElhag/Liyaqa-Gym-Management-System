package com.liyaqa.gym.presentation.dto.payment

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Request DTO for processing a payment
 */
@Schema(description = "Request to process a payment")
data class ProcessPaymentRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @field:NotNull(message = "Organization ID is required")
    @Schema(description = "Organization ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val organizationId: UUID,

    @field:NotNull(message = "Branch ID is required")
    @Schema(description = "Branch ID", example = "123e4567-e89b-12d3-a456-426614174002")
    val branchId: UUID,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    @Schema(description = "Payment amount (excluding VAT)", example = "299.99")
    val amount: BigDecimal,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Schema(description = "Currency code (ISO 4217)", example = "SAR")
    val currency: String = "SAR",

    @field:NotBlank(message = "Payment method is required")
    @Schema(
        description = "Payment method",
        example = "CREDIT_CARD",
        allowableValues = ["CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "APPLE_PAY", "STCPAY", "MADA", "ONLINE"]
    )
    val method: String,

    @Schema(description = "Subscription ID (if payment is for subscription)")
    val subscriptionId: UUID? = null,

    @Schema(description = "PT Session ID (if payment is for PT session)")
    val ptSessionId: UUID? = null,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Payment description", example = "Payment for Gold Membership")
    val description: String? = null,

    @Schema(description = "Payment metadata (e.g., card token, gateway reference)")
    val metadata: Map<String, Any> = emptyMap(),

    @field:Size(max = 100, message = "Idempotency key must not exceed 100 characters")
    @Schema(description = "Idempotency key to prevent duplicate payments")
    val idempotencyKey: String? = null
)

/**
 * Request DTO for processing a refund
 */
@Schema(description = "Request to process a refund")
data class ProcessRefundRequest(
    @field:NotNull(message = "Payment ID is required")
    @Schema(description = "Payment ID to refund", example = "123e4567-e89b-12d3-a456-426614174000")
    val paymentId: UUID,

    @field:NotNull(message = "Refund amount is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Refund amount must be positive")
    @Schema(description = "Refund amount", example = "299.99")
    val amount: BigDecimal,

    @field:NotBlank(message = "Refund reason is required")
    @field:Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    @Schema(description = "Refund reason", example = "Customer requested refund due to relocation")
    val reason: String,

    @Schema(description = "Validate refund policy (time limits, etc.)", example = "true")
    val validatePolicy: Boolean = true
)

/**
 * Request DTO for webhook payloads from Stripe
 */
@Schema(description = "Stripe webhook payload")
data class StripeWebhookRequest(
    @field:NotBlank(message = "Event type is required")
    @Schema(description = "Stripe event type", example = "payment_intent.succeeded")
    val type: String,

    @Schema(description = "Event data")
    val data: Map<String, Any> = emptyMap(),

    @Schema(description = "Event ID")
    val id: String? = null
)

/**
 * Request DTO for webhook payloads from Mada
 */
@Schema(description = "Mada webhook payload")
data class MadaWebhookRequest(
    @field:NotBlank(message = "Transaction ID is required")
    @Schema(description = "Mada transaction ID")
    val transactionId: String,

    @field:NotBlank(message = "Status is required")
    @Schema(description = "Transaction status", example = "APPROVED", allowableValues = ["APPROVED", "DECLINED", "PENDING"])
    val status: String,

    @Schema(description = "Transaction amount")
    val amount: BigDecimal? = null,

    @Schema(description = "Transaction currency")
    val currency: String? = null,

    @Schema(description = "Additional webhook data")
    val metadata: Map<String, Any> = emptyMap()
)
