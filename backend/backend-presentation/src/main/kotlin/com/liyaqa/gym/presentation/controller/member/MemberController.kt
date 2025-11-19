package com.liyaqa.gym.presentation.controller.member

import com.liyaqa.gym.application.member.*
import com.liyaqa.gym.application.member.commands.*
import com.liyaqa.gym.application.member.dto.MemberDTO
import com.liyaqa.gym.application.member.dto.MemberSummaryDTO
import com.liyaqa.gym.application.member.dto.PageResult
import com.liyaqa.gym.application.member.queries.SearchMembersQuery
import com.liyaqa.gym.application.member.queries.SortDirection
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.MemberStatus
import com.liyaqa.gym.presentation.dto.common.ApiResponse
import com.liyaqa.gym.presentation.dto.member.*
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
 * REST Controller for member management operations.
 * Handles member registration, profile management, search, and administrative actions.
 */
@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "Member management endpoints")
class MemberController(
    private val registerMemberUseCase: RegisterMemberUseCase,
    private val getMemberDetailsUseCase: GetMemberDetailsUseCase,
    private val updateMemberProfileUseCase: UpdateMemberProfileUseCase,
    private val searchMembersUseCase: SearchMembersUseCase,
    private val suspendMemberUseCase: SuspendMemberUseCase,
    private val deleteMemberUseCase: DeleteMemberUseCase
) {

    private val logger = LoggerFactory.getLogger(MemberController::class.java)

    /**
     * Register a new member
     */
    @PostMapping
    @Operation(
        summary = "Register new member",
        description = "Register a new member in the system. Email must be unique."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Member registered successfully",
                content = [Content(schema = Schema(implementation = MemberResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "Member with this email already exists"
            )
        ]
    )
    fun registerMember(
        @Valid @RequestBody request: RegisterMemberRequest
    ): ResponseEntity<ApiResponse<MemberResponse>> {
        logger.info("Registering new member with email: ${request.email}")

        val command = RegisterMemberCommand(
            branchId = request.branchId,
            name = request.name,
            nameArabic = request.nameArabic,
            email = request.email,
            phone = request.phone,
            nationalId = request.nationalId,
            gender = Gender.valueOf(request.gender),
            dateOfBirth = request.dateOfBirth,
            emergencyContactName = request.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone,
            profilePhotoUrl = request.profilePhotoUrl,
            notes = request.notes
        )

        val result = registerMemberUseCase.execute(command)
            .getOrThrow()

        val response = result.toMemberResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Get member details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get member details",
        description = "Retrieve detailed information about a specific member. Requires authentication."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Member details retrieved successfully",
                content = [Content(schema = Schema(implementation = MemberResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - insufficient permissions"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Member not found"
            )
        ]
    )
    fun getMemberDetails(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<MemberResponse>> {
        logger.info("Fetching member details for ID: $id")

        val result = getMemberDetailsUseCase.execute(id)
            .getOrElse {
                throw ResourceNotFoundException("Member not found with ID: $id")
            }

        val response = result.toMemberResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Update member profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update member profile",
        description = "Update member profile information. Only provided fields will be updated."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Member profile updated successfully",
                content = [Content(schema = Schema(implementation = MemberResponse::class))]
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
                responseCode = "404",
                description = "Member not found"
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "Email already in use by another member"
            )
        ]
    )
    fun updateMemberProfile(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateMemberRequest
    ): ResponseEntity<ApiResponse<MemberResponse>> {
        logger.info("Updating member profile for ID: $id")

        val command = UpdateMemberCommand(
            memberId = id,
            name = request.name,
            nameArabic = request.nameArabic,
            email = request.email,
            phone = request.phone,
            nationalId = request.nationalId,
            gender = request.gender?.let { Gender.valueOf(it) },
            dateOfBirth = request.dateOfBirth,
            emergencyContactName = request.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone,
            profilePhotoUrl = request.profilePhotoUrl,
            notes = request.notes
        )

        val result = updateMemberProfileUseCase.execute(command)
            .getOrThrow()

        val response = result.toMemberResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Search members with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Search members",
        description = "Search and filter members with pagination. Staff and admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Members retrieved successfully",
                content = [Content(schema = Schema(implementation = PagedMemberResponse::class))]
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
    fun searchMembers(
        @Parameter(description = "Branch ID to filter by")
        @RequestParam(required = false) branchId: UUID?,
        @Parameter(description = "Search query (name, email, phone)")
        @RequestParam(required = false) query: String?,
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Filter by gender")
        @RequestParam(required = false) gender: String?,
        @Parameter(description = "Filter by minimum age")
        @RequestParam(required = false) minAge: Int?,
        @Parameter(description = "Filter by maximum age")
        @RequestParam(required = false) maxAge: Int?,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @Parameter(description = "Sort direction")
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<ApiResponse<PagedMemberResponse>> {
        logger.info("Searching members with query: $query, status: $status, page: $page, size: $size")

        val searchQuery = SearchMembersQuery(
            branchId = branchId,
            searchQuery = query,
            status = status?.let { MemberStatus.valueOf(it) },
            gender = gender?.let { Gender.valueOf(it) },
            minAge = minAge,
            maxAge = maxAge,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = SortDirection.valueOf(sortDirection)
        )

        val result = searchMembersUseCase.execute(searchQuery)
            .getOrThrow()

        val response = result.toPagedMemberResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Suspend a member
     */
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Suspend member",
        description = "Suspend a member account. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Member suspended successfully",
                content = [Content(schema = Schema(implementation = MemberSuspensionResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request or member already suspended"
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
    fun suspendMember(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: SuspendMemberRequest
    ): ResponseEntity<ApiResponse<MemberSuspensionResponse>> {
        logger.info("Suspending member ID: $id")

        // TODO: Get current user ID from SecurityContext
        val currentUserId = UUID.randomUUID() // Placeholder

        val command = SuspendMemberCommand(
            memberId = id,
            reason = request.reason,
            suspendedBy = currentUserId,
            notifyMember = request.notifyMember
        )

        val result = suspendMemberUseCase.execute(command)
            .getOrThrow()

        val response = MemberSuspensionResponse(
            memberId = result.id,
            status = result.status.name,
            reason = request.reason,
            notified = request.notifyMember,
            suspendedAt = Instant.now(),
            message = "Member successfully suspended"
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Delete member (GDPR compliant)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Delete member",
        description = "Delete a member account (GDPR compliant). Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Member deleted successfully",
                content = [Content(schema = Schema(implementation = MemberDeletionResponse::class))]
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
    fun deleteMember(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: DeleteMemberRequest
    ): ResponseEntity<ApiResponse<MemberDeletionResponse>> {
        logger.info("Deleting member ID: $id")

        // TODO: Get current user ID from SecurityContext
        val currentUserId = UUID.randomUUID() // Placeholder

        val command = DeleteMemberCommand(
            memberId = id,
            reason = request.reason,
            deletedBy = currentUserId,
            exportDataBeforeDeletion = request.exportDataBeforeDeletion
        )

        val result = deleteMemberUseCase.execute(command)
            .getOrThrow()

        val response = MemberDeletionResponse(
            memberId = result.memberId,
            exportedDataPath = result.exportedDataPath,
            deletedAt = result.deletedAt,
            success = result.success,
            message = result.message
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get member's subscriptions
     * TODO: Implement when subscription query use cases are available
     */
    @GetMapping("/{id}/subscriptions")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get member's subscriptions",
        description = "Retrieve all subscriptions for a specific member"
    )
    fun getMemberSubscriptions(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<Any>>> {
        logger.info("Fetching subscriptions for member ID: $id")
        // TODO: Implement subscription query use case
        return ResponseEntity.ok(ApiResponse.success(emptyList()))
    }

    /**
     * Get member's bookings
     * TODO: Implement when booking query use cases are available
     */
    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get member's bookings",
        description = "Retrieve all bookings for a specific member"
    )
    fun getMemberBookings(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<Any>>> {
        logger.info("Fetching bookings for member ID: $id")
        // TODO: Implement booking query use case
        return ResponseEntity.ok(ApiResponse.success(emptyList()))
    }

    /**
     * Get member's attendance history
     * TODO: Implement when attendance query use cases are available
     */
    @GetMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get member's attendance history",
        description = "Retrieve attendance history for a specific member"
    )
    fun getMemberAttendance(
        @Parameter(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<Any>>> {
        logger.info("Fetching attendance for member ID: $id")
        // TODO: Implement attendance query use case
        return ResponseEntity.ok(ApiResponse.success(emptyList()))
    }

    // Extension functions for DTO conversion

    private fun MemberDTO.toMemberResponse(): MemberResponse {
        return MemberResponse(
            id = this.id,
            branchId = this.branchId,
            name = this.name,
            nameArabic = this.nameArabic,
            email = this.email,
            phone = this.phone,
            nationalId = this.nationalId,
            gender = this.gender.name,
            dateOfBirth = this.dateOfBirth,
            age = this.age,
            status = this.status.name,
            profilePhotoUrl = this.profilePhotoUrl,
            emergencyContactName = this.emergencyContactName,
            emergencyContactPhone = this.emergencyContactPhone,
            notes = this.notes,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun MemberSummaryDTO.toMemberSummaryResponse(): MemberSummaryResponse {
        return MemberSummaryResponse(
            id = this.id,
            branchId = this.branchId,
            name = this.name,
            email = this.email,
            phone = this.phone,
            status = this.status.name,
            gender = this.gender.name,
            profilePhotoUrl = this.profilePhotoUrl,
            createdAt = this.createdAt
        )
    }

    private fun PageResult<MemberSummaryDTO>.toPagedMemberResponse(): PagedMemberResponse {
        return PagedMemberResponse(
            content = this.content.map { it.toMemberSummaryResponse() },
            page = this.page,
            size = this.size,
            totalElements = this.totalElements,
            totalPages = this.totalPages,
            hasNext = this.hasNext,
            hasPrevious = this.hasPrevious
        )
    }
}
