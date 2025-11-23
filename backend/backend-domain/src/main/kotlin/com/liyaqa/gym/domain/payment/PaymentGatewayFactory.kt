package com.liyaqa.gym.domain.payment

/**
 * Factory interface for selecting the appropriate payment gateway
 *
 * This interface defines the contract for payment gateway selection and management.
 * Infrastructure layer will provide the concrete implementation.
 */
interface PaymentGatewayFactory {

    /**
     * Get the appropriate payment gateway for the given payment method
     *
     * @param method The payment method (e.g., "mada", "stcpay", "credit_card")
     * @return PaymentGateway instance that supports the payment method
     * @throws IllegalArgumentException if no gateway supports the payment method
     */
    fun getGateway(method: String): PaymentGateway

    /**
     * Get all available payment gateways
     */
    fun getAllGateways(): List<PaymentGateway>

    /**
     * Get gateway by name
     */
    fun getGatewayByName(name: String): PaymentGateway?

    /**
     * Check if a payment method is supported by any gateway
     */
    fun isPaymentMethodSupported(method: String): Boolean

    /**
     * Get supported payment methods across all enabled gateways
     */
    fun getSupportedPaymentMethods(): List<String>
}
