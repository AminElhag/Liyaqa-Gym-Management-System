package com.liyaqa.gym.application.subscription

import com.liyaqa.gym.application.subscription.commands.RenewSubscriptionCommand
import com.liyaqa.gym.application.subscription.dto.SubscriptionDTO
import com.liyaqa.gym.application.subscription.dto.SubscriptionMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.MembershipPlan
import com.liyaqa.gym.domain.entities.Payment
import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.PaymentProcessedEvent
import com.liyaqa.gym.domain.events.SubscriptionRenewedEvent
import com.liyaqa.gym.domain.repositories.MembershipPlanRepository
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import com.liyaqa.gym.domain.payment.PaymentGatewayFactory
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

/**
 * Use case for renewing an existing subscription.
 *
 * This use case handles:
 * - Subscription renewal eligibility check
 * - Payment processing for renewal
 * - End date extension based on plan duration
 * - Domain event publishing
 * - Confirmation notification (via events)
 *
 * @property subscriptionRepository Repository for subscription persistence
 * @property planRepository Repository for plan lookups
 * @property paymentRepository Repository for payment persistence
 * @property paymentGatewayFactory Factory for payment gateway selection
 * @property eventPublisher Publisher for domain events
 * @property subscriptionMapper Mapper for DTO conversion
 */
@Service
@Transactional
class RenewSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val planRepository: MembershipPlanRepository,
    private val paymentRepository: PaymentRepository,
    private val memberRepository: com.liyaqa.gym.domain.repositories.MemberRepository,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val eventPublisher: EventPublisher,
    private val subscriptionMapper: SubscriptionMapper
) {

    private val logger = LoggerFactory.getLogger(RenewSubscriptionUseCase::class.java)

    /**
     * Executes the renew subscription use case.
     *
     * @param command The renew subscription command
     * @return Result containing the renewed subscription DTO or error
     */
    @CachePut(
        value = ["subscription"],
        key = "#result.getOrNull()?.id",
        condition = "#result.isSuccess"
    )
    fun execute(command: RenewSubscriptionCommand): Result<SubscriptionDTO> {
        return runCatching {
            logger.info("Renewing subscription: ${command.subscriptionId}")

            // 1. Load subscription
            val subscription = loadSubscription(command.subscriptionId)

            // 2. Check if renewal is due or allowed
            validateRenewalEligibility(subscription)

            // 3. Load plan
            val plan = loadPlan(subscription.planId)

            // 4. Process payment
            val payment = processRenewalPayment(subscription, plan, command)

            // 5. Extend end date
            val newEndDate = calculateNewEndDate(subscription, plan)

            // 6. Reset visits for visit-based plans
            val remainingVisits = if (plan.isVisitBased()) plan.visitCount else subscription.remainingVisits

            // 7. Update subscription
            val renewedSubscription = subscription.copy(
                endDate = newEndDate,
                remainingVisits = remainingVisits,
                status = SubscriptionStatus.ACTIVE,
                updatedAt = Instant.now()
            )

            // 8. Persist updated subscription
            val savedSubscription = subscriptionRepository.save(renewedSubscription)
                .getOrElse { error ->
                    logger.error("Failed to save renewed subscription: ${error.message}", error)
                    throw error
                }

            logger.info("Subscription renewed successfully: ${savedSubscription.id}, new end date: $newEndDate")

            // 9. Publish domain events
            publishSubscriptionRenewedEvent(savedSubscription, newEndDate)
            publishPaymentProcessedEvent(payment)

            // 10. Convert to DTO and return
            subscriptionMapper.toDTO(savedSubscription)

        }.onFailure { error ->
            logger.error("Failed to renew subscription: ${error.message}", error)
        }
    }

    /**
     * Loads the subscription from the repository.
     *
     * @param subscriptionId The subscription identifier
     * @return The loaded subscription
     * @throws ResourceNotFoundException if subscription not found
     */
    private fun loadSubscription(subscriptionId: java.util.UUID): Subscription {
        val subscriptionOptional = subscriptionRepository.findById(subscriptionId)
            .getOrElse { error ->
                logger.error("Failed to query subscription repository: ${error.message}", error)
                throw error
            }

        if (!subscriptionOptional.isPresent) {
            logger.warn("Subscription not found: $subscriptionId")
            throw ResourceNotFoundException("Subscription with ID $subscriptionId not found")
        }

        return subscriptionOptional.get()
    }

    /**
     * Validates that the subscription is eligible for renewal.
     *
     * @param subscription The subscription to validate
     * @throws ValidationException if subscription is not eligible for renewal
     */
    private fun validateRenewalEligibility(subscription: Subscription) {
        // Cannot renew cancelled subscriptions
        if (subscription.status == SubscriptionStatus.CANCELLED) {
            logger.warn("Attempted to renew cancelled subscription: ${subscription.id}")
            throw ValidationException("Cannot renew a cancelled subscription")
        }

        // Check if renewal is due (within 7 days of expiry or already expired)
        val renewalWindow = LocalDate.now().plusDays(7)
        val endDate = subscription.endDate

        if (endDate != null && endDate.isAfter(renewalWindow)) {
            logger.warn("Attempted to renew subscription too early: ${subscription.id}, end date: $endDate")
            throw ValidationException("Subscription renewal is not yet due. Can renew starting 7 days before expiry.")
        }

        logger.debug("Renewal eligibility check passed for subscription: ${subscription.id}")
    }

    /**
     * Loads the membership plan from the repository.
     *
     * @param planId The plan identifier
     * @return The loaded plan
     * @throws ResourceNotFoundException if plan not found
     */
    private fun loadPlan(planId: java.util.UUID): MembershipPlan {
        val planOptional = planRepository.findById(planId)
            .getOrElse { error ->
                logger.error("Failed to query plan repository: ${error.message}", error)
                throw error
            }

        if (!planOptional.isPresent) {
            logger.warn("Membership plan not found: $planId")
            throw ResourceNotFoundException("Membership plan with ID $planId not found")
        }

        return planOptional.get()
    }

    /**
     * Processes the renewal payment.
     *
     * @param subscription The subscription being renewed
     * @param plan The membership plan
     * @param command The renew subscription command
     * @return The created payment entity
     * @throws ValidationException if payment processing fails
     */
    private fun processRenewalPayment(
        subscription: Subscription,
        plan: MembershipPlan,
        command: RenewSubscriptionCommand
    ): Payment {
        logger.info("Processing renewal payment of ${plan.price} via ${command.paymentMethod}")

        return try {
            // Load member to get organization and branch IDs
            val member = memberRepository.findById(subscription.memberId)
                .getOrElse { error ->
                    logger.error("Failed to load member: ${error.message}", error)
                    throw error
                }
                .orElseThrow { ResourceNotFoundException("Member not found: ${subscription.memberId}") }

            // Get the appropriate payment gateway
            val gateway = paymentGatewayFactory.getGateway(command.paymentMethod.name.lowercase())

            // Prepare payment metadata
            val metadata = command.paymentMetadata.toMutableMap().apply {
                put("subscriptionId", subscription.id.toString())
                put("memberId", subscription.memberId.toString())
                put("planId", plan.id.toString())
                put("planName", plan.name)
                put("paymentType", "renewal")
            }

            // Process payment through gateway
            val paymentResult = gateway.processPayment(
                amount = plan.price.amount,
                currency = plan.price.currency.currencyCode,
                method = command.paymentMethod.name.lowercase(),
                metadata = metadata
            )

            if (!paymentResult.success) {
                logger.error("Renewal payment processing failed: ${paymentResult.errorMessage}")
                throw ValidationException("Payment failed: ${paymentResult.errorMessage ?: "Unknown error"}")
            }

            // Calculate VAT (15% for Saudi Arabia)
            val vat = com.liyaqa.gym.domain.valueobjects.VAT.calculateSaudiVAT(plan.price)

            // Generate invoice number
            val invoiceNumber = com.liyaqa.gym.domain.entities.Payment.generateInvoiceNumber()

            // Create payment entity
            val payment = Payment.create(
                tenantId = member.organizationId,
                memberId = subscription.memberId,
                organizationId = member.organizationId,
                branchId = member.branchId,
                amount = plan.price,
                vat = vat,
                method = command.paymentMethod,
                invoiceNumber = invoiceNumber,
                subscriptionId = subscription.id,
                ptSessionId = null,
                description = "Subscription renewal for plan: ${plan.name}"
            )

            // Mark payment as completed
            val completedPayment = payment.markAsPaid(paymentResult.gatewayPaymentId)

            // Persist payment
            val savedPayment = paymentRepository.save(completedPayment)
                .getOrElse { error ->
                    logger.error("Failed to save renewal payment: ${error.message}", error)
                    throw error
                }

            logger.info("Renewal payment processed successfully: ${savedPayment.id}")
            savedPayment

        } catch (e: Exception) {
            logger.error("Renewal payment processing failed: ${e.message}", e)
            when (e) {
                is ValidationException -> throw e
                else -> throw ValidationException("Payment processing failed: ${e.message}")
            }
        }
    }

    /**
     * Calculates the new end date after renewal.
     *
     * @param subscription The current subscription
     * @param plan The membership plan
     * @return The new end date
     */
    private fun calculateNewEndDate(subscription: Subscription, plan: MembershipPlan): LocalDate? {
        val currentEndDate = subscription.endDate ?: LocalDate.now()
        val startDate = if (currentEndDate.isBefore(LocalDate.now())) {
            LocalDate.now()
        } else {
            currentEndDate
        }

        return when {
            plan.isDurationBased() || plan.isTimeRestricted() -> {
                val duration = plan.durationDays
                    ?: throw IllegalStateException("Duration-based or time-restricted plans must have durationDays set")
                val newEndDate = startDate.plusDays(duration.toLong())
                logger.debug("Calculated new end date: $newEndDate ($duration days from $startDate)")
                newEndDate
            }
            plan.isVisitBased() && plan.durationDays != null -> {
                plan.durationDays?.let { duration ->
                    val newEndDate = startDate.plusDays(duration.toLong())
                    logger.debug("Calculated new end date for visit-based plan: $newEndDate")
                    newEndDate
                }
            }
            else -> {
                logger.debug("No end date calculated for visit-based plan without duration")
                null
            }
        }
    }

    /**
     * Publishes the SubscriptionRenewedEvent after successful renewal.
     *
     * @param subscription The renewed subscription
     * @param newEndDate The new end date
     */
    private fun publishSubscriptionRenewedEvent(subscription: Subscription, newEndDate: LocalDate?) {
        try {
            val event = SubscriptionRenewedEvent(
                subscriptionId = subscription.id,
                newEndDate = newEndDate ?: LocalDate.now().plusYears(10) // Fallback for visit-based
            )

            eventPublisher.publish(event)
            logger.info("Published SubscriptionRenewedEvent for subscription: ${subscription.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish SubscriptionRenewedEvent for subscription ${subscription.id}: ${e.message}", e)
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
                amount = payment.amount,
                method = payment.method,
                timestamp = payment.paidAt ?: Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published PaymentProcessedEvent for renewal payment: ${payment.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish PaymentProcessedEvent for payment ${payment.id}: ${e.message}", e)
        }
    }
}
