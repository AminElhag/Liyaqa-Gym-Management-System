package com.liyaqa.gym.application.financial

import com.liyaqa.gym.application.financial.commands.ProcessPaymentCommand
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.domain.entities.Payment
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.PaymentProcessedEvent
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.domain.valueobjects.VAT
import com.liyaqa.infrastructure.payment.gateway.PaymentGatewayFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Use case for processing payments with automatic VAT calculation.
 *
 * This use case handles:
 * - VAT calculation (15% for Saudi Arabia)
 * - Payment gateway integration
 * - Payment record creation
 * - Transaction handling
 * - Domain event publishing
 *
 * @property paymentRepository Repository for payment persistence
 * @property memberRepository Repository for member lookups
 * @property paymentGatewayFactory Factory for payment gateway selection
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val memberRepository: MemberRepository,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(ProcessPaymentUseCase::class.java)

    /**
     * Executes the payment processing use case.
     *
     * @param command The process payment command
     * @return Result containing the payment ID or error
     */
    fun execute(command: ProcessPaymentCommand): Result<UUID> {
        return runCatching {
            logger.info("Processing payment for member: ${command.memberId}, amount: ${command.amount} ${command.currency}")

            // 1. Validate member exists and is active
            val member = validateMember(command.memberId)

            // 2. Create Money value object from amount
            val amount = Money.of(command.amount, command.currency)

            // 3. Calculate VAT (15% for Saudi Arabia)
            val vat = VAT.calculateSaudiVAT(amount)
            val totalAmount = amount + vat.amount

            logger.info("Payment calculation - Amount: $amount, VAT: ${vat.amount}, Total: $totalAmount")

            // 4. Generate invoice number
            val invoiceNumber = Payment.generateInvoiceNumber()

            // 5. Create payment entity
            val payment = Payment.create(
                memberId = command.memberId,
                organizationId = command.organizationId,
                branchId = command.branchId,
                amount = amount,
                vat = vat,
                method = command.method,
                invoiceNumber = invoiceNumber,
                subscriptionId = command.subscriptionId,
                ptSessionId = command.ptSessionId,
                description = command.description
            )

            // 6. Save payment (creates audit trail)
            val savedPayment = paymentRepository.save(payment)
                .getOrElse { error ->
                    logger.error("Failed to save payment to repository: ${error.message}", error)
                    throw error
                }

            logger.info("Payment record created with ID: ${savedPayment.id}, invoice number: $invoiceNumber")

            // 7. Process payment via gateway
            val completedPayment = processViaGateway(savedPayment, member, command.metadata)

            // 8. Update payment status
            val finalPayment = paymentRepository.save(completedPayment)
                .getOrElse { error ->
                    logger.error("Failed to update payment status: ${error.message}", error)
                    throw error
                }

            logger.info("Payment processed successfully via gateway with ID: ${finalPayment.id}")

            // 9. Publish PaymentProcessedEvent
            publishPaymentProcessedEvent(finalPayment)

            // 10. Return payment ID
            finalPayment.id

        }.onFailure { error ->
            logger.error("Failed to process payment: ${error.message}", error)
        }
    }

    /**
     * Validates that the member exists and is active.
     *
     * @param memberId The member identifier
     * @return The validated member entity
     * @throws ResourceNotFoundException if member not found
     * @throws ValidationException if member is not active
     */
    private fun validateMember(memberId: UUID): Member {
        val memberOptional = memberRepository.findById(memberId)
            .getOrElse { error ->
                logger.error("Failed to query member repository: ${error.message}", error)
                throw error
            }

        if (!memberOptional.isPresent) {
            logger.warn("Member not found: $memberId")
            throw ResourceNotFoundException("Member with ID $memberId not found")
        }

        val member = memberOptional.get()

        if (member.status != MemberStatus.ACTIVE) {
            logger.warn("Attempted to process payment for inactive member: $memberId (status: ${member.status})")
            throw ValidationException("Cannot process payment for inactive member (status: ${member.status})")
        }

        logger.debug("Member validation passed for: $memberId")
        return member
    }

    /**
     * Processes payment through the payment gateway.
     *
     * @param payment The payment entity
     * @param member The member entity
     * @param metadata Additional metadata for the payment
     * @return The updated payment entity
     * @throws ValidationException if payment processing fails
     */
    private fun processViaGateway(
        payment: Payment,
        member: Member,
        metadata: Map<String, Any>
    ): Payment {
        logger.info("Processing payment ${payment.id} via gateway: ${payment.method}")

        return try {
            // Get the appropriate payment gateway
            val gateway = paymentGatewayFactory.getGateway(payment.method.name.lowercase())

            // Prepare payment metadata
            val enrichedMetadata = metadata.toMutableMap().apply {
                put("paymentId", payment.id.toString())
                put("memberId", member.id.toString())
                put("memberName", member.name)
                put("invoiceNumber", payment.invoiceNumber)
                put("vatAmount", payment.vat.amount.amount.toString())
                put("vatRate", payment.vat.ratePercentage.toString())
            }

            // Process payment through gateway
            val paymentResult = gateway.processPayment(
                amount = payment.totalAmount.amount,
                currency = payment.totalAmount.currency.currencyCode,
                method = payment.method.name.lowercase(),
                metadata = enrichedMetadata
            )

            if (!paymentResult.success) {
                logger.error("Payment gateway processing failed: ${paymentResult.errorMessage}")
                return payment.markAsFailed(paymentResult.errorMessage)
            }

            logger.info("Payment processed successfully via gateway. Transaction ID: ${paymentResult.transactionId}")

            // Mark payment as completed
            payment.markAsPaid(
                paymentGatewayResponse = "Transaction ID: ${paymentResult.transactionId}, Status: ${paymentResult.status}"
            )

        } catch (e: Exception) {
            logger.error("Payment gateway processing failed with exception: ${e.message}", e)
            payment.markAsFailed("Gateway error: ${e.message}")
        }
    }

    /**
     * Publishes the PaymentProcessedEvent after successful payment.
     *
     * @param payment The processed payment
     */
    private fun publishPaymentProcessedEvent(payment: Payment) {
        try {
            val event = PaymentProcessedEvent(
                paymentId = payment.id,
                memberId = payment.memberId,
                amount = payment.totalAmount,
                method = payment.method,
                timestamp = payment.paidAt ?: Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published PaymentProcessedEvent for payment: ${payment.id}")

        } catch (e: Exception) {
            // Log error but don't fail the payment
            // Event publishing failures should not prevent payment processing
            logger.error("Failed to publish PaymentProcessedEvent for payment ${payment.id}: ${e.message}", e)
        }
    }
}
