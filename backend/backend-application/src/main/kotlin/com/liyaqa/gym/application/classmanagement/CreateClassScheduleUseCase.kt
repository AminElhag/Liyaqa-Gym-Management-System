package com.liyaqa.gym.application.classmanagement

import com.liyaqa.gym.application.classmanagement.commands.CreateScheduleCommand
import com.liyaqa.gym.application.classmanagement.dto.ScheduleDTO
import com.liyaqa.gym.application.classmanagement.dto.ScheduleMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.ClassSchedule
import com.liyaqa.gym.domain.repositories.ClassRepository
import com.liyaqa.gym.domain.repositories.ClassScheduleRepository
import com.liyaqa.gym.domain.repositories.TrainerRepository
import com.liyaqa.gym.domain.valueobjects.TimeSlot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Use case for creating a new class schedule.
 *
 * This use case handles:
 * - Class validation (exists and is active)
 * - Trainer validation (exists and is available)
 * - Room validation (if provided, must be available)
 * - Schedule conflict checking
 * - Schedule creation
 * - Repository persistence
 *
 * @property classRepository Repository for class lookups
 * @property trainerRepository Repository for trainer lookups
 * @property scheduleRepository Repository for schedule persistence
 * @property scheduleMapper Mapper for DTO conversion
 */
@Service
@Transactional
class CreateClassScheduleUseCase(
    private val classRepository: ClassRepository,
    private val trainerRepository: TrainerRepository,
    private val scheduleRepository: ClassScheduleRepository,
    private val scheduleMapper: ScheduleMapper
) {

    private val logger = LoggerFactory.getLogger(CreateClassScheduleUseCase::class.java)

    /**
     * Executes the create class schedule use case.
     *
     * @param command The create schedule command
     * @return Result containing the created schedule DTO or error
     */
    fun execute(command: CreateScheduleCommand): Result<ScheduleDTO> {
        return runCatching {
            logger.info("Creating schedule for class: ${command.classId} with trainer: ${command.trainerId}")

            // 1. Validate class exists and is active
            val gymClass = validateClass(command.classId)

            // 2. Validate trainer exists and is available
            validateTrainer(command.trainerId, command.dayOfWeek, command.startTime, command.endTime)

            // 3. Validate room availability (if room is specified)
            command.roomId?.let { roomId ->
                validateRoomAvailability(roomId, command.dayOfWeek, command.startTime, command.endTime)
            }

            // 4. Create time slot
            val timeSlot = TimeSlot(command.startTime, command.endTime)

            // 5. Calculate start date (next occurrence of the day of week)
            val startDate = calculateNextOccurrence(command.dayOfWeek)

            // 6. Create schedule entity
            val schedule = ClassSchedule.create(
                classId = command.classId,
                trainerId = command.trainerId,
                roomId = command.roomId,
                timeSlot = timeSlot,
                dayOfWeek = command.dayOfWeek,
                startDate = startDate
            )

            // 7. Persist schedule
            val savedSchedule = scheduleRepository.save(schedule)
                .getOrElse { error ->
                    logger.error("Failed to save schedule to repository: ${error.message}", error)
                    throw error
                }

            logger.info("Schedule created successfully with ID: ${savedSchedule.id}")

            // 8. Convert to DTO and return
            scheduleMapper.toDTO(savedSchedule)

        }.onFailure { error ->
            logger.error("Failed to create schedule: ${error.message}", error)
        }
    }

    /**
     * Validates that the class exists and is active.
     *
     * @param classId The class identifier
     * @return The validated class entity
     * @throws ResourceNotFoundException if class not found
     * @throws ValidationException if class is not active
     */
    private fun validateClass(classId: java.util.UUID): com.liyaqa.gym.domain.entities.Class {
        val classOptional = classRepository.findById(classId)
            .getOrElse { error ->
                logger.error("Failed to query class repository: ${error.message}", error)
                throw error
            }

        if (!classOptional.isPresent) {
            logger.warn("Class not found: $classId")
            throw ResourceNotFoundException("Class with ID $classId not found")
        }

        val gymClass = classOptional.get()

        if (!gymClass.isActive) {
            logger.warn("Attempted to create schedule for inactive class: $classId")
            throw ValidationException("Cannot create schedule for inactive class")
        }

        logger.debug("Class validation passed for: $classId")
        return gymClass
    }

    /**
     * Validates that the trainer exists and is available for the specified time.
     *
     * @param trainerId The trainer identifier
     * @param dayOfWeek The day of week for the schedule
     * @param startTime The start time
     * @param endTime The end time
     * @throws ResourceNotFoundException if trainer not found
     * @throws ValidationException if trainer has conflicting schedules
     */
    private fun validateTrainer(
        trainerId: java.util.UUID,
        dayOfWeek: DayOfWeek,
        startTime: java.time.LocalTime,
        endTime: java.time.LocalTime
    ) {
        val trainerOptional = trainerRepository.findById(trainerId)
            .getOrElse { error ->
                logger.error("Failed to query trainer repository: ${error.message}", error)
                throw error
            }

        if (!trainerOptional.isPresent) {
            logger.warn("Trainer not found: $trainerId")
            throw ResourceNotFoundException("Trainer with ID $trainerId not found")
        }

        // Check for conflicting trainer schedules
        val existingSchedules = scheduleRepository.findByTrainer(trainerId)
            .getOrElse { error ->
                logger.error("Failed to query schedules for trainer: ${error.message}", error)
                throw error
            }

        val requestedTimeSlot = TimeSlot(startTime, endTime)

        val hasConflict = existingSchedules.any { schedule ->
            schedule.dayOfWeek == dayOfWeek &&
            schedule.isActive() &&
            schedule.timeSlot.overlaps(requestedTimeSlot)
        }

        if (hasConflict) {
            logger.warn("Trainer $trainerId has conflicting schedule on $dayOfWeek at $startTime-$endTime")
            throw ValidationException(
                "Trainer is not available at the requested time. " +
                "There is a conflicting schedule on $dayOfWeek from $startTime to $endTime"
            )
        }

        logger.debug("Trainer validation passed for: $trainerId")
    }

    /**
     * Validates that the room is available for the specified time.
     *
     * @param roomId The room identifier
     * @param dayOfWeek The day of week for the schedule
     * @param startTime The start time
     * @param endTime The end time
     * @throws ValidationException if room has conflicting schedules
     */
    private fun validateRoomAvailability(
        roomId: java.util.UUID,
        dayOfWeek: DayOfWeek,
        startTime: java.time.LocalTime,
        endTime: java.time.LocalTime
    ) {
        // Check for conflicting room schedules
        val existingSchedules = scheduleRepository.findByBranch(roomId) // Using branch as proxy for now
            .getOrElse { error ->
                logger.error("Failed to query schedules for room: ${error.message}", error)
                throw error
            }

        val requestedTimeSlot = TimeSlot(startTime, endTime)

        val hasConflict = existingSchedules.any { schedule ->
            schedule.roomId == roomId &&
            schedule.dayOfWeek == dayOfWeek &&
            schedule.isActive() &&
            schedule.timeSlot.overlaps(requestedTimeSlot)
        }

        if (hasConflict) {
            logger.warn("Room $roomId is not available on $dayOfWeek at $startTime-$endTime")
            throw ValidationException(
                "Room is not available at the requested time. " +
                "There is a conflicting schedule on $dayOfWeek from $startTime to $endTime"
            )
        }

        logger.debug("Room availability validated for: $roomId")
    }

    /**
     * Calculates the next occurrence of a specific day of week.
     *
     * @param dayOfWeek The target day of week
     * @return The LocalDateTime of the next occurrence
     */
    private fun calculateNextOccurrence(dayOfWeek: DayOfWeek): LocalDateTime {
        val now = LocalDateTime.now()
        val currentDayOfWeek = now.dayOfWeek
        val daysUntilTarget = (dayOfWeek.value - currentDayOfWeek.value + 7) % 7

        return if (daysUntilTarget == 0) {
            // If it's the same day, schedule for next week
            now.plusWeeks(1)
        } else {
            now.plusDays(daysUntilTarget.toLong())
        }
    }
}
