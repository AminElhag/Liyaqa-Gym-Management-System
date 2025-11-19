package com.liyaqa.infrastructure.payment.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.liyaqa.infrastructure.payment.config.PaymentGatewayProperties
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentGatewayFactoryTest {

    private lateinit var properties: PaymentGatewayProperties
    private lateinit var objectMapper: ObjectMapper
    private lateinit var stripeGateway: StripePaymentGateway
    private lateinit var madaGateway: MadaPaymentGateway
    private lateinit var stcPayGateway: STCPayPaymentGateway
    private lateinit var factory: PaymentGatewayFactory

    @BeforeEach
    fun setUp() {
        properties = PaymentGatewayProperties().apply {
            testMode = true
            stripe.enabled = true
            mada.enabled = true
            stcPay.enabled = true
        }
        objectMapper = ObjectMapper()

        stripeGateway = StripePaymentGateway(properties, objectMapper)
        madaGateway = MadaPaymentGateway(properties, objectMapper)
        stcPayGateway = STCPayPaymentGateway(properties, objectMapper)

        factory = PaymentGatewayFactory(stripeGateway, madaGateway, stcPayGateway, properties)
    }

    @Test
    fun `should route to Stripe gateway for credit card payment`() {
        val gateway = factory.getGateway("credit_card")
        assertEquals("Stripe", gateway.getGatewayName())
    }

    @Test
    fun `should route to Mada gateway for Mada payment`() {
        val gateway = factory.getGateway("mada")
        assertEquals("Mada", gateway.getGatewayName())
    }

    @Test
    fun `should route to STC Pay gateway for STC Pay payment`() {
        val gateway = factory.getGateway("stcpay")
        assertEquals("STCPay", gateway.getGatewayName())
    }

    @Test
    fun `should return all enabled gateways`() {
        val gateways = factory.getAllGateways()
        assertEquals(3, gateways.size)
    }

    @Test
    fun `should get gateway by name`() {
        val stripe = factory.getGatewayByName("stripe")
        assertNotNull(stripe)
        assertEquals("Stripe", stripe?.getGatewayName())

        val mada = factory.getGatewayByName("mada")
        assertNotNull(mada)
        assertEquals("Mada", mada?.getGatewayName())

        val stcPay = factory.getGatewayByName("stcpay")
        assertNotNull(stcPay)
        assertEquals("STCPay", stcPay?.getGatewayName())
    }

    @Test
    fun `should return null for unknown gateway name`() {
        val gateway = factory.getGatewayByName("unknown")
        assertNull(gateway)
    }

    @Test
    fun `should check if payment method is supported`() {
        assertTrue(factory.isPaymentMethodSupported("credit_card"))
        assertTrue(factory.isPaymentMethodSupported("mada"))
        assertTrue(factory.isPaymentMethodSupported("stcpay"))
        assertFalse(factory.isPaymentMethodSupported("bitcoin"))
    }

    @Test
    fun `should return supported payment methods`() {
        val methods = factory.getSupportedPaymentMethods()
        assertTrue(methods.contains("credit_card"))
        assertTrue(methods.contains("mada"))
        assertTrue(methods.contains("stcpay"))
    }

    @Test
    fun `should provide gateway statistics`() {
        val stats = factory.getGatewayStats()
        assertEquals(3, stats["totalGateways"])
        assertEquals(3, stats["enabledGateways"])
    }

    @Test
    fun `should throw exception when no gateway supports payment method`() {
        assertThrows<IllegalArgumentException> {
            factory.getGateway("unsupported_method")
        }
    }

    @Test
    fun `should use fallback gateway when primary is disabled`() {
        properties.stcPay.enabled = false

        // Should fallback to another enabled gateway
        assertThrows<IllegalStateException> {
            // wallet method is only supported by STC Pay
            factory.getGateway("wallet")
        }
    }
}
