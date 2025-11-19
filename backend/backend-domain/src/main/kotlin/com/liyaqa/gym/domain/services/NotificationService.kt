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

    /**
     * Send a check-in confirmation notification.
     *
     * @param memberId The ID of the member
     * @param branchId The ID of the branch
     * @param accessLogId The ID of the access log
     * @return Result indicating success or failure
     */
    fun sendCheckInConfirmation(
        memberId: UUID,
        branchId: UUID,
        accessLogId: UUID
    ): Result<Unit>

    /**
     * Send an access denied notification.
     *
     * @param memberId The ID of the member
     * @param branchId The ID of the branch
     * @param reason The reason for denial
     * @return Result indicating success or failure
     */
    fun sendAccessDeniedNotification(
        memberId: UUID,
        branchId: UUID,
        reason: String
    ): Result<Unit>

    /**
     * Send a guest access code notification.
     *
     * @param hostMemberId The ID of the host member
     * @param guestAccessId The ID of the guest access
     * @param guestName The name of the guest
     * @param qrCode The QR code for guest access
     * @return Result indicating success or failure
     */
    fun sendGuestAccessCode(
        hostMemberId: UUID,
        guestAccessId: UUID,
        guestName: String,
        qrCode: String
    ): Result<Unit>
}
