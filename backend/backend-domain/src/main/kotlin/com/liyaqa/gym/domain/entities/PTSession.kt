package com.liyaqa.gym.domain.entities

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/**
 * PTSession (Personal Training Session) entity.
 * Represents a one-on-one or small group training session.
 */
data class PTSession(
    val id: UUID,
    val trainerId: UUID,
    val memberId: UUID,
    val scheduledAt: LocalDateTime,
    val durationMinutes: Int,
    val status: PTSessionStatus,
    val sessionType: SessionType,
    val price: Money,
    val notes: String?,
    val memberGoals: String?,
    val trainerNotes: String?,
    val completedAt: Instant?,
    val cancelledAt: Instant?,
    val cancellationReason: String?,
    val noShowMarkedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(durationMinutes > 0) { "Duration must be positive" }
        require(price.isPositive() || price.isZero()) { "Price cannot be negative" }
    }

    fun isScheduled(): Boolean = status == PTSessionStatus.SCHEDULED

    fun isCompleted(): Boolean = status == PTSessionStatus.COMPLETED

    fun isCancelled(): Boolean = status == PTSessionStatus.CANCELLED

    fun isNoShow(): Boolean = status == PTSessionStatus.NO_SHOW

    fun isInProgress(): Boolean = status == PTSessionStatus.IN_PROGRESS

    fun start(): PTSession {
        require(status == PTSessionStatus.SCHEDULED) {
            "Only scheduled sessions can be started"
        }
        return copy(status = PTSessionStatus.IN_PROGRESS, updatedAt = Instant.now())
    }

    fun complete(trainerNotes: String? = null): PTSession {
        require(status == PTSessionStatus.IN_PROGRESS || status == PTSessionStatus.SCHEDULED) {
            "Only in-progress or scheduled sessions can be completed"
        }
        val now = Instant.now()
        return copy(
            status = PTSessionStatus.COMPLETED,
            completedAt = now,
            trainerNotes = trainerNotes ?: this.trainerNotes,
            updatedAt = now
        )
    }

    fun cancel(reason: String? = null): PTSession {
        require(!isCompleted() && !isCancelled()) {
            "Cannot cancel a completed or already cancelled session"
        }
        val now = Instant.now()
        return copy(
            status = PTSessionStatus.CANCELLED,
            cancelledAt = now,
            cancellationReason = reason,
            updatedAt = now
        )
    }

    fun markAsNoShow(): PTSession {
        require(status == PTSessionStatus.SCHEDULED) {
            "Only scheduled sessions can be marked as no-show"
        }
        val now = Instant.now()
        return copy(
            status = PTSessionStatus.NO_SHOW,
            noShowMarkedAt = now,
            updatedAt = now
        )
    }

    fun reschedule(newScheduledAt: LocalDateTime): PTSession {
        require(!isCompleted() && !isCancelled()) {
            "Cannot reschedule a completed or cancelled session"
        }
        return copy(
            scheduledAt = newScheduledAt,
            status = PTSessionStatus.SCHEDULED,
            updatedAt = Instant.now()
        )
    }

    fun updateNotes(notes: String): PTSession {
        return copy(notes = notes, updatedAt = Instant.now())
    }

    fun updateTrainerNotes(trainerNotes: String): PTSession {
        return copy(trainerNotes = trainerNotes, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            trainerId: UUID,
            memberId: UUID,
            scheduledAt: LocalDateTime,
            durationMinutes: Int,
            sessionType: SessionType,
            price: Money,
            memberGoals: String? = null
        ): PTSession {
            val now = Instant.now()
            return PTSession(
                id = UUID.randomUUID(),
                trainerId = trainerId,
                memberId = memberId,
                scheduledAt = scheduledAt,
                durationMinutes = durationMinutes,
                status = PTSessionStatus.SCHEDULED,
                sessionType = sessionType,
                price = price,
                notes = null,
                memberGoals = memberGoals,
                trainerNotes = null,
                completedAt = null,
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
 * PT Session status enumeration
 */
enum class PTSessionStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

/**
 * Session type enumeration
 */
enum class SessionType {
    ONE_ON_ONE,
    SMALL_GROUP,
    ASSESSMENT,
    FOLLOW_UP
}
