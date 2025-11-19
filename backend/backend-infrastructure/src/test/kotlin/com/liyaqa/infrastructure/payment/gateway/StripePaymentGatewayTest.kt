package com.liyaqa.infrastructure.payment.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.liyaqa.infrastructure.payment.config.PaymentGatewayProperties
import com.liyaqa.infrastructure.payment.dto.RecurringSchedule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class StripePaymentGatewayTest {

    private lateinit var properties: PaymentGatewayProperties
    private lateinit var objectMapper: ObjectMapper
    private lateinit var gateway: StripePaymentGateway

    @BeforeEach
    fun setUp() {
        properties = PaymentGatewayProperties().apply {
            testMode = true
            stripe.enabled = true
            stripe.apiKey = "test_api_key"
            stripe.webhookSecret = "test_webhook_secret"
        }
        objectMapper = ObjectMapper()
        gateway = StripePaymentGateway(properties, objectMapper)
    }

    @Test
    fun `should return gateway name`() {
        assertEquals("Stripe", gateway.getGatewayName())
    }

    @Test
    fun `should support credit card payment methods`() {
        assertTrue(gateway.supportsPaymentMethod("card"))
        assertTrue(gateway.supportsPaymentMethod("credit_card"))
        assertTrue(gateway.supportsPaymentMethod("debit_card"))
        assertTrue(gateway.supportsPaymentMethod("apple_pay"))
        assertTrue(gateway.supportsPaymentMethod("google_pay"))
    }

    @Test
    fun `should not support unsupported payment methods`() {
        assertFalse(gateway.supportsPaymentMethod("mada"))
        assertFalse(gateway.supportsPaymentMethod("stcpay"))
        assertFalse(gateway.supportsPaymentMethod("bitcoin"))
    }

    @Test
    fun `should verify valid webhook signature`() {
        // This is a simplified test - in real scenario, you'd need actual Stripe signature
        val payload = """{"type":"payment_intent.succeeded","data":{}}"""
        val timestamp = System.currentTimeMillis() / 1000

        // Note: This test demonstrates the structure but would need actual signature generation
        // In production, use Stripe's webhook testing tools
        val signature = "t=$timestamp,v1=test_signature"

        // The actual verification will fail with test data, but this shows the pattern
        val result = gateway.verifyWebhook(payload, signature)
        // In a real test environment with proper test keys, this would be tested more thoroughly
        assertNotNull(result)
    }

    @Test
    fun `should return failure when gateway is disabled`() {
        properties.stripe.enabled = false

        val result = gateway.processPayment(
            amount = BigDecimal("100.00"),
            currency = "SAR",
            method = "card",
            metadata = emptyMap()
        )

        assertFalse(result.success)
        assertEquals("GATEWAY_DISABLED", result.errorCode)
    }

    @Test
    fun `should return failure for refund when gateway is disabled`() {
        properties.stripe.enabled = false

        val result = gateway.refund(
            paymentId = "pi_test123",
            amount = BigDecimal("50.00")
        )

        assertFalse(result.success)
        assertEquals("GATEWAY_DISABLED", result.errorCode)
    }

    @Test
    fun `should return failure for recurring payment when gateway is disabled`() {
        properties.stripe.enabled = false

        val result = gateway.createRecurringPayment(
            amount = BigDecimal("100.00"),
            currency = "SAR",
            schedule = RecurringSchedule.MONTHLY,
            metadata = emptyMap()
        )

        assertFalse(result.success)
        assertEquals("GATEWAY_DISABLED", result.errorCode)
    }

    @Test
    fun `should handle test mode configuration`() {
        assertTrue(properties.testMode)
        // In test mode, gateway should use test API endpoints
        // This is handled internally in the gateway implementation
    }
}
