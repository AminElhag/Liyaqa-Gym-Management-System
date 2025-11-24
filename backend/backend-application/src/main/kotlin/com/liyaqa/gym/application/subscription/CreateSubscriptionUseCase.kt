package com.liyaqa.gym.application.subscription

import com.liyaqa.gym.application.subscription.commands.CreateSubscriptionCommand
import com.liyaqa.gym.application.subscription.dto.SubscriptionDTO
import com.liyaqa.gym.application.subscription.dto.SubscriptionMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.*
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.PaymentProcessedEvent
import com.liyaqa.gym.domain.events.SubscriptionCreatedEvent
import com.liyaqa.gym.domain.repositories.MemberRepository
import com.liyaqa.gym.domain.repositories.MembershipPlanRepository
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.domain.payment.PaymentGatewayFactory
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

/**
 * Use case for creating a new subscription.
 *
 * This use case handles:
 * - Member validation (exists and active)
 * - Plan validation (exists and active)
 * - End date calculation based on plan duration
 * - Initial payment processing
 * - Subscription entity creation
 * - Domain event publishing
 * - Access permissions (handled via events)
 *
 * @property subscriptionRepository Repository for subscription persistence
 * @property memberRepository Repository for member lookups
 * @property planRepository Repository for plan lookups
 * @property paymentRepository Repository for payment persistence
 * @property paymentGatewayFactory Factory for payment gateway selection
 * @property eventPublisher Publisher for domain events
 * @property subscriptionMapper Mapper for DTO conversion
 */
@Service
@Transactional
class CreateSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val memberRepository: MemberRepository,
    private val planRepository: MembershipPlanRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val eventPublisher: EventPublisher,
    private val subscriptionMapper: SubscriptionMapper
) {

    private val logger = LoggerFactory.getLogger(CreateSubscriptionUseCase::class.java)

    /**
     * Executes the create subscription use case.
     *
     * @param command The create subscription command
     * @return Result containing the created subscription DTO or error
     */
    @CachePut(
        value = ["subscription"],
        key = "#result.getOrNull()?.id",
        condition = "#result.isSuccess"
    )
    fun execute(command: CreateSubscriptionCommand): Result<SubscriptionDTO> {
        return runCatching {
            logger.info("Creating subscription for member: ${command.memberId} with plan: ${command.planId}")

            // 1. Validate member exists and is active
            val member = validateMember(command.memberId)

            // 2. Validate plan exists and is active
            val plan = validatePlan(command.planId)

            // 3. Calculate end date based on plan duration
            val endDate = calculateEndDate(command.startDate, plan)

            // 4. Calculate remaining visits for visit-based plans
            val remainingVisits = if (plan.isVisitBased()) plan.visitCount else null

            // 5. Process initial payment
            val payment = processPayment(member, plan, command)

            // 6. Create subscription entity
            val subscription = Subscription.create(
                memberId = command.memberId,
                planId = command.planId,
                startDate = command.startDate,
                endDate = endDate,
                autoRenew = command.autoRenew,
                remainingVisits = remainingVisits
            )

            // 7. Persist subscription
            val savedSubscription = subscriptionRepository.save(subscription)
                .getOrElse { error ->
                    logger.error("Failed to save subscription to repository: ${error.message}", error)
                    throw error
                }

            logger.info("Subscription created successfully with ID: ${savedSubscription.id}")

            // 8. Publish domain events
            publishSubscriptionCreatedEvent(savedSubscription, plan)
            publishPaymentProcessedEvent(payment)

            // 9. Convert to DTO and return
            subscriptionMapper.toDTO(savedSubscription)

        }.onFailure { error ->
            logger.error("Failed to create subscription: ${error.message}", error)
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
    private fun validateMember(memberId: java.util.UUID): Member {
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
            logger.warn("Attempted to create subscription for inactive member: $memberId (status: ${member.status})")
            throw ValidationException("Cannot create subscription for inactive member (status: ${member.status})")
        }

        logger.debug("Member validation passed for: $memberId")
        return member
    }

    /**
     * Validates that the plan exists and is active.
     *
     * @param planId The plan identifier
     * @return The validated plan entity
     * @throws ResourceNotFoundException if plan not found
     * @throws ValidationException if plan is not active
     */
    private fun validatePlan(planId: java.util.UUID): MembershipPlan {
        val planOptional = planRepository.findById(planId)
            .getOrElse { error ->
                logger.error("Failed to query plan repository: ${error.message}", error)
                throw error
            }

        if (!planOptional.isPresent) {
            logger.warn("Membership plan not found: $planId")
            throw ResourceNotFoundException("Membership plan with ID $planId not found")
        }

        val plan = planOptional.get()

        if (!plan.isActive) {
            logger.warn("Attempted to create subscription with inactive plan: $planId")
            throw ValidationException("Cannot create subscription with inactive plan")
        }

        logger.debug("Plan validation passed for: $planId")
        return plan
    }

    /**
     * Calculates the subscription end date based on plan duration.
     *
     * @param startDate The subscription start date
     * @param plan The membership plan
     * @return The calculated end date, or null for visit-based plans without duration
     */
    private fun calculateEndDate(
        startDate: java.time.LocalDate,
        plan: MembershipPlan
    ): java.time.LocalDate? {
        return when {
            plan.isDurationBased() || plan.isTimeRestricted() -> {
                val endDate = startDate.plusDays(plan.durationDays!!.toLong())
                logger.debug("Calculated end date: $endDate for ${plan.durationDays} days duration")
                endDate
            }
            plan.isVisitBased() && plan.durationDays != null -> {
                val duration = plan.durationDays!!
                val endDate = startDate.plusDays(duration.toLong())
                logger.debug("Calculated end date: $endDate for visit-based plan with duration")
                endDate
            }
            else -> {
                logger.debug("No end date calculated for visit-based plan without duration")
                null
            }
        }
    }

    /**
     * Processes the initial payment for the subscription.
     *
     * @param member The member entity
     * @param plan The membership plan
     * @param command The create subscription command
     * @return The created payment entity
     * @throws ValidationException if payment processing fails
     */
    private fun processPayment(
        member: Member,
        plan: MembershipPlan,
        command: CreateSubscriptionCommand
    ): Payment {
        logger.info("Processing payment of ${plan.price} via ${command.paymentMethod} for member: ${member.id}")

        return try {
            // Get the appropriate payment gateway
            val gateway = paymentGatewayFactory.getGateway(command.paymentMethod.name.lowercase())

            // Prepare payment metadata
            val metadata = command.paymentMetadata.toMutableMap().apply {
                put("memberId", member.id.toString())
                put("planId", plan.id.toString())
                put("memberName", member.name)
                put("planName", plan.name)
            }

            // Process payment through gateway
            val paymentResult = gateway.processPayment(
                amount = plan.price.amount,
                currency = plan.price.currency.currencyCode,
                method = command.paymentMethod.name.lowercase(),
                metadata = metadata
            )

            if (!paymentResult.success) {
                logger.error("Payment processing failed: ${paymentResult.errorMessage}")
                throw ValidationException("Payment failed: ${paymentResult.errorMessage ?: "Unknown error"}")
            }

            // Calculate VAT (15% for Saudi Arabia)
            val vat = com.liyaqa.gym.domain.valueobjects.VAT.calculateSaudiVAT(plan.price)

            // Generate invoice number
            val invoiceNumber = com.liyaqa.gym.domain.entities.Payment.generateInvoiceNumber()

            // Create payment entity
            val payment = Payment.create(
                memberId = member.id,
                organizationId = member.organizationId,
                branchId = member.branchId,
                amount = plan.price,
                vat = vat,
                method = command.paymentMethod,
                invoiceNumber = invoiceNumber,
                subscriptionId = null,
                ptSessionId = null,
                description = "Subscription payment for plan: ${plan.name}"
            )

            // Mark payment as completed
            val completedPayment = payment.markAsPaid(paymentResult.gatewayPaymentId)

            // Persist payment
            val savedPayment = paymentRepository.save(completedPayment)
                .getOrElse { error ->
                    logger.error("Failed to save payment to repository: ${error.message}", error)
                    throw error
                }

            logger.info("Payment processed successfully: ${savedPayment.id}")
            savedPayment

        } catch (e: Exception) {
            logger.error("Payment processing failed: ${e.message}", e)
            when (e) {
                is ValidationException -> throw e
                else -> throw ValidationException("Payment processing failed: ${e.message}")
            }
        }
    }

    /**
     * Publishes the SubscriptionCreatedEvent after successful creation.
     *
     * @param subscription The created subscription
     * @param plan The membership plan
     */
    private fun publishSubscriptionCreatedEvent(subscription: Subscription, plan: MembershipPlan) {
        try {
            val event = SubscriptionCreatedEvent(
                subscriptionId = subscription.id,
                memberId = subscription.memberId,
                planId = subscription.planId,
                startDate = subscription.startDate,
                endDate = subscription.endDate ?: subscription.startDate.plusYears(10) // Fallback for visit-based plans
            )

            eventPublisher.publish(event)
            logger.info("Published SubscriptionCreatedEvent for subscription: ${subscription.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish SubscriptionCreatedEvent for subscription ${subscription.id}: ${e.message}", e)
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
            logger.info("Published PaymentProcessedEvent for payment: ${payment.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish PaymentProcessedEvent for payment ${payment.id}: ${e.message}", e)
        }
    }
}
