package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.presentation.dto.equipment.*
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
import java.time.LocalDate
import java.util.UUID

/**
 * REST Controller for gym equipment management.
 * Handles equipment inventory, maintenance scheduling, and issue reporting.
 */
@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Equipment", description = "Gym equipment management endpoints")
class EquipmentController {

    private val logger = LoggerFactory.getLogger(EquipmentController::class.java)

    /**
     * List all equipment with filtering
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "List all equipment",
        description = "Retrieve list of all gym equipment with filtering by branch and status."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Equipment list retrieved successfully",
                content = [Content(schema = Schema(implementation = EquipmentListResponse::class))]
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
    fun listEquipment(
        @Parameter(description = "Filter by branch ID")
        @RequestParam(required = false) branchId: UUID?,
        @Parameter(description = "Filter by status (AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE)")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Filter by category")
        @RequestParam(required = false) category: String?,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<EquipmentListResponse>> {
        logger.info("Listing equipment - branchId: $branchId, status: $status, page: $page")

        // TODO: Implement via use case
        // - Query equipment with filters
        // - Include maintenance status
        // - Calculate usage statistics

        val response = EquipmentListResponse(
            content = emptyList(), // TODO: Get actual equipment
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
     * Get equipment details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get equipment details",
        description = "Retrieve detailed information about a specific equipment item including maintenance history."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Equipment details retrieved successfully",
                content = [Content(schema = Schema(implementation = EquipmentResponse::class))]
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
                description = "Equipment not found"
            )
        ]
    )
    fun getEquipmentDetails(
        @Parameter(description = "Equipment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<EquipmentResponse>> {
        logger.info("Fetching equipment details for ID: $id")

        // TODO: Implement via use case
        // - Get equipment details
        // - Include maintenance history
        // - Include current status and location

        val response = EquipmentResponse(
            id = id,
            branchId = UUID.randomUUID(),
            name = "Equipment Name", // TODO: Get from database
            category = "CARDIO",
            manufacturer = "Manufacturer",
            model = "Model",
            serialNumber = "SN-12345",
            purchaseDate = LocalDate.now(),
            purchasePrice = 5000.0,
            status = "AVAILABLE",
            location = "Main Floor - Section A",
            lastMaintenanceDate = LocalDate.now().minusMonths(1),
            nextMaintenanceDate = LocalDate.now().plusMonths(2),
            maintenanceHistory = emptyList(),
            notes = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Add new equipment (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Add new equipment",
        description = "Add a new equipment item to the inventory. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Equipment added successfully",
                content = [Content(schema = Schema(implementation = EquipmentResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden - admin role required"
            )
        ]
    )
    fun addEquipment(
        @Valid @RequestBody request: AddEquipmentRequest
    ): ResponseEntity<ApiResponse<EquipmentResponse>> {
        logger.info("Adding new equipment: ${request.name}")

        // TODO: Implement via use case
        // - Validate equipment data
        // - Generate equipment ID
        // - Store in database
        // - Schedule initial maintenance

        val response = EquipmentResponse(
            id = UUID.randomUUID(),
            branchId = request.branchId,
            name = request.name,
            category = request.category,
            manufacturer = request.manufacturer,
            model = request.model,
            serialNumber = request.serialNumber,
            purchaseDate = request.purchaseDate,
            purchasePrice = request.purchasePrice,
            status = "AVAILABLE",
            location = request.location,
            lastMaintenanceDate = null,
            nextMaintenanceDate = request.nextMaintenanceDate,
            maintenanceHistory = emptyList(),
            notes = request.notes,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Update equipment details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update equipment",
        description = "Update equipment information. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Equipment updated successfully",
                content = [Content(schema = Schema(implementation = EquipmentResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request data"
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
                description = "Equipment not found"
            )
        ]
    )
    fun updateEquipment(
        @Parameter(description = "Equipment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateEquipmentRequest
    ): ResponseEntity<ApiResponse<EquipmentResponse>> {
        logger.info("Updating equipment ID: $id")

        // TODO: Implement via use case
        // - Validate equipment exists
        // - Update fields
        // - Maintain audit trail

        val response = EquipmentResponse(
            id = id,
            branchId = UUID.randomUUID(),
            name = request.name ?: "Equipment Name",
            category = request.category ?: "CARDIO",
            manufacturer = request.manufacturer ?: "Manufacturer",
            model = request.model ?: "Model",
            serialNumber = request.serialNumber ?: "SN-12345",
            purchaseDate = request.purchaseDate ?: LocalDate.now(),
            purchasePrice = request.purchasePrice ?: 5000.0,
            status = request.status ?: "AVAILABLE",
            location = request.location ?: "Main Floor",
            lastMaintenanceDate = request.lastMaintenanceDate,
            nextMaintenanceDate = request.nextMaintenanceDate,
            maintenanceHistory = emptyList(),
            notes = request.notes,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Remove equipment from inventory
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Remove equipment",
        description = "Remove equipment from inventory (soft delete). Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Equipment removed successfully"
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
                description = "Equipment not found"
            )
        ]
    )
    fun removeEquipment(
        @Parameter(description = "Equipment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Removing equipment ID: $id")

        // TODO: Implement via use case
        // - Soft delete equipment
        // - Update inventory records
        // - Cancel future maintenance schedules

        return ResponseEntity.ok(ApiResponse.success("Equipment removed successfully"))
    }

    /**
     * Report equipment issue
     */
    @PostMapping("/{id}/report-issue")
    @PreAuthorize("hasAnyRole('MEMBER', 'STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Report equipment issue",
        description = "Report a problem or malfunction with equipment. Available to all authenticated users."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Issue reported successfully",
                content = [Content(schema = Schema(implementation = EquipmentIssueResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Equipment not found"
            )
        ]
    )
    fun reportIssue(
        @Parameter(description = "Equipment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: ReportIssueRequest
    ): ResponseEntity<ApiResponse<EquipmentIssueResponse>> {
        logger.info("Reporting issue for equipment ID: $id")

        // TODO: Implement via use case
        // - Create issue record
        // - Update equipment status if needed
        // - Notify maintenance staff
        // - Generate work order

        val response = EquipmentIssueResponse(
            issueId = UUID.randomUUID(),
            equipmentId = id,
            reportedBy = UUID.randomUUID(), // TODO: Get from security context
            severity = request.severity,
            description = request.description,
            reportedAt = Instant.now(),
            status = "OPEN",
            message = "Issue reported successfully. Maintenance team has been notified."
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Get maintenance schedule
     */
    @GetMapping("/maintenance")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get maintenance schedule",
        description = "Retrieve equipment maintenance schedule with upcoming and overdue maintenance tasks."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Maintenance schedule retrieved successfully",
                content = [Content(schema = Schema(implementation = MaintenanceScheduleResponse::class))]
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
    fun getMaintenanceSchedule(
        @Parameter(description = "Filter by branch ID")
        @RequestParam(required = false) branchId: UUID?,
        @Parameter(description = "Show only overdue maintenance")
        @RequestParam(defaultValue = "false") overdueOnly: Boolean,
        @Parameter(description = "Number of days to look ahead")
        @RequestParam(defaultValue = "30") daysAhead: Int
    ): ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> {
        logger.info("Fetching maintenance schedule - branchId: $branchId, overdueOnly: $overdueOnly")

        // TODO: Implement via use case
        // - Get all equipment needing maintenance
        // - Calculate overdue items
        // - Get upcoming maintenance within date range
        // - Prioritize by urgency

        val response = MaintenanceScheduleResponse(
            overdue = emptyList(), // TODO: Get overdue maintenance
            upcoming = emptyList(), // TODO: Get upcoming maintenance
            totalOverdue = 0,
            totalUpcoming = 0
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
