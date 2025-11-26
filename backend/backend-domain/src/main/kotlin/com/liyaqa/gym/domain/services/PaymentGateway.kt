package com.liyaqa.gym.domain.services

import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.valueobjects.Money

/**
 * Service interface for processing payments through a payment gateway.
 */
interface PaymentGateway {

    /**
     * Process a payment.
     *
     * @param amount The amount to charge
     * @param paymentMethod The payment method to use
     * @param metadata Additional metadata for the payment
     * @return Result containing payment transaction ID if successful
     */
    suspend fun processPayment(
        amount: Money,
        paymentMethod: PaymentMethod,
        metadata: Map<String, String>
    ): PaymentResult

    /**
     * Create a payment intent for later processing.
     */
    suspend fun createPaymentIntent(
        amount: Money,
        paymentMethod: PaymentMethod
    ): Result<String>

    /**
     * Refund a payment.
     */
    suspend fun refundPayment(
        transactionId: String,
        amount: Money,
        reason: String
    ): Result<String>
}

/**
 * Result of a payment operation.
 */
data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val errorMessage: String?
) {
    companion object {
        fun success(transactionId: String) = PaymentResult(
            success = true,
            transactionId = transactionId,
            errorMessage = null
        )

        fun failure(errorMessage: String) = PaymentResult(
            success = false,
            transactionId = null,
            errorMessage = errorMessage
        )
    }
}
