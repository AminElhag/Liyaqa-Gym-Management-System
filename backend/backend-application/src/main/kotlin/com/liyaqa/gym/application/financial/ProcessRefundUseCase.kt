package com.liyaqa.gym.application.financial

import com.liyaqa.gym.application.financial.commands.ProcessRefundCommand
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.Invoice
import com.liyaqa.gym.domain.entities.Payment
import com.liyaqa.gym.domain.entities.Refund
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.RefundProcessedEvent
import com.liyaqa.gym.domain.payment.PaymentGatewayFactory
import com.liyaqa.gym.domain.repositories.InvoiceRepository
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.gym.domain.repositories.RefundRepository
import com.liyaqa.gym.domain.valueobjects.Money
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Use case for processing payment refunds.
 *
 * This use case handles:
 * - Refund policy validation (time limits, prorated amounts)
 * - Refund amount calculation (full or partial)
 * - Payment gateway refund processing
 * - Credit note generation
 * - Invoice status updates
 * - Transaction handling
 * - Domain event publishing
 *
 * @property refundRepository Repository for refund persistence
 * @property paymentRepository Repository for payment lookups
 * @property invoiceRepository Repository for invoice updates
 * @property paymentGatewayFactory Factory for payment gateway selection
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class ProcessRefundUseCase(
    private val refundRepository: RefundRepository,
    private val paymentRepository: PaymentRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val eventPublisher: EventPublisher,
    @Value("\${refund.policy.max-days:30}") private val refundPolicyMaxDays: Long,
    @Value("\${refund.policy.prorate:true}") private val shouldProrate: Boolean
) {

    private val logger = LoggerFactory.getLogger(ProcessRefundUseCase::class.java)

    /**
     * Executes the refund processing use case.
     *
     * @param command The process refund command
     * @return Result containing the refund ID or error
     */
    fun execute(command: ProcessRefundCommand): Result<UUID> {
        return runCatching {
            logger.info("Processing refund for payment: ${command.paymentId}, amount: ${command.amount}")

            // 1. Retrieve and validate payment
            val payment = validatePayment(command.paymentId)

            // 2. Validate refund policy if requested
            if (command.validatePolicy) {
                validateRefundPolicy(payment)
            }

            // 3. Calculate refund amount (may be prorated)
            val refundAmount = calculateRefundAmount(payment, command.amount)

            // 4. Validate refund amount
            validateRefundAmount(payment, refundAmount)

            // 5. Create refund entity
            val refund = Refund.create(
                paymentId = payment.id,
                invoiceId = null, // Would be populated if invoice is linked
                memberId = payment.memberId,
                organizationId = payment.organizationId,
                branchId = payment.branchId,
                amount = refundAmount,
                reason = command.reason
            )

            // 6. Save refund (creates audit trail)
            val savedRefund = refundRepository.save(refund)
                .getOrElse { error ->
                    logger.error("Failed to save refund to repository: ${error.message}", error)
                    throw error
                }

            logger.info("Refund record created with ID: ${savedRefund.id}")

            // 7. Process refund via payment gateway
            val processedRefund = processViaGateway(savedRefund, payment)

            // 8. Generate credit note
            val creditNoteNumber = Refund.generateCreditNoteNumber()
            val refundWithCreditNote = processedRefund.addCreditNote(
                creditNoteNumber = creditNoteNumber,
                filePath = "/credit-notes/$creditNoteNumber.pdf"
            )

            // 9. Update refund with credit note
            val finalRefund = refundRepository.save(refundWithCreditNote)
                .getOrElse { error ->
                    logger.error("Failed to update refund with credit note: ${error.message}", error)
                    throw error
                }

            logger.info("Credit note generated: $creditNoteNumber")

            // 10. Update payment with refund information
            val refundedPayment = payment.refund(refundAmount, command.reason)
            paymentRepository.save(refundedPayment)
                .getOrElse { error ->
                    logger.error("Failed to update payment with refund: ${error.message}", error)
                    throw error
                }

            // 11. Update invoice status if applicable
            updateInvoiceStatus(payment)

            // 12. Publish RefundProcessedEvent
            publishRefundProcessedEvent(finalRefund)

            // 13. Return refund ID
            finalRefund.id

        }.onFailure { error ->
            logger.error("Failed to process refund: ${error.message}", error)
        }
    }

    /**
     * Validates that the payment exists and can be refunded.
     *
     * @param paymentId The payment identifier
     * @return The validated payment entity
     * @throws ResourceNotFoundException if payment not found
     * @throws ValidationException if payment cannot be refunded
     */
    private fun validatePayment(paymentId: UUID): Payment {
        val paymentOptional = paymentRepository.findById(paymentId)
            .getOrElse { error ->
                logger.error("Failed to query payment repository: ${error.message}", error)
                throw error
            }

        if (!paymentOptional.isPresent) {
            logger.warn("Payment not found: $paymentId")
            throw ResourceNotFoundException("Payment with ID $paymentId not found")
        }

        val payment = paymentOptional.get()

        if (!payment.isCompleted()) {
            logger.warn("Attempted to refund non-completed payment: $paymentId (status: ${payment.status})")
            throw ValidationException("Only completed payments can be refunded (current status: ${payment.status})")
        }

        if (payment.isRefunded()) {
            logger.warn("Attempted to refund already refunded payment: $paymentId")
            throw ValidationException("Payment has already been refunded")
        }

        logger.debug("Payment validation passed for: $paymentId")
        return payment
    }

    /**
     * Validates the refund against the refund policy.
     *
     * @param payment The payment to refund
     * @throws ValidationException if refund violates policy
     */
    private fun validateRefundPolicy(payment: Payment) {
        // Check if refund is within the allowed time window
        val paidAt = payment.paidAt ?: payment.createdAt
        val daysSincePayment = ChronoUnit.DAYS.between(paidAt, Instant.now())

        if (daysSincePayment > refundPolicyMaxDays) {
            logger.warn("Refund request exceeds policy limit: $daysSincePayment days (max: $refundPolicyMaxDays)")
            throw ValidationException(
                "Refund request exceeds the $refundPolicyMaxDays-day refund policy " +
                "(payment was $daysSincePayment days ago)"
            )
        }

        logger.debug("Refund policy validation passed: $daysSincePayment days since payment")
    }

    /**
     * Calculates the refund amount, applying proration if configured.
     *
     * @param payment The payment being refunded
     * @param requestedAmount The requested refund amount
     * @return The calculated refund amount
     */
    private fun calculateRefundAmount(payment: Payment, requestedAmount: BigDecimal): Money {
        val baseAmount = Money.of(requestedAmount, payment.totalAmount.currency.currencyCode)

        if (!shouldProrate) {
            return baseAmount
        }

        // Apply proration based on time elapsed
        // For example, if refund is requested after 15 days of a 30-day period,
        // only refund 50% of the amount
        val paidAt = payment.paidAt ?: payment.createdAt
        val daysSincePayment = ChronoUnit.DAYS.between(paidAt, Instant.now())

        if (daysSincePayment == 0L) {
            return baseAmount // Full refund for same-day refunds
        }

        // Simple proration: reduce by 10% for each week elapsed, minimum 50%
        val weeksElapsed = (daysSincePayment / 7).toInt()
        val proratePercentage = maxOf(0.5, 1.0 - (weeksElapsed * 0.1))
        val proratedAmount = baseAmount * BigDecimal.valueOf(proratePercentage)

        logger.info(
            "Refund prorated: Original: ${baseAmount.amount}, " +
            "Prorated ($proratePercentage): ${proratedAmount.amount}, " +
            "Days elapsed: $daysSincePayment"
        )

        return proratedAmount
    }

    /**
     * Validates that the refund amount is valid.
     *
     * @param payment The payment being refunded
     * @param refundAmount The refund amount to validate
     * @throws ValidationException if amount is invalid
     */
    private fun validateRefundAmount(payment: Payment, refundAmount: Money) {
        if (!refundAmount.isPositive()) {
            throw ValidationException("Refund amount must be positive")
        }

        // Check if there are existing refunds
        val existingRefunds = refundRepository.findByPayment(payment.id)
            .getOrElse { error ->
                logger.error("Failed to query existing refunds: ${error.message}", error)
                throw error
            }

        val totalRefunded = existingRefunds
            .filter { it.isProcessed() }
            .fold(Money.zero(payment.totalAmount.currency.currencyCode)) { acc, refund ->
                acc + refund.amount
            }

        val remainingAmount = payment.totalAmount - totalRefunded

        if (refundAmount.amount > remainingAmount.amount) {
            throw ValidationException(
                "Refund amount (${refundAmount.amount}) exceeds remaining refundable amount (${remainingAmount.amount}). " +
                "Total paid: ${payment.totalAmount.amount}, Already refunded: ${totalRefunded.amount}"
            )
        }

        logger.debug("Refund amount validation passed: ${refundAmount.amount}")
    }

    /**
     * Processes the refund through the payment gateway.
     *
     * @param refund The refund entity
     * @param payment The original payment
     * @return The updated refund entity
     */
    private fun processViaGateway(refund: Refund, payment: Payment): Refund {
        logger.info("Processing refund ${refund.id} via gateway: ${payment.method}")

        return try {
            // Get the appropriate payment gateway
            val gateway = paymentGatewayFactory.getGateway(payment.method.name.lowercase())

            // Process refund through gateway
            // Note: We need the payment gateway ID from the original payment
            val gatewayPaymentId = payment.paymentGatewayId
                ?: throw ValidationException("Payment does not have a gateway transaction ID")

            val refundResult = gateway.refund(
                paymentId = gatewayPaymentId,
                amount = refund.amount.amount
            )

            if (!refundResult.success) {
                logger.error("Payment gateway refund failed: ${refundResult.errorMessage}")
                return refund.markAsFailed(
                    failureReason = refundResult.errorMessage ?: "Unknown error",
                    gatewayResponse = refundResult.refundId
                )
            }

            logger.info("Refund processed successfully via gateway. Refund ID: ${refundResult.refundId}")

            // Mark refund as processed
            refund.markAsProcessed(
                gatewayRefundId = refundResult.refundId,
                creditNoteNumber = null // Will be added later
            )

        } catch (e: Exception) {
            logger.error("Payment gateway refund failed with exception: ${e.message}", e)
            refund.markAsFailed(
                failureReason = "Gateway error: ${e.message}",
                gatewayResponse = null
            )
        }
    }

    /**
     * Updates the invoice status after refund.
     *
     * @param payment The refunded payment
     */
    private fun updateInvoiceStatus(payment: Payment) {
        try {
            // Find invoice by invoice number
            val invoiceOptional = invoiceRepository.findByInvoiceNumber(payment.invoiceNumber)
                .getOrElse { error ->
                    logger.error("Failed to query invoice: ${error.message}", error)
                    return
                }

            if (!invoiceOptional.isPresent) {
                logger.debug("No invoice found for payment ${payment.id}")
                return
            }

            val invoice = invoiceOptional.get()

            // Update invoice status to refunded
            val refundedInvoice = invoice.markAsRefunded()
            invoiceRepository.save(refundedInvoice)
                .getOrElse { error ->
                    logger.error("Failed to update invoice status: ${error.message}", error)
                    return
                }

            logger.info("Updated invoice ${invoice.id} status to REFUNDED")

        } catch (e: Exception) {
            // Log error but don't fail the refund
            logger.error("Failed to update invoice status: ${e.message}", e)
        }
    }

    /**
     * Publishes the RefundProcessedEvent after successful refund.
     *
     * @param refund The processed refund
     */
    private fun publishRefundProcessedEvent(refund: Refund) {
        try {
            val event = RefundProcessedEvent(
                refundId = refund.id,
                paymentId = refund.paymentId,
                memberId = refund.memberId,
                amount = refund.amount,
                creditNoteNumber = refund.creditNoteNumber,
                timestamp = refund.processedAt ?: Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published RefundProcessedEvent for refund: ${refund.id}")

        } catch (e: Exception) {
            // Log error but don't fail the refund
            // Event publishing failures should not prevent refund processing
            logger.error("Failed to publish RefundProcessedEvent for refund ${refund.id}: ${e.message}", e)
        }
    }
}
