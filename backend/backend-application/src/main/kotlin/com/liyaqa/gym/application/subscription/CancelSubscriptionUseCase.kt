package com.liyaqa.gym.application.subscription

import com.liyaqa.gym.application.subscription.commands.CancelSubscriptionCommand
import com.liyaqa.gym.application.subscription.dto.SubscriptionDTO
import com.liyaqa.gym.application.subscription.dto.SubscriptionMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.MembershipPlan
import com.liyaqa.gym.domain.entities.Payment
import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.SubscriptionCancelledEvent
import com.liyaqa.gym.domain.repositories.MembershipPlanRepository
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import com.liyaqa.gym.domain.valueobjects.Money
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Use case for cancelling a subscription.
 *
 * This use case handles:
 * - Cancellation policy validation (notice period)
 * - Refund calculation based on remaining time
 * - Subscription status update to CANCELLED
 * - Access revocation (via events)
 * - Effective cancellation date scheduling
 * - Exit survey notification (via events)
 *
 * @property subscriptionRepository Repository for subscription persistence
 * @property planRepository Repository for plan lookups
 * @property paymentRepository Repository for payment lookups
 * @property eventPublisher Publisher for domain events
 * @property subscriptionMapper Mapper for DTO conversion
 */
@Service
@Transactional
class CancelSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val planRepository: MembershipPlanRepository,
    private val paymentRepository: PaymentRepository,
    private val memberRepository: com.liyaqa.gym.domain.repositories.MemberRepository,
    private val eventPublisher: EventPublisher,
    private val subscriptionMapper: SubscriptionMapper
) {

    private val logger = LoggerFactory.getLogger(CancelSubscriptionUseCase::class.java)

    companion object {
        private const val NOTICE_PERIOD_DAYS = 3L // Minimum notice period for cancellation
        private const val REFUND_ELIGIBILITY_PERCENTAGE = 50 // Refund if >50% time remaining
    }

    /**
     * Executes the cancel subscription use case.
     *
     * @param command The cancel subscription command
     * @return Result containing the cancelled subscription DTO or error
     */
    @CachePut(
        value = ["subscription"],
        key = "#result.getOrNull()?.id",
        condition = "#result.isSuccess"
    )
    fun execute(command: CancelSubscriptionCommand): Result<SubscriptionDTO> {
        return runCatching {
            logger.info("Cancelling subscription: ${command.subscriptionId}, immediate: ${command.immediate}")

            // 1. Load subscription
            val subscription = loadSubscription(command.subscriptionId)

            // 2. Validate cancellation policy
            validateCancellationPolicy(subscription, command.immediate)

            // 3. Load plan for refund calculation
            val plan = loadPlan(subscription.planId)

            // 4. Calculate refund if applicable
            val refundAmount = calculateRefund(subscription, plan)

            // 5. Process refund if applicable
            if (refundAmount != null && refundAmount.isPositive()) {
                processRefund(subscription, refundAmount)
            }

            // 6. Determine effective cancellation date
            val effectiveDate = if (command.immediate) {
                LocalDate.now()
            } else {
                subscription.endDate ?: LocalDate.now().plusDays(NOTICE_PERIOD_DAYS)
            }

            // 7. Cancel the subscription
            val cancelledSubscription = subscription.cancel(command.reason).copy(
                endDate = if (command.immediate) LocalDate.now() else subscription.endDate,
                updatedAt = Instant.now()
            )

            // 8. Persist cancelled subscription
            val savedSubscription = subscriptionRepository.save(cancelledSubscription)
                .getOrElse { error ->
                    logger.error("Failed to save cancelled subscription: ${error.message}", error)
                    throw error
                }

            logger.info(
                "Subscription cancelled successfully: ${savedSubscription.id}, " +
                "effective date: $effectiveDate, refund: $refundAmount"
            )

            // 9. Publish domain event
            publishSubscriptionCancelledEvent(savedSubscription, refundAmount, effectiveDate)

            // 10. Convert to DTO and return
            subscriptionMapper.toDTO(savedSubscription)

        }.onFailure { error ->
            logger.error("Failed to cancel subscription: ${error.message}", error)
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
     * Validates the cancellation policy rules.
     *
     * @param subscription The subscription to cancel
     * @param immediate Whether cancellation is immediate
     * @throws ValidationException if policy validation fails
     */
    private fun validateCancellationPolicy(subscription: Subscription, immediate: Boolean) {
        // 1. Check if already cancelled
        if (subscription.status == SubscriptionStatus.CANCELLED) {
            logger.warn("Attempted to cancel already cancelled subscription: ${subscription.id}")
            throw ValidationException("Subscription is already cancelled")
        }

        // 2. Check if subscription is expired
        if (subscription.isExpired()) {
            logger.warn("Attempted to cancel expired subscription: ${subscription.id}")
            throw ValidationException("Cannot cancel an expired subscription")
        }

        // 3. Check notice period for non-immediate cancellations
        if (!immediate) {
            val endDate = subscription.endDate
            if (endDate != null) {
                val daysUntilEnd = ChronoUnit.DAYS.between(LocalDate.now(), endDate)
                if (daysUntilEnd < NOTICE_PERIOD_DAYS) {
                    logger.info(
                        "Notice period not met ($daysUntilEnd days remaining), " +
                        "but allowing cancellation at end date"
                    )
                }
            }
        }

        logger.debug("Cancellation policy validation passed for subscription: ${subscription.id}")
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
     * Calculates the refund amount based on remaining subscription time.
     *
     * @param subscription The subscription being cancelled
     * @param plan The membership plan
     * @return The refund amount, or null if no refund applicable
     */
    private fun calculateRefund(subscription: Subscription, plan: MembershipPlan): Money? {
        val endDate = subscription.endDate ?: return null
        val now = LocalDate.now()

        if (endDate.isBefore(now) || endDate.isEqual(now)) {
            logger.debug("No refund: subscription has ended or ends today")
            return null
        }

        // Calculate remaining days
        val totalDays = ChronoUnit.DAYS.between(subscription.startDate, endDate)
        val remainingDays = ChronoUnit.DAYS.between(now, endDate)
        val usedDays = totalDays - remainingDays

        if (totalDays <= 0) {
            logger.debug("No refund: invalid date range")
            return null
        }

        // Calculate percentage of time remaining
        val remainingPercentage = (remainingDays.toDouble() / totalDays.toDouble()) * 100

        logger.debug(
            "Refund calculation: total=$totalDays days, used=$usedDays days, " +
            "remaining=$remainingDays days (${remainingPercentage.toInt()}%)"
        )

        // Only refund if more than threshold percentage of time remains
        if (remainingPercentage < REFUND_ELIGIBILITY_PERCENTAGE) {
            logger.info("No refund: less than $REFUND_ELIGIBILITY_PERCENTAGE% of subscription time remaining")
            return null
        }

        // Calculate prorated refund
        val refundAmount = plan.price.amount
            .multiply(BigDecimal(remainingDays))
            .divide(BigDecimal(totalDays), 2, RoundingMode.HALF_UP)

        val refund = Money.of(refundAmount, plan.price.currency.currencyCode)
        logger.info("Calculated refund amount: $refund for $remainingDays remaining days")

        return refund
    }

    /**
     * Processes a refund for the cancelled subscription.
     *
     * @param subscription The subscription being cancelled
     * @param refundAmount The amount to refund
     */
    private fun processRefund(subscription: Subscription, refundAmount: Money) {
        try {
            logger.info("Processing refund of $refundAmount for subscription: ${subscription.id}")

            // Load member to get organization and branch IDs
            val member = memberRepository.findById(subscription.memberId)
                .getOrElse { error ->
                    logger.error("Failed to load member: ${error.message}", error)
                    throw error
                }
                .orElseThrow { ResourceNotFoundException("Member not found: ${subscription.memberId}") }

            // Calculate VAT (15% for Saudi Arabia)
            val vat = com.liyaqa.gym.domain.valueobjects.VAT.calculateSaudiVAT(refundAmount)

            // Generate invoice number
            val invoiceNumber = com.liyaqa.gym.domain.entities.Payment.generateInvoiceNumber()

            // Create a refund payment record
            val refundPayment = Payment.create(
                memberId = subscription.memberId,
                organizationId = member.organizationId,
                branchId = member.branchId,
                amount = refundAmount.times(-1), // Negative amount for refund
                vat = vat.times(-1), // Negative VAT for refund
                method = com.liyaqa.gym.domain.entities.PaymentMethod.BANK_TRANSFER, // Default refund method
                invoiceNumber = invoiceNumber,
                subscriptionId = subscription.id,
                ptSessionId = null,
                description = "Refund for cancelled subscription"
            ).copy(
                status = com.liyaqa.gym.domain.entities.PaymentStatus.COMPLETED
            )

            // Save refund payment
            paymentRepository.save(refundPayment)
                .getOrElse { error ->
                    logger.error("Failed to save refund payment: ${error.message}", error)
                    // Don't fail the cancellation if refund recording fails
                }

            logger.info("Refund processed and recorded: ${refundPayment.id}")

        } catch (e: Exception) {
            // Log error but don't fail the cancellation
            logger.error("Failed to process refund: ${e.message}", e)
        }
    }

    /**
     * Publishes the SubscriptionCancelledEvent after successful cancellation.
     *
     * @param subscription The cancelled subscription
     * @param refundAmount The refund amount (if any)
     * @param effectiveDate The effective cancellation date
     */
    private fun publishSubscriptionCancelledEvent(
        subscription: Subscription,
        refundAmount: Money?,
        effectiveDate: LocalDate
    ) {
        try {
            val event = SubscriptionCancelledEvent(
                subscriptionId = subscription.id,
                memberId = subscription.memberId,
                cancellationReason = subscription.cancellationReason,
                refundAmount = refundAmount,
                effectiveDate = effectiveDate
            )

            eventPublisher.publish(event)
            logger.info("Published SubscriptionCancelledEvent for subscription: ${subscription.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish SubscriptionCancelledEvent for subscription ${subscription.id}: ${e.message}", e)
        }
    }
}
