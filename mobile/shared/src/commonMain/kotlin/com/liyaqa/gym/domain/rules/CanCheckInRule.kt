package com.liyaqa.gym.domain.rules

import com.liyaqa.gym.domain.*
import kotlinx.datetime.*

/**
 * Input data for CanCheckInRule
 */
data class CheckInRuleInput(
    val booking: Booking,
    val schedule: ClassSchedule,
    val checkInWindowMinutesBefore: Int = 15,
    val checkInWindowMinutesAfter: Int = 5,
    val currentDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)

/**
 * Business rule to check if a member can check in to a class
 * Checks:
 * 1. Booking is confirmed (not waitlisted, cancelled, etc.)
 * 2. Check-in is within allowed time window (e.g., 15 minutes before to 5 minutes after class start)
 * 3. Class is not cancelled
 * 4. Booking hasn't already been checked in
 */
class CanCheckInRule : BusinessRule<CheckInRuleInput> {

    override fun evaluate(input: CheckInRuleInput): RuleResult {
        // Check booking can be checked in
        if (!input.booking.canCheckIn()) {
            return RuleResult.NotSatisfied(
                "Booking cannot be checked in. Status: ${input.booking.status}"
            )
        }

        // Check class is not cancelled
        if (input.schedule.isCancelled) {
            return RuleResult.NotSatisfied("Cannot check in to a cancelled class")
        }

        // Check check-in window
        val minutesUntilClass = (input.schedule.startDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                                input.currentDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds) / 60

        // Too early
        if (minutesUntilClass > input.checkInWindowMinutesBefore) {
            return RuleResult.NotSatisfied(
                "Check-in opens ${input.checkInWindowMinutesBefore} minutes before class starts"
            )
        }

        // Too late (after class has started beyond grace period)
        if (minutesUntilClass < -input.checkInWindowMinutesAfter) {
            return RuleResult.NotSatisfied(
                "Check-in closed ${input.checkInWindowMinutesAfter} minutes after class started"
            )
        }

        return RuleResult.Satisfied
    }
}
