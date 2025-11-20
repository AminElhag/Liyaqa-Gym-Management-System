package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.presentation.dto.access.*
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
 * REST Controller for access control and facility entry management.
 * Handles member check-in/check-out, occupancy tracking, access logs, and guest passes.
 */
@RestController
@RequestMapping("/api/v1/access")
@Tag(name = "Access Control", description = "Facility access and entry management endpoints")
class AccessControlController {

    private val logger = LoggerFactory.getLogger(AccessControlController::class.java)

    /**
     * Check in a member to the facility
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Check in member",
        description = "Check in a member to the facility. Validates membership status and access rights."
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
                description = "Invalid request or member cannot check in (suspended, expired subscription, etc.)"
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
                description = "Member not found"
            )
        ]
    )
    fun checkIn(
        @Valid @RequestBody request: CheckInRequest
    ): ResponseEntity<ApiResponse<CheckInResponse>> {
        logger.info("Checking in member: ${request.memberId}")

        // TODO: Implement check-in logic via use case
        // - Validate member exists and is active
        // - Check subscription status
        // - Verify access permissions
        // - Record check-in time
        // - Update facility occupancy

        val response = CheckInResponse(
            accessLogId = UUID.randomUUID(),
            memberId = request.memberId,
            memberName = "Member Name", // TODO: Get from member service
            branchId = UUID.randomUUID(), // TODO: Get from context or request
            checkInTime = Instant.now(),
            accessGranted = true,
            message = "Check-in successful"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Check out a member from the facility
     */
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Check out member",
        description = "Check out a member from the facility. Records exit time and updates occupancy."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Member checked out successfully",
                content = [Content(schema = Schema(implementation = CheckOutResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or member not currently checked in"
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
    fun checkOut(
        @Valid @RequestBody request: CheckOutRequest
    ): ResponseEntity<ApiResponse<CheckOutResponse>> {
        logger.info("Checking out member: ${request.memberId}")

        // TODO: Implement check-out logic via use case
        // - Find active check-in record
        // - Record check-out time
        // - Calculate duration
        // - Update facility occupancy

        val response = CheckOutResponse(
            accessLogId = UUID.randomUUID(),
            memberId = request.memberId,
            checkOutTime = Instant.now(),
            duration = "2h 30m", // TODO: Calculate actual duration
            message = "Check-out successful"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get current facility occupancy
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get current occupancy",
        description = "Get current facility occupancy count and list of checked-in members."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Occupancy data retrieved successfully",
                content = [Content(schema = Schema(implementation = CurrentOccupancyResponse::class))]
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
    fun getCurrentOccupancy(): ResponseEntity<ApiResponse<CurrentOccupancyResponse>> {
        logger.info("Fetching current facility occupancy")

        // TODO: Implement via use case
        // - Get all active check-ins (no check-out)
        // - Group by branch if multi-branch
        // - Calculate capacity percentage

        val response = CurrentOccupancyResponse(
            currentCount = 0, // TODO: Get actual count
            capacity = 100, // TODO: Get from branch configuration
            occupancyPercentage = 0.0,
            checkedInMembers = emptyList() // TODO: Get actual list
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get access logs with filtering and pagination
     */
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get access logs",
        description = "Retrieve access logs with filtering by date range, member, and branch. Supports pagination."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Access logs retrieved successfully",
                content = [Content(schema = Schema(implementation = PagedAccessLogsResponse::class))]
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
    fun getAccessLogs(
        @Parameter(description = "Filter by member ID")
        @RequestParam(required = false) memberId: UUID?,
        @Parameter(description = "Filter by branch ID")
        @RequestParam(required = false) branchId: UUID?,
        @Parameter(description = "Filter by start date (ISO format)")
        @RequestParam(required = false) startDate: Instant?,
        @Parameter(description = "Filter by end date (ISO format)")
        @RequestParam(required = false) endDate: Instant?,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PagedAccessLogsResponse>> {
        logger.info("Fetching access logs - memberId: $memberId, branchId: $branchId, page: $page")

        // TODO: Implement via use case
        // - Query access logs with filters
        // - Include member details
        // - Calculate durations
        // - Paginate results

        val response = PagedAccessLogsResponse(
            content = emptyList(), // TODO: Get actual logs
            page = page,
            size = size,
            totalElements = 0,
            totalPages = 0,
            hasNext = false,
            hasPrevious = false
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Generate a temporary guest pass
     */
    @PostMapping("/grant-temporary")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Grant temporary access",
        description = "Generate a temporary guest pass for non-members. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Guest pass created successfully",
                content = [Content(schema = Schema(implementation = GuestPassResponse::class))]
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
    fun grantTemporaryAccess(
        @Valid @RequestBody request: GuestPassRequest
    ): ResponseEntity<ApiResponse<GuestPassResponse>> {
        logger.info("Creating guest pass for: ${request.guestName}")

        // TODO: Implement via use case
        // - Generate unique access code
        // - Set expiration time
        // - Record issuer details
        // - Store in database

        val response = GuestPassResponse(
            guestPassId = UUID.randomUUID(),
            guestName = request.guestName,
            accessCode = "GUEST-${UUID.randomUUID().toString().substring(0, 8).uppercase()}", // TODO: Better code generation
            validFrom = Instant.now(),
            validUntil = request.validUntil,
            issuedBy = UUID.randomUUID(), // TODO: Get from security context
            message = "Guest pass created successfully"
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Revoke access for a member
     */
    @PutMapping("/revoke/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Revoke member access",
        description = "Revoke facility access for a member. Admin only. Used for suspensions or security concerns."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Access revoked successfully",
                content = [Content(schema = Schema(implementation = RevokeAccessResponse::class))]
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
                description = "Forbidden - admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Member not found"
            )
        ]
    )
    fun revokeAccess(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable memberId: UUID,
        @Valid @RequestBody request: RevokeAccessRequest
    ): ResponseEntity<ApiResponse<RevokeAccessResponse>> {
        logger.info("Revoking access for member: $memberId")

        // TODO: Implement via use case
        // - Update member access status
        // - Force check-out if currently in facility
        // - Record revocation reason
        // - Notify relevant parties

        val response = RevokeAccessResponse(
            memberId = memberId,
            accessRevoked = true,
            reason = request.reason,
            revokedAt = Instant.now(),
            revokedBy = UUID.randomUUID(), // TODO: Get from security context
            message = "Access revoked successfully"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
