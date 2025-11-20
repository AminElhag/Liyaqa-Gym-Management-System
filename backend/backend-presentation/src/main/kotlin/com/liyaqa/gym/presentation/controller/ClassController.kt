package com.liyaqa.gym.presentation.controller

import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.entities.Class
import com.liyaqa.gym.domain.entities.ClassLevel
import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.repositories.ClassRepository
import com.liyaqa.gym.presentation.dto.classmanagement.ClassResponse
import com.liyaqa.gym.presentation.dto.classmanagement.ClassSummaryResponse
import com.liyaqa.gym.presentation.dto.classmanagement.CreateClassRequest
import com.liyaqa.gym.presentation.dto.classmanagement.UpdateClassRequest
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
 * REST Controller for class management operations.
 * Handles CRUD operations for fitness class types.
 */
@RestController
@RequestMapping("/api/v1/classes")
@Tag(name = "Classes", description = "Class management endpoints")
class ClassController(
    private val classRepository: ClassRepository
) {

    private val logger = LoggerFactory.getLogger(ClassController::class.java)

    /**
     * Get all class types
     */
    @GetMapping
    @Operation(
        summary = "List all class types",
        description = "Retrieve all class types with optional filtering by branch"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Classes retrieved successfully",
                content = [Content(schema = Schema(implementation = ClassSummaryResponse::class))]
            )
        ]
    )
    fun getAllClasses(
        @Parameter(description = "Filter by branch ID")
        @RequestParam(required = false) branchId: UUID?,
        @Parameter(description = "Filter by active status")
        @RequestParam(required = false) active: Boolean?,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<ClassSummaryResponse>>> {
        logger.info("Fetching all classes (branchId: $branchId, active: $active, page: $page, size: $size)")

        val classes = when {
            branchId != null -> classRepository.findByBranch(branchId, page, size).getOrThrow()
            active == true -> classRepository.findAllActive(page, size).getOrThrow()
            else -> classRepository.findAllActive(page, size).getOrThrow() // Default to active only
        }

        val response = classes.map { it.toClassSummaryResponse() }
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Get class details by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get class details",
        description = "Retrieve detailed information about a specific class type"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Class details retrieved successfully",
                content = [Content(schema = Schema(implementation = ClassResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Class not found"
            )
        ]
    )
    fun getClassDetails(
        @Parameter(description = "Class ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ClassResponse>> {
        logger.info("Fetching class details for ID: $id")

        val classOptional = classRepository.findById(id).getOrThrow()
        if (!classOptional.isPresent) {
            throw ResourceNotFoundException("Class not found with ID: $id")
        }

        val response = classOptional.get().toClassResponse()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Create a new class (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Create new class",
        description = "Create a new class type. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "Class created successfully",
                content = [Content(schema = Schema(implementation = ClassResponse::class))]
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
                description = "Forbidden - admin role required"
            )
        ]
    )
    fun createClass(
        @Valid @RequestBody request: CreateClassRequest
    ): ResponseEntity<ApiResponse<ClassResponse>> {
        logger.info("Creating new class: ${request.name}")

        val now = Instant.now()
        val gymClass = Class(
            id = UUID.randomUUID(),
            branchId = request.branchId,
            name = request.name,
            nameArabic = request.nameArabic,
            description = request.description,
            type = ClassType.valueOf(request.type),
            level = ClassLevel.valueOf(request.level),
            capacity = request.capacity,
            durationMinutes = request.durationMinutes,
            genderRestriction = request.genderRestriction?.let { Gender.valueOf(it) },
            imageUrl = request.imageUrl,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        val savedClass = classRepository.save(gymClass).getOrThrow()
        val response = savedClass.toClassResponse()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * Update a class (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update class",
        description = "Update an existing class type. Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Class updated successfully",
                content = [Content(schema = Schema(implementation = ClassResponse::class))]
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
                description = "Forbidden - admin role required"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Class not found"
            )
        ]
    )
    fun updateClass(
        @Parameter(description = "Class ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateClassRequest
    ): ResponseEntity<ApiResponse<ClassResponse>> {
        logger.info("Updating class ID: $id")

        val classOptional = classRepository.findById(id).getOrThrow()
        if (!classOptional.isPresent) {
            throw ResourceNotFoundException("Class not found with ID: $id")
        }

        val existingClass = classOptional.get()
        val updatedClass = existingClass.copy(
            name = request.name ?: existingClass.name,
            nameArabic = request.nameArabic ?: existingClass.nameArabic,
            description = request.description ?: existingClass.description,
            type = request.type?.let { ClassType.valueOf(it) } ?: existingClass.type,
            level = request.level?.let { ClassLevel.valueOf(it) } ?: existingClass.level,
            capacity = request.capacity ?: existingClass.capacity,
            durationMinutes = request.durationMinutes ?: existingClass.durationMinutes,
            genderRestriction = request.genderRestriction?.let { Gender.valueOf(it) } ?: existingClass.genderRestriction,
            imageUrl = request.imageUrl ?: existingClass.imageUrl,
            isActive = request.isActive ?: existingClass.isActive,
            updatedAt = Instant.now()
        )

        val savedClass = classRepository.save(updatedClass).getOrThrow()
        val response = savedClass.toClassResponse()

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Delete/deactivate a class (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Delete class",
        description = "Deactivate a class type (soft delete). Admin only."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Class deactivated successfully"
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
                description = "Class not found"
            )
        ]
    )
    fun deleteClass(
        @Parameter(description = "Class ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Deactivating class ID: $id")

        val classOptional = classRepository.findById(id).getOrThrow()
        if (!classOptional.isPresent) {
            throw ResourceNotFoundException("Class not found with ID: $id")
        }

        val deactivatedClass = classOptional.get().deactivate()
        classRepository.save(deactivatedClass).getOrThrow()

        return ResponseEntity.ok(ApiResponse.success("Class deactivated successfully"))
    }

    // Extension functions for DTO conversion

    private fun Class.toClassResponse(): ClassResponse {
        return ClassResponse(
            id = this.id,
            branchId = this.branchId,
            name = this.name,
            nameArabic = this.nameArabic,
            description = this.description,
            type = this.type.name,
            level = this.level.name,
            capacity = this.capacity,
            durationMinutes = this.durationMinutes,
            genderRestriction = this.genderRestriction?.name,
            imageUrl = this.imageUrl,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    private fun Class.toClassSummaryResponse(): ClassSummaryResponse {
        return ClassSummaryResponse(
            id = this.id,
            branchId = this.branchId,
            name = this.name,
            type = this.type.name,
            level = this.level.name,
            capacity = this.capacity,
            durationMinutes = this.durationMinutes,
            isActive = this.isActive
        )
    }
}
