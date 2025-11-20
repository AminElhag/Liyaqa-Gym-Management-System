package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.data.repositories.BookingRepository
import com.liyaqa.gym.data.repositories.MemberRepository
import com.liyaqa.gym.data.repositories.ScheduleRepository
import com.liyaqa.gym.data.repositories.SubscriptionRepository
import com.liyaqa.gym.domain.Booking
import com.liyaqa.gym.domain.rules.BookClassRuleInput
import com.liyaqa.gym.domain.rules.CanBookClassRule
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use case to book a class
 * Applies business rules before booking
 */
class BookClassUseCase(
    private val bookingRepository: BookingRepository,
    private val memberRepository: MemberRepository,
    private val scheduleRepository: ScheduleRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val bookClassRule: CanBookClassRule = CanBookClassRule()
) {
    /**
     * Execute the use case
     * @param scheduleId The schedule ID to book
     * @param memberId The member ID making the booking
     * @param notes Optional notes for the booking
     * @return Result containing Booking or error
     */
    suspend operator fun invoke(
        scheduleId: String,
        memberId: String,
        notes: String? = null
    ): Result<Booking> {
        return try {
            // 1. Fetch member profile
            val memberResult = memberRepository.getMemberProfile(memberId)
            if (memberResult.isFailure) {
                return Result.failure(
                    memberResult.exceptionOrNull() ?: Exception("Failed to fetch member profile")
                )
            }
            val member = memberResult.getOrThrow()

            // 2. Fetch schedule
            val scheduleResult = scheduleRepository.getScheduleById(scheduleId)
            if (scheduleResult.isFailure) {
                return Result.failure(
                    scheduleResult.exceptionOrNull() ?: Exception("Failed to fetch class schedule")
                )
            }
            val schedule = scheduleResult.getOrThrow()

            // 3. Fetch active subscription
            val subscriptionsResult = subscriptionRepository.getActiveSubscriptions(memberId)
            if (subscriptionsResult.isFailure) {
                return Result.failure(
                    subscriptionsResult.exceptionOrNull() ?: Exception("Failed to fetch subscriptions")
                )
            }
            val subscriptions = subscriptionsResult.getOrThrow()
            val activeSubscription = subscriptions.firstOrNull()

            // 4. Get current bookings count for concurrent booking limit check
            val bookingsResult = bookingRepository.getMyBookings(memberId)
            val currentBookingsCount = bookingsResult.getOrNull()
                ?.count { it.isConfirmed() || it.isWaitlisted() } ?: 0

            // 5. Apply business rule
            val ruleInput = BookClassRuleInput(
                member = member,
                subscription = activeSubscription,
                schedule = schedule,
                currentBookingsCount = currentBookingsCount,
                currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            val ruleResult = bookClassRule.evaluate(ruleInput)
            if (ruleResult.isNotSatisfied()) {
                return Result.failure(
                    Exception(ruleResult.getReasonOrNull() ?: "Cannot book class")
                )
            }

            // 6. Proceed with booking
            bookingRepository.bookClass(scheduleId, memberId, notes)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
