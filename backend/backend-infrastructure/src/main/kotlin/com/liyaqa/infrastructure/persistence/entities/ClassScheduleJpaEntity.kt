package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.RecurrencePattern
import com.liyaqa.gym.domain.entities.ScheduleStatus
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * JPA entity for ClassSchedule.
 * Represents a scheduled instance of a class, linking Class, Trainer, and time information.
 */
@Entity
@Table(
    name = "class_schedules",
    indexes = [
        Index(name = "idx_class_schedule_class_id", columnList = "class_id"),
        Index(name = "idx_class_schedule_trainer_id", columnList = "trainer_id"),
        Index(name = "idx_class_schedule_room_id", columnList = "room_id"),
        Index(name = "idx_class_schedule_status", columnList = "status"),
        Index(name = "idx_class_schedule_start_date", columnList = "start_date"),
        Index(name = "idx_class_schedule_day_of_week", columnList = "day_of_week")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class ClassScheduleJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "class_id", nullable = false)
    var classId: UUID,

    @Column(name = "trainer_id", nullable = false)
    var trainerId: UUID,

    @Column(name = "room_id")
    var roomId: UUID? = null,

    @Embedded
    var timeSlot: TimeSlotEmbeddable,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    var dayOfWeek: DayOfWeek,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDateTime,

    @Column(name = "end_date")
    var endDate: LocalDateTime? = null,

    @Column(name = "current_bookings", nullable = false)
    var currentBookings: Int = 0,

    @Column(name = "waiting_list", nullable = false)
    var waitingList: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: ScheduleStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern", length = 50)
    var recurrencePattern: RecurrencePattern? = null,

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    var cancellationReason: String? = null,

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
        classId = UUID.randomUUID(),
        trainerId = UUID.randomUUID(),
        timeSlot = TimeSlotEmbeddable(),
        dayOfWeek = DayOfWeek.MONDAY,
        startDate = LocalDateTime.now(),
        status = ScheduleStatus.SCHEDULED
    )
}

/**
 * Embeddable for TimeSlot value object.
 */
@Embeddable
data class TimeSlotEmbeddable(
    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.of(8, 0),

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.of(9, 0)
)
