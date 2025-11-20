package com.liyaqa.gym.domain.rules

import com.liyaqa.gym.domain.*
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanBookClassRuleTest {

    private val rule = CanBookClassRule()

    private fun createMember(status: MemberStatus = MemberStatus.ACTIVE): Member {
        return Member(
            id = "member1",
            branchId = "branch1",
            name = "Test Member",
            nameArabic = null,
            contactInfo = ContactInfo.of("test@example.com", "+966512345678"),
            nationalId = "1234567890",
            gender = Gender.MALE,
            dateOfBirth = LocalDate(1990, 1, 1),
            status = status,
            profilePhotoUrl = null,
            emergencyContactName = null,
            emergencyContactPhone = null,
            notes = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    private fun createSubscription(
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)
    ): Subscription {
        return Subscription(
            id = "sub1",
            memberId = "member1",
            planId = "plan1",
            planName = "Monthly",
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            endDate = endDate,
            status = status,
            autoRenew = false,
            remainingVisits = null,
            pausedAt = null,
            pausedUntil = null,
            cancelledAt = null,
            cancellationReason = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    private fun createSchedule(
        isCancelled: Boolean = false,
        startDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(2, DateTimeUnit.HOUR),
        bookedCount: Int = 5,
        capacity: Int = 10
    ): ClassSchedule {
        return ClassSchedule(
            id = "schedule1",
            classId = "class1",
            instructorId = "instructor1",
            instructorName = "Test Instructor",
            startDateTime = startDateTime,
            endDateTime = startDateTime.plus(1, DateTimeUnit.HOUR),
            capacity = capacity,
            bookedCount = bookedCount,
            waitlistCount = 0,
            isCancelled = isCancelled,
            cancellationReason = null,
            notes = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    @Test
    fun `should allow booking when all conditions are met`() {
        val input = BookClassRuleInput(
            member = createMember(),
            subscription = createSubscription(),
            schedule = createSchedule(),
            currentBookingsCount = 0
        )

        val result = rule.evaluate(input)
        assertTrue(result.isSatisfied())
    }

    @Test
    fun `should not allow booking when member is inactive`() {
        val input = BookClassRuleInput(
            member = createMember(status = MemberStatus.INACTIVE),
            subscription = createSubscription(),
            schedule = createSchedule(),
            currentBookingsCount = 0
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow booking when no active subscription`() {
        val input = BookClassRuleInput(
            member = createMember(),
            subscription = null,
            schedule = createSchedule(),
            currentBookingsCount = 0
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow booking when subscription is expired`() {
        val expiredDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(1, DateTimeUnit.DAY)
        val input = BookClassRuleInput(
            member = createMember(),
            subscription = createSubscription(endDate = expiredDate),
            schedule = createSchedule(),
            currentBookingsCount = 0
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow booking when class is cancelled`() {
        val input = BookClassRuleInput(
            member = createMember(),
            subscription = createSubscription(),
            schedule = createSchedule(isCancelled = true),
            currentBookingsCount = 0
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow booking when class has already started`() {
        val pastTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).minus(1, DateTimeUnit.HOUR)
        val input = BookClassRuleInput(
            member = createMember(),
            subscription = createSubscription(),
            schedule = createSchedule(startDateTime = pastTime),
            currentBookingsCount = 0
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow booking when concurrent bookings limit reached`() {
        val input = BookClassRuleInput(
            member = createMember(),
            subscription = createSubscription(),
            schedule = createSchedule(),
            currentBookingsCount = 3,
            maxConcurrentBookings = 3
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should allow booking even when class is full (will be waitlisted)`() {
        val input = BookClassRuleInput(
            member = createMember(),
            subscription = createSubscription(),
            schedule = createSchedule(bookedCount = 10, capacity = 10),
            currentBookingsCount = 0
        )

        val result = rule.evaluate(input)
        assertTrue(result.isSatisfied())
    }
}
