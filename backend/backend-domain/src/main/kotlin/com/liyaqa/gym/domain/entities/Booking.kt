package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.util.UUID

/**
 * Booking entity representing a member's reservation for a class schedule.
 * Links Member to ClassSchedule.
 */
data class Booking(
    val id: UUID,
    val memberId: UUID,
    val scheduleId: UUID,
    val status: BookingStatus,
    val bookedAt: Instant,
    val waitlistPosition: Int?,
    val confirmedAt: Instant?,
    val checkedInAt: Instant?,
    val cancelledAt: Instant?,
    val cancellationReason: String?,
    val noShowMarkedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        if (status == BookingStatus.WAITLISTED) {
            require(waitlistPosition != null && waitlistPosition > 0) {
                "Waitlisted bookings must have a valid waitlist position"
            }
        }
    }

    fun isConfirmed(): Boolean = status == BookingStatus.CONFIRMED

    fun isWaitlisted(): Boolean = status == BookingStatus.WAITLISTED

    fun isCancelled(): Boolean = status == BookingStatus.CANCELLED

    fun isNoShow(): Boolean = status == BookingStatus.NO_SHOW

    fun isAttended(): Boolean = status == BookingStatus.ATTENDED

    fun confirm(): Booking {
        val now = Instant.now()
        return copy(
            status = BookingStatus.CONFIRMED,
            confirmedAt = now,
            waitlistPosition = null,
            updatedAt = now
        )
    }

    fun checkIn(): Booking {
        require(status == BookingStatus.CONFIRMED) {
            "Only confirmed bookings can be checked in"
        }
        val now = Instant.now()
        return copy(
            status = BookingStatus.ATTENDED,
            checkedInAt = now,
            updatedAt = now
        )
    }

    fun cancel(reason: String? = null): Booking {
        require(status != BookingStatus.CANCELLED) {
            "Booking is already cancelled"
        }
        val now = Instant.now()
        return copy(
            status = BookingStatus.CANCELLED,
            cancelledAt = now,
            cancellationReason = reason,
            updatedAt = now
        )
    }

    fun markAsNoShow(): Booking {
        require(status == BookingStatus.CONFIRMED) {
            "Only confirmed bookings can be marked as no-show"
        }
        val now = Instant.now()
        return copy(
            status = BookingStatus.NO_SHOW,
            noShowMarkedAt = now,
            updatedAt = now
        )
    }

    fun moveFromWaitlist(newPosition: Int): Booking {
        require(status == BookingStatus.WAITLISTED) {
            "Only waitlisted bookings can be moved"
        }
        require(newPosition > 0) { "Position must be positive" }
        return copy(
            waitlistPosition = newPosition,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun createConfirmed(
            memberId: UUID,
            scheduleId: UUID
        ): Booking {
            val now = Instant.now()
            return Booking(
                id = UUID.randomUUID(),
                memberId = memberId,
                scheduleId = scheduleId,
                status = BookingStatus.CONFIRMED,
                bookedAt = now,
                waitlistPosition = null,
                confirmedAt = now,
                checkedInAt = null,
                cancelledAt = null,
                cancellationReason = null,
                noShowMarkedAt = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createWaitlisted(
            memberId: UUID,
            scheduleId: UUID,
            waitlistPosition: Int
        ): Booking {
            require(waitlistPosition > 0) { "Waitlist position must be positive" }
            val now = Instant.now()
            return Booking(
                id = UUID.randomUUID(),
                memberId = memberId,
                scheduleId = scheduleId,
                status = BookingStatus.WAITLISTED,
                bookedAt = now,
                waitlistPosition = waitlistPosition,
                confirmedAt = null,
                checkedInAt = null,
                cancelledAt = null,
                cancellationReason = null,
                noShowMarkedAt = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Booking status enumeration
 */
enum class BookingStatus {
    CONFIRMED,
    WAITLISTED,
    ATTENDED,
    CANCELLED,
    NO_SHOW
}
