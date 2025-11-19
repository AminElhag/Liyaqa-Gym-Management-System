package com.liyaqa.infrastructure.payment.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.liyaqa.infrastructure.payment.config.PaymentGatewayProperties
import com.liyaqa.infrastructure.payment.dto.*
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Mada payment gateway implementation
 *
 * Mada is the Saudi national debit card scheme. Integration is typically done through
 * payment processors like HyperPay (OPPWA) or PayTabs.
 * This implementation uses HyperPay's REST API.
 *
 * Features:
 * - Payment processing for Mada cards
 * - Refund handling
 * - Webhook signature verification
 * - Recurring payment support
 * - Retry logic with exponential backoff
 * - Test mode support
 */
@Component
class MadaPaymentGateway(
    private val properties: PaymentGatewayProperties,
    private val objectMapper: ObjectMapper
) : PaymentGateway {

    private val logger = LoggerFactory.getLogger(MadaPaymentGateway::class.java)

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(properties.mada.connectTimeout))
        .build()

    private val baseUrl = if (properties.testMode) {
        properties.mada.testGatewayUrl
    } else {
        properties.mada.gatewayUrl
    }

    companion object {
        private const val GATEWAY_NAME = "Mada"
        private const val HMAC_SHA256 = "HmacSHA256"
    }

    /**
     * Process a payment through Mada (via HyperPay)
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    override fun processPayment(
        amount: BigDecimal,
        currency: String,
        method: String,
        metadata: Map<String, Any>
    ): PaymentResult {
        if (!properties.mada.enabled) {
            logger.warn("Mada gateway is disabled")
            return PaymentResult.failure(
                amount, currency,
                "GATEWAY_DISABLED",
                "Mada payment gateway is currently disabled"
            )
        }

        try {
            logger.info("Processing Mada payment: amount={}, currency={}, method={}", amount, currency, method)

            // Step 1: Create checkout
            val checkoutId = createCheckout(amount, currency, metadata)

            // Step 2: Process payment with checkout ID
            val paymentResult = processPaymentWithCheckout(checkoutId, method, metadata)

            logger.info("Mada payment processed: checkoutId={}, success={}", checkoutId, paymentResult.success)

            return paymentResult

        } catch (e: PaymentProcessingException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process Mada payment", e)
            throw PaymentProcessingException("Failed to process payment: ${e.message}", cause = e)
        }
    }

    /**
     * Create a checkout session (Step 1 of HyperPay payment flow)
     */
    private fun createCheckout(
        amount: BigDecimal,
        currency: String,
        metadata: Map<String, Any>
    ): String {
        val requestBody = buildFormData(mapOf(
            "entityId" to properties.mada.entityId,
            "amount" to amount.toString(),
            "currency" to currency,
            "paymentType" to "DB", // Debit transaction
            "merchantTransactionId" to (metadata["invoiceNumber"]?.toString() ?: UUID.randomUUID().toString()),
            "customer.email" to (metadata["customerEmail"]?.toString() ?: ""),
            "customer.givenName" to (metadata["customerName"]?.toString() ?: ""),
            "billing.country" to "SA"
        ))

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/checkouts"))
            .header("Authorization", "Bearer ${properties.mada.apiKey}")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofMillis(properties.mada.readTimeout))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val jsonResponse = objectMapper.readTree(response.body())

        if (response.statusCode() == 200) {
            val checkoutId = jsonResponse.get("id")?.asText()
            val resultCode = jsonResponse.get("result")?.get("code")?.asText()

            // Check if checkout was created successfully
            // HyperPay uses result codes: ^(000\.200|000\.100\.1)
            if (checkoutId != null && resultCode?.matches(Regex("^(000\\.200|000\\.100\\.1).*")) == true) {
                logger.debug("Mada checkout created: checkoutId={}", checkoutId)
                return checkoutId
            } else {
                val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Failed to create checkout"
                logger.error("Mada checkout creation failed: code={}, message={}", resultCode, errorMessage)
                throw PaymentProcessingException(errorMessage, resultCode)
            }
        } else {
            val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Unknown error"
            throw PaymentProcessingException(errorMessage)
        }
    }

    /**
     * Process payment with checkout ID (Step 2 of HyperPay payment flow)
     */
    private fun processPaymentWithCheckout(
        checkoutId: String,
        method: String,
        metadata: Map<String, Any>
    ): PaymentResult {
        // In a real implementation, this would involve tokenizing the card
        // and sending the payment details. For now, we'll query the payment status.

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/checkouts/$checkoutId/payment"))
            .header("Authorization", "Bearer ${properties.mada.apiKey}")
            .timeout(Duration.ofMillis(properties.mada.readTimeout))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val jsonResponse = objectMapper.readTree(response.body())

        if (response.statusCode() == 200) {
            val resultCode = jsonResponse.get("result")?.get("code")?.asText()
            val amount = jsonResponse.get("amount")?.asText()?.let { BigDecimal(it) } ?: BigDecimal.ZERO
            val currency = jsonResponse.get("currency")?.asText() ?: "SAR"
            val paymentId = jsonResponse.get("id")?.asText() ?: checkoutId

            // Success codes: ^(000\.000\.|000\.100\.1|000\.[36])
            return if (resultCode?.matches(Regex("^(000\\.000\\.|000\\.100\\.1|000\\.[36]).*")) == true) {
                PaymentResult.success(
                    transactionId = UUID.randomUUID().toString(),
                    gatewayPaymentId = paymentId,
                    amount = amount,
                    currency = currency,
                    metadata = mapOf(
                        "gateway" to GATEWAY_NAME,
                        "checkoutId" to checkoutId,
                        "resultCode" to resultCode
                    )
                )
            } else {
                val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Payment failed"
                PaymentResult.failure(
                    amount = amount,
                    currency = currency,
                    errorCode = resultCode ?: "UNKNOWN",
                    errorMessage = errorMessage,
                    transactionId = checkoutId
                )
            }
        } else {
            val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Unknown error"
            throw PaymentProcessingException(errorMessage)
        }
    }

    /**
     * Refund a Mada payment
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    override fun refund(paymentId: String, amount: BigDecimal): RefundResult {
        if (!properties.mada.enabled) {
            logger.warn("Mada gateway is disabled")
            return RefundResult.failure(
                originalPaymentId = paymentId,
                amount = amount,
                currency = "SAR",
                errorCode = "GATEWAY_DISABLED",
                errorMessage = "Mada payment gateway is currently disabled"
            )
        }

        try {
            logger.info("Processing Mada refund: paymentId={}, amount={}", paymentId, amount)

            val requestBody = buildFormData(mapOf(
                "entityId" to properties.mada.entityId,
                "amount" to amount.toString(),
                "currency" to "SAR",
                "paymentType" to "RF" // Refund transaction
            ))

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/payments/$paymentId"))
                .header("Authorization", "Bearer ${properties.mada.apiKey}")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofMillis(properties.mada.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val jsonResponse = objectMapper.readTree(response.body())

            if (response.statusCode() == 200) {
                val resultCode = jsonResponse.get("result")?.get("code")?.asText()
                val refundId = jsonResponse.get("id")?.asText() ?: UUID.randomUUID().toString()

                // Success codes for refund: ^(000\.000\.|000\.100\.1|000\.[36])
                return if (resultCode?.matches(Regex("^(000\\.000\\.|000\\.100\\.1|000\\.[36]).*")) == true) {
                    logger.info("Mada refund succeeded: refundId={}", refundId)
                    RefundResult.success(
                        refundId = refundId,
                        originalPaymentId = paymentId,
                        amount = amount,
                        currency = "SAR",
                        metadata = mapOf(
                            "gateway" to GATEWAY_NAME,
                            "resultCode" to resultCode
                        )
                    )
                } else {
                    val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Refund failed"
                    logger.error("Mada refund failed: code={}, message={}", resultCode, errorMessage)
                    throw RefundProcessingException(errorMessage, resultCode)
                }
            } else {
                val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Unknown error"
                throw RefundProcessingException(errorMessage)
            }
        } catch (e: RefundProcessingException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process Mada refund", e)
            throw RefundProcessingException("Failed to process refund: ${e.message}", cause = e)
        }
    }

    /**
     * Verify Mada webhook signature
     */
    override fun verifyWebhook(payload: String, signature: String): Boolean {
        try {
            logger.debug("Verifying Mada webhook signature")

            // Compute HMAC-SHA256
            val mac = Mac.getInstance(HMAC_SHA256)
            val secretKey = SecretKeySpec(properties.mada.webhookSecret.toByteArray(), HMAC_SHA256)
            mac.init(secretKey)
            val computedSignature = mac.doFinal(payload.toByteArray())
            val computedHex = computedSignature.joinToString("") { "%02x".format(it) }

            val isValid = computedHex.equals(signature, ignoreCase = true)

            if (!isValid) {
                logger.warn("Mada webhook signature verification failed")
            } else {
                logger.debug("Mada webhook signature verified successfully")
            }

            return isValid
        } catch (e: Exception) {
            logger.error("Error verifying Mada webhook signature", e)
            return false
        }
    }

    /**
     * Create recurring payment (subscription) for Mada
     * Note: Recurring payments require PCI DSS Level 1 compliance and card tokenization
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    override fun createRecurringPayment(
        amount: BigDecimal,
        currency: String,
        schedule: RecurringSchedule,
        metadata: Map<String, Any>
    ): RecurringPaymentResult {
        if (!properties.mada.enabled) {
            logger.warn("Mada gateway is disabled")
            return RecurringPaymentResult.failure(
                amount = amount,
                currency = currency,
                schedule = schedule,
                errorCode = "GATEWAY_DISABLED",
                errorMessage = "Mada payment gateway is currently disabled"
            )
        }

        try {
            logger.info("Creating Mada recurring payment: amount={}, currency={}, schedule={}", amount, currency, schedule)

            // For recurring payments, we need to register the card first
            val registrationId = metadata["cardRegistrationId"]?.toString()
                ?: throw RecurringPaymentException("Card registration ID is required for recurring payments")

            // Create a standing instruction
            val requestBody = buildFormData(mapOf(
                "entityId" to properties.mada.entityId,
                "amount" to amount.toString(),
                "currency" to currency,
                "paymentType" to "DB",
                "recurringType" to "REPEATED",
                "registrationId" to registrationId,
                "merchantTransactionId" to UUID.randomUUID().toString(),
                "standingInstruction.type" to "RECURRING",
                "standingInstruction.mode" to "INITIAL",
                "standingInstruction.source" to "CIT" // Customer Initiated Transaction
            ))

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/registrations/$registrationId/payments"))
                .header("Authorization", "Bearer ${properties.mada.apiKey}")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofMillis(properties.mada.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val jsonResponse = objectMapper.readTree(response.body())

            if (response.statusCode() == 200) {
                val resultCode = jsonResponse.get("result")?.get("code")?.asText()
                val paymentId = jsonResponse.get("id")?.asText() ?: UUID.randomUUID().toString()

                return if (resultCode?.matches(Regex("^(000\\.000\\.|000\\.100\\.1|000\\.[36]).*")) == true) {
                    logger.info("Mada recurring payment created: paymentId={}", paymentId)

                    // Calculate next payment date based on schedule
                    val nextPaymentDate = calculateNextPaymentDate(schedule)

                    RecurringPaymentResult.success(
                        subscriptionId = paymentId,
                        customerId = registrationId,
                        amount = amount,
                        currency = currency,
                        schedule = schedule,
                        nextPaymentDate = nextPaymentDate,
                        metadata = mapOf(
                            "gateway" to GATEWAY_NAME,
                            "resultCode" to resultCode,
                            "registrationId" to registrationId
                        )
                    )
                } else {
                    val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Failed to create recurring payment"
                    logger.error("Mada recurring payment failed: code={}, message={}", resultCode, errorMessage)
                    throw RecurringPaymentException(errorMessage, resultCode)
                }
            } else {
                val errorMessage = jsonResponse.get("result")?.get("description")?.asText() ?: "Unknown error"
                throw RecurringPaymentException(errorMessage)
            }
        } catch (e: RecurringPaymentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to create Mada recurring payment", e)
            throw RecurringPaymentException("Failed to create recurring payment: ${e.message}", cause = e)
        }
    }

    override fun getGatewayName(): String = GATEWAY_NAME

    override fun supportsPaymentMethod(method: String): Boolean {
        return method.lowercase() in listOf("mada", "mada_debit", "debit_card")
    }

    /**
     * Build form-urlencoded request body
     */
    private fun buildFormData(params: Map<String, String>): String {
        return params.entries
            .filter { it.value.isNotEmpty() }
            .joinToString("&") { "${it.key}=${java.net.URLEncoder.encode(it.value, "UTF-8")}" }
    }

    /**
     * Calculate next payment date based on schedule
     */
    private fun calculateNextPaymentDate(schedule: RecurringSchedule): Instant {
        val now = Instant.now()
        return when (schedule) {
            RecurringSchedule.DAILY -> now.plus(Duration.ofDays(1))
            RecurringSchedule.WEEKLY -> now.plus(Duration.ofDays(7))
            RecurringSchedule.MONTHLY -> now.plus(Duration.ofDays(30))
            RecurringSchedule.QUARTERLY -> now.plus(Duration.ofDays(90))
            RecurringSchedule.YEARLY -> now.plus(Duration.ofDays(365))
        }
    }
}
