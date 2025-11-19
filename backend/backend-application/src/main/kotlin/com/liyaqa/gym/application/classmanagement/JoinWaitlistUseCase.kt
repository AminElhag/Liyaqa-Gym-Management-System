package com.liyaqa.gym.application.classmanagement

import com.liyaqa.gym.application.classmanagement.commands.JoinWaitlistCommand
import com.liyaqa.gym.application.classmanagement.dto.WaitlistPosition
import com.liyaqa.gym.common.exception.ConflictException
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.*
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.WaitlistJoinedEvent
import com.liyaqa.gym.domain.repositories.*
import com.liyaqa.gym.domain.services.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for joining a class waitlist.
 *
 * This use case handles:
 * - Member validation (exists and has active subscription)
 * - Schedule validation (exists and is active)
 * - Class capacity verification (must be full to join waitlist)
 * - Duplicate waitlist entry prevention
 * - Waitlist position calculation
 * - Waitlist booking creation
 * - Domain event publishing
 * - Waitlist notification
 *
 * @property memberRepository Repository for member lookups
 * @property subscriptionRepository Repository for subscription lookups
 * @property classRepository Repository for class lookups
 * @property scheduleRepository Repository for schedule operations
 * @property bookingRepository Repository for booking persistence
 * @property eventPublisher Publisher for domain events
 * @property notificationService Service for sending notifications
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
class JoinWaitlistUseCase(
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val classRepository: ClassRepository,
    private val scheduleRepository: ClassScheduleRepository,
    private val bookingRepository: BookingRepository,
    private val eventPublisher: EventPublisher,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(JoinWaitlistUseCase::class.java)

    /**
     * Executes the join waitlist use case.
     *
     * @param command The join waitlist command
     * @return Result containing the waitlist position or error
     */
    fun execute(command: JoinWaitlistCommand): Result<WaitlistPosition> {
        return runCatching {
            logger.info("Adding member ${command.memberId} to waitlist for schedule: ${command.scheduleId}")

            // 1. Validate member exists and has active subscription
            val member = validateMember(command.memberId)
            validateActiveSubscription(command.memberId)

            // 2. Validate schedule exists and is active
            val schedule = validateSchedule(command.scheduleId)

            // 3. Get class details
            val gymClass = validateClass(schedule.classId)

            // 4. Verify class is full (can't join waitlist if not full)
            validateClassIsFull(schedule, gymClass.capacity)

            // 5. Check if member already has a booking or waitlist entry
            validateNoExistingEntry(command.memberId, command.scheduleId)

            // 6. Calculate waitlist position
            val position = schedule.waitingList + 1

            // 7. Create waitlisted booking
            val booking = Booking.createWaitlisted(
                memberId = command.memberId,
                scheduleId = command.scheduleId,
                waitlistPosition = position
            )

            // 8. Save booking
            val savedBooking = bookingRepository.save(booking)
                .getOrElse { error ->
                    logger.error("Failed to save waitlist booking: ${error.message}", error)
                    throw error
                }

            // 9. Update schedule waitlist count
            val updatedSchedule = schedule.addToWaitingList()
            scheduleRepository.save(updatedSchedule)
                .getOrElse { error ->
                    logger.error("Failed to update schedule: ${error.message}", error)
                    throw error
                }

            // 10. Publish waitlist joined event
            publishWaitlistJoinedEvent(command.memberId, command.scheduleId, position)

            // 11. Send waitlist notification
            sendWaitlistNotification(command.memberId, command.scheduleId, position)

            logger.info("Member added to waitlist successfully: ${savedBooking.id}, position: $position")

            // 12. Return waitlist position
            WaitlistPosition(
                bookingId = savedBooking.id,
                position = position,
                estimatedWaitTime = estimateWaitTime(position)
            )

        }.onFailure { error ->
            logger.error("Failed to join waitlist: ${error.message}", error)
        }
    }

    /**
     * Validates that the member exists and is active.
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
            logger.warn("Attempted to join waitlist for inactive member: $memberId")
            throw ValidationException("Cannot join waitlist for inactive member")
        }

        logger.debug("Member validation passed for: $memberId")
        return member
    }

    /**
     * Validates that the member has an active subscription.
     */
    private fun validateActiveSubscription(memberId: java.util.UUID) {
        val subscriptions = subscriptionRepository.findByMember(memberId)
            .getOrElse { error ->
                logger.error("Failed to query subscriptions: ${error.message}", error)
                throw error
            }

        val hasActiveSubscription = subscriptions.any { it.status == SubscriptionStatus.ACTIVE }

        if (!hasActiveSubscription) {
            logger.warn("Member $memberId has no active subscription")
            throw ValidationException("Member must have an active subscription to join waitlist")
        }

        logger.debug("Active subscription found for member: $memberId")
    }

    /**
     * Validates that the schedule exists and is active.
     */
    private fun validateSchedule(scheduleId: java.util.UUID): ClassSchedule {
        val scheduleOptional = scheduleRepository.findById(scheduleId)
            .getOrElse { error ->
                logger.error("Failed to query schedule repository: ${error.message}", error)
                throw error
            }

        if (!scheduleOptional.isPresent) {
            logger.warn("Schedule not found: $scheduleId")
            throw ResourceNotFoundException("Schedule with ID $scheduleId not found")
        }

        val schedule = scheduleOptional.get()

        if (!schedule.isActive()) {
            logger.warn("Attempted to join waitlist for inactive schedule: $scheduleId")
            throw ValidationException("Cannot join waitlist for inactive or cancelled schedule")
        }

        logger.debug("Schedule validation passed for: $scheduleId")
        return schedule
    }

    /**
     * Validates that the class exists and is active.
     */
    private fun validateClass(classId: java.util.UUID): Class {
        val classOptional = classRepository.findById(classId)
            .getOrElse { error ->
                logger.error("Failed to query class repository: ${error.message}", error)
                throw error
            }

        if (!classOptional.isPresent) {
            logger.warn("Class not found: $classId")
            throw ResourceNotFoundException("Class with ID $classId not found")
        }

        val gymClass = classOptional.get()

        if (!gymClass.isActive) {
            logger.warn("Attempted to join waitlist for inactive class: $classId")
            throw ValidationException("Cannot join waitlist for inactive class")
        }

        logger.debug("Class validation passed for: $classId")
        return gymClass
    }

    /**
     * Validates that the class is full.
     */
    private fun validateClassIsFull(schedule: ClassSchedule, capacity: Int) {
        if (!schedule.isFull(capacity)) {
            logger.warn("Attempted to join waitlist for class with available spots")
            throw ValidationException(
                "Class has ${schedule.availableSpots(capacity)} available spots. " +
                "Please book the class directly instead of joining the waitlist."
            )
        }

        logger.debug("Class is full, waitlist is available")
    }

    /**
     * Validates that the member doesn't already have a booking or waitlist entry.
     */
    private fun validateNoExistingEntry(memberId: java.util.UUID, scheduleId: java.util.UUID) {
        val existingBookings = bookingRepository.findBySchedule(scheduleId)
            .getOrElse { error ->
                logger.error("Failed to query bookings: ${error.message}", error)
                throw error
            }

        val hasExistingEntry = existingBookings.any {
            it.memberId == memberId && !it.isCancelled()
        }

        if (hasExistingEntry) {
            logger.warn("Member $memberId already has an entry for schedule $scheduleId")
            throw ConflictException("You already have a booking or waitlist entry for this class")
        }

        logger.debug("No existing entry found for member: $memberId")
    }

    /**
     * Publishes the WaitlistJoinedEvent.
     */
    private fun publishWaitlistJoinedEvent(
        memberId: java.util.UUID,
        scheduleId: java.util.UUID,
        position: Int
    ) {
        try {
            val event = WaitlistJoinedEvent(
                memberId = memberId,
                scheduleId = scheduleId,
                position = position
            )

            eventPublisher.publish(event)
            logger.info("Published WaitlistJoinedEvent for member: $memberId, position: $position")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish WaitlistJoinedEvent: ${e.message}", e)
        }
    }

    /**
     * Sends waitlist notification to member.
     */
    private fun sendWaitlistNotification(
        memberId: java.util.UUID,
        scheduleId: java.util.UUID,
        position: Int
    ) {
        try {
            notificationService.sendWaitlistNotification(memberId, scheduleId, position)
                .getOrElse { error ->
                    logger.warn("Failed to send waitlist notification: ${error.message}")
                }
        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to send waitlist notification: ${e.message}", e)
        }
    }

    /**
     * Estimates wait time based on position.
     * Simple heuristic: assumes 1 cancellation per week per 10 spots.
     */
    private fun estimateWaitTime(position: Int): String {
        return when {
            position <= 3 -> "Within 24-48 hours"
            position <= 7 -> "Within 3-5 days"
            position <= 15 -> "Within 1-2 weeks"
            else -> "More than 2 weeks"
        }
    }
}
