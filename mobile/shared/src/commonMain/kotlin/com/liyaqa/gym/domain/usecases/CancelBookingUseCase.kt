package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.domain.rules.CanCancelBookingRule
import com.liyaqa.gym.domain.rules.CancelBookingRuleInput
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case to cancel a booking
 * Applies business rules before cancellation
 */
class CancelBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val scheduleRepository: ScheduleRepository,
    private val cancelBookingRule: CanCancelBookingRule = CanCancelBookingRule()
) {
    /**
     * Execute the use case
     * @param bookingId The booking ID to cancel
     * @param reason Optional cancellation reason
     * @return Result indicating success or error
     */
    suspend operator fun invoke(
        bookingId: String,
        reason: String? = null
    ): Result<Unit> {
        return try {
            // 1. Fetch booking
            val bookingResult = bookingRepository.getBookingById(bookingId)
            if (bookingResult.isFailure) {
                return Result.failure(
                    bookingResult.exceptionOrNull() ?: Exception("Failed to fetch booking")
                )
            }
            val booking = bookingResult.getOrThrow()

            // 2. Fetch schedule
            val scheduleResult = scheduleRepository.getScheduleById(booking.scheduleId)
            if (scheduleResult.isFailure) {
                return Result.failure(
                    scheduleResult.exceptionOrNull() ?: Exception("Failed to fetch class schedule")
                )
            }
            val schedule = scheduleResult.getOrThrow()

            // 3. Apply business rule
            val ruleInput = CancelBookingRuleInput(
                booking = booking,
                schedule = schedule,
                currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            val ruleResult = cancelBookingRule.evaluate(ruleInput)
            if (ruleResult.isNotSatisfied()) {
                return Result.failure(
                    Exception(ruleResult.getReasonOrNull() ?: "Cannot cancel booking")
                )
            }

            // 4. Proceed with cancellation
            bookingRepository.cancelBooking(bookingId, reason)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
