package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.application.financial.ProcessPaymentUseCase
import com.liyaqa.gym.application.financial.ProcessRefundUseCase
import com.liyaqa.gym.application.financial.commands.ProcessPaymentCommand
import com.liyaqa.gym.application.financial.commands.ProcessRefundCommand
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.Payment
import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.gym.domain.repositories.RefundRepository
import com.liyaqa.gym.presentation.dto.common.ApiResponse
import com.liyaqa.gym.presentation.dto.payment.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * REST Controller for payment processing operations.
 * Handles payment processing, refunds, and payment gateway webhooks.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment processing and management endpoints")
class PaymentController(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val processRefundUseCase: ProcessRefundUseCase,
    private val paymentRepository: PaymentRepository,
    private val refundRepository: RefundRepository
) {

    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    // In-memory idempotency cache (in production, use Redis or database)
    private val idempotencyCache = ConcurrentHashMap<String, UUID>()

    /**
     * Process a new payment
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Process payment",
        description = "Process a new payment for a member. Supports idempotency via idempotencyKey."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Payment processed successfully",
                content = [Content(schema = Schema(implementation = PaymentConfirmationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid input data or payment processing failed"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Member not found"
            )
        ]
    )
    fun processPayment(
        @Valid @RequestBody request: ProcessPaymentRequest
    ): ResponseEntity<ApiResponse<PaymentConfirmationResponse>> {
        logger.info("Processing payment for member: ${request.memberId}, amount: ${request.amount} ${request.currency}")

        // Handle idempotency
        if (request.idempotencyKey != null) {
            val existingPaymentId = idempotencyCache[request.idempotencyKey]
            if (existingPaymentId != null) {
                logger.info("Idempotent request detected, returning existing payment: $existingPaymentId")
                val existingPayment = paymentRepository.findById(existingPaymentId).getOrThrow()
                if (existingPayment.isPresent) {
                    val response = existingPayment.get().toPaymentConfirmationResponse()
                    return ResponseEntity.ok(ApiResponse.success(response))
                }
            }
        }

        val command = ProcessPaymentCommand(
            memberId = request.memberId,
            organizationId = request.organizationId,
            branchId = request.branchId,
            amount = request.amount,
            currency = request.currency,
            method = PaymentMethod.valueOf(request.method),
            subscriptionId = request.subscriptionId,
            ptSessionId = request.ptSessionId,
            description = request.description,
            metadata = request.metadata
        )

        val paymentId = processPaymentUseCase.execute(command)
            .getOrThrow()

        // Cache idempotency key
        if (request.idempotencyKey != null) {
            idempotencyCache[request.idempotencyKey] = paymentId
        }

        // Retrieve the payment to get full details
        val payment = paymentRepository.findById(paymentId)
            .getOrThrow()
            .orElseThrow { ResourceNotFoundException("Payment not found after creation") }

        val response = payment.toPaymentConfirmationResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Get payment details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get payment details",
        description = "Retrieve detailed information about a specific payment"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Payment retrieved successfully",
                content = [Content(schema = Schema(implementation = PaymentResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Payment not found"
            )
        ]
    )
    fun getPaymentDetails(
        @Parameter(description = "Payment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        logger.info("Fetching payment details for ID: $id")

        val paymentOptional = paymentRepository.findById(id)
            .getOrThrow()

        if (!paymentOptional.isPresent) {
            throw ResourceNotFoundException("Payment not found with ID: $id")
        }

        val response = paymentOptional.get().toPaymentResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Process a refund (admin only)
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Process refund",
        description = "Process a refund for a payment. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Refund processed successfully",
                content = [Content(schema = Schema(implementation = RefundConfirmationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or refund processing failed"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Payment not found"
            )
        ]
    )
    fun processRefund(
        @Parameter(description = "Payment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: ProcessRefundRequest
    ): ResponseEntity<ApiResponse<RefundConfirmationResponse>> {
        logger.info("Processing refund for payment ID: $id, amount: ${request.amount}")

        val command = ProcessRefundCommand(
            paymentId = id,
            amount = request.amount,
            reason = request.reason,
            validatePolicy = request.validatePolicy
        )

        val refundId = processRefundUseCase.execute(command)
            .getOrThrow()

        // Retrieve the refund to get full details
        val refund = refundRepository.findById(refundId)
            .getOrThrow()
            .orElseThrow { ResourceNotFoundException("Refund not found after creation") }

        val response = RefundConfirmationResponse(
            refundId = refund.id,
            paymentId = refund.paymentId,
            amount = refund.amount.amount,
            currency = refund.amount.currency.currencyCode,
            creditNoteNumber = refund.creditNoteNumber,
            status = refund.status.name,
            gatewayRefundId = refund.gatewayRefundId,
            processedAt = refund.processedAt,
            message = "Refund processed successfully"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get member's payment history
     */
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get member's payment history",
        description = "Retrieve all payments for a specific member"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Payment history retrieved successfully",
                content = [Content(schema = Schema(implementation = PaymentSummaryResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            )
        ]
    )
    fun getMemberPaymentHistory(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable memberId: UUID,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<PaymentSummaryResponse>>> {
        logger.info("Fetching payment history for member: $memberId (page: $page, size: $size)")

        val payments = paymentRepository.findByMember(memberId, page, size)
            .getOrThrow()

        val response = payments.map { it.toPaymentSummaryResponse() }
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Stripe webhook endpoint
     */
    @PostMapping("/webhooks/stripe")
    @Operation(
        summary = "Stripe webhook endpoint",
        description = "Receive webhook events from Stripe payment gateway"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Webhook received and processed",
                content = [Content(schema = Schema(implementation = WebhookAcknowledgmentResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid webhook payload"
            )
        ]
    )
    fun handleStripeWebhook(
        @RequestBody request: StripeWebhookRequest,
        @RequestHeader("Stripe-Signature", required = false) signature: String?
    ): ResponseEntity<ApiResponse<WebhookAcknowledgmentResponse>> {
        logger.info("Received Stripe webhook: ${request.type}")

        // TODO: Verify webhook signature
        // TODO: Process webhook event asynchronously

        val response = WebhookAcknowledgmentResponse(
            received = true,
            eventType = request.type,
            eventId = request.id,
            message = "Stripe webhook received and queued for processing"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Mada webhook endpoint
     */
    @PostMapping("/webhooks/mada")
    @Operation(
        summary = "Mada webhook endpoint",
        description = "Receive webhook events from Mada payment gateway"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Webhook received and processed",
                content = [Content(schema = Schema(implementation = WebhookAcknowledgmentResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid webhook payload"
            )
        ]
    )
    fun handleMadaWebhook(
        @Valid @RequestBody request: MadaWebhookRequest
    ): ResponseEntity<ApiResponse<WebhookAcknowledgmentResponse>> {
        logger.info("Received Mada webhook: ${request.transactionId}, status: ${request.status}")

        // TODO: Verify webhook signature
        // TODO: Process webhook event asynchronously
        // TODO: Update payment status based on webhook data

        val response = WebhookAcknowledgmentResponse(
            received = true,
            eventType = "mada.${request.status.lowercase()}",
            eventId = request.transactionId,
            message = "Mada webhook received and queued for processing"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // Extension functions for DTO conversion

    private fun Payment.toPaymentResponse(): PaymentResponse {
        return PaymentResponse(
            id = this.id,
            memberId = this.memberId,
            organizationId = this.organizationId,
            branchId = this.branchId,
            invoiceNumber = this.invoiceNumber,
            amount = this.amount.amount,
            vatAmount = this.vat.amount.amount,
            totalAmount = this.totalAmount.amount,
            currency = this.totalAmount.currency.currencyCode,
            method = this.method.name,
            status = this.status.name,
            subscriptionId = this.subscriptionId,
            ptSessionId = this.ptSessionId,
            description = this.description,
            paymentGatewayId = this.paymentGatewayId,
            refundedAmount = this.refundedAmount?.amount,
            paidAt = this.paidAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun Payment.toPaymentSummaryResponse(): PaymentSummaryResponse {
        return PaymentSummaryResponse(
            id = this.id,
            memberId = this.memberId,
            invoiceNumber = this.invoiceNumber,
            totalAmount = this.totalAmount.amount,
            currency = this.totalAmount.currency.currencyCode,
            method = this.method.name,
            status = this.status.name,
            paidAt = this.paidAt,
            createdAt = this.createdAt
        )
    }

    private fun Payment.toPaymentConfirmationResponse(): PaymentConfirmationResponse {
        return PaymentConfirmationResponse(
            paymentId = this.id,
            invoiceNumber = this.invoiceNumber,
            totalAmount = this.totalAmount.amount,
            currency = this.totalAmount.currency.currencyCode,
            status = this.status.name,
            transactionId = this.paymentGatewayId,
            message = "Payment processed successfully"
        )
    }
}
