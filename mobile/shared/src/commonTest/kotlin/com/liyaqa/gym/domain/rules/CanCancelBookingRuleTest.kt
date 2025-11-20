package com.liyaqa.gym.domain.rules

import com.liyaqa.gym.domain.*
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertTrue

class CanCancelBookingRuleTest {

    private val rule = CanCancelBookingRule()

    private fun createBooking(status: BookingStatus = BookingStatus.CONFIRMED): Booking {
        return Booking(
            id = "booking1",
            memberId = "member1",
            scheduleId = "schedule1",
            status = status,
            bookedAt = Clock.System.now(),
            waitlistPosition = null,
            confirmedAt = Clock.System.now(),
            checkedInAt = null,
            cancelledAt = null,
            cancellationReason = null,
            noShowMarkedAt = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    private fun createSchedule(
        startDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(3, DateTimeUnit.HOUR)
    ): ClassSchedule {
        return ClassSchedule(
            id = "schedule1",
            classId = "class1",
            instructorId = "instructor1",
            instructorName = "Test Instructor",
            startDateTime = startDateTime,
            endDateTime = startDateTime.plus(1, DateTimeUnit.HOUR),
            capacity = 10,
            bookedCount = 5,
            waitlistCount = 0,
            isCancelled = false,
            cancellationReason = null,
            notes = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    @Test
    fun `should allow cancellation when all conditions are met`() {
        val input = CancelBookingRuleInput(
            booking = createBooking(),
            schedule = createSchedule(),
            minCancellationHoursBeforeClass = 2
        )

        val result = rule.evaluate(input)
        assertTrue(result.isSatisfied())
    }

    @Test
    fun `should not allow cancellation when booking is already cancelled`() {
        val input = CancelBookingRuleInput(
            booking = createBooking(status = BookingStatus.CANCELLED),
            schedule = createSchedule()
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow cancellation when booking is attended`() {
        val input = CancelBookingRuleInput(
            booking = createBooking(status = BookingStatus.ATTENDED),
            schedule = createSchedule()
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow cancellation when class has already started`() {
        val pastTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).minus(1, DateTimeUnit.HOUR)
        val input = CancelBookingRuleInput(
            booking = createBooking(),
            schedule = createSchedule(startDateTime = pastTime)
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }

    @Test
    fun `should not allow cancellation within minimum cancellation window`() {
        val soonTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.HOUR)
        val input = CancelBookingRuleInput(
            booking = createBooking(),
            schedule = createSchedule(startDateTime = soonTime),
            minCancellationHoursBeforeClass = 2
        )

        val result = rule.evaluate(input)
        assertTrue(result.isNotSatisfied())
    }
}
