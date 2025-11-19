package com.liyaqa.gym.application.classmanagement

import com.liyaqa.gym.application.classmanagement.commands.BookClassCommand
import com.liyaqa.gym.application.classmanagement.dto.BookingDTO
import com.liyaqa.gym.application.classmanagement.dto.BookingMapper
import com.liyaqa.gym.common.exception.ConflictException
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.*
import com.liyaqa.gym.domain.events.ClassBookedEvent
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.repositories.*
import com.liyaqa.gym.domain.services.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Use case for booking a class for a member.
 *
 * This use case handles:
 * - Member validation (exists and has active subscription)
 * - Schedule validation (exists and is active)
 * - Capacity checking with race condition handling
 * - Duplicate booking prevention
 * - Booking window validation (7 days in advance)
 * - Class credit deduction (if applicable)
 * - Booking creation
 * - Domain event publishing
 * - Confirmation notification
 *
 * Race condition handling:
 * - Uses SERIALIZABLE isolation level for transactions
 * - Optimistic locking on schedule updates
 * - Atomic booking count increments
 *
 * @property memberRepository Repository for member lookups
 * @property subscriptionRepository Repository for subscription lookups
 * @property classRepository Repository for class lookups
 * @property scheduleRepository Repository for schedule operations
 * @property bookingRepository Repository for booking persistence
 * @property eventPublisher Publisher for domain events
 * @property notificationService Service for sending notifications
 * @property bookingMapper Mapper for DTO conversion
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
class BookClassUseCase(
    private val memberRepository: MemberRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val classRepository: ClassRepository,
    private val scheduleRepository: ClassScheduleRepository,
    private val bookingRepository: BookingRepository,
    private val eventPublisher: EventPublisher,
    private val notificationService: NotificationService,
    private val bookingMapper: BookingMapper
) {

    private val logger = LoggerFactory.getLogger(BookClassUseCase::class.java)

    companion object {
        private const val BOOKING_ADVANCE_DAYS = 7L
        private const val LATE_CANCELLATION_HOURS = 2L
    }

    /**
     * Executes the book class use case.
     *
     * @param command The book class command
     * @return Result containing the created booking DTO or error
     */
    fun execute(command: BookClassCommand): Result<BookingDTO> {
        return runCatching {
            logger.info("Booking class for member: ${command.memberId}, schedule: ${command.scheduleId}")

            // 1. Validate member exists and has active subscription
            val member = validateMember(command.memberId)
            val subscription = validateActiveSubscription(command.memberId)

            // 2. Validate schedule exists and is active
            val schedule = validateSchedule(command.scheduleId)

            // 3. Get class details
            val gymClass = validateClass(schedule.classId)

            // 4. Check if member can join class (gender restriction)
            validateGenderRestriction(member, gymClass)

            // 5. Check booking window (can book up to 7 days in advance)
            validateBookingWindow(schedule)

            // 6. Check if member already booked this class
            validateNoDuplicateBooking(command.memberId, command.scheduleId)

            // 7. Check class capacity with race condition handling
            val (booking, updatedSchedule) = atomicBookingCreation(
                command.memberId,
                command.scheduleId,
                schedule,
                gymClass.capacity
            )

            // 8. Deduct class credit if applicable
            deductClassCredit(subscription)

            // 9. Publish domain event
            publishClassBookedEvent(booking)

            // 10. Send confirmation notification
            sendNotification(booking)

            logger.info("Class booked successfully with ID: ${booking.id}")

            // 11. Convert to DTO and return
            bookingMapper.toDTO(booking)

        }.onFailure { error ->
            logger.error("Failed to book class: ${error.message}", error)
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
            logger.warn("Attempted to book class for inactive member: $memberId")
            throw ValidationException("Cannot book class for inactive member")
        }

        logger.debug("Member validation passed for: $memberId")
        return member
    }

    /**
     * Validates that the member has an active subscription.
     */
    private fun validateActiveSubscription(memberId: java.util.UUID): Subscription {
        val subscriptions = subscriptionRepository.findByMember(memberId)
            .getOrElse { error ->
                logger.error("Failed to query subscriptions: ${error.message}", error)
                throw error
            }

        val activeSubscription = subscriptions.firstOrNull { it.status == SubscriptionStatus.ACTIVE }

        if (activeSubscription == null) {
            logger.warn("Member $memberId has no active subscription")
            throw ValidationException("Member must have an active subscription to book classes")
        }

        logger.debug("Active subscription found for member: $memberId")
        return activeSubscription
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
            logger.warn("Attempted to book inactive schedule: $scheduleId")
            throw ValidationException("Cannot book inactive or cancelled schedule")
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
            logger.warn("Attempted to book inactive class: $classId")
            throw ValidationException("Cannot book inactive class")
        }

        logger.debug("Class validation passed for: $classId")
        return gymClass
    }

    /**
     * Validates gender restrictions.
     */
    private fun validateGenderRestriction(member: Member, gymClass: Class) {
        if (!gymClass.canMemberJoin(member.gender)) {
            logger.warn("Gender restriction violated for member ${member.id} and class ${gymClass.id}")
            throw ValidationException(
                "This class is restricted to ${gymClass.genderRestriction} members only"
            )
        }
    }

    /**
     * Validates booking window (7 days in advance).
     */
    private fun validateBookingWindow(schedule: ClassSchedule) {
        val now = LocalDate.now()
        val scheduleDate = schedule.startDate.toLocalDate()
        val daysUntilClass = ChronoUnit.DAYS.between(now, scheduleDate)

        if (daysUntilClass > BOOKING_ADVANCE_DAYS) {
            logger.warn("Attempted to book class too far in advance: $daysUntilClass days")
            throw ValidationException(
                "Classes can only be booked up to $BOOKING_ADVANCE_DAYS days in advance"
            )
        }

        if (daysUntilClass < 0) {
            logger.warn("Attempted to book class in the past")
            throw ValidationException("Cannot book classes that have already occurred")
        }
    }

    /**
     * Validates no duplicate booking.
     */
    private fun validateNoDuplicateBooking(memberId: java.util.UUID, scheduleId: java.util.UUID) {
        val existingBookings = bookingRepository.findBySchedule(scheduleId)
            .getOrElse { error ->
                logger.error("Failed to query bookings: ${error.message}", error)
                throw error
            }

        val hasExistingBooking = existingBookings.any {
            it.memberId == memberId && !it.isCancelled()
        }

        if (hasExistingBooking) {
            logger.warn("Member $memberId already has a booking for schedule $scheduleId")
            throw ConflictException("You have already booked this class")
        }
    }

    /**
     * Atomically creates a booking with race condition handling.
     * Uses optimistic locking and transaction serialization.
     */
    private fun atomicBookingCreation(
        memberId: java.util.UUID,
        scheduleId: java.util.UUID,
        schedule: ClassSchedule,
        capacity: Int
    ): Pair<Booking, ClassSchedule> {
        // Check if class has available capacity
        if (schedule.isFull(capacity)) {
            logger.warn("Class is full for schedule: $scheduleId")
            throw ValidationException(
                "Class is full. Current bookings: ${schedule.currentBookings}, Capacity: $capacity"
            )
        }

        // Create booking
        val booking = Booking.createConfirmed(memberId, scheduleId)

        // Increment booking count atomically
        val updatedSchedule = schedule.incrementBooking()

        // Save both in the same transaction (SERIALIZABLE isolation prevents race conditions)
        val savedBooking = bookingRepository.save(booking)
            .getOrElse { error ->
                logger.error("Failed to save booking: ${error.message}", error)
                throw error
            }

        val savedSchedule = scheduleRepository.save(updatedSchedule)
            .getOrElse { error ->
                logger.error("Failed to update schedule: ${error.message}", error)
                throw error
            }

        logger.info("Booking created atomically: ${savedBooking.id}")
        return Pair(savedBooking, savedSchedule)
    }

    /**
     * Deducts class credit from subscription if applicable.
     */
    private fun deductClassCredit(subscription: Subscription) {
        // If subscription has remaining visits, decrement
        subscription.remainingVisits?.let { remaining ->
            if (remaining > 0) {
                val updatedSubscription = subscription.copy(
                    remainingVisits = remaining - 1,
                    updatedAt = Instant.now()
                )

                subscriptionRepository.save(updatedSubscription)
                    .getOrElse { error ->
                        logger.error("Failed to update subscription: ${error.message}", error)
                        throw error
                    }

                logger.info("Deducted class credit for subscription: ${subscription.id}")
            }
        }
    }

    /**
     * Publishes the ClassBookedEvent.
     */
    private fun publishClassBookedEvent(booking: Booking) {
        try {
            val event = ClassBookedEvent(
                bookingId = booking.id,
                memberId = booking.memberId,
                scheduleId = booking.scheduleId,
                timestamp = booking.bookedAt
            )

            eventPublisher.publish(event)
            logger.info("Published ClassBookedEvent for booking: ${booking.id}")

        } catch (e: Exception) {
            // Log error but don't fail the booking
            logger.error("Failed to publish ClassBookedEvent for booking ${booking.id}: ${e.message}", e)
        }
    }

    /**
     * Sends confirmation notification to member.
     */
    private fun sendNotification(booking: Booking) {
        try {
            notificationService.sendBookingConfirmation(
                booking.memberId,
                booking.scheduleId,
                booking.id
            ).getOrElse { error ->
                logger.warn("Failed to send booking confirmation: ${error.message}")
            }
        } catch (e: Exception) {
            // Log error but don't fail the booking
            logger.error("Failed to send notification for booking ${booking.id}: ${e.message}", e)
        }
    }
}
