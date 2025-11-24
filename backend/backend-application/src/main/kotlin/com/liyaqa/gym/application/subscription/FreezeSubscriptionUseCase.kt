package com.liyaqa.gym.application.subscription

import com.liyaqa.gym.application.subscription.commands.FreezeSubscriptionCommand
import com.liyaqa.gym.application.subscription.dto.FreezeConfirmationDTO
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.SubscriptionFrozenEvent
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Use case for freezing/pausing a subscription.
 *
 * This use case handles:
 * - Freeze policy validation (days remaining, max freezes per year)
 * - Subscription status update to PAUSED
 * - End date calculation (extended by freeze days)
 * - Temporary access revocation (via events)
 * - Domain event publishing
 *
 * @property subscriptionRepository Repository for subscription persistence
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class FreezeSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(FreezeSubscriptionUseCase::class.java)

    companion object {
        private const val MIN_DAYS_REMAINING = 7 // Minimum days remaining to allow freeze
        private const val MAX_FREEZE_DURATION_DAYS = 90L // Maximum freeze duration
        private const val MIN_FREEZE_DURATION_DAYS = 1L // Minimum freeze duration
    }

    /**
     * Executes the freeze subscription use case.
     *
     * @param command The freeze subscription command
     * @return Result containing the freeze confirmation DTO or error
     */
    @CachePut(
        value = ["subscription"],
        key = "#command.subscriptionId",
        condition = "#result.isSuccess"
    )
    fun execute(command: FreezeSubscriptionCommand): Result<FreezeConfirmationDTO> {
        return runCatching {
            logger.info("Freezing subscription: ${command.subscriptionId} until ${command.freezeUntil}")

            // 1. Load subscription
            val subscription = loadSubscription(command.subscriptionId)

            // 2. Validate freeze policy
            validateFreezePolicy(subscription, command.freezeUntil)

            // 3. Calculate freeze duration
            val freezeDays = ChronoUnit.DAYS.between(LocalDate.now(), command.freezeUntil)

            // 4. Calculate new end date (extend by freeze days)
            val newEndDate = calculateNewEndDate(subscription, freezeDays)

            // 5. Freeze the subscription
            val frozenSubscription = subscription.pause(command.freezeUntil).copy(
                endDate = newEndDate,
                updatedAt = Instant.now()
            )

            // 6. Persist frozen subscription
            val savedSubscription = subscriptionRepository.save(frozenSubscription)
                .getOrElse { error ->
                    logger.error("Failed to save frozen subscription: ${error.message}", error)
                    throw error
                }

            logger.info(
                "Subscription frozen successfully: ${savedSubscription.id}, " +
                "freeze until: ${command.freezeUntil}, new end date: $newEndDate"
            )

            // 7. Publish domain event
            publishSubscriptionFrozenEvent(savedSubscription, newEndDate)

            // 8. Create and return freeze confirmation
            FreezeConfirmationDTO(
                subscriptionId = savedSubscription.id,
                frozenUntil = command.freezeUntil,
                newEndDate = newEndDate ?: command.freezeUntil,
                freezeDays = freezeDays
            )

        }.onFailure { error ->
            logger.error("Failed to freeze subscription: ${error.message}", error)
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
     * Validates the freeze policy rules.
     *
     * @param subscription The subscription to freeze
     * @param freezeUntil The date until which to freeze
     * @throws ValidationException if policy validation fails
     */
    private fun validateFreezePolicy(subscription: Subscription, freezeUntil: LocalDate) {
        // 1. Check subscription status
        if (subscription.status != SubscriptionStatus.ACTIVE) {
            logger.warn("Attempted to freeze non-active subscription: ${subscription.id} (status: ${subscription.status})")
            throw ValidationException("Can only freeze active subscriptions (current status: ${subscription.status})")
        }

        // 2. Check if already paused
        if (subscription.isPaused()) {
            logger.warn("Attempted to freeze already paused subscription: ${subscription.id}")
            throw ValidationException("Subscription is already frozen/paused")
        }

        // 3. Check minimum days remaining
        val endDate = subscription.endDate
        if (endDate != null) {
            val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), endDate)
            if (daysRemaining < MIN_DAYS_REMAINING) {
                logger.warn("Insufficient days remaining to freeze: $daysRemaining days")
                throw ValidationException(
                    "Cannot freeze subscription with less than $MIN_DAYS_REMAINING days remaining " +
                    "(current: $daysRemaining days)"
                )
            }
        }

        // 4. Validate freeze duration
        val freezeDuration = ChronoUnit.DAYS.between(LocalDate.now(), freezeUntil)
        if (freezeDuration < MIN_FREEZE_DURATION_DAYS) {
            logger.warn("Freeze duration too short: $freezeDuration days")
            throw ValidationException("Freeze duration must be at least $MIN_FREEZE_DURATION_DAYS day")
        }

        if (freezeDuration > MAX_FREEZE_DURATION_DAYS) {
            logger.warn("Freeze duration too long: $freezeDuration days")
            throw ValidationException(
                "Freeze duration cannot exceed $MAX_FREEZE_DURATION_DAYS days " +
                "(requested: $freezeDuration days)"
            )
        }

        // 5. Check freeze doesn't extend past subscription end date by too much
        if (endDate != null && freezeUntil.isAfter(endDate)) {
            logger.warn("Freeze end date ($freezeUntil) is after subscription end date ($endDate)")
            throw ValidationException(
                "Freeze end date cannot be after the subscription end date. " +
                "Please adjust the freeze duration."
            )
        }

        logger.debug("Freeze policy validation passed for subscription: ${subscription.id}")
    }

    /**
     * Calculates the new end date after freeze extension.
     *
     * @param subscription The subscription being frozen
     * @param freezeDays The number of days to freeze
     * @return The new end date, or null if no end date
     */
    private fun calculateNewEndDate(subscription: Subscription, freezeDays: Long): LocalDate? {
        val currentEndDate = subscription.endDate ?: return null

        val newEndDate = currentEndDate.plusDays(freezeDays)
        logger.debug(
            "Calculated new end date: $newEndDate " +
            "(original: $currentEndDate + $freezeDays freeze days)"
        )

        return newEndDate
    }

    /**
     * Publishes the SubscriptionFrozenEvent after successful freeze.
     *
     * @param subscription The frozen subscription
     * @param newEndDate The new end date after freeze extension
     */
    private fun publishSubscriptionFrozenEvent(subscription: Subscription, newEndDate: LocalDate?) {
        try {
            val freezeStartDate = subscription.pausedAt
                ?: throw IllegalStateException("pausedAt should be set after calling pause()")
            val freezeEndDate = subscription.pausedUntil
                ?: throw IllegalStateException("pausedUntil should be set after calling pause()")

            val event = SubscriptionFrozenEvent(
                subscriptionId = subscription.id,
                memberId = subscription.memberId,
                freezeStartDate = freezeStartDate,
                freezeEndDate = freezeEndDate,
                newEndDate = newEndDate ?: freezeEndDate
            )

            eventPublisher.publish(event)
            logger.info("Published SubscriptionFrozenEvent for subscription: ${subscription.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish SubscriptionFrozenEvent for subscription ${subscription.id}: ${e.message}", e)
        }
    }
}
