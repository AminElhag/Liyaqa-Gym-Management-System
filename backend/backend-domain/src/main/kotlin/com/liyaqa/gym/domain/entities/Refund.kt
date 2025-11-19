package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.util.UUID

/**
 * Refund entity representing a payment refund transaction.
 *
 * This entity tracks refunds for payments, including:
 * - Full or partial refunds
 * - Refund reasons
 * - Payment gateway transaction details
 * - Credit note generation for accounting
 */
data class Refund(
    val id: UUID,
    val paymentId: UUID,
    val invoiceId: UUID?,
    val memberId: UUID,
    val organizationId: UUID,
    val branchId: UUID,
    val amount: Money,
    val reason: String,
    val status: RefundStatus,
    val paymentGatewayRefundId: String?,
    val paymentGatewayResponse: String?,
    val creditNoteNumber: String?,
    val creditNoteFilePath: String?,
    val processedAt: Instant?,
    val failedAt: Instant?,
    val failureReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(amount.isPositive()) { "Refund amount must be positive" }
        require(reason.isNotBlank()) { "Refund reason cannot be blank" }
    }

    fun isPending(): Boolean = status == RefundStatus.PENDING

    fun isProcessed(): Boolean = status == RefundStatus.PROCESSED

    fun isFailed(): Boolean = status == RefundStatus.FAILED

    fun markAsProcessed(gatewayRefundId: String?, creditNoteNumber: String?): Refund {
        require(isPending()) { "Only pending refunds can be marked as processed" }
        return copy(
            status = RefundStatus.PROCESSED,
            paymentGatewayRefundId = gatewayRefundId,
            creditNoteNumber = creditNoteNumber,
            processedAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun markAsFailed(failureReason: String, gatewayResponse: String?): Refund {
        require(isPending()) { "Only pending refunds can be marked as failed" }
        return copy(
            status = RefundStatus.FAILED,
            failureReason = failureReason,
            paymentGatewayResponse = gatewayResponse,
            failedAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun retry(): Refund {
        require(isFailed()) { "Only failed refunds can be retried" }
        return copy(
            status = RefundStatus.PENDING,
            failedAt = null,
            failureReason = null,
            paymentGatewayResponse = null,
            updatedAt = Instant.now()
        )
    }

    fun addCreditNote(creditNoteNumber: String, filePath: String): Refund {
        return copy(
            creditNoteNumber = creditNoteNumber,
            creditNoteFilePath = filePath,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            paymentId: UUID,
            invoiceId: UUID?,
            memberId: UUID,
            organizationId: UUID,
            branchId: UUID,
            amount: Money,
            reason: String
        ): Refund {
            val now = Instant.now()
            return Refund(
                id = UUID.randomUUID(),
                paymentId = paymentId,
                invoiceId = invoiceId,
                memberId = memberId,
                organizationId = organizationId,
                branchId = branchId,
                amount = amount,
                reason = reason,
                status = RefundStatus.PENDING,
                paymentGatewayRefundId = null,
                paymentGatewayResponse = null,
                creditNoteNumber = null,
                creditNoteFilePath = null,
                processedAt = null,
                failedAt = null,
                failureReason = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun generateCreditNoteNumber(): String {
            val timestamp = Instant.now().toEpochMilli()
            val random = (100000..999999).random()
            return "CN-$timestamp-$random"
        }
    }
}

/**
 * Refund status enumeration
 */
enum class RefundStatus {
    PENDING,
    PROCESSED,
    FAILED
}
