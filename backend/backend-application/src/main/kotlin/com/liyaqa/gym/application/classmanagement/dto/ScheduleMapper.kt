package com.liyaqa.gym.application.classmanagement.dto

import com.liyaqa.gym.domain.entities.ClassSchedule
import org.springframework.stereotype.Component

/**
 * Mapper for converting between ClassSchedule domain entities and DTOs.
 */
@Component
class ScheduleMapper {

    /**
     * Converts a ClassSchedule domain entity to a ScheduleDTO.
     */
    fun toDTO(schedule: ClassSchedule): ScheduleDTO {
        return ScheduleDTO(
            id = schedule.id,
            classId = schedule.classId,
            trainerId = schedule.trainerId,
            roomId = schedule.roomId,
            dayOfWeek = schedule.dayOfWeek,
            startTime = schedule.timeSlot.startTime,
            endTime = schedule.timeSlot.endTime,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            currentBookings = schedule.currentBookings,
            waitingList = schedule.waitingList,
            status = schedule.status,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt
        )
    }

    /**
     * Converts a list of ClassSchedule entities to ScheduleDTOs.
     */
    fun toDTOList(schedules: List<ClassSchedule>): List<ScheduleDTO> {
        return schedules.map { toDTO(it) }
    }
}
