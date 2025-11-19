package com.liyaqa.gym.domain.services

import java.util.UUID

/**
 * Service interface for sending notifications to members.
 * Implementations can use email, SMS, push notifications, etc.
 */
interface NotificationService {

    /**
     * Send a class booking confirmation notification.
     *
     * @param memberId The ID of the member
     * @param scheduleId The ID of the class schedule
     * @param bookingId The ID of the booking
     * @return Result indicating success or failure
     */
    fun sendBookingConfirmation(
        memberId: UUID,
        scheduleId: UUID,
        bookingId: UUID
    ): Result<Unit>

    /**
     * Send a cancellation confirmation notification.
     *
     * @param memberId The ID of the member
     * @param scheduleId The ID of the class schedule
     * @param bookingId The ID of the booking
     * @return Result indicating success or failure
     */
    fun sendCancellationConfirmation(
        memberId: UUID,
        scheduleId: UUID,
        bookingId: UUID
    ): Result<Unit>

    /**
     * Send a waitlist notification.
     *
     * @param memberId The ID of the member
     * @param scheduleId The ID of the class schedule
     * @param position The position in the waitlist
     * @return Result indicating success or failure
     */
    fun sendWaitlistNotification(
        memberId: UUID,
        scheduleId: UUID,
        position: Int
    ): Result<Unit>

    /**
     * Send a spot available notification to a waitlisted member.
     *
     * @param memberId The ID of the member
     * @param scheduleId The ID of the class schedule
     * @param bookingId The ID of the booking
     * @return Result indicating success or failure
     */
    fun sendSpotAvailableNotification(
        memberId: UUID,
        scheduleId: UUID,
        bookingId: UUID
    ): Result<Unit>
}
