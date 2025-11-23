package com.liyaqa.infrastructure.payment.gateway

import com.liyaqa.gym.domain.payment.PaymentGateway
import com.liyaqa.gym.domain.payment.PaymentGatewayFactory
import com.liyaqa.infrastructure.payment.config.PaymentGatewayProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Infrastructure implementation of PaymentGatewayFactory
 *
 * Features:
 * - Routes payment requests to the correct gateway
 * - Supports fallback gateway if primary fails
 * - Handles gateway priority and availability
 * - Comprehensive logging
 */
@Component
class PaymentGatewayFactoryImpl(
    private val stripeGateway: StripePaymentGateway,
    private val madaGateway: MadaPaymentGateway,
    private val stcPayGateway: STCPayPaymentGateway,
    private val properties: PaymentGatewayProperties
) : PaymentGatewayFactory {

    private val logger = LoggerFactory.getLogger(PaymentGatewayFactoryImpl::class.java)

    /**
     * Get the appropriate payment gateway for the given payment method
     *
     * @param method The payment method (e.g., "mada", "stcpay", "credit_card")
     * @return PaymentGateway instance that supports the payment method
     * @throws IllegalArgumentException if no gateway supports the payment method
     */
    override fun getGateway(method: String): PaymentGateway {
        val normalizedMethod = method.lowercase().trim()

        logger.debug("Selecting payment gateway for method: {}", normalizedMethod)

        // Route to specific gateway based on payment method
        val gateway = when {
            // STC Pay - Saudi digital wallet
            normalizedMethod in listOf("stcpay", "stc_pay", "wallet") -> {
                if (properties.stcPay.enabled) {
                    logger.info("Selected STC Pay gateway for method: {}", method)
                    stcPayGateway
                } else {
                    logger.warn("STC Pay gateway is disabled, trying fallback")
                    getFallbackGateway(normalizedMethod)
                }
            }

            // Mada - Saudi debit cards
            normalizedMethod in listOf("mada", "mada_debit", "debit_card") -> {
                if (properties.mada.enabled) {
                    logger.info("Selected Mada gateway for method: {}", method)
                    madaGateway
                } else {
                    logger.warn("Mada gateway is disabled, trying fallback")
                    getFallbackGateway(normalizedMethod)
                }
            }

            // Credit cards, Apple Pay, Google Pay - Stripe
            normalizedMethod in listOf("card", "credit_card", "apple_pay", "google_pay") -> {
                if (properties.stripe.enabled) {
                    logger.info("Selected Stripe gateway for method: {}", method)
                    stripeGateway
                } else {
                    logger.warn("Stripe gateway is disabled, trying fallback")
                    getFallbackGateway(normalizedMethod)
                }
            }

            // Try to find any gateway that supports this method
            else -> {
                logger.debug("No specific gateway matched, searching for compatible gateway")
                findCompatibleGateway(normalizedMethod)
                    ?: throw IllegalArgumentException("No payment gateway supports method: $method")
            }
        }

        return gateway
    }

    /**
     * Get all available payment gateways
     */
    override fun getAllGateways(): List<PaymentGateway> {
        return listOf(stripeGateway, madaGateway, stcPayGateway)
            .filter { isGatewayEnabled(it) }
    }

    /**
     * Get gateway by name
     */
    override fun getGatewayByName(name: String): PaymentGateway? {
        return when (name.lowercase()) {
            "stripe" -> if (properties.stripe.enabled) stripeGateway else null
            "mada" -> if (properties.mada.enabled) madaGateway else null
            "stcpay", "stc_pay" -> if (properties.stcPay.enabled) stcPayGateway else null
            else -> null
        }
    }

    /**
     * Check if a payment method is supported by any gateway
     */
    override fun isPaymentMethodSupported(method: String): Boolean {
        return getAllGateways().any { it.supportsPaymentMethod(method) }
    }

    /**
     * Get fallback gateway when primary gateway is unavailable
     * Priority: Stripe > Mada > STC Pay
     */
    private fun getFallbackGateway(method: String): PaymentGateway {
        logger.debug("Looking for fallback gateway for method: {}", method)

        // Try in order of preference
        val fallbacks = listOf(
            stripeGateway to properties.stripe.enabled,
            madaGateway to properties.mada.enabled,
            stcPayGateway to properties.stcPay.enabled
        )

        for ((gateway, enabled) in fallbacks) {
            if (enabled && gateway.supportsPaymentMethod(method)) {
                logger.info("Using {} as fallback gateway for method: {}", gateway.getGatewayName(), method)
                return gateway
            }
        }

        throw IllegalStateException("No fallback gateway available for method: $method")
    }

    /**
     * Find a compatible gateway for the given payment method
     */
    private fun findCompatibleGateway(method: String): PaymentGateway? {
        return getAllGateways().firstOrNull { gateway ->
            gateway.supportsPaymentMethod(method).also { supported ->
                if (supported) {
                    logger.info("Found compatible gateway {} for method: {}", gateway.getGatewayName(), method)
                }
            }
        }
    }

    /**
     * Check if gateway is enabled in configuration
     */
    private fun isGatewayEnabled(gateway: PaymentGateway): Boolean {
        return when (gateway) {
            is StripePaymentGateway -> properties.stripe.enabled
            is MadaPaymentGateway -> properties.mada.enabled
            is STCPayPaymentGateway -> properties.stcPay.enabled
            else -> false
        }
    }

    /**
     * Get supported payment methods across all enabled gateways
     */
    override fun getSupportedPaymentMethods(): List<String> {
        val methods = mutableSetOf<String>()

        if (properties.stripe.enabled) {
            methods.addAll(listOf("card", "credit_card", "debit_card", "apple_pay", "google_pay"))
        }

        if (properties.mada.enabled) {
            methods.addAll(listOf("mada", "mada_debit"))
        }

        if (properties.stcPay.enabled) {
            methods.addAll(listOf("stcpay", "stc_pay", "wallet"))
        }

        return methods.sorted()
    }

    /**
     * Get gateway statistics (for monitoring/admin purposes)
     */
    fun getGatewayStats(): Map<String, Any> {
        return mapOf(
            "totalGateways" to 3,
            "enabledGateways" to getAllGateways().size,
            "supportedMethods" to getSupportedPaymentMethods(),
            "gateways" to listOf(
                mapOf(
                    "name" to "Stripe",
                    "enabled" to properties.stripe.enabled,
                    "testMode" to properties.testMode
                ),
                mapOf(
                    "name" to "Mada",
                    "enabled" to properties.mada.enabled,
                    "testMode" to properties.testMode
                ),
                mapOf(
                    "name" to "STC Pay",
                    "enabled" to properties.stcPay.enabled,
                    "testMode" to properties.testMode
                )
            )
        )
    }
}
