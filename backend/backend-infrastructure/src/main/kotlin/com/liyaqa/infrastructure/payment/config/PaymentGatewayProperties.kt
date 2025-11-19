package com.liyaqa.infrastructure.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for payment gateways
 */
@Configuration
@ConfigurationProperties(prefix = "payment")
class PaymentGatewayProperties {
    var testMode: Boolean = true
    var stripe = StripeProperties()
    var mada = MadaProperties()
    var stcPay = STCPayProperties()
}

/**
 * Stripe payment gateway configuration
 */
class StripeProperties {
    var enabled: Boolean = true
    var apiKey: String = ""
    var webhookSecret: String = ""
    var apiVersion: String = "2023-10-16"
    var connectTimeout: Long = 30000
    var readTimeout: Long = 80000
}

/**
 * Mada (Saudi debit card) payment gateway configuration
 * Mada is typically integrated through payment processors like HyperPay or PayTabs
 */
class MadaProperties {
    var enabled: Boolean = true
    var merchantId: String = ""
    var apiKey: String = ""
    var webhookSecret: String = ""
    var gatewayUrl: String = "https://api.hyperpay.com/v1"
    var testGatewayUrl: String = "https://test.oppwa.com/v1"
    var entityId: String = ""
    var connectTimeout: Long = 30000
    var readTimeout: Long = 80000
}

/**
 * STC Pay (Saudi digital wallet) payment gateway configuration
 */
class STCPayProperties {
    var enabled: Boolean = true
    var merchantId: String = ""
    var apiKey: String = ""
    var apiSecret: String = ""
    var webhookSecret: String = ""
    var gatewayUrl: String = "https://api.stcpay.com.sa/v1"
    var testGatewayUrl: String = "https://sandbox.stcpay.com.sa/v1"
    var connectTimeout: Long = 30000
    var readTimeout: Long = 80000
}
