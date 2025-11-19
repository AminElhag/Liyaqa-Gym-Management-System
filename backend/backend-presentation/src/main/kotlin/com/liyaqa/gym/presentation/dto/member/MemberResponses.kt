package com.liyaqa.gym.presentation.dto.member

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Response DTO for complete member details
 */
@Schema(description = "Complete member details")
data class MemberResponse(
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Branch ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val branchId: UUID,

    @Schema(description = "Member's full name", example = "Ahmed Mohammed")
    val name: String,

    @Schema(description = "Member's name in Arabic", example = "أحمد محمد")
    val nameArabic: String?,

    @Schema(description = "Member's email address", example = "ahmed@example.com")
    val email: String,

    @Schema(description = "Member's phone number", example = "+966501234567")
    val phone: String,

    @Schema(description = "Saudi National ID", example = "1234567890")
    val nationalId: String?,

    @Schema(description = "Member's gender", example = "MALE", allowableValues = ["MALE", "FEMALE"])
    val gender: String,

    @Schema(description = "Member's date of birth", example = "1990-01-15")
    val dateOfBirth: LocalDate?,

    @Schema(description = "Member's age in years", example = "35")
    val age: Int?,

    @Schema(description = "Member's status", example = "ACTIVE", allowableValues = ["ACTIVE", "INACTIVE", "SUSPENDED", "PENDING_APPROVAL"])
    val status: String,

    @Schema(description = "URL to member's profile photo", example = "https://cdn.example.com/photos/member123.jpg")
    val profilePhotoUrl: String?,

    @Schema(description = "Emergency contact person's name", example = "Fatima Ahmed")
    val emergencyContactName: String?,

    @Schema(description = "Emergency contact person's phone", example = "+966507654321")
    val emergencyContactPhone: String?,

    @Schema(description = "Additional notes about the member", example = "Prefers morning workout sessions")
    val notes: String?,

    @Schema(description = "Member creation timestamp", example = "2025-01-15T10:30:00Z")
    val createdAt: Instant,

    @Schema(description = "Member last update timestamp", example = "2025-11-19T14:45:00Z")
    val updatedAt: Instant
)

/**
 * Response DTO for member summary (used in lists)
 */
@Schema(description = "Member summary information for list views")
data class MemberSummaryResponse(
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Branch ID", example = "123e4567-e89b-12d3-a456-426614174001")
    val branchId: UUID,

    @Schema(description = "Member's full name", example = "Ahmed Mohammed")
    val name: String,

    @Schema(description = "Member's email address", example = "ahmed@example.com")
    val email: String,

    @Schema(description = "Member's phone number", example = "+966501234567")
    val phone: String,

    @Schema(description = "Member's status", example = "ACTIVE", allowableValues = ["ACTIVE", "INACTIVE", "SUSPENDED", "PENDING_APPROVAL"])
    val status: String,

    @Schema(description = "Member's gender", example = "MALE", allowableValues = ["MALE", "FEMALE"])
    val gender: String,

    @Schema(description = "URL to member's profile photo", example = "https://cdn.example.com/photos/member123.jpg")
    val profilePhotoUrl: String?,

    @Schema(description = "Member creation timestamp", example = "2025-01-15T10:30:00Z")
    val createdAt: Instant
)

/**
 * Paginated response wrapper for member lists
 */
@Schema(description = "Paginated member list response")
data class PagedMemberResponse(
    @Schema(description = "List of members in the current page")
    val content: List<MemberSummaryResponse>,

    @Schema(description = "Current page number (zero-based)", example = "0")
    val page: Int,

    @Schema(description = "Number of items per page", example = "20")
    val size: Int,

    @Schema(description = "Total number of members", example = "150")
    val totalElements: Long,

    @Schema(description = "Total number of pages", example = "8")
    val totalPages: Int,

    @Schema(description = "Whether there is a next page", example = "true")
    val hasNext: Boolean,

    @Schema(description = "Whether there is a previous page", example = "false")
    val hasPrevious: Boolean
)

/**
 * Response DTO for member deletion operations
 */
@Schema(description = "Member deletion result")
data class MemberDeletionResponse(
    @Schema(description = "Deleted member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @Schema(description = "Path to exported data file (if requested)", example = "/exports/member_123e4567_data.json")
    val exportedDataPath: String?,

    @Schema(description = "Deletion timestamp", example = "2025-11-19T14:45:00Z")
    val deletedAt: Instant,

    @Schema(description = "Deletion success status", example = "true")
    val success: Boolean,

    @Schema(description = "Result message", example = "Member successfully deleted and data exported")
    val message: String
)

/**
 * Response DTO for member suspension operations
 */
@Schema(description = "Member suspension result")
data class MemberSuspensionResponse(
    @Schema(description = "Suspended member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @Schema(description = "New member status", example = "SUSPENDED")
    val status: String,

    @Schema(description = "Suspension reason", example = "Violation of gym rules")
    val reason: String,

    @Schema(description = "Whether member was notified", example = "true")
    val notified: Boolean,

    @Schema(description = "Suspension timestamp", example = "2025-11-19T14:45:00Z")
    val suspendedAt: Instant,

    @Schema(description = "Message", example = "Member successfully suspended")
    val message: String
)
