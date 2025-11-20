package com.liyaqa.gym.domain.rules

import com.liyaqa.gym.domain.*
import kotlinx.datetime.*

/**
 * Input data for CanBookClassRule
 */
data class BookClassRuleInput(
    val member: Member,
    val subscription: Subscription?,
    val schedule: ClassSchedule,
    val currentBookingsCount: Int = 0,
    val maxConcurrentBookings: Int = 3,
    val minBookingHoursInAdvance: Int = 1,
    val currentDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)

/**
 * Business rule to check if a member can book a class
 * Checks:
 * 1. Member is active
 * 2. Member has active subscription
 * 3. Class is not cancelled
 * 4. Class is not full (or can be waitlisted)
 * 5. Booking is within allowed time window
 * 6. Member doesn't exceed concurrent booking limit
 */
class CanBookClassRule : BusinessRule<BookClassRuleInput> {

    override fun evaluate(input: BookClassRuleInput): RuleResult {
        // Check member is active
        if (!input.member.isActive()) {
            return RuleResult.NotSatisfied("Member account is not active")
        }

        // Check subscription exists and is active
        val subscription = input.subscription
            ?: return RuleResult.NotSatisfied("No active subscription found")

        if (!subscription.isActive()) {
            return RuleResult.NotSatisfied("Subscription is not active")
        }

        val currentDate = input.currentDateTime.date

        // Check subscription is not expired
        if (subscription.isExpired(currentDate)) {
            return RuleResult.NotSatisfied("Subscription has expired")
        }

        // Check subscription is not paused
        if (subscription.isPaused()) {
            return RuleResult.NotSatisfied("Subscription is currently paused")
        }

        // Check remaining visits for visit-based subscriptions
        if (subscription.isVisitBased() && (subscription.remainingVisits ?: 0) <= 0) {
            return RuleResult.NotSatisfied("No remaining visits on subscription")
        }

        // Check class is not cancelled
        if (input.schedule.isCancelled) {
            return RuleResult.NotSatisfied("This class has been cancelled")
        }

        // Check class hasn't already started
        if (input.schedule.startDateTime <= input.currentDateTime) {
            return RuleResult.NotSatisfied("Cannot book a class that has already started")
        }

        // Check booking window (must book at least X hours in advance)
        val hoursUntilClass = (input.schedule.startDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds -
                               input.currentDateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds) / 3600

        if (hoursUntilClass < input.minBookingHoursInAdvance) {
            return RuleResult.NotSatisfied(
                "Must book at least ${input.minBookingHoursInAdvance} hour(s) in advance"
            )
        }

        // Check concurrent bookings limit
        if (input.currentBookingsCount >= input.maxConcurrentBookings) {
            return RuleResult.NotSatisfied(
                "Maximum concurrent bookings limit reached (${input.maxConcurrentBookings})"
            )
        }

        // Note: We allow booking even if class is full (will be waitlisted)
        // The repository/use case should handle waitlist logic

        return RuleResult.Satisfied
    }
}
