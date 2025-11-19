package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.BookingStatus
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for Booking.
 * Represents a member's reservation for a class schedule.
 */
@Entity
@Table(
    name = "bookings",
    indexes = [
        Index(name = "idx_booking_member_id", columnList = "member_id"),
        Index(name = "idx_booking_schedule_id", columnList = "schedule_id"),
        Index(name = "idx_booking_status", columnList = "status"),
        Index(name = "idx_booking_booked_at", columnList = "booked_at"),
        Index(name = "idx_booking_waitlist_position", columnList = "waitlist_position")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class BookingJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    @Column(name = "schedule_id", nullable = false)
    var scheduleId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: BookingStatus,

    @Column(name = "booked_at", nullable = false)
    var bookedAt: Instant,

    @Column(name = "waitlist_position")
    var waitlistPosition: Int? = null,

    @Column(name = "confirmed_at")
    var confirmedAt: Instant? = null,

    @Column(name = "checked_in_at")
    var checkedInAt: Instant? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: Instant? = null,

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    var cancellationReason: String? = null,

    @Column(name = "no_show_marked_at")
    var noShowMarkedAt: Instant? = null,

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    constructor() : this(
        id = UUID.randomUUID(),
        memberId = UUID.randomUUID(),
        scheduleId = UUID.randomUUID(),
        status = BookingStatus.CONFIRMED,
        bookedAt = Instant.now()
    )
}
