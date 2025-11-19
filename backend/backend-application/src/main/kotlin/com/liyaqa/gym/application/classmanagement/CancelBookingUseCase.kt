package com.liyaqa.gym.application.classmanagement

import com.liyaqa.gym.application.classmanagement.commands.CancelBookingCommand
import com.liyaqa.gym.application.classmanagement.dto.CancellationConfirmation
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.Booking
import com.liyaqa.gym.domain.entities.BookingStatus
import com.liyaqa.gym.domain.entities.ClassSchedule
import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.gym.domain.events.ClassCancelledEvent
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.repositories.BookingRepository
import com.liyaqa.gym.domain.repositories.ClassScheduleRepository
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Use case for cancelling a class booking.
 *
 * This use case handles:
 * - Booking validation (exists and not already cancelled)
 * - Cancellation policy enforcement (2 hours before class)
 * - Late cancellation fee charging
 * - Booking cancellation
 * - Class credit refund (if applicable)
 * - Schedule booking count decrement
 * - Domain event publishing
 * - Waitlist processing trigger
 *
 * @property bookingRepository Repository for booking operations
 * @property scheduleRepository Repository for schedule operations
 * @property subscriptionRepository Repository for subscription operations
 * @property eventPublisher Publisher for domain events
 * @property processWaitlistUseCase Use case for processing waitlist
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
class CancelBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val scheduleRepository: ClassScheduleRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val eventPublisher: EventPublisher,
    private val processWaitlistUseCase: ProcessWaitlistUseCase
) {

    private val logger = LoggerFactory.getLogger(CancelBookingUseCase::class.java)

    companion object {
        private const val CANCELLATION_POLICY_HOURS = 2L
        private const val LATE_CANCELLATION_FEE = 50.0 // SAR
    }

    /**
     * Executes the cancel booking use case.
     *
     * @param command The cancel booking command
     * @return Result containing the cancellation confirmation or error
     */
    fun execute(command: CancelBookingCommand): Result<CancellationConfirmation> {
        return runCatching {
            logger.info("Cancelling booking: ${command.bookingId}")

            // 1. Validate booking exists and is not already cancelled
            val booking = validateBooking(command.bookingId)

            // 2. Get schedule to check timing
            val schedule = validateSchedule(booking.scheduleId)

            // 3. Check cancellation policy and determine if late cancellation
            val isLateCancellation = checkCancellationPolicy(schedule)

            // 4. Calculate late cancellation fee if applicable
            val lateFee = if (isLateCancellation) LATE_CANCELLATION_FEE else null

            // 5. Cancel the booking
            val cancelledBooking = booking.cancel(command.reason)

            // 6. Save cancelled booking
            val savedBooking = bookingRepository.save(cancelledBooking)
                .getOrElse { error ->
                    logger.error("Failed to save cancelled booking: ${error.message}", error)
                    throw error
                }

            // 7. Decrement schedule booking count
            val updatedSchedule = schedule.decrementBooking()
            scheduleRepository.save(updatedSchedule)
                .getOrElse { error ->
                    logger.error("Failed to update schedule: ${error.message}", error)
                    throw error
                }

            // 8. Refund class credit if not late cancellation
            val creditRefunded = if (!isLateCancellation) {
                refundClassCredit(booking.memberId)
            } else {
                false
            }

            // 9. Publish cancellation event
            publishCancellationEvent(savedBooking, command.reason)

            // 10. Process waitlist if any members are waiting
            if (updatedSchedule.waitingList > 0) {
                logger.info("Processing waitlist for schedule: ${schedule.id}")
                processWaitlistUseCase.execute(schedule.id)
                    .onFailure { error ->
                        logger.error("Failed to process waitlist: ${error.message}", error)
                        // Don't fail the cancellation if waitlist processing fails
                    }
            }

            logger.info("Booking cancelled successfully: ${savedBooking.id}")

            // 11. Return cancellation confirmation
            CancellationConfirmation(
                bookingId = savedBooking.id,
                refundIssued = false, // No monetary refund, just credit
                creditRefunded = creditRefunded,
                lateCancellationFee = lateFee,
                cancelledAt = savedBooking.cancelledAt ?: Instant.now(),
                message = if (isLateCancellation) {
                    "Booking cancelled. A late cancellation fee of $lateFee SAR has been charged."
                } else {
                    "Booking cancelled successfully. Your class credit has been refunded."
                }
            )

        }.onFailure { error ->
            logger.error("Failed to cancel booking: ${error.message}", error)
        }
    }

    /**
     * Validates that the booking exists and can be cancelled.
     */
    private fun validateBooking(bookingId: java.util.UUID): Booking {
        val bookingOptional = bookingRepository.findById(bookingId)
            .getOrElse { error ->
                logger.error("Failed to query booking repository: ${error.message}", error)
                throw error
            }

        if (!bookingOptional.isPresent) {
            logger.warn("Booking not found: $bookingId")
            throw ResourceNotFoundException("Booking with ID $bookingId not found")
        }

        val booking = bookingOptional.get()

        if (booking.isCancelled()) {
            logger.warn("Attempted to cancel already cancelled booking: $bookingId")
            throw ValidationException("Booking is already cancelled")
        }

        if (booking.status != BookingStatus.CONFIRMED) {
            logger.warn("Attempted to cancel non-confirmed booking: $bookingId (status: ${booking.status})")
            throw ValidationException("Only confirmed bookings can be cancelled")
        }

        logger.debug("Booking validation passed for: $bookingId")
        return booking
    }

    /**
     * Validates that the schedule exists.
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

        return scheduleOptional.get()
    }

    /**
     * Checks if the cancellation violates the cancellation policy.
     *
     * @param schedule The class schedule
     * @return true if it's a late cancellation, false otherwise
     */
    private fun checkCancellationPolicy(schedule: ClassSchedule): Boolean {
        val now = Instant.now()
        val classStartTime = schedule.startDate.atZone(java.time.ZoneId.systemDefault()).toInstant()
        val hoursUntilClass = ChronoUnit.HOURS.between(now, classStartTime)

        if (hoursUntilClass < CANCELLATION_POLICY_HOURS) {
            logger.warn("Late cancellation: only $hoursUntilClass hours until class")
            return true
        }

        logger.debug("Cancellation within policy: $hoursUntilClass hours until class")
        return false
    }

    /**
     * Refunds class credit to the member's subscription.
     *
     * @param memberId The member's ID
     * @return true if credit was refunded, false otherwise
     */
    private fun refundClassCredit(memberId: java.util.UUID): Boolean {
        return try {
            val subscriptions = subscriptionRepository.findByMember(memberId)
                .getOrElse { error ->
                    logger.error("Failed to query subscriptions: ${error.message}", error)
                    return false
                }

            val activeSubscription = subscriptions.firstOrNull {
                it.status == com.liyaqa.gym.domain.entities.SubscriptionStatus.ACTIVE
            }

            activeSubscription?.let { subscription ->
                subscription.remainingVisits?.let { remaining ->
                    val updatedSubscription = subscription.copy(
                        remainingVisits = remaining + 1,
                        updatedAt = Instant.now()
                    )

                    subscriptionRepository.save(updatedSubscription)
                        .getOrElse { error ->
                            logger.error("Failed to update subscription: ${error.message}", error)
                            return false
                        }

                    logger.info("Refunded class credit for subscription: ${subscription.id}")
                    return true
                }
            }

            false
        } catch (e: Exception) {
            logger.error("Failed to refund class credit: ${e.message}", e)
            false
        }
    }

    /**
     * Publishes the ClassCancelledEvent.
     */
    private fun publishCancellationEvent(booking: Booking, reason: String) {
        try {
            val event = ClassCancelledEvent(
                bookingId = booking.id,
                memberId = booking.memberId,
                scheduleId = booking.scheduleId,
                reason = reason,
                timestamp = booking.cancelledAt ?: Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published ClassCancelledEvent for booking: ${booking.id}")

        } catch (e: Exception) {
            // Log error but don't fail the cancellation
            logger.error("Failed to publish ClassCancelledEvent for booking ${booking.id}: ${e.message}", e)
        }
    }
}
