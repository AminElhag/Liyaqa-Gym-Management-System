package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Booking
import com.liyaqa.infrastructure.persistence.entities.BookingJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Booking domain entity and BookingJpaEntity.
 */
@Component
class BookingEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Booking): BookingJpaEntity {
        return BookingJpaEntity(
            id = domain.id,
            memberId = domain.memberId,
            scheduleId = domain.scheduleId,
            status = domain.status,
            bookedAt = domain.bookedAt,
            waitlistPosition = domain.waitlistPosition,
            confirmedAt = domain.confirmedAt,
            checkedInAt = domain.checkedInAt,
            cancelledAt = domain.cancelledAt,
            cancellationReason = domain.cancellationReason,
            noShowMarkedAt = domain.noShowMarkedAt,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: BookingJpaEntity): Booking {
        return Booking(
            id = entity.id,
            memberId = entity.memberId,
            scheduleId = entity.scheduleId,
            status = entity.status,
            bookedAt = entity.bookedAt,
            waitlistPosition = entity.waitlistPosition,
            confirmedAt = entity.confirmedAt,
            checkedInAt = entity.checkedInAt,
            cancelledAt = entity.cancelledAt,
            cancellationReason = entity.cancellationReason,
            noShowMarkedAt = entity.noShowMarkedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
