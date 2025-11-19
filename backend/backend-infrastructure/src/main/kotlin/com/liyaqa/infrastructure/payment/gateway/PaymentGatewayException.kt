package com.liyaqa.infrastructure.payment.gateway

/**
 * Base exception for payment gateway errors
 */
sealed class PaymentGatewayException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception thrown when payment processing fails
 */
class PaymentProcessingException(
    message: String,
    val errorCode: String? = null,
    cause: Throwable? = null
) : PaymentGatewayException(message, cause)

/**
 * Exception thrown when refund processing fails
 */
class RefundProcessingException(
    message: String,
    val errorCode: String? = null,
    cause: Throwable? = null
) : PaymentGatewayException(message, cause)

/**
 * Exception thrown when authentication with the gateway fails
 */
class GatewayAuthenticationException(
    message: String,
    cause: Throwable? = null
) : PaymentGatewayException(message, cause)

/**
 * Exception thrown when webhook verification fails
 */
class WebhookVerificationException(
    message: String,
    cause: Throwable? = null
) : PaymentGatewayException(message, cause)

/**
 * Exception thrown when recurring payment setup fails
 */
class RecurringPaymentException(
    message: String,
    val errorCode: String? = null,
    cause: Throwable? = null
) : PaymentGatewayException(message, cause)
