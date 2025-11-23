package com.liyaqa.gym.domain.payment

import java.math.BigDecimal
import java.time.Instant

/**
 * Result of a refund operation
 */
data class RefundResult(
    val success: Boolean,
    val refundId: String?,
    val originalPaymentId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: RefundStatus,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val processedAt: Instant = Instant.now(),
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun success(
            refundId: String,
            originalPaymentId: String,
            amount: BigDecimal,
            currency: String,
            metadata: Map<String, String> = emptyMap()
        ): RefundResult {
            return RefundResult(
                success = true,
                refundId = refundId,
                originalPaymentId = originalPaymentId,
                amount = amount,
                currency = currency,
                status = RefundStatus.COMPLETED,
                metadata = metadata
            )
        }

        fun failure(
            originalPaymentId: String,
            amount: BigDecimal,
            currency: String,
            errorCode: String,
            errorMessage: String
        ): RefundResult {
            return RefundResult(
                success = false,
                refundId = null,
                originalPaymentId = originalPaymentId,
                amount = amount,
                currency = currency,
                status = RefundStatus.FAILED,
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        }

        fun pending(
            refundId: String,
            originalPaymentId: String,
            amount: BigDecimal,
            currency: String,
            metadata: Map<String, String> = emptyMap()
        ): RefundResult {
            return RefundResult(
                success = false,
                refundId = refundId,
                originalPaymentId = originalPaymentId,
                amount = amount,
                currency = currency,
                status = RefundStatus.PENDING,
                metadata = metadata
            )
        }
    }
}

enum class RefundStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}
