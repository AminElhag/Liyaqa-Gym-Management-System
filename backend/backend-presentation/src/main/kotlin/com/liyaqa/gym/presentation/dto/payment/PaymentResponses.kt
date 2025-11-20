package com.liyaqa.gym.presentation.dto.payment

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Response DTO for payment details
 */
@Schema(description = "Payment details response")
data class PaymentResponse(
    @Schema(description = "Payment ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val memberId: UUID,

    @Schema(description = "Organization ID")
    val organizationId: UUID,

    @Schema(description = "Branch ID")
    val branchId: UUID,

    @Schema(description = "Invoice number", example = "INV-2025-001234")
    val invoiceNumber: String,

    @Schema(description = "Payment amount (excluding VAT)", example = "299.99")
    val amount: BigDecimal,

    @Schema(description = "VAT amount", example = "44.99")
    val vatAmount: BigDecimal,

    @Schema(description = "Total amount (including VAT)", example = "344.98")
    val totalAmount: BigDecimal,

    @Schema(description = "Currency code", example = "SAR")
    val currency: String,

    @Schema(description = "Payment method", example = "CREDIT_CARD")
    val method: String,

    @Schema(description = "Payment status", example = "COMPLETED", allowableValues = ["PENDING", "COMPLETED", "FAILED", "REFUNDED"])
    val status: String,

    @Schema(description = "Subscription ID (if applicable)")
    val subscriptionId: UUID?,

    @Schema(description = "PT Session ID (if applicable)")
    val ptSessionId: UUID?,

    @Schema(description = "Payment description")
    val description: String?,

    @Schema(description = "Payment gateway transaction ID")
    val paymentGatewayId: String?,

    @Schema(description = "Refunded amount")
    val refundedAmount: BigDecimal?,

    @Schema(description = "Payment timestamp")
    val paidAt: Instant?,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant,

    @Schema(description = "Last update timestamp")
    val updatedAt: Instant
)

/**
 * Summary response for payment lists
 */
@Schema(description = "Payment summary for list views")
data class PaymentSummaryResponse(
    @Schema(description = "Payment ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Invoice number")
    val invoiceNumber: String,

    @Schema(description = "Total amount (including VAT)")
    val totalAmount: BigDecimal,

    @Schema(description = "Currency code")
    val currency: String,

    @Schema(description = "Payment method")
    val method: String,

    @Schema(description = "Payment status")
    val status: String,

    @Schema(description = "Payment timestamp")
    val paidAt: Instant?,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant
)

/**
 * Response for payment processing
 */
@Schema(description = "Payment processing confirmation")
data class PaymentConfirmationResponse(
    @Schema(description = "Payment ID")
    val paymentId: UUID,

    @Schema(description = "Invoice number")
    val invoiceNumber: String,

    @Schema(description = "Total amount paid (including VAT)")
    val totalAmount: BigDecimal,

    @Schema(description = "Currency code")
    val currency: String,

    @Schema(description = "Payment status")
    val status: String,

    @Schema(description = "Payment gateway transaction ID")
    val transactionId: String?,

    @Schema(description = "Success message")
    val message: String = "Payment processed successfully"
)

/**
 * Response for refund processing
 */
@Schema(description = "Refund processing confirmation")
data class RefundConfirmationResponse(
    @Schema(description = "Refund ID")
    val refundId: UUID,

    @Schema(description = "Payment ID")
    val paymentId: UUID,

    @Schema(description = "Refund amount")
    val amount: BigDecimal,

    @Schema(description = "Currency code")
    val currency: String,

    @Schema(description = "Credit note number")
    val creditNoteNumber: String?,

    @Schema(description = "Refund status")
    val status: String,

    @Schema(description = "Gateway refund ID")
    val gatewayRefundId: String?,

    @Schema(description = "Processing timestamp")
    val processedAt: Instant?,

    @Schema(description = "Success message")
    val message: String = "Refund processed successfully"
)

/**
 * Response for webhook acknowledgment
 */
@Schema(description = "Webhook acknowledgment response")
data class WebhookAcknowledgmentResponse(
    @Schema(description = "Whether webhook was received successfully", example = "true")
    val received: Boolean,

    @Schema(description = "Event type")
    val eventType: String?,

    @Schema(description = "Event ID")
    val eventId: String?,

    @Schema(description = "Processing message")
    val message: String = "Webhook received and queued for processing"
)
