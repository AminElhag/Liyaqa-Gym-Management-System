package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.ClassSchedule
import com.liyaqa.gym.domain.valueobjects.TimeSlot
import com.liyaqa.infrastructure.persistence.entities.ClassScheduleJpaEntity
import com.liyaqa.infrastructure.persistence.entities.TimeSlotEmbeddable
import org.springframework.stereotype.Component

/**
 * Mapper between ClassSchedule domain entity and ClassScheduleJpaEntity.
 */
@Component
class ClassScheduleEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: ClassSchedule): ClassScheduleJpaEntity {
        return ClassScheduleJpaEntity(
            id = domain.id,
            classId = domain.classId,
            trainerId = domain.trainerId,
            roomId = domain.roomId,
            timeSlot = toEmbeddable(domain.timeSlot),
            dayOfWeek = domain.dayOfWeek,
            startDate = domain.startDate,
            endDate = domain.endDate,
            currentBookings = domain.currentBookings,
            waitingList = domain.waitingList,
            status = domain.status,
            recurrencePattern = domain.recurrencePattern,
            cancellationReason = domain.cancellationReason,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: ClassScheduleJpaEntity): ClassSchedule {
        return ClassSchedule(
            id = entity.id,
            classId = entity.classId,
            trainerId = entity.trainerId,
            roomId = entity.roomId,
            timeSlot = toDomainTimeSlot(entity.timeSlot),
            dayOfWeek = entity.dayOfWeek,
            startDate = entity.startDate,
            endDate = entity.endDate,
            currentBookings = entity.currentBookings,
            waitingList = entity.waitingList,
            status = entity.status,
            recurrencePattern = entity.recurrencePattern,
            cancellationReason = entity.cancellationReason,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEmbeddable(timeSlot: TimeSlot): TimeSlotEmbeddable {
        return TimeSlotEmbeddable(
            startTime = timeSlot.startTime,
            endTime = timeSlot.endTime
        )
    }

    private fun toDomainTimeSlot(embeddable: TimeSlotEmbeddable): TimeSlot {
        return TimeSlot(
            startTime = embeddable.startTime,
            endTime = embeddable.endTime
        )
    }
}
