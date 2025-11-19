package com.liyaqa.infrastructure.payment.gateway

import com.liyaqa.infrastructure.payment.dto.PaymentResult
import com.liyaqa.infrastructure.payment.dto.RecurringPaymentResult
import com.liyaqa.infrastructure.payment.dto.RecurringSchedule
import com.liyaqa.infrastructure.payment.dto.RefundResult
import java.math.BigDecimal

/**
 * Main interface for payment gateway integrations.
 * All payment gateway implementations must implement this interface.
 */
interface PaymentGateway {

    /**
     * Process a one-time payment
     *
     * @param amount The payment amount
     * @param currency The currency code (e.g., "SAR", "USD")
     * @param method The payment method identifier
     * @param metadata Additional metadata for the payment (customer info, order details, etc.)
     * @return PaymentResult containing the transaction details
     */
    fun processPayment(
        amount: BigDecimal,
        currency: String,
        method: String,
        metadata: Map<String, Any> = emptyMap()
    ): PaymentResult

    /**
     * Refund a previously processed payment
     *
     * @param paymentId The gateway payment ID to refund
     * @param amount The amount to refund (can be partial or full)
     * @return RefundResult containing the refund details
     */
    fun refund(paymentId: String, amount: BigDecimal): RefundResult

    /**
     * Verify webhook signature to ensure the webhook came from the payment gateway
     *
     * @param payload The webhook payload as a string
     * @param signature The signature from the webhook headers
     * @return true if the signature is valid, false otherwise
     */
    fun verifyWebhook(payload: String, signature: String): Boolean

    /**
     * Create a recurring payment subscription
     *
     * @param amount The recurring payment amount
     * @param currency The currency code
     * @param schedule The recurring schedule (monthly, yearly, etc.)
     * @param metadata Additional metadata (customer info, subscription details, etc.)
     * @return RecurringPaymentResult containing the subscription details
     */
    fun createRecurringPayment(
        amount: BigDecimal,
        currency: String,
        schedule: RecurringSchedule,
        metadata: Map<String, Any> = emptyMap()
    ): RecurringPaymentResult

    /**
     * Get the name of this payment gateway (for logging and routing)
     */
    fun getGatewayName(): String

    /**
     * Check if this gateway supports the given payment method
     */
    fun supportsPaymentMethod(method: String): Boolean
}
