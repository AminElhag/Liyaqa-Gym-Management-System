package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.application.classmanagement.BookClassUseCase
import com.liyaqa.gym.application.classmanagement.CancelBookingUseCase
import com.liyaqa.gym.application.classmanagement.JoinWaitlistUseCase
import com.liyaqa.gym.application.classmanagement.MarkClassAttendanceUseCase
import com.liyaqa.gym.application.classmanagement.commands.BookClassCommand
import com.liyaqa.gym.application.classmanagement.commands.CancelBookingCommand
import com.liyaqa.gym.application.classmanagement.commands.JoinWaitlistCommand
import com.liyaqa.gym.application.classmanagement.commands.MarkAttendanceCommand
import com.liyaqa.gym.application.classmanagement.dto.BookingDTO
import com.liyaqa.gym.application.classmanagement.dto.BookingMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.Booking
import com.liyaqa.gym.domain.entities.BookingStatus
import com.liyaqa.gym.domain.repositories.BookingRepository
import com.liyaqa.gym.domain.repositories.ClassRepository
import com.liyaqa.gym.domain.repositories.ClassScheduleRepository
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

/**
 * REST Controller for booking management operations.
 * Handles class bookings, cancellations, check-ins, and waitlist management.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Booking management endpoints")
class BookingController(
    private val bookingRepository: BookingRepository,
    private val scheduleRepository: ClassScheduleRepository,
    private val classRepository: ClassRepository,
    private val bookClassUseCase: BookClassUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val joinWaitlistUseCase: JoinWaitlistUseCase,
    private val markAttendanceUseCase: MarkClassAttendanceUseCase,
    private val bookingMapper: BookingMapper
) {

    private val logger = LoggerFactory.getLogger(BookingController::class.java)

    /**
     * Book a class
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Book a class",
        description = "Book a class for a member. If class is full, member will be added to waitlist."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Class booked successfully",
                content = [Content(schema = Schema(implementation = BookingResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or booking not possible"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "Member already has a booking for this schedule"
            )
        ]
    )
    fun bookClass(
        @Valid @RequestBody request: BookClassRequest
    ): ResponseEntity<ApiResponse<BookingResponse>> {
        logger.info("Booking class for schedule: ${request.scheduleId}")

        // TODO: Get member ID from security context if not provided
        val memberId = request.memberId ?: UUID.randomUUID() // Placeholder

        val command = BookClassCommand(
            memberId = memberId,
            scheduleId = request.scheduleId
        )

        val result = bookClassUseCase.execute(command).getOrThrow()

        val schedule = scheduleRepository.findById(result.scheduleId).getOrThrow().orElse(null)
        val gymClass = schedule?.let {
            classRepository.findById(it.classId).getOrThrow().orElse(null)
        }

        val response = bookingMapper.toDTO(result).toBookingResponse(
            className = gymClass?.name,
            classTime = schedule?.startDate
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Get booking details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get booking details",
        description = "Retrieve detailed information about a specific booking"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Booking details retrieved successfully",
                content = [Content(schema = Schema(implementation = BookingResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Booking not found"
            )
        ]
    )
    fun getBookingDetails(
        @Parameter(description = "Booking ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<BookingResponse>> {
        logger.info("Fetching booking details for ID: $id")

        val bookingOptional = bookingRepository.findById(id).getOrThrow()
        if (!bookingOptional.isPresent) {
            throw ResourceNotFoundException("Booking not found with ID: $id")
        }

        val booking = bookingOptional.get()
        val schedule = scheduleRepository.findById(booking.scheduleId).getOrThrow().orElse(null)
        val gymClass = schedule?.let {
            classRepository.findById(it.classId).getOrThrow().orElse(null)
        }

        val response = booking.toBookingResponse(
            className = gymClass?.name,
            classTime = schedule?.startDate
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Cancel a booking
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Cancel booking",
        description = "Cancel a booking. Will process waitlist if spots become available."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Booking cancelled successfully",
                content = [Content(schema = Schema(implementation = CancellationResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Booking already cancelled or cannot be cancelled"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Booking not found"
            )
        ]
    )
    fun cancelBooking(
        @Parameter(description = "Booking ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: CancelBookingRequest
    ): ResponseEntity<ApiResponse<CancellationResponse>> {
        logger.info("Cancelling booking ID: $id")

        val command = CancelBookingCommand(
            bookingId = id,
            reason = request.reason
        )

        val result = cancelBookingUseCase.execute(command).getOrThrow()

        val response = CancellationResponse(
            bookingId = result.bookingId,
            cancelledAt = result.cancelledAt,
            reason = request.reason,
            refundAmount = null, // TODO: Calculate refund if applicable
            message = result.message
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get member's bookings (upcoming and past)
     */
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get member's bookings",
        description = "Retrieve all bookings for a specific member"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Bookings retrieved successfully",
                content = [Content(schema = Schema(implementation = BookingSummaryResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            )
        ]
    )
    fun getMemberBookings(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable memberId: UUID,
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Show only upcoming bookings")
        @RequestParam(defaultValue = "false") upcomingOnly: Boolean,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<BookingSummaryResponse>>> {
        logger.info("Fetching bookings for member: $memberId")

        val bookings = bookingRepository.findByMember(memberId, page, size).getOrThrow()

        // Filter by status if provided
        val filteredBookings = if (status != null) {
            bookings.filter { it.status == BookingStatus.valueOf(status) }
        } else {
            bookings
        }

        val response = filteredBookings.map { booking ->
            val schedule = scheduleRepository.findById(booking.scheduleId).getOrThrow().orElse(null)
            val gymClass = schedule?.let {
                classRepository.findById(it.classId).getOrThrow().orElse(null)
            }

            booking.toBookingSummaryResponse(
                className = gymClass?.name,
                classTime = schedule?.startDate
            )
        }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get all bookings for a schedule (staff only)
     */
    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get schedule bookings",
        description = "Retrieve all bookings for a specific schedule. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Bookings retrieved successfully",
                content = [Content(schema = Schema(implementation = BookingSummaryResponse::class))]
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
    fun getScheduleBookings(
        @Parameter(description = "Schedule ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable scheduleId: UUID,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "100") size: Int
    ): ResponseEntity<ApiResponse<List<BookingSummaryResponse>>> {
        logger.info("Fetching bookings for schedule: $scheduleId")

        val bookings = bookingRepository.findBySchedule(scheduleId, page, size).getOrThrow()

        val schedule = scheduleRepository.findById(scheduleId).getOrThrow().orElse(null)
        val gymClass = schedule?.let {
            classRepository.findById(it.classId).getOrThrow().orElse(null)
        }

        val response = bookings.map { booking ->
            booking.toBookingSummaryResponse(
                className = gymClass?.name,
                classTime = schedule?.startDate
            )
        }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Mark attendance (trainer/staff)
     */
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Check in member",
        description = "Mark a member as attended for their booking. Trainer and staff only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Member checked in successfully",
                content = [Content(schema = Schema(implementation = CheckInResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Booking cannot be checked in (already checked in, cancelled, etc.)"
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
                description = "Booking not found"
            )
        ]
    )
    fun checkIn(
        @Parameter(description = "Booking ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: CheckInRequest
    ): ResponseEntity<ApiResponse<CheckInResponse>> {
        logger.info("Checking in booking ID: $id")

        val command = MarkAttendanceCommand(
            bookingId = id,
            attended = true
        )

        val result = markAttendanceUseCase.execute(command).getOrThrow()

        val response = CheckInResponse(
            bookingId = result.id,
            memberId = result.memberId,
            scheduleId = result.scheduleId,
            checkedInAt = result.checkedInAt ?: Instant.now(),
            message = "Member checked in successfully"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Join waitlist
     */
    @PostMapping("/waitlist")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Join waitlist",
        description = "Add member to waitlist for a full class"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Added to waitlist successfully",
                content = [Content(schema = Schema(implementation = WaitlistResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Class not full or member already on waitlist"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            )
        ]
    )
    fun joinWaitlist(
        @Valid @RequestBody request: JoinWaitlistRequest
    ): ResponseEntity<ApiResponse<WaitlistResponse>> {
        logger.info("Joining waitlist for schedule: ${request.scheduleId}")

        // TODO: Get member ID from security context if not provided
        val memberId = request.memberId ?: UUID.randomUUID() // Placeholder

        val command = JoinWaitlistCommand(
            memberId = memberId,
            scheduleId = request.scheduleId
        )

        val result = joinWaitlistUseCase.execute(command).getOrThrow()

        val response = WaitlistResponse(
            bookingId = result.bookingId,
            memberId = memberId,
            scheduleId = request.scheduleId,
            waitlistPosition = result.position,
            message = "Added to waitlist at position ${result.position}"
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Leave waitlist
     */
    @DeleteMapping("/waitlist/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Leave waitlist",
        description = "Remove member from waitlist"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Removed from waitlist successfully"
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Booking is not on waitlist"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Booking not found"
            )
        ]
    )
    fun leaveWaitlist(
        @Parameter(description = "Booking ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Leaving waitlist for booking ID: $id")

        val bookingOptional = bookingRepository.findById(id).getOrThrow()
        if (!bookingOptional.isPresent) {
            throw ResourceNotFoundException("Booking not found with ID: $id")
        }

        val booking = bookingOptional.get()
        if (!booking.isWaitlisted()) {
            throw IllegalStateException("Booking is not on waitlist")
        }

        val cancelledBooking = booking.cancel("Left waitlist")
        bookingRepository.save(cancelledBooking).getOrThrow()

        return ResponseEntity.ok(ApiResponse.success("Removed from waitlist successfully"))
    }

    /**
     * Bulk book for a group (staff/admin)
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Bulk book for group",
        description = "Book a class for multiple members at once. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Bulk booking completed",
                content = [Content(schema = Schema(implementation = BulkBookingResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request"
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
    fun bulkBook(
        @Valid @RequestBody request: BulkBookRequest
    ): ResponseEntity<ApiResponse<BulkBookingResponse>> {
        logger.info("Bulk booking ${request.memberIds.size} members for schedule: ${request.scheduleId}")

        val successfulBookings = mutableListOf<BookingSummaryResponse>()
        val failedBookings = mutableListOf<FailedBooking>()

        val schedule = scheduleRepository.findById(request.scheduleId).getOrThrow().orElse(null)
        val gymClass = schedule?.let {
            classRepository.findById(it.classId).getOrThrow().orElse(null)
        }

        request.memberIds.forEach { memberId ->
            try {
                val command = BookClassCommand(
                    memberId = memberId,
                    scheduleId = request.scheduleId
                )
                val booking = bookClassUseCase.execute(command).getOrThrow()

                successfulBookings.add(
                    bookingMapper.toDTO(booking).toBookingSummaryResponse(
                        className = gymClass?.name,
                        classTime = schedule?.startDate
                    )
                )
            } catch (e: Exception) {
                logger.warn("Failed to book for member $memberId: ${e.message}")
                failedBookings.add(
                    FailedBooking(
                        memberId = memberId,
                        reason = e.message ?: "Unknown error"
                    )
                )
            }
        }

        val response = BulkBookingResponse(
            scheduleId = request.scheduleId,
            successfulBookings = successfulBookings,
            failedBookings = failedBookings,
            totalRequested = request.memberIds.size,
            totalSuccess = successfulBookings.size,
            totalFailed = failedBookings.size
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    // Extension functions for DTO conversion

    private fun Booking.toBookingResponse(
        className: String? = null,
        classTime: java.time.LocalDateTime? = null,
        memberName: String? = null
    ): BookingResponse {
        return BookingResponse(
            id = this.id,
            memberId = this.memberId,
            memberName = memberName,
            scheduleId = this.scheduleId,
            className = className,
            classTime = classTime,
            status = this.status.name,
            bookedAt = this.bookedAt,
            waitlistPosition = this.waitlistPosition,
            confirmedAt = this.confirmedAt,
            checkedInAt = this.checkedInAt,
            cancelledAt = this.cancelledAt,
            cancellationReason = this.cancellationReason,
            noShowMarkedAt = this.noShowMarkedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun BookingDTO.toBookingResponse(
        className: String? = null,
        classTime: java.time.LocalDateTime? = null,
        memberName: String? = null
    ): BookingResponse {
        return BookingResponse(
            id = this.id,
            memberId = this.memberId,
            memberName = memberName,
            scheduleId = this.scheduleId,
            className = className,
            classTime = classTime,
            status = this.status.name,
            bookedAt = this.bookedAt,
            waitlistPosition = this.waitlistPosition,
            confirmedAt = this.confirmedAt,
            checkedInAt = this.checkedInAt,
            cancelledAt = this.cancelledAt,
            cancellationReason = this.cancellationReason,
            noShowMarkedAt = this.noShowMarkedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun Booking.toBookingSummaryResponse(
        className: String? = null,
        classTime: java.time.LocalDateTime? = null,
        memberName: String? = null
    ): BookingSummaryResponse {
        return BookingSummaryResponse(
            id = this.id,
            memberId = this.memberId,
            memberName = memberName,
            scheduleId = this.scheduleId,
            className = className,
            classTime = classTime,
            status = this.status.name,
            bookedAt = this.bookedAt
        )
    }

    private fun BookingDTO.toBookingSummaryResponse(
        className: String? = null,
        classTime: java.time.LocalDateTime? = null,
        memberName: String? = null
    ): BookingSummaryResponse {
        return BookingSummaryResponse(
            id = this.id,
            memberId = this.memberId,
            memberName = memberName,
            scheduleId = this.scheduleId,
            className = className,
            classTime = classTime,
            status = this.status.name,
            bookedAt = this.bookedAt
        )
    }
}
