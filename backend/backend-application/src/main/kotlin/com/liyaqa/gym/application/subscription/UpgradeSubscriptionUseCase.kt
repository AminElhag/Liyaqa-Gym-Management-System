package com.liyaqa.gym.application.subscription

import com.liyaqa.gym.application.subscription.commands.UpgradeSubscriptionCommand
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
import com.liyaqa.gym.domain.events.SubscriptionUpgradedEvent
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
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Use case for upgrading a subscription to a different plan.
 *
 * This use case handles:
 * - Plan validation (new plan must be different and active)
 * - Proration calculation (credit for unused time on old plan)
 * - Payment processing for the difference
 * - Plan switch and subscription update
 * - Access permissions update (via events)
 * - Domain event publishing
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
class UpgradeSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val planRepository: MembershipPlanRepository,
    private val paymentRepository: PaymentRepository,
    private val memberRepository: com.liyaqa.gym.domain.repositories.MemberRepository,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val eventPublisher: EventPublisher,
    private val subscriptionMapper: SubscriptionMapper
) {

    private val logger = LoggerFactory.getLogger(UpgradeSubscriptionUseCase::class.java)

    /**
     * Executes the upgrade subscription use case.
     *
     * @param command The upgrade subscription command
     * @return Result containing the upgraded subscription DTO or error
     */
    @CachePut(
        value = ["subscription"],
        key = "#result.getOrNull()?.id",
        condition = "#result.isSuccess"
    )
    fun execute(command: UpgradeSubscriptionCommand): Result<SubscriptionDTO> {
        return runCatching {
            logger.info("Upgrading subscription: ${command.subscriptionId} to plan: ${command.newPlanId}")

            // 1. Load subscription
            val subscription = loadSubscription(command.subscriptionId)

            // 2. Validate upgrade eligibility
            validateUpgradeEligibility(subscription, command.newPlanId)

            // 3. Load old and new plans
            val oldPlan = loadPlan(subscription.planId)
            val newPlan = loadPlan(command.newPlanId)

            // 4. Validate plan compatibility
            validatePlanCompatibility(oldPlan, newPlan)

            // 5. Calculate proration
            val prorationAmount = calculateProration(subscription, oldPlan, newPlan)

            // 6. Process payment if proration is positive (upgrade costs more)
            if (prorationAmount.isPositive()) {
                processUpgradePayment(subscription, newPlan, prorationAmount, command)
            }

            // 7. Update subscription with new plan
            val upgradedSubscription = upgradeSubscription(subscription, oldPlan, newPlan)

            // 8. Persist upgraded subscription
            val savedSubscription = subscriptionRepository.save(upgradedSubscription)
                .getOrElse { error ->
                    logger.error("Failed to save upgraded subscription: ${error.message}", error)
                    throw error
                }

            logger.info(
                "Subscription upgraded successfully: ${savedSubscription.id}, " +
                "from plan ${oldPlan.id} to ${newPlan.id}, proration: $prorationAmount"
            )

            // 9. Publish domain events
            publishSubscriptionUpgradedEvent(savedSubscription, oldPlan.id, newPlan.id, prorationAmount)

            // 10. Convert to DTO and return
            subscriptionMapper.toDTO(savedSubscription)

        }.onFailure { error ->
            logger.error("Failed to upgrade subscription: ${error.message}", error)
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
     * Validates that the subscription is eligible for upgrade.
     *
     * @param subscription The subscription to upgrade
     * @param newPlanId The new plan identifier
     * @throws ValidationException if upgrade is not allowed
     */
    private fun validateUpgradeEligibility(subscription: Subscription, newPlanId: java.util.UUID) {
        // 1. Check subscription status
        if (subscription.status != SubscriptionStatus.ACTIVE) {
            logger.warn("Attempted to upgrade non-active subscription: ${subscription.id} (status: ${subscription.status})")
            throw ValidationException("Can only upgrade active subscriptions (current status: ${subscription.status})")
        }

        // 2. Check if upgrading to the same plan
        if (subscription.planId == newPlanId) {
            logger.warn("Attempted to upgrade to the same plan: $newPlanId")
            throw ValidationException("New plan must be different from current plan")
        }

        // 3. Check if subscription has expired
        if (subscription.isExpired()) {
            logger.warn("Attempted to upgrade expired subscription: ${subscription.id}")
            throw ValidationException("Cannot upgrade an expired subscription. Please renew instead.")
        }

        logger.debug("Upgrade eligibility validation passed for subscription: ${subscription.id}")
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

        val plan = planOptional.get()

        if (!plan.isActive) {
            logger.warn("Attempted to upgrade to inactive plan: $planId")
            throw ValidationException("Cannot upgrade to an inactive plan")
        }

        return plan
    }

    /**
     * Validates that the old and new plans are compatible for upgrade.
     *
     * @param oldPlan The current plan
     * @param newPlan The new plan
     * @throws ValidationException if plans are incompatible
     */
    private fun validatePlanCompatibility(oldPlan: MembershipPlan, newPlan: MembershipPlan) {
        // Plans must be from the same branch
        if (oldPlan.branchId != newPlan.branchId) {
            logger.warn(
                "Attempted to upgrade between different branches: " +
                "${oldPlan.branchId} -> ${newPlan.branchId}"
            )
            throw ValidationException("Cannot upgrade to a plan from a different branch")
        }

        // For visit-based plans, both should be visit-based
        if (oldPlan.isVisitBased() && !newPlan.isVisitBased()) {
            logger.warn("Attempted to upgrade from visit-based to non-visit-based plan")
            throw ValidationException(
                "Cannot upgrade from a visit-based plan to a time-based plan. " +
                "Please cancel and create a new subscription."
            )
        }

        logger.debug("Plan compatibility validation passed")
    }

    /**
     * Calculates the prorated amount for the upgrade.
     * Positive amount = customer pays more (upgrade)
     * Negative amount = customer gets credit (downgrade, but we don't support downgrades)
     *
     * @param subscription The current subscription
     * @param oldPlan The current plan
     * @param newPlan The new plan
     * @return The prorated amount to charge
     */
    private fun calculateProration(
        subscription: Subscription,
        oldPlan: MembershipPlan,
        newPlan: MembershipPlan
    ): Money {
        val endDate = subscription.endDate
        if (endDate == null) {
            // For visit-based plans without end date, charge full new plan price
            logger.debug("No end date, charging full new plan price: ${newPlan.price}")
            return newPlan.price
        }

        val now = LocalDate.now()
        if (endDate.isBefore(now) || endDate.isEqual(now)) {
            // Subscription has ended, charge full price
            logger.debug("Subscription ended, charging full new plan price: ${newPlan.price}")
            return newPlan.price
        }

        // Calculate remaining days
        val totalDays = ChronoUnit.DAYS.between(subscription.startDate, endDate)
        val remainingDays = ChronoUnit.DAYS.between(now, endDate)

        if (totalDays <= 0 || remainingDays <= 0) {
            logger.debug("Invalid date range, charging full new plan price")
            return newPlan.price
        }

        // Calculate unused value from old plan
        val oldPlanDailyRate = oldPlan.price.amount.divide(BigDecimal(totalDays), 4, RoundingMode.HALF_UP)
        val unusedOldPlanValue = oldPlanDailyRate.multiply(BigDecimal(remainingDays))

        // Calculate cost for remaining period on new plan
        val newPlanDailyRate = newPlan.price.amount.divide(
            BigDecimal.valueOf((newPlan.durationDays ?: totalDays).toLong()),
            4,
            RoundingMode.HALF_UP
        )
        val newPlanCostForRemaining = newPlanDailyRate.multiply(BigDecimal(remainingDays))

        // Proration = new plan cost - unused old plan value
        val prorationAmount = newPlanCostForRemaining.subtract(unusedOldPlanValue)

        logger.debug(
            "Proration calculation: " +
            "totalDays=$totalDays, remainingDays=$remainingDays, " +
            "unusedOldValue=$unusedOldPlanValue, newCost=$newPlanCostForRemaining, " +
            "proration=$prorationAmount"
        )

        return Money.of(
            prorationAmount.max(BigDecimal.ZERO), // Never charge negative (no downgrades)
            newPlan.price.currency.currencyCode
        )
    }

    /**
     * Processes the upgrade payment.
     *
     * @param subscription The subscription being upgraded
     * @param newPlan The new plan
     * @param prorationAmount The amount to charge
     * @param command The upgrade command
     * @return The created payment entity
     * @throws ValidationException if payment processing fails
     */
    private fun processUpgradePayment(
        subscription: Subscription,
        newPlan: MembershipPlan,
        prorationAmount: Money,
        command: UpgradeSubscriptionCommand
    ): Payment {
        logger.info("Processing upgrade payment of $prorationAmount via ${command.paymentMethod}")

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
                put("oldPlanId", subscription.planId.toString())
                put("newPlanId", newPlan.id.toString())
                put("newPlanName", newPlan.name)
                put("paymentType", "upgrade")
            }

            // Process payment through gateway
            val paymentResult = gateway.processPayment(
                amount = prorationAmount.amount,
                currency = prorationAmount.currency.currencyCode,
                method = command.paymentMethod.name.lowercase(),
                metadata = metadata
            )

            if (!paymentResult.success) {
                logger.error("Upgrade payment processing failed: ${paymentResult.errorMessage}")
                throw ValidationException("Payment failed: ${paymentResult.errorMessage ?: "Unknown error"}")
            }

            // Calculate VAT (15% for Saudi Arabia)
            val vat = com.liyaqa.gym.domain.valueobjects.VAT.calculateSaudiVAT(prorationAmount)

            // Generate invoice number
            val invoiceNumber = com.liyaqa.gym.domain.entities.Payment.generateInvoiceNumber()

            // Create payment entity
            val payment = Payment.create(
                tenantId = member.organizationId,
                memberId = subscription.memberId,
                organizationId = member.organizationId,
                branchId = member.branchId,
                amount = prorationAmount,
                vat = vat,
                method = command.paymentMethod,
                invoiceNumber = invoiceNumber,
                subscriptionId = subscription.id,
                ptSessionId = null,
                description = "Subscription upgrade to plan: ${newPlan.name}"
            )

            // Mark payment as completed
            val completedPayment = payment.markAsPaid(paymentResult.gatewayPaymentId)

            // Persist payment
            val savedPayment = paymentRepository.save(completedPayment)
                .getOrElse { error ->
                    logger.error("Failed to save upgrade payment: ${error.message}", error)
                    throw error
                }

            logger.info("Upgrade payment processed successfully: ${savedPayment.id}")
            savedPayment

        } catch (e: Exception) {
            logger.error("Upgrade payment processing failed: ${e.message}", e)
            when (e) {
                is ValidationException -> throw e
                else -> throw ValidationException("Payment processing failed: ${e.message}")
            }
        }
    }

    /**
     * Upgrades the subscription to the new plan.
     *
     * @param subscription The current subscription
     * @param oldPlan The current plan
     * @param newPlan The new plan
     * @return The upgraded subscription
     */
    private fun upgradeSubscription(
        subscription: Subscription,
        oldPlan: MembershipPlan,
        newPlan: MembershipPlan
    ): Subscription {
        // Update plan ID
        var upgraded = subscription.copy(
            planId = newPlan.id,
            updatedAt = Instant.now()
        )

        // Update visit count if new plan is visit-based
        if (newPlan.isVisitBased()) {
            upgraded = upgraded.copy(remainingVisits = newPlan.visitCount)
        }

        // Recalculate end date if plans have different durations
        newPlan.durationDays?.let { duration ->
            if (oldPlan.durationDays != duration) {
                val now = LocalDate.now()
                val newEndDate = now.plusDays(duration.toLong())
                upgraded = upgraded.copy(endDate = newEndDate)
                logger.debug("Updated end date to: $newEndDate based on new plan duration")
            }
        }

        return upgraded
    }

    /**
     * Publishes the SubscriptionUpgradedEvent after successful upgrade.
     *
     * @param subscription The upgraded subscription
     * @param oldPlanId The old plan identifier
     * @param newPlanId The new plan identifier
     * @param prorationAmount The prorated amount charged
     */
    private fun publishSubscriptionUpgradedEvent(
        subscription: Subscription,
        oldPlanId: java.util.UUID,
        newPlanId: java.util.UUID,
        prorationAmount: Money
    ) {
        try {
            val event = SubscriptionUpgradedEvent(
                subscriptionId = subscription.id,
                memberId = subscription.memberId,
                oldPlanId = oldPlanId,
                newPlanId = newPlanId,
                prorationAmount = prorationAmount
            )

            eventPublisher.publish(event)
            logger.info("Published SubscriptionUpgradedEvent for subscription: ${subscription.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish SubscriptionUpgradedEvent for subscription ${subscription.id}: ${e.message}", e)
        }
    }
}
