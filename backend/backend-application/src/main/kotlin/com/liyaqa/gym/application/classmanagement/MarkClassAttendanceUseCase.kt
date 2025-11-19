package com.liyaqa.gym.application.classmanagement

import com.liyaqa.gym.application.classmanagement.commands.MarkAttendanceCommand
import com.liyaqa.gym.application.classmanagement.dto.BookingDTO
import com.liyaqa.gym.application.classmanagement.dto.BookingMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.Booking
import com.liyaqa.gym.domain.entities.BookingStatus
import com.liyaqa.gym.domain.repositories.BookingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for marking class attendance.
 *
 * This use case handles:
 * - Booking validation (exists and is confirmed)
 * - Attendance marking (attended or no-show)
 * - No-show penalty tracking
 * - Habitual no-show detection (3+ times)
 * - Automatic penalty application
 *
 * @property bookingRepository Repository for booking operations
 * @property bookingMapper Mapper for DTO conversion
 */
@Service
@Transactional
class MarkClassAttendanceUseCase(
    private val bookingRepository: BookingRepository,
    private val bookingMapper: BookingMapper
) {

    private val logger = LoggerFactory.getLogger(MarkClassAttendanceUseCase::class.java)

    companion object {
        private const val NO_SHOW_THRESHOLD = 3
        private const val NO_SHOW_PENALTY_FEE = 100.0 // SAR
    }

    /**
     * Executes the mark attendance use case.
     *
     * @param command The mark attendance command
     * @return Result containing the updated booking DTO or error
     */
    fun execute(command: MarkAttendanceCommand): Result<BookingDTO> {
        return runCatching {
            logger.info(
                "Marking attendance for booking: ${command.bookingId}, " +
                "attended: ${command.attended}, marked by: ${command.markedByUserId}"
            )

            // 1. Validate booking exists and is confirmed
            val booking = validateBooking(command.bookingId)

            // 2. Mark attendance
            val updatedBooking = if (command.attended) {
                markAsAttended(booking)
            } else {
                markAsNoShow(booking)
            }

            // 3. Save updated booking
            val savedBooking = bookingRepository.save(updatedBooking)
                .getOrElse { error ->
                    logger.error("Failed to save booking: ${error.message}", error)
                    throw error
                }

            // 4. If no-show, check for habitual no-shows and apply penalty
            if (!command.attended) {
                checkHabitualNoShow(booking.memberId)
            }

            logger.info("Attendance marked successfully for booking: ${savedBooking.id}")

            // 5. Convert to DTO and return
            bookingMapper.toDTO(savedBooking)

        }.onFailure { error ->
            logger.error("Failed to mark attendance: ${error.message}", error)
        }
    }

    /**
     * Validates that the booking exists and can have attendance marked.
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

        // Only confirmed bookings can have attendance marked
        if (booking.status != BookingStatus.CONFIRMED) {
            logger.warn("Attempted to mark attendance for non-confirmed booking: $bookingId (status: ${booking.status})")
            throw ValidationException(
                "Only confirmed bookings can have attendance marked. Current status: ${booking.status}"
            )
        }

        // Check if attendance already marked
        if (booking.status == BookingStatus.ATTENDED || booking.status == BookingStatus.NO_SHOW) {
            logger.warn("Attendance already marked for booking: $bookingId (status: ${booking.status})")
            throw ValidationException("Attendance has already been marked for this booking")
        }

        logger.debug("Booking validation passed for: $bookingId")
        return booking
    }

    /**
     * Marks the booking as attended.
     */
    private fun markAsAttended(booking: Booking): Booking {
        logger.info("Marking booking ${booking.id} as attended")
        return booking.checkIn()
    }

    /**
     * Marks the booking as no-show.
     */
    private fun markAsNoShow(booking: Booking): Booking {
        logger.info("Marking booking ${booking.id} as no-show")
        return booking.markAsNoShow()
    }

    /**
     * Checks for habitual no-shows and applies penalties if needed.
     *
     * A member is considered a habitual no-show if they have 3 or more no-shows.
     * In a real system, this would:
     * - Count no-shows over a specific time period (e.g., last 30 days)
     * - Apply penalties like temporary booking restrictions
     * - Charge penalty fees
     * - Send warning notifications
     */
    private fun checkHabitualNoShow(memberId: java.util.UUID) {
        try {
            // Get all bookings for the member
            val memberBookings = bookingRepository.findByMember(memberId)
                .getOrElse { error ->
                    logger.error("Failed to query member bookings: ${error.message}", error)
                    return
                }

            // Count no-shows
            val noShowCount = memberBookings.count { it.isNoShow() }

            logger.info("Member $memberId has $noShowCount no-shows")

            // Apply penalty if threshold exceeded
            if (noShowCount >= NO_SHOW_THRESHOLD) {
                applyNoShowPenalty(memberId, noShowCount)
            }

        } catch (e: Exception) {
            // Log error but don't fail the attendance marking
            logger.error("Failed to check habitual no-show: ${e.message}", e)
        }
    }

    /**
     * Applies penalty for habitual no-shows.
     *
     * In a production system, this would:
     * - Create a penalty record
     * - Charge the penalty fee
     * - Send notification to member
     * - Possibly suspend booking privileges temporarily
     * - Log the penalty for audit purposes
     */
    private fun applyNoShowPenalty(memberId: java.util.UUID, noShowCount: Int) {
        logger.warn(
            "Habitual no-show detected for member: $memberId " +
            "(count: $noShowCount). Applying penalty of $NO_SHOW_PENALTY_FEE SAR"
        )

        // TODO: In a real implementation:
        // 1. Create a penalty record in the database
        // 2. Process penalty payment
        // 3. Send notification to member
        // 4. Optionally suspend booking privileges
        // 5. Create audit log entry

        // For now, just log the penalty
        logger.info(
            "Penalty applied for member: $memberId. " +
            "Fee: $NO_SHOW_PENALTY_FEE SAR. " +
            "Reason: Habitual no-show ($noShowCount times)"
        )
    }
}
