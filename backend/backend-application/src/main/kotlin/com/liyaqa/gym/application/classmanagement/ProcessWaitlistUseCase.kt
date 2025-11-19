package com.liyaqa.gym.application.classmanagement

import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.Booking
import com.liyaqa.gym.domain.entities.BookingStatus
import com.liyaqa.gym.domain.entities.ClassSchedule
import com.liyaqa.gym.domain.events.ClassBookedEvent
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.repositories.BookingRepository
import com.liyaqa.gym.domain.repositories.ClassRepository
import com.liyaqa.gym.domain.repositories.ClassScheduleRepository
import com.liyaqa.gym.domain.services.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Use case for processing the waitlist when a spot becomes available.
 *
 * This use case handles:
 * - Finding the next member in waitlist (by position)
 * - Automatically confirming their booking
 * - Updating schedule counts
 * - Sending spot available notification
 * - Publishing domain events
 *
 * Note: This is triggered automatically when a booking is cancelled.
 * In a production system, you might want to add a timeout mechanism
 * where if the member doesn't respond within 30 minutes, the spot
 * moves to the next person. This can be implemented using a scheduled
 * job or message queue with TTL.
 *
 * @property bookingRepository Repository for booking operations
 * @property scheduleRepository Repository for schedule operations
 * @property classRepository Repository for class lookups
 * @property eventPublisher Publisher for domain events
 * @property notificationService Service for sending notifications
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
class ProcessWaitlistUseCase(
    private val bookingRepository: BookingRepository,
    private val scheduleRepository: ClassScheduleRepository,
    private val classRepository: ClassRepository,
    private val eventPublisher: EventPublisher,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(ProcessWaitlistUseCase::class.java)

    /**
     * Executes the process waitlist use case.
     *
     * @param scheduleId The ID of the schedule to process waitlist for
     * @return Result containing the confirmed booking or error
     */
    fun execute(scheduleId: UUID): Result<Unit> {
        return runCatching {
            logger.info("Processing waitlist for schedule: $scheduleId")

            // 1. Get schedule
            val schedule = getSchedule(scheduleId)

            // 2. Get class to check capacity
            val gymClass = getClass(schedule.classId)

            // 3. Check if there's available capacity
            if (!schedule.hasAvailableSpots(gymClass.capacity)) {
                logger.info("No available spots for schedule: $scheduleId")
                return Result.success(Unit)
            }

            // 4. Get all waitlisted bookings for this schedule, sorted by position
            val waitlistedBookings = getWaitlistedBookings(scheduleId)

            if (waitlistedBookings.isEmpty()) {
                logger.info("No waitlisted bookings for schedule: $scheduleId")
                return Result.success(Unit)
            }

            // 5. Get the first person in waitlist
            val nextBooking = waitlistedBookings.first()

            // 6. Confirm the booking
            val confirmedBooking = confirmWaitlistBooking(nextBooking, schedule)

            logger.info("Processed waitlist successfully for schedule: $scheduleId, booking: ${confirmedBooking.id}")

        }.onFailure { error ->
            logger.error("Failed to process waitlist: ${error.message}", error)
        }
    }

    /**
     * Gets the schedule.
     */
    private fun getSchedule(scheduleId: UUID): ClassSchedule {
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
     * Gets the class.
     */
    private fun getClass(classId: UUID): com.liyaqa.gym.domain.entities.Class {
        val classOptional = classRepository.findById(classId)
            .getOrElse { error ->
                logger.error("Failed to query class repository: ${error.message}", error)
                throw error
            }

        if (!classOptional.isPresent) {
            logger.warn("Class not found: $classId")
            throw ResourceNotFoundException("Class with ID $classId not found")
        }

        return classOptional.get()
    }

    /**
     * Gets all waitlisted bookings for a schedule, sorted by position.
     */
    private fun getWaitlistedBookings(scheduleId: UUID): List<Booking> {
        val allBookings = bookingRepository.findBySchedule(scheduleId)
            .getOrElse { error ->
                logger.error("Failed to query bookings: ${error.message}", error)
                throw error
            }

        return allBookings
            .filter { it.status == BookingStatus.WAITLISTED }
            .sortedBy { it.waitlistPosition }
    }

    /**
     * Confirms a waitlist booking and updates all related entities.
     */
    private fun confirmWaitlistBooking(
        booking: Booking,
        schedule: ClassSchedule
    ): Booking {
        // 1. Confirm the booking
        val confirmedBooking = booking.confirm()

        // 2. Save confirmed booking
        val savedBooking = bookingRepository.save(confirmedBooking)
            .getOrElse { error ->
                logger.error("Failed to save confirmed booking: ${error.message}", error)
                throw error
            }

        // 3. Update schedule: increment bookings, decrement waitlist
        val updatedSchedule = schedule
            .incrementBooking()
            .removeFromWaitingList()

        scheduleRepository.save(updatedSchedule)
            .getOrElse { error ->
                logger.error("Failed to update schedule: ${error.message}", error)
                throw error
            }

        // 4. Update waitlist positions for remaining members
        updateWaitlistPositions(schedule.id)

        // 5. Publish booking event
        publishBookingEvent(savedBooking)

        // 6. Send notification to member
        sendSpotAvailableNotification(savedBooking)

        return savedBooking
    }

    /**
     * Updates waitlist positions for all remaining waitlisted members.
     * Decrements each position by 1 since someone moved up.
     */
    private fun updateWaitlistPositions(scheduleId: UUID) {
        try {
            val waitlistedBookings = getWaitlistedBookings(scheduleId)

            waitlistedBookings.forEachIndexed { index, booking ->
                val newPosition = index + 1
                if (booking.waitlistPosition != newPosition) {
                    val updatedBooking = booking.moveFromWaitlist(newPosition)
                    bookingRepository.save(updatedBooking)
                        .getOrElse { error ->
                            logger.error("Failed to update waitlist position: ${error.message}", error)
                        }
                }
            }

            logger.info("Updated waitlist positions for schedule: $scheduleId")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to update waitlist positions: ${e.message}", e)
        }
    }

    /**
     * Publishes the ClassBookedEvent for the confirmed booking.
     */
    private fun publishBookingEvent(booking: Booking) {
        try {
            val event = ClassBookedEvent(
                bookingId = booking.id,
                memberId = booking.memberId,
                scheduleId = booking.scheduleId,
                timestamp = booking.confirmedAt ?: Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published ClassBookedEvent for waitlist booking: ${booking.id}")

        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to publish ClassBookedEvent: ${e.message}", e)
        }
    }

    /**
     * Sends spot available notification to the member.
     */
    private fun sendSpotAvailableNotification(booking: Booking) {
        try {
            notificationService.sendSpotAvailableNotification(
                booking.memberId,
                booking.scheduleId,
                booking.id
            ).getOrElse { error ->
                logger.warn("Failed to send spot available notification: ${error.message}")
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
            logger.error("Failed to send notification: ${e.message}", e)
        }
    }
}
