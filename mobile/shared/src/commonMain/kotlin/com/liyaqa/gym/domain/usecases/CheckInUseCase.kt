package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.domain.BookingStatus
import com.liyaqa.gym.domain.rules.CanCheckInRule
import com.liyaqa.gym.domain.rules.CheckInRuleInput
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case to check in to a class
 * Applies business rules before check-in
 *
 * Note: This use case would typically interact with an Access API
 * For now, it demonstrates the business logic validation
 */
class CheckInUseCase(
    private val bookingRepository: BookingRepository,
    private val scheduleRepository: ScheduleRepository,
    private val checkInRule: CanCheckInRule = CanCheckInRule()
) {
    /**
     * Execute the use case
     * @param bookingId The booking ID to check in
     * @return Result indicating success or error
     */
    suspend operator fun invoke(bookingId: String): Result<Unit> {
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
            val ruleInput = CheckInRuleInput(
                booking = booking,
                schedule = schedule,
                currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            val ruleResult = checkInRule.evaluate(ruleInput)
            if (ruleResult.isNotSatisfied()) {
                return Result.failure(
                    Exception(ruleResult.getReasonOrNull() ?: "Cannot check in to class")
                )
            }

            // 4. Update booking status to ATTENDED
            // Note: In a real implementation, this would call an Access API endpoint
            // For now, we'll just return success as the actual check-in logic
            // would be implemented in the repository/API layer

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate if check-in is allowed without performing the actual check-in
     * Useful for UI validation
     */
    suspend fun canCheckIn(bookingId: String): Result<Boolean> {
        return try {
            val bookingResult = bookingRepository.getBookingById(bookingId)
            if (bookingResult.isFailure) {
                return Result.success(false)
            }
            val booking = bookingResult.getOrThrow()

            val scheduleResult = scheduleRepository.getScheduleById(booking.scheduleId)
            if (scheduleResult.isFailure) {
                return Result.success(false)
            }
            val schedule = scheduleResult.getOrThrow()

            val ruleInput = CheckInRuleInput(
                booking = booking,
                schedule = schedule,
                currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            val ruleResult = checkInRule.evaluate(ruleInput)
            Result.success(ruleResult.isSatisfied())

        } catch (e: Exception) {
            Result.success(false)
        }
    }
}
