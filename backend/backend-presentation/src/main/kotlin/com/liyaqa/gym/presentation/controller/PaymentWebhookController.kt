package com.liyaqa.gym.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.PaymentProcessedEvent
import com.liyaqa.gym.domain.payment.PaymentGatewayFactory
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.gym.domain.valueobjects.Money
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

/**
 * Controller for handling payment gateway webhooks
 *
 * Features:
 * - Receives async payment notifications from payment gateways
 * - Verifies webhook signatures for security
 * - Updates payment status in the database
 * - Publishes PaymentProcessedEvent for downstream processing
 * - Comprehensive logging for audit trail
 */
@RestController
@RequestMapping("/api/v1/webhooks/payments")
class PaymentWebhookController(
    private val gatewayFactory: PaymentGatewayFactory,
    private val paymentRepository: PaymentRepository,
    private val eventPublisher: EventPublisher,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(PaymentWebhookController::class.java)

    /**
     * Handle Stripe webhook notifications
     */
    @PostMapping("/stripe")
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<WebhookResponse> {
        logger.info("Received Stripe webhook notification")

        return try {
            val gateway = gatewayFactory.getGatewayByName("stripe")
                ?: return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(WebhookResponse(success = false, message = "Stripe gateway not available"))

            // Verify webhook signature
            if (!gateway.verifyWebhook(payload, signature)) {
                logger.warn("Stripe webhook signature verification failed")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(WebhookResponse(success = false, message = "Invalid signature"))
            }

            // Parse webhook payload
            val event = objectMapper.readTree(payload)
            val eventType = event.get("type")?.asText()
            val eventData = event.get("data")?.get("object")

            logger.debug("Stripe webhook event type: {}", eventType)

            when (eventType) {
                "payment_intent.succeeded" -> {
                    handlePaymentSuccess(
                        gatewayPaymentId = eventData?.get("id")?.asText() ?: "",
                        amount = eventData?.get("amount")?.asLong()?.let { it / 100.0 } ?: 0.0,
                        currency = eventData?.get("currency")?.asText()?.uppercase() ?: "SAR",
                        gatewayName = "Stripe",
                        metadata = eventData?.get("metadata")
                    )
                }
                "payment_intent.payment_failed" -> {
                    handlePaymentFailure(
                        gatewayPaymentId = eventData?.get("id")?.asText() ?: "",
                        errorMessage = eventData?.get("last_payment_error")?.get("message")?.asText() ?: "Payment failed"
                    )
                }
                else -> {
                    logger.debug("Unhandled Stripe webhook event type: {}", eventType)
                }
            }

            ResponseEntity.ok(WebhookResponse(success = true, message = "Webhook processed"))
        } catch (e: Exception) {
            logger.error("Error processing Stripe webhook", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebhookResponse(success = false, message = "Error processing webhook: ${e.message}"))
        }
    }

    /**
     * Handle Mada webhook notifications
     */
    @PostMapping("/mada")
    fun handleMadaWebhook(
        @RequestBody payload: String,
        @RequestHeader("X-Signature") signature: String
    ): ResponseEntity<WebhookResponse> {
        logger.info("Received Mada webhook notification")

        return try {
            val gateway = gatewayFactory.getGatewayByName("mada")
                ?: return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(WebhookResponse(success = false, message = "Mada gateway not available"))

            // Verify webhook signature
            if (!gateway.verifyWebhook(payload, signature)) {
                logger.warn("Mada webhook signature verification failed")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(WebhookResponse(success = false, message = "Invalid signature"))
            }

            // Parse webhook payload
            val event = objectMapper.readTree(payload)
            val paymentId = event.get("id")?.asText() ?: ""
            val resultCode = event.get("result")?.get("code")?.asText() ?: ""
            val amount = event.get("amount")?.asText()?.toDoubleOrNull() ?: 0.0
            val currency = event.get("currency")?.asText()?.uppercase() ?: "SAR"

            logger.debug("Mada webhook: paymentId={}, resultCode={}", paymentId, resultCode)

            // Success codes: ^(000\.000\.|000\.100\.1|000\.[36])
            if (resultCode.matches(Regex("^(000\\.000\\.|000\\.100\\.1|000\\.[36]).*"))) {
                handlePaymentSuccess(
                    gatewayPaymentId = paymentId,
                    amount = amount,
                    currency = currency,
                    gatewayName = "Mada",
                    metadata = event.get("merchantTransactionId")
                )
            } else {
                val errorMessage = event.get("result")?.get("description")?.asText() ?: "Payment failed"
                handlePaymentFailure(paymentId, errorMessage)
            }

            ResponseEntity.ok(WebhookResponse(success = true, message = "Webhook processed"))
        } catch (e: Exception) {
            logger.error("Error processing Mada webhook", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebhookResponse(success = false, message = "Error processing webhook: ${e.message}"))
        }
    }

    /**
     * Handle STC Pay webhook notifications
     */
    @PostMapping("/stcpay")
    fun handleSTCPayWebhook(
        @RequestBody payload: String,
        @RequestHeader("X-Signature") signature: String
    ): ResponseEntity<WebhookResponse> {
        logger.info("Received STC Pay webhook notification")

        return try {
            val gateway = gatewayFactory.getGatewayByName("stcpay")
                ?: return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(WebhookResponse(success = false, message = "STC Pay gateway not available"))

            // Verify webhook signature
            if (!gateway.verifyWebhook(payload, signature)) {
                logger.warn("STC Pay webhook signature verification failed")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(WebhookResponse(success = false, message = "Invalid signature"))
            }

            // Parse webhook payload
            val event = objectMapper.readTree(payload)
            val paymentId = event.get("paymentId")?.asText() ?: ""
            val status = event.get("status")?.asText()?.lowercase() ?: ""
            val amount = event.get("amount")?.asText()?.toDoubleOrNull() ?: 0.0
            val currency = event.get("currency")?.asText()?.uppercase() ?: "SAR"

            logger.debug("STC Pay webhook: paymentId={}, status={}", paymentId, status)

            when (status) {
                "success", "completed" -> {
                    handlePaymentSuccess(
                        gatewayPaymentId = paymentId,
                        amount = amount,
                        currency = currency,
                        gatewayName = "STCPay",
                        metadata = event.get("referenceNumber")
                    )
                }
                "failed", "cancelled" -> {
                    val errorMessage = event.get("message")?.asText() ?: "Payment failed"
                    handlePaymentFailure(paymentId, errorMessage)
                }
                else -> {
                    logger.debug("STC Pay payment still processing: paymentId={}, status={}", paymentId, status)
                }
            }

            ResponseEntity.ok(WebhookResponse(success = true, message = "Webhook processed"))
        } catch (e: Exception) {
            logger.error("Error processing STC Pay webhook", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebhookResponse(success = false, message = "Error processing webhook: ${e.message}"))
        }
    }

    /**
     * Handle successful payment notification
     */
    private fun handlePaymentSuccess(
        gatewayPaymentId: String,
        amount: Double,
        currency: String,
        gatewayName: String,
        metadata: Any?
    ) {
        try {
            logger.info("Processing successful payment: gatewayPaymentId={}, amount={}, currency={}, gateway={}",
                gatewayPaymentId, amount, currency, gatewayName)

            // Find payment by gateway payment ID
            val payment = paymentRepository.findByPaymentGatewayId(gatewayPaymentId)
            if (payment == null) {
                logger.warn("Payment not found for gatewayPaymentId: {}", gatewayPaymentId)
                return
            }

            // Update payment status
            val updatedPayment = payment.markAsPaid(
                paymentGatewayResponse = "Payment successful via $gatewayName"
            )
            paymentRepository.save(updatedPayment)

            logger.info("Payment marked as paid: paymentId={}, gatewayPaymentId={}",
                updatedPayment.id, gatewayPaymentId)

            // Publish PaymentProcessedEvent
            val event = PaymentProcessedEvent(
                paymentId = updatedPayment.id,
                memberId = updatedPayment.memberId,
                amount = updatedPayment.totalAmount,
                method = updatedPayment.method,
                timestamp = Instant.now()
            )
            eventPublisher.publish(event)

            logger.info("Published PaymentProcessedEvent: paymentId={}", updatedPayment.id)
        } catch (e: Exception) {
            logger.error("Error handling payment success for gatewayPaymentId: {}", gatewayPaymentId, e)
            throw e
        }
    }

    /**
     * Handle failed payment notification
     */
    private fun handlePaymentFailure(gatewayPaymentId: String, errorMessage: String) {
        try {
            logger.info("Processing failed payment: gatewayPaymentId={}, error={}",
                gatewayPaymentId, errorMessage)

            // Find payment by gateway payment ID
            val payment = paymentRepository.findByPaymentGatewayId(gatewayPaymentId)
            if (payment == null) {
                logger.warn("Payment not found for gatewayPaymentId: {}", gatewayPaymentId)
                return
            }

            // Update payment status
            val updatedPayment = payment.markAsFailed(gatewayResponse = errorMessage)
            paymentRepository.save(updatedPayment)

            logger.info("Payment marked as failed: paymentId={}, gatewayPaymentId={}",
                updatedPayment.id, gatewayPaymentId)
        } catch (e: Exception) {
            logger.error("Error handling payment failure for gatewayPaymentId: {}", gatewayPaymentId, e)
            throw e
        }
    }

    /**
     * Health check endpoint for webhook
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        val stats = gatewayFactory.getGatewayStats()
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "timestamp" to Instant.now().toString(),
            "gateways" to stats
        ))
    }
}

/**
 * Response DTO for webhook processing
 */
data class WebhookResponse(
    val success: Boolean,
    val message: String,
    val timestamp: Instant = Instant.now()
)
