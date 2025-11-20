package com.liyaqa.gym.domain.rules

import com.liyaqa.gym.domain.*
import kotlinx.datetime.*

/**
 * Input data for CanCancelBookingRule
 */
data class CancelBookingRuleInput(
    val booking: Booking,
    val schedule: ClassSchedule,
    val minCancellationHoursBeforeClass: Int = 2,
    val currentDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)

/**
 * Business rule to check if a booking can be cancelled
 * Checks:
 * 1. Booking is in cancellable status (CONFIRMED or WAITLISTED)
 * 2. Cancellation is within allowed time window (e.g., at least 2 hours before class)
 * 3. Class hasn't already started
 */
class CanCancelBookingRule : BusinessRule<CancelBookingRuleInput> {

    override fun evaluate(input: CancelBookingRuleInput): RuleResult {
        // Check booking is in cancellable status
        if (!input.booking.canCancel()) {
            return RuleResult.NotSatisfied(
                "Booking cannot be cancelled in ${input.booking.status} status"
            )
        }

        // Check class hasn't already started
        if (input.schedule.startDateTime <= input.currentDateTime) {
            return RuleResult.NotSatisfied("Cannot cancel a booking for a class that has already started")
        }

        // Check cancellation window
        val hoursUntilClass = (input.schedule.startDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                               input.currentDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds) / 3600

        if (hoursUntilClass < input.minCancellationHoursBeforeClass) {
            return RuleResult.NotSatisfied(
                "Must cancel at least ${input.minCancellationHoursBeforeClass} hour(s) before class starts"
            )
        }

        return RuleResult.Satisfied
    }
}
