package com.liyaqa.infrastructure.payment.dto

import java.math.BigDecimal
import java.time.Instant

/**
 * Result of a payment processing operation
 */
data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val gatewayPaymentId: String?,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentTransactionStatus,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val processedAt: Instant = Instant.now(),
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun success(
            transactionId: String,
            gatewayPaymentId: String,
            amount: BigDecimal,
            currency: String,
            metadata: Map<String, String> = emptyMap()
        ): PaymentResult {
            return PaymentResult(
                success = true,
                transactionId = transactionId,
                gatewayPaymentId = gatewayPaymentId,
                amount = amount,
                currency = currency,
                status = PaymentTransactionStatus.COMPLETED,
                metadata = metadata
            )
        }

        fun failure(
            amount: BigDecimal,
            currency: String,
            errorCode: String,
            errorMessage: String,
            transactionId: String? = null
        ): PaymentResult {
            return PaymentResult(
                success = false,
                transactionId = transactionId,
                gatewayPaymentId = null,
                amount = amount,
                currency = currency,
                status = PaymentTransactionStatus.FAILED,
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        }

        fun pending(
            transactionId: String,
            gatewayPaymentId: String,
            amount: BigDecimal,
            currency: String,
            metadata: Map<String, String> = emptyMap()
        ): PaymentResult {
            return PaymentResult(
                success = false,
                transactionId = transactionId,
                gatewayPaymentId = gatewayPaymentId,
                amount = amount,
                currency = currency,
                status = PaymentTransactionStatus.PENDING,
                metadata = metadata
            )
        }
    }
}

enum class PaymentTransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REQUIRES_ACTION
}
