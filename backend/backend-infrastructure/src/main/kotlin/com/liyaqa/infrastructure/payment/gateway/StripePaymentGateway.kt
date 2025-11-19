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
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Stripe payment gateway implementation
 *
 * Features:
 * - Payment processing via Stripe API
 * - Refund handling
 * - Webhook signature verification
 * - Recurring payment subscriptions
 * - Retry logic with exponential backoff
 * - Comprehensive logging
 */
@Component
class StripePaymentGateway(
    private val properties: PaymentGatewayProperties,
    private val objectMapper: ObjectMapper
) : PaymentGateway {

    private val logger = LoggerFactory.getLogger(StripePaymentGateway::class.java)

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(properties.stripe.connectTimeout))
        .build()

    private val baseUrl = if (properties.testMode) {
        "https://api.stripe.com/v1"
    } else {
        "https://api.stripe.com/v1"
    }

    companion object {
        private const val GATEWAY_NAME = "Stripe"
        private const val HMAC_SHA256 = "HmacSHA256"
    }

    /**
     * Process a payment through Stripe
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
        if (!properties.stripe.enabled) {
            logger.warn("Stripe gateway is disabled")
            return PaymentResult.failure(
                amount, currency,
                "GATEWAY_DISABLED",
                "Stripe payment gateway is currently disabled"
            )
        }

        try {
            logger.info("Processing Stripe payment: amount={}, currency={}, method={}", amount, currency, method)

            // Convert amount to smallest currency unit (cents for most currencies)
            val amountInCents = (amount * BigDecimal(100)).toLong()

            // Build request body
            val requestBody = buildFormData(mapOf(
                "amount" to amountInCents.toString(),
                "currency" to currency.lowercase(),
                "payment_method" to method,
                "confirm" to "true",
                "description" to (metadata["description"] ?: "Gym membership payment"),
                "metadata[customer_id]" to (metadata["customerId"] ?: ""),
                "metadata[member_id]" to (metadata["memberId"] ?: ""),
                "metadata[invoice_number]" to (metadata["invoiceNumber"] ?: "")
            ))

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/payment_intents"))
                .header("Authorization", "Bearer ${properties.stripe.apiKey}")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Stripe-Version", properties.stripe.apiVersion)
                .timeout(Duration.ofMillis(properties.stripe.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            logger.debug("Stripe API response: status={}, body={}", response.statusCode(), response.body())

            if (response.statusCode() == 200) {
                val jsonResponse = objectMapper.readTree(response.body())
                val paymentIntentId = jsonResponse.get("id").asText()
                val status = jsonResponse.get("status").asText()

                return when (status) {
                    "succeeded" -> {
                        logger.info("Stripe payment succeeded: paymentIntentId={}", paymentIntentId)
                        PaymentResult.success(
                            transactionId = UUID.randomUUID().toString(),
                            gatewayPaymentId = paymentIntentId,
                            amount = amount,
                            currency = currency,
                            metadata = mapOf(
                                "gateway" to GATEWAY_NAME,
                                "status" to status
                            )
                        )
                    }
                    "requires_action", "requires_payment_method" -> {
                        logger.warn("Stripe payment requires action: paymentIntentId={}, status={}", paymentIntentId, status)
                        PaymentResult.pending(
                            transactionId = UUID.randomUUID().toString(),
                            gatewayPaymentId = paymentIntentId,
                            amount = amount,
                            currency = currency,
                            metadata = mapOf(
                                "gateway" to GATEWAY_NAME,
                                "status" to status,
                                "client_secret" to (jsonResponse.get("client_secret")?.asText() ?: "")
                            )
                        )
                    }
                    else -> {
                        logger.error("Stripe payment failed: paymentIntentId={}, status={}", paymentIntentId, status)
                        PaymentResult.failure(
                            amount = amount,
                            currency = currency,
                            errorCode = "PAYMENT_FAILED",
                            errorMessage = "Payment status: $status",
                            transactionId = paymentIntentId
                        )
                    }
                }
            } else {
                val errorResponse = objectMapper.readTree(response.body())
                val errorMessage = errorResponse.get("error")?.get("message")?.asText() ?: "Unknown error"
                val errorCode = errorResponse.get("error")?.get("code")?.asText() ?: "UNKNOWN"

                logger.error("Stripe API error: code={}, message={}", errorCode, errorMessage)
                throw PaymentProcessingException(errorMessage, errorCode)
            }
        } catch (e: PaymentProcessingException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process Stripe payment", e)
            throw PaymentProcessingException("Failed to process payment: ${e.message}", cause = e)
        }
    }

    /**
     * Refund a Stripe payment
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    override fun refund(paymentId: String, amount: BigDecimal): RefundResult {
        if (!properties.stripe.enabled) {
            logger.warn("Stripe gateway is disabled")
            return RefundResult.failure(
                originalPaymentId = paymentId,
                amount = amount,
                currency = "SAR",
                errorCode = "GATEWAY_DISABLED",
                errorMessage = "Stripe payment gateway is currently disabled"
            )
        }

        try {
            logger.info("Processing Stripe refund: paymentId={}, amount={}", paymentId, amount)

            val amountInCents = (amount * BigDecimal(100)).toLong()

            val requestBody = buildFormData(mapOf(
                "payment_intent" to paymentId,
                "amount" to amountInCents.toString()
            ))

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/refunds"))
                .header("Authorization", "Bearer ${properties.stripe.apiKey}")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Stripe-Version", properties.stripe.apiVersion)
                .timeout(Duration.ofMillis(properties.stripe.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                val jsonResponse = objectMapper.readTree(response.body())
                val refundId = jsonResponse.get("id").asText()
                val status = jsonResponse.get("status").asText()
                val refundedAmount = jsonResponse.get("amount").asLong()
                val currency = jsonResponse.get("currency").asText()

                logger.info("Stripe refund succeeded: refundId={}, status={}", refundId, status)

                return RefundResult.success(
                    refundId = refundId,
                    originalPaymentId = paymentId,
                    amount = BigDecimal(refundedAmount).divide(BigDecimal(100)),
                    currency = currency.uppercase(),
                    metadata = mapOf(
                        "gateway" to GATEWAY_NAME,
                        "status" to status
                    )
                )
            } else {
                val errorResponse = objectMapper.readTree(response.body())
                val errorMessage = errorResponse.get("error")?.get("message")?.asText() ?: "Unknown error"
                val errorCode = errorResponse.get("error")?.get("code")?.asText() ?: "UNKNOWN"

                logger.error("Stripe refund failed: code={}, message={}", errorCode, errorMessage)
                throw RefundProcessingException(errorMessage, errorCode)
            }
        } catch (e: RefundProcessingException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process Stripe refund", e)
            throw RefundProcessingException("Failed to process refund: ${e.message}", cause = e)
        }
    }

    /**
     * Verify Stripe webhook signature
     */
    override fun verifyWebhook(payload: String, signature: String): Boolean {
        try {
            logger.debug("Verifying Stripe webhook signature")

            // Stripe signature format: t=timestamp,v1=signature
            val parts = signature.split(",")
            val timestamp = parts.find { it.startsWith("t=") }?.substring(2) ?: return false
            val signaturePart = parts.find { it.startsWith("v1=") }?.substring(3) ?: return false

            // Construct signed payload
            val signedPayload = "$timestamp.$payload"

            // Compute HMAC
            val mac = Mac.getInstance(HMAC_SHA256)
            val secretKey = SecretKeySpec(properties.stripe.webhookSecret.toByteArray(), HMAC_SHA256)
            mac.init(secretKey)
            val computedSignature = mac.doFinal(signedPayload.toByteArray())
            val computedHex = computedSignature.joinToString("") { "%02x".format(it) }

            val isValid = computedHex == signaturePart

            if (!isValid) {
                logger.warn("Stripe webhook signature verification failed")
            } else {
                logger.debug("Stripe webhook signature verified successfully")
            }

            return isValid
        } catch (e: Exception) {
            logger.error("Error verifying Stripe webhook signature", e)
            return false
        }
    }

    /**
     * Create recurring payment subscription
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
        if (!properties.stripe.enabled) {
            logger.warn("Stripe gateway is disabled")
            return RecurringPaymentResult.failure(
                amount = amount,
                currency = currency,
                schedule = schedule,
                errorCode = "GATEWAY_DISABLED",
                errorMessage = "Stripe payment gateway is currently disabled"
            )
        }

        try {
            logger.info("Creating Stripe subscription: amount={}, currency={}, schedule={}", amount, currency, schedule)

            val amountInCents = (amount * BigDecimal(100)).toLong()
            val interval = when (schedule) {
                RecurringSchedule.DAILY -> "day"
                RecurringSchedule.WEEKLY -> "week"
                RecurringSchedule.MONTHLY -> "month"
                RecurringSchedule.QUARTERLY -> "month"
                RecurringSchedule.YEARLY -> "year"
            }
            val intervalCount = if (schedule == RecurringSchedule.QUARTERLY) 3 else 1

            // First, create a price
            val priceRequestBody = buildFormData(mapOf(
                "unit_amount" to amountInCents.toString(),
                "currency" to currency.lowercase(),
                "recurring[interval]" to interval,
                "recurring[interval_count]" to intervalCount.toString(),
                "product_data[name]" to "Gym Membership"
            ))

            val priceRequest = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/prices"))
                .header("Authorization", "Bearer ${properties.stripe.apiKey}")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Stripe-Version", properties.stripe.apiVersion)
                .timeout(Duration.ofMillis(properties.stripe.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(priceRequestBody))
                .build()

            val priceResponse = httpClient.send(priceRequest, HttpResponse.BodyHandlers.ofString())

            if (priceResponse.statusCode() != 200) {
                val errorResponse = objectMapper.readTree(priceResponse.body())
                val errorMessage = errorResponse.get("error")?.get("message")?.asText() ?: "Failed to create price"
                throw RecurringPaymentException(errorMessage)
            }

            val priceJson = objectMapper.readTree(priceResponse.body())
            val priceId = priceJson.get("id").asText()

            // Then, create a subscription
            val customerId = metadata["customerId"] as? String ?: throw RecurringPaymentException("Customer ID is required")

            val subscriptionRequestBody = buildFormData(mapOf(
                "customer" to customerId,
                "items[0][price]" to priceId,
                "metadata[member_id]" to (metadata["memberId"]?.toString() ?: "")
            ))

            val subscriptionRequest = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/subscriptions"))
                .header("Authorization", "Bearer ${properties.stripe.apiKey}")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Stripe-Version", properties.stripe.apiVersion)
                .timeout(Duration.ofMillis(properties.stripe.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(subscriptionRequestBody))
                .build()

            val subscriptionResponse = httpClient.send(subscriptionRequest, HttpResponse.BodyHandlers.ofString())

            if (subscriptionResponse.statusCode() == 200) {
                val jsonResponse = objectMapper.readTree(subscriptionResponse.body())
                val subscriptionId = jsonResponse.get("id").asText()
                val status = jsonResponse.get("status").asText()
                val currentPeriodEnd = jsonResponse.get("current_period_end").asLong()

                logger.info("Stripe subscription created: subscriptionId={}, status={}", subscriptionId, status)

                return RecurringPaymentResult.success(
                    subscriptionId = subscriptionId,
                    customerId = customerId,
                    amount = amount,
                    currency = currency,
                    schedule = schedule,
                    nextPaymentDate = Instant.ofEpochSecond(currentPeriodEnd),
                    metadata = mapOf(
                        "gateway" to GATEWAY_NAME,
                        "status" to status,
                        "price_id" to priceId
                    )
                )
            } else {
                val errorResponse = objectMapper.readTree(subscriptionResponse.body())
                val errorMessage = errorResponse.get("error")?.get("message")?.asText() ?: "Unknown error"
                val errorCode = errorResponse.get("error")?.get("code")?.asText() ?: "UNKNOWN"

                logger.error("Stripe subscription failed: code={}, message={}", errorCode, errorMessage)
                throw RecurringPaymentException(errorMessage, errorCode)
            }
        } catch (e: RecurringPaymentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to create Stripe subscription", e)
            throw RecurringPaymentException("Failed to create subscription: ${e.message}", cause = e)
        }
    }

    override fun getGatewayName(): String = GATEWAY_NAME

    override fun supportsPaymentMethod(method: String): Boolean {
        return method.lowercase() in listOf(
            "card", "credit_card", "debit_card", "apple_pay", "google_pay"
        )
    }

    /**
     * Build form-urlencoded request body
     */
    private fun buildFormData(params: Map<String, String>): String {
        return params.entries
            .filter { it.value.isNotEmpty() }
            .joinToString("&") { "${it.key}=${java.net.URLEncoder.encode(it.value, "UTF-8")}" }
    }
}
