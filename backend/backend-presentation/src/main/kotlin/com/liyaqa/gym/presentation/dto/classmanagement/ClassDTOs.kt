package com.liyaqa.gym.presentation.dto.classmanagement

import jakarta.validation.constraints.*
import java.time.Instant
import java.util.UUID

/**
 * Request to create a new class
 */
data class CreateClassRequest(
    @field:NotNull(message = "Branch ID is required")
    val branchId: UUID,

    @field:NotBlank(message = "Class name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,

    @field:Size(max = 100, message = "Arabic name cannot exceed 100 characters")
    val nameArabic: String?,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String?,

    @field:NotBlank(message = "Class type is required")
    val type: String,

    @field:NotBlank(message = "Class level is required")
    val level: String,

    @field:NotNull(message = "Capacity is required")
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 100, message = "Capacity cannot exceed 100")
    val capacity: Int,

    @field:NotNull(message = "Duration is required")
    @field:Min(value = 15, message = "Duration must be at least 15 minutes")
    @field:Max(value = 180, message = "Duration cannot exceed 180 minutes")
    val durationMinutes: Int,

    val genderRestriction: String?,

    @field:Size(max = 500, message = "Image URL cannot exceed 500 characters")
    val imageUrl: String?
)

/**
 * Request to update a class
 */
data class UpdateClassRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String?,

    @field:Size(max = 100, message = "Arabic name cannot exceed 100 characters")
    val nameArabic: String?,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String?,

    val type: String?,

    val level: String?,

    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 100, message = "Capacity cannot exceed 100")
    val capacity: Int?,

    @field:Min(value = 15, message = "Duration must be at least 15 minutes")
    @field:Max(value = 180, message = "Duration cannot exceed 180 minutes")
    val durationMinutes: Int?,

    val genderRestriction: String?,

    @field:Size(max = 500, message = "Image URL cannot exceed 500 characters")
    val imageUrl: String?,

    val isActive: Boolean?
)

/**
 * Response containing class details
 */
data class ClassResponse(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val nameArabic: String?,
    val description: String?,
    val type: String,
    val level: String,
    val capacity: Int,
    val durationMinutes: Int,
    val genderRestriction: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Summary response for class listings
 */
data class ClassSummaryResponse(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val type: String,
    val level: String,
    val capacity: Int,
    val durationMinutes: Int,
    val isActive: Boolean
)
