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
 * STC Pay payment gateway implementation
 *
 * STC Pay is a popular Saudi digital wallet service operated by Saudi Telecom Company.
 * It supports payments via mobile numbers and QR codes.
 *
 * Features:
 * - Payment processing via STC Pay wallet
 * - Refund handling
 * - Webhook signature verification
 * - Recurring payment subscriptions
 * - Retry logic with exponential backoff
 * - Test mode support
 */
@Component
class STCPayPaymentGateway(
    private val properties: PaymentGatewayProperties,
    private val objectMapper: ObjectMapper
) : PaymentGateway {

    private val logger = LoggerFactory.getLogger(STCPayPaymentGateway::class.java)

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(properties.stcPay.connectTimeout))
        .build()

    private val baseUrl = if (properties.testMode) {
        properties.stcPay.testGatewayUrl
    } else {
        properties.stcPay.gatewayUrl
    }

    companion object {
        private const val GATEWAY_NAME = "STCPay"
        private const val HMAC_SHA256 = "HmacSHA256"
    }

    /**
     * Process a payment through STC Pay
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
        if (!properties.stcPay.enabled) {
            logger.warn("STC Pay gateway is disabled")
            return PaymentResult.failure(
                amount, currency,
                "GATEWAY_DISABLED",
                "STC Pay gateway is currently disabled"
            )
        }

        try {
            logger.info("Processing STC Pay payment: amount={}, currency={}, method={}", amount, currency, method)

            // Build payment request
            val paymentRequest = mapOf(
                "merchantId" to properties.stcPay.merchantId,
                "amount" to amount.toString(),
                "currency" to currency,
                "mobileNumber" to (metadata["mobileNumber"]?.toString() ?: ""),
                "referenceNumber" to (metadata["invoiceNumber"]?.toString() ?: UUID.randomUUID().toString()),
                "description" to (metadata["description"]?.toString() ?: "Gym membership payment"),
                "customerName" to (metadata["customerName"]?.toString() ?: ""),
                "customerEmail" to (metadata["customerEmail"]?.toString() ?: ""),
                "callbackUrl" to (metadata["callbackUrl"]?.toString() ?: ""),
                "metadata" to mapOf(
                    "memberId" to (metadata["memberId"]?.toString() ?: ""),
                    "organizationId" to (metadata["organizationId"]?.toString() ?: "")
                )
            )

            val requestBody = objectMapper.writeValueAsString(paymentRequest)

            // Generate signature
            val signature = generateSignature(requestBody)

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/payments"))
                .header("Content-Type", "application/json")
                .header("X-Merchant-Id", properties.stcPay.merchantId)
                .header("X-API-Key", properties.stcPay.apiKey)
                .header("X-Signature", signature)
                .timeout(Duration.ofMillis(properties.stcPay.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            logger.debug("STC Pay API response: status={}, body={}", response.statusCode(), response.body())

            if (response.statusCode() in 200..299) {
                val jsonResponse = objectMapper.readTree(response.body())
                val paymentId = jsonResponse.get("paymentId")?.asText() ?: UUID.randomUUID().toString()
                val status = jsonResponse.get("status")?.asText() ?: "unknown"
                val stcReferenceNumber = jsonResponse.get("stcReferenceNumber")?.asText() ?: ""

                return when (status.lowercase()) {
                    "success", "completed" -> {
                        logger.info("STC Pay payment succeeded: paymentId={}", paymentId)
                        PaymentResult.success(
                            transactionId = UUID.randomUUID().toString(),
                            gatewayPaymentId = paymentId,
                            amount = amount,
                            currency = currency,
                            metadata = mapOf(
                                "gateway" to GATEWAY_NAME,
                                "status" to status,
                                "stcReferenceNumber" to stcReferenceNumber
                            )
                        )
                    }
                    "pending", "processing" -> {
                        logger.info("STC Pay payment pending: paymentId={}", paymentId)
                        PaymentResult.pending(
                            transactionId = UUID.randomUUID().toString(),
                            gatewayPaymentId = paymentId,
                            amount = amount,
                            currency = currency,
                            metadata = mapOf(
                                "gateway" to GATEWAY_NAME,
                                "status" to status,
                                "stcReferenceNumber" to stcReferenceNumber
                            )
                        )
                    }
                    else -> {
                        val errorMessage = jsonResponse.get("message")?.asText() ?: "Payment failed"
                        logger.error("STC Pay payment failed: paymentId={}, status={}", paymentId, status)
                        PaymentResult.failure(
                            amount = amount,
                            currency = currency,
                            errorCode = status.uppercase(),
                            errorMessage = errorMessage,
                            transactionId = paymentId
                        )
                    }
                }
            } else {
                val errorResponse = try {
                    objectMapper.readTree(response.body())
                } catch (e: Exception) {
                    null
                }
                val errorMessage = errorResponse?.get("message")?.asText() ?: "Unknown error"
                val errorCode = errorResponse?.get("code")?.asText() ?: "UNKNOWN"

                logger.error("STC Pay API error: code={}, message={}", errorCode, errorMessage)
                throw PaymentProcessingException(errorMessage, errorCode)
            }
        } catch (e: PaymentProcessingException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process STC Pay payment", e)
            throw PaymentProcessingException("Failed to process payment: ${e.message}", cause = e)
        }
    }

    /**
     * Refund a STC Pay payment
     */
    @Retryable(
        retryFor = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    override fun refund(paymentId: String, amount: BigDecimal): RefundResult {
        if (!properties.stcPay.enabled) {
            logger.warn("STC Pay gateway is disabled")
            return RefundResult.failure(
                originalPaymentId = paymentId,
                amount = amount,
                currency = "SAR",
                errorCode = "GATEWAY_DISABLED",
                errorMessage = "STC Pay gateway is currently disabled"
            )
        }

        try {
            logger.info("Processing STC Pay refund: paymentId={}, amount={}", paymentId, amount)

            val refundRequest = mapOf(
                "merchantId" to properties.stcPay.merchantId,
                "paymentId" to paymentId,
                "amount" to amount.toString(),
                "currency" to "SAR",
                "reason" to "Customer requested refund",
                "referenceNumber" to UUID.randomUUID().toString()
            )

            val requestBody = objectMapper.writeValueAsString(refundRequest)
            val signature = generateSignature(requestBody)

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/refunds"))
                .header("Content-Type", "application/json")
                .header("X-Merchant-Id", properties.stcPay.merchantId)
                .header("X-API-Key", properties.stcPay.apiKey)
                .header("X-Signature", signature)
                .timeout(Duration.ofMillis(properties.stcPay.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() in 200..299) {
                val jsonResponse = objectMapper.readTree(response.body())
                val refundId = jsonResponse.get("refundId")?.asText() ?: UUID.randomUUID().toString()
                val status = jsonResponse.get("status")?.asText() ?: "unknown"
                val refundedAmount = jsonResponse.get("amount")?.asText()?.let { BigDecimal(it) } ?: amount

                return if (status.lowercase() in listOf("success", "completed")) {
                    logger.info("STC Pay refund succeeded: refundId={}", refundId)
                    RefundResult.success(
                        refundId = refundId,
                        originalPaymentId = paymentId,
                        amount = refundedAmount,
                        currency = "SAR",
                        metadata = mapOf(
                            "gateway" to GATEWAY_NAME,
                            "status" to status
                        )
                    )
                } else {
                    val errorMessage = jsonResponse.get("message")?.asText() ?: "Refund failed"
                    logger.error("STC Pay refund failed: refundId={}, status={}", refundId, status)
                    throw RefundProcessingException(errorMessage, status)
                }
            } else {
                val errorResponse = objectMapper.readTree(response.body())
                val errorMessage = errorResponse.get("message")?.asText() ?: "Unknown error"
                val errorCode = errorResponse.get("code")?.asText() ?: "UNKNOWN"

                logger.error("STC Pay refund failed: code={}, message={}", errorCode, errorMessage)
                throw RefundProcessingException(errorMessage, errorCode)
            }
        } catch (e: RefundProcessingException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process STC Pay refund", e)
            throw RefundProcessingException("Failed to process refund: ${e.message}", cause = e)
        }
    }

    /**
     * Verify STC Pay webhook signature
     */
    override fun verifyWebhook(payload: String, signature: String): Boolean {
        try {
            logger.debug("Verifying STC Pay webhook signature")

            val computedSignature = generateSignature(payload)
            val isValid = computedSignature.equals(signature, ignoreCase = true)

            if (!isValid) {
                logger.warn("STC Pay webhook signature verification failed")
            } else {
                logger.debug("STC Pay webhook signature verified successfully")
            }

            return isValid
        } catch (e: Exception) {
            logger.error("Error verifying STC Pay webhook signature", e)
            return false
        }
    }

    /**
     * Create recurring payment subscription for STC Pay
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
        if (!properties.stcPay.enabled) {
            logger.warn("STC Pay gateway is disabled")
            return RecurringPaymentResult.failure(
                amount = amount,
                currency = currency,
                schedule = schedule,
                errorCode = "GATEWAY_DISABLED",
                errorMessage = "STC Pay gateway is currently disabled"
            )
        }

        try {
            logger.info("Creating STC Pay subscription: amount={}, currency={}, schedule={}", amount, currency, schedule)

            val subscriptionRequest = mapOf(
                "merchantId" to properties.stcPay.merchantId,
                "amount" to amount.toString(),
                "currency" to currency,
                "mobileNumber" to (metadata["mobileNumber"]?.toString()
                    ?: throw RecurringPaymentException("Mobile number is required for STC Pay subscriptions")),
                "frequency" to mapScheduleToFrequency(schedule),
                "startDate" to Instant.now().toString(),
                "customerName" to (metadata["customerName"]?.toString() ?: ""),
                "description" to (metadata["description"]?.toString() ?: "Gym membership subscription"),
                "metadata" to mapOf(
                    "memberId" to (metadata["memberId"]?.toString() ?: ""),
                    "organizationId" to (metadata["organizationId"]?.toString() ?: "")
                )
            )

            val requestBody = objectMapper.writeValueAsString(subscriptionRequest)
            val signature = generateSignature(requestBody)

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/subscriptions"))
                .header("Content-Type", "application/json")
                .header("X-Merchant-Id", properties.stcPay.merchantId)
                .header("X-API-Key", properties.stcPay.apiKey)
                .header("X-Signature", signature)
                .timeout(Duration.ofMillis(properties.stcPay.readTimeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() in 200..299) {
                val jsonResponse = objectMapper.readTree(response.body())
                val subscriptionId = jsonResponse.get("subscriptionId")?.asText() ?: UUID.randomUUID().toString()
                val customerId = jsonResponse.get("customerId")?.asText() ?: ""
                val status = jsonResponse.get("status")?.asText() ?: "unknown"
                val nextPaymentDateStr = jsonResponse.get("nextPaymentDate")?.asText()
                val nextPaymentDate = nextPaymentDateStr?.let { Instant.parse(it) } ?: calculateNextPaymentDate(schedule)

                return if (status.lowercase() in listOf("active", "success")) {
                    logger.info("STC Pay subscription created: subscriptionId={}", subscriptionId)
                    RecurringPaymentResult.success(
                        subscriptionId = subscriptionId,
                        customerId = customerId,
                        amount = amount,
                        currency = currency,
                        schedule = schedule,
                        nextPaymentDate = nextPaymentDate,
                        metadata = mapOf(
                            "gateway" to GATEWAY_NAME,
                            "status" to status
                        )
                    )
                } else {
                    val errorMessage = jsonResponse.get("message")?.asText() ?: "Failed to create subscription"
                    logger.error("STC Pay subscription failed: subscriptionId={}, status={}", subscriptionId, status)
                    throw RecurringPaymentException(errorMessage, status)
                }
            } else {
                val errorResponse = objectMapper.readTree(response.body())
                val errorMessage = errorResponse.get("message")?.asText() ?: "Unknown error"
                val errorCode = errorResponse.get("code")?.asText() ?: "UNKNOWN"

                logger.error("STC Pay subscription failed: code={}, message={}", errorCode, errorMessage)
                throw RecurringPaymentException(errorMessage, errorCode)
            }
        } catch (e: RecurringPaymentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to create STC Pay subscription", e)
            throw RecurringPaymentException("Failed to create subscription: ${e.message}", cause = e)
        }
    }

    override fun getGatewayName(): String = GATEWAY_NAME

    override fun supportsPaymentMethod(method: String): Boolean {
        return method.lowercase() in listOf("stcpay", "stc_pay", "wallet")
    }

    /**
     * Generate HMAC-SHA256 signature for request authentication
     */
    private fun generateSignature(payload: String): String {
        val mac = Mac.getInstance(HMAC_SHA256)
        val secretKey = SecretKeySpec(properties.stcPay.apiSecret.toByteArray(), HMAC_SHA256)
        mac.init(secretKey)
        val signature = mac.doFinal(payload.toByteArray())
        return Base64.getEncoder().encodeToString(signature)
    }

    /**
     * Map RecurringSchedule to STC Pay frequency format
     */
    private fun mapScheduleToFrequency(schedule: RecurringSchedule): String {
        return when (schedule) {
            RecurringSchedule.DAILY -> "DAILY"
            RecurringSchedule.WEEKLY -> "WEEKLY"
            RecurringSchedule.MONTHLY -> "MONTHLY"
            RecurringSchedule.QUARTERLY -> "QUARTERLY"
            RecurringSchedule.YEARLY -> "YEARLY"
        }
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
