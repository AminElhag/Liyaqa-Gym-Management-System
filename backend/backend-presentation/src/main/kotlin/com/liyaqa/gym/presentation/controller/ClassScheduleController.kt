package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.application.classmanagement.CreateClassScheduleUseCase
import com.liyaqa.gym.application.classmanagement.commands.CreateScheduleCommand
import com.liyaqa.gym.application.classmanagement.dto.ScheduleDTO
import com.liyaqa.gym.application.classmanagement.dto.ScheduleMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.ClassSchedule
import com.liyaqa.gym.domain.entities.RecurrencePattern
import com.liyaqa.gym.domain.entities.ScheduleStatus
import com.liyaqa.gym.domain.repositories.BookingRepository
import com.liyaqa.gym.domain.repositories.ClassRepository
import com.liyaqa.gym.domain.repositories.ClassScheduleRepository
import com.liyaqa.gym.domain.valueobjects.TimeSlot
import com.liyaqa.gym.presentation.dto.classmanagement.*
import com.liyaqa.gym.presentation.dto.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * REST Controller for class schedule management operations.
 * Handles schedule creation, updates, calendar views, and availability tracking.
 */
@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "Class Schedules", description = "Class schedule management endpoints")
class ClassScheduleController(
    private val scheduleRepository: ClassScheduleRepository,
    private val classRepository: ClassRepository,
    private val bookingRepository: BookingRepository,
    private val createClassScheduleUseCase: CreateClassScheduleUseCase,
    private val scheduleMapper: ScheduleMapper
) {

    private val logger = LoggerFactory.getLogger(ClassScheduleController::class.java)

    /**
     * Get all schedules with filtering
     */
    @GetMapping
    @Operation(
        summary = "Get all schedules",
        description = "Retrieve schedules with optional filtering by date, class type, trainer, and branch"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Schedules retrieved successfully",
                content = [Content(schema = Schema(implementation = ScheduleSummaryResponse::class))]
            )
        ]
    )
    fun getSchedules(
        @Parameter(description = "Filter by start date")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @Parameter(description = "Filter by end date")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
        @Parameter(description = "Filter by class type ID")
        @RequestParam(required = false) classId: UUID?,
        @Parameter(description = "Filter by trainer ID")
        @RequestParam(required = false) trainerId: UUID?,
        @Parameter(description = "Filter by branch ID")
        @RequestParam(required = false) branchId: UUID?,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<ScheduleSummaryResponse>>> {
        logger.info("Fetching schedules with filters - startDate: $startDate, endDate: $endDate, classId: $classId, trainerId: $trainerId, branchId: $branchId")

        val schedules = when {
            startDate != null && endDate != null -> {
                scheduleRepository.findByDateRange(startDate, endDate, page, size).getOrThrow()
            }
            trainerId != null -> {
                scheduleRepository.findByTrainer(trainerId, page, size).getOrThrow()
            }
            branchId != null -> {
                scheduleRepository.findByBranch(branchId, page, size).getOrThrow()
            }
            else -> {
                // Default to next 7 days
                val now = LocalDateTime.now()
                val weekLater = now.plusDays(7)
                scheduleRepository.findByDateRange(now, weekLater, page, size).getOrThrow()
            }
        }

        // Filter by classId if provided
        val filteredSchedules = if (classId != null) {
            schedules.filter { it.classId == classId }
        } else {
            schedules
        }

        val response = filteredSchedules.map { schedule ->
            val gymClass = classRepository.findById(schedule.classId).getOrThrow().orElse(null)
            schedule.toScheduleSummaryResponse(gymClass?.name, gymClass?.capacity)
        }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get calendar view of schedules
     */
    @GetMapping("/calendar")
    @Operation(
        summary = "Get calendar view",
        description = "Retrieve schedules grouped by date for calendar display"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Calendar data retrieved successfully",
                content = [Content(schema = Schema(implementation = CalendarResponse::class))]
            )
        ]
    )
    fun getCalendarView(
        @Parameter(description = "Start date", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "End date", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @Parameter(description = "Filter by branch ID")
        @RequestParam(required = false) branchId: UUID?
    ): ResponseEntity<ApiResponse<List<CalendarResponse>>> {
        logger.info("Fetching calendar view from $startDate to $endDate")

        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(23, 59, 59)

        val schedules = when (branchId) {
            null -> scheduleRepository.findByDateRange(startDateTime, endDateTime, 0, 1000).getOrThrow()
            else -> {
                scheduleRepository.findByBranch(branchId, 0, 1000).getOrThrow()
                    .filter { it.startDate >= startDateTime && it.startDate <= endDateTime }
            }
        }

        // Group schedules by date
        val calendarData = schedules
            .groupBy { it.startDate.toLocalDate() }
            .map { (date, daySchedules) ->
                CalendarResponse(
                    date = date,
                    schedules = daySchedules.map { schedule ->
                        val gymClass = classRepository.findById(schedule.classId).getOrThrow().orElse(null)
                        schedule.toScheduleSummaryResponse(gymClass?.name, gymClass?.capacity)
                    }
                )
            }
            .sortedBy { it.date }

        return ResponseEntity.ok(ApiResponse.success(calendarData))
    }

    /**
     * Get schedule details with booking status
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get schedule details",
        description = "Retrieve detailed information about a specific schedule including booking status"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Schedule details retrieved successfully",
                content = [Content(schema = Schema(implementation = ScheduleResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun getScheduleDetails(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ScheduleResponse>> {
        logger.info("Fetching schedule details for ID: $id")

        val scheduleOptional = scheduleRepository.findById(id).getOrThrow()
        if (!scheduleOptional.isPresent) {
            throw ResourceNotFoundException("Schedule not found with ID: $id")
        }

        val schedule = scheduleOptional.get()
        val gymClass = classRepository.findById(schedule.classId).getOrThrow().orElse(null)

        val response = schedule.toScheduleResponse(
            className = gymClass?.name,
            capacity = gymClass?.capacity
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Create a new schedule (staff/admin)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Create new schedule",
        description = "Create a new class schedule. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Schedule created successfully",
                content = [Content(schema = Schema(implementation = ScheduleResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - staff or admin role required"
            )
        ]
    )
    fun createSchedule(
        @Valid @RequestBody request: CreateScheduleRequest
    ): ResponseEntity<ApiResponse<ScheduleResponse>> {
        logger.info("Creating new schedule for class: ${request.classId}")

        val command = CreateScheduleCommand(
            classId = request.classId,
            trainerId = request.trainerId,
            roomId = request.roomId,
            startTime = request.startTime,
            endTime = request.endTime,
            dayOfWeek = DayOfWeek.valueOf(request.dayOfWeek),
            startDate = request.startDate,
            endDate = request.endDate,
            recurrencePattern = request.recurrencePattern?.let { RecurrencePattern.valueOf(it) }
        )

        val result = createClassScheduleUseCase.execute(command).getOrThrow()
        val gymClass = classRepository.findById(result.classId).getOrThrow().orElse(null)

        val response = scheduleMapper.toDTO(result).toScheduleResponse(
            className = gymClass?.name,
            capacity = gymClass?.capacity
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Update a schedule (staff/admin)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update schedule",
        description = "Update an existing schedule. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Schedule updated successfully",
                content = [Content(schema = Schema(implementation = ScheduleResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - staff or admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun updateSchedule(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateScheduleRequest
    ): ResponseEntity<ApiResponse<ScheduleResponse>> {
        logger.info("Updating schedule ID: $id")

        val scheduleOptional = scheduleRepository.findById(id).getOrThrow()
        if (!scheduleOptional.isPresent) {
            throw ResourceNotFoundException("Schedule not found with ID: $id")
        }

        val existingSchedule = scheduleOptional.get()
        val updatedSchedule = existingSchedule.copy(
            trainerId = request.trainerId ?: existingSchedule.trainerId,
            roomId = request.roomId ?: existingSchedule.roomId,
            timeSlot = if (request.startTime != null && request.endTime != null) {
                TimeSlot(request.startTime, request.endTime)
            } else {
                existingSchedule.timeSlot
            },
            startDate = request.startDate ?: existingSchedule.startDate,
            endDate = request.endDate ?: existingSchedule.endDate,
            updatedAt = Instant.now()
        )

        val savedSchedule = scheduleRepository.save(updatedSchedule).getOrThrow()
        val gymClass = classRepository.findById(savedSchedule.classId).getOrThrow().orElse(null)

        val response = savedSchedule.toScheduleResponse(
            className = gymClass?.name,
            capacity = gymClass?.capacity
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Cancel a schedule (staff/admin) and notify all booked members
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Cancel schedule",
        description = "Cancel a schedule and notify all booked members. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Schedule cancelled successfully"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - staff or admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun cancelSchedule(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: CancelScheduleRequest
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Cancelling schedule ID: $id")

        val scheduleOptional = scheduleRepository.findById(id).getOrThrow()
        if (!scheduleOptional.isPresent) {
            throw ResourceNotFoundException("Schedule not found with ID: $id")
        }

        val schedule = scheduleOptional.get()
        val cancelledSchedule = schedule.cancel(request.reason)
        scheduleRepository.save(cancelledSchedule).getOrThrow()

        // Get all bookings for this schedule
        val bookings = bookingRepository.findBySchedule(id, 0, 1000).getOrThrow()
        val activeBookingsCount = bookings.filter { !it.isCancelled() }.size

        // TODO: Implement notification service to notify all booked members
        if (request.notifyMembers && activeBookingsCount > 0) {
            logger.info("Notifying $activeBookingsCount members about schedule cancellation")
            // Notification logic would go here
        }

        val message = "Schedule cancelled successfully. ${if (request.notifyMembers) "$activeBookingsCount members will be notified." else ""}"
        return ResponseEntity.ok(ApiResponse.success(message))
    }

    /**
     * Get real-time availability for a schedule
     */
    @GetMapping("/{id}/availability")
    @Operation(
        summary = "Get schedule availability",
        description = "Get real-time availability information including capacity, booked spots, and waitlist"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Availability retrieved successfully",
                content = [Content(schema = Schema(implementation = AvailabilityResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun getAvailability(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<AvailabilityResponse>> {
        logger.info("Fetching availability for schedule ID: $id")

        val scheduleOptional = scheduleRepository.findById(id).getOrThrow()
        if (!scheduleOptional.isPresent) {
            throw ResourceNotFoundException("Schedule not found with ID: $id")
        }

        val schedule = scheduleOptional.get()
        val gymClass = classRepository.findById(schedule.classId).getOrThrow()
            .orElseThrow { ResourceNotFoundException("Class not found") }

        val response = AvailabilityResponse(
            scheduleId = schedule.id,
            capacity = gymClass.capacity,
            currentBookings = schedule.currentBookings,
            availableSpots = schedule.availableSpots(gymClass.capacity),
            waitingList = schedule.waitingList,
            isFull = schedule.isFull(gymClass.capacity),
            hasWaitingList = schedule.waitingList > 0
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Cancel all bookings for a schedule (staff/admin)
     */
    @DeleteMapping("/{id}/bookings")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Cancel all bookings",
        description = "Cancel all bookings for a schedule. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "All bookings cancelled successfully"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - staff or admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun cancelAllBookings(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "Schedule cancelled by staff") reason: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        logger.info("Cancelling all bookings for schedule ID: $id")

        val scheduleOptional = scheduleRepository.findById(id).getOrThrow()
        if (!scheduleOptional.isPresent) {
            throw ResourceNotFoundException("Schedule not found with ID: $id")
        }

        // Get all bookings for this schedule
        val bookings = bookingRepository.findBySchedule(id, 0, 1000).getOrThrow()
        val activeBookings = bookings.filter { !it.isCancelled() }

        // Cancel each active booking
        var cancelledCount = 0
        activeBookings.forEach { booking ->
            try {
                val cancelledBooking = booking.cancel(reason)
                bookingRepository.save(cancelledBooking).getOrThrow()
                cancelledCount++
            } catch (e: Exception) {
                logger.error("Error cancelling booking ${booking.id}", e)
            }
        }

        // TODO: Send notifications to all affected members

        val response = mapOf(
            "scheduleId" to id,
            "totalBookings" to bookings.size,
            "cancelledCount" to cancelledCount,
            "message" to "Successfully cancelled $cancelledCount booking(s)"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // Extension functions for DTO conversion

    private fun ClassSchedule.toScheduleResponse(
        className: String?,
        capacity: Int?,
        trainerName: String? = null,
        roomName: String? = null
    ): ScheduleResponse {
        return ScheduleResponse(
            id = this.id,
            classId = this.classId,
            className = className,
            trainerId = this.trainerId,
            trainerName = trainerName,
            roomId = this.roomId,
            roomName = roomName,
            dayOfWeek = this.dayOfWeek.name,
            startTime = this.timeSlot.startTime,
            endTime = this.timeSlot.endTime,
            startDate = this.startDate,
            endDate = this.endDate,
            currentBookings = this.currentBookings,
            waitingList = this.waitingList,
            capacity = capacity,
            availableSpots = capacity?.let { this.availableSpots(it) },
            status = this.status.name,
            recurrencePattern = this.recurrencePattern?.name,
            cancellationReason = this.cancellationReason,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun ScheduleDTO.toScheduleResponse(
        className: String?,
        capacity: Int?,
        trainerName: String? = null,
        roomName: String? = null
    ): ScheduleResponse {
        return ScheduleResponse(
            id = this.id,
            classId = this.classId,
            className = className,
            trainerId = this.trainerId,
            trainerName = trainerName,
            roomId = this.roomId,
            roomName = roomName,
            dayOfWeek = this.dayOfWeek.name,
            startTime = this.startTime,
            endTime = this.endTime,
            startDate = this.startDate,
            endDate = this.endDate,
            currentBookings = this.currentBookings,
            waitingList = this.waitingList,
            capacity = capacity,
            availableSpots = capacity?.let { it - this.currentBookings },
            status = this.status.name,
            recurrencePattern = null,
            cancellationReason = null,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun ClassSchedule.toScheduleSummaryResponse(
        className: String?,
        capacity: Int?,
        trainerName: String? = null
    ): ScheduleSummaryResponse {
        return ScheduleSummaryResponse(
            id = this.id,
            classId = this.classId,
            className = className,
            trainerId = this.trainerId,
            trainerName = trainerName,
            dayOfWeek = this.dayOfWeek.name,
            startTime = this.timeSlot.startTime,
            endTime = this.timeSlot.endTime,
            startDate = this.startDate,
            currentBookings = this.currentBookings,
            capacity = capacity,
            availableSpots = capacity?.let { this.availableSpots(it) },
            status = this.status.name
        )
    }
}
