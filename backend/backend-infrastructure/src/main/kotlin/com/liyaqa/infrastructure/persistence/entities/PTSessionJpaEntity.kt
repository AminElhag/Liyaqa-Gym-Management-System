package com.liyaqa.infrastructure.persistence.entities

import com.liyaqa.gym.domain.entities.PTSessionStatus
import com.liyaqa.gym.domain.entities.SessionType
import jakarta.persistence.*
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity for PTSession (Personal Training Session).
 * Represents a one-on-one or small group training session.
 */
@Entity
@Table(
    name = "pt_sessions",
    indexes = [
        Index(name = "idx_pt_session_trainer_id", columnList = "trainer_id"),
        Index(name = "idx_pt_session_member_id", columnList = "member_id"),
        Index(name = "idx_pt_session_scheduled_at", columnList = "scheduled_at"),
        Index(name = "idx_pt_session_status", columnList = "status"),
        Index(name = "idx_pt_session_session_type", columnList = "session_type")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "is_deleted = false")
data class PTSessionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "trainer_id", nullable = false)
    var trainerId: UUID,

    @Column(name = "member_id", nullable = false)
    var memberId: UUID,

    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: LocalDateTime,

    @Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: PTSessionStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 50)
    var sessionType: SessionType,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "price_amount")),
        AttributeOverride(name = "currency", column = Column(name = "price_currency"))
    )
    var price: MoneyEmbeddable,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "member_goals", columnDefinition = "TEXT")
    var memberGoals: String? = null,

    @Column(name = "trainer_notes", columnDefinition = "TEXT")
    var trainerNotes: String? = null,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

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
        trainerId = UUID.randomUUID(),
        memberId = UUID.randomUUID(),
        scheduledAt = LocalDateTime.now(),
        durationMinutes = 60,
        status = PTSessionStatus.SCHEDULED,
        sessionType = SessionType.ONE_ON_ONE,
        price = MoneyEmbeddable()
    )
}
