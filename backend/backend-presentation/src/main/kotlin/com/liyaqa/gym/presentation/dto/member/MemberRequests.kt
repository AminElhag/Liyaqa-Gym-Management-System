package com.liyaqa.gym.presentation.dto.member

import com.liyaqa.gym.presentation.validation.SaudiNationalId
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.util.UUID

/**
 * Request DTO for registering a new member
 */
@Schema(description = "Request to register a new member")
data class RegisterMemberRequest(
    @field:NotNull(message = "Branch ID is required")
    @Schema(description = "Branch ID where the member is registering", example = "123e4567-e89b-12d3-a456-426614174000")
    val branchId: UUID,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Member's full name", example = "Ahmed Mohammed")
    val name: String,

    @field:Size(max = 100, message = "Arabic name must not exceed 100 characters")
    @Schema(description = "Member's name in Arabic (optional)", example = "أحمد محمد")
    val nameArabic: String? = null,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "Member's email address", example = "ahmed@example.com")
    val email: String,

    @field:NotBlank(message = "Phone is required")
    @field:Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Phone must be 10-15 digits, optionally starting with +"
    )
    @Schema(description = "Member's phone number", example = "+966501234567")
    val phone: String,

    @field:SaudiNationalId
    @Schema(description = "Saudi National ID (optional)", example = "1234567890")
    val nationalId: String? = null,

    @field:NotNull(message = "Gender is required")
    @Schema(description = "Member's gender", example = "MALE", allowableValues = ["MALE", "FEMALE"])
    val gender: String,

    @field:Past(message = "Date of birth must be in the past")
    @Schema(description = "Member's date of birth (optional)", example = "1990-01-15")
    val dateOfBirth: LocalDate? = null,

    @field:Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    @Schema(description = "Emergency contact person's name (optional)", example = "Fatima Ahmed")
    val emergencyContactName: String? = null,

    @field:Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Emergency phone must be 10-15 digits, optionally starting with +"
    )
    @Schema(description = "Emergency contact person's phone (optional)", example = "+966507654321")
    val emergencyContactPhone: String? = null,

    @field:Size(max = 500, message = "Profile photo URL must not exceed 500 characters")
    @Schema(description = "URL to member's profile photo (optional)", example = "https://cdn.example.com/photos/member123.jpg")
    val profilePhotoUrl: String? = null,

    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes about the member (optional)", example = "Prefers morning workout sessions")
    val notes: String? = null
) {
    init {
        // Validate that if emergency contact name is provided, phone is also provided
        if (!emergencyContactName.isNullOrBlank() && emergencyContactPhone.isNullOrBlank()) {
            throw IllegalArgumentException("Emergency contact phone is required when emergency contact name is provided")
        }
    }
}

/**
 * Request DTO for updating member profile
 */
@Schema(description = "Request to update member profile")
data class UpdateMemberRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Member's full name (optional)", example = "Ahmed Mohammed Al-Rashid")
    val name: String? = null,

    @field:Size(max = 100, message = "Arabic name must not exceed 100 characters")
    @Schema(description = "Member's name in Arabic (optional)", example = "أحمد محمد الراشد")
    val nameArabic: String? = null,

    @field:Email(message = "Invalid email format")
    @Schema(description = "Member's email address (optional)", example = "ahmed.new@example.com")
    val email: String? = null,

    @field:Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Phone must be 10-15 digits, optionally starting with +"
    )
    @Schema(description = "Member's phone number (optional)", example = "+966501234567")
    val phone: String? = null,

    @field:SaudiNationalId
    @Schema(description = "Saudi National ID (optional)", example = "1234567890")
    val nationalId: String? = null,

    @Schema(description = "Member's gender (optional)", example = "MALE", allowableValues = ["MALE", "FEMALE"])
    val gender: String? = null,

    @field:Past(message = "Date of birth must be in the past")
    @Schema(description = "Member's date of birth (optional)", example = "1990-01-15")
    val dateOfBirth: LocalDate? = null,

    @field:Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    @Schema(description = "Emergency contact person's name (optional)", example = "Fatima Ahmed")
    val emergencyContactName: String? = null,

    @field:Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Emergency phone must be 10-15 digits, optionally starting with +"
    )
    @Schema(description = "Emergency contact person's phone (optional)", example = "+966507654321")
    val emergencyContactPhone: String? = null,

    @field:Size(max = 500, message = "Profile photo URL must not exceed 500 characters")
    @Schema(description = "URL to member's profile photo (optional)", example = "https://cdn.example.com/photos/member123.jpg")
    val profilePhotoUrl: String? = null,

    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes about the member (optional)", example = "Prefers evening workout sessions")
    val notes: String? = null
)

/**
 * Request DTO for suspending a member
 */
@Schema(description = "Request to suspend a member")
data class SuspendMemberRequest(
    @field:NotBlank(message = "Reason is required")
    @field:Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    @Schema(description = "Reason for suspension", example = "Violation of gym rules - repeated complaints")
    val reason: String,

    @Schema(description = "Whether to notify the member about suspension", example = "true")
    val notifyMember: Boolean = true
)

/**
 * Request DTO for searching members with pagination and filtering
 */
@Schema(description = "Request to search members with pagination and filtering")
data class SearchMembersRequest(
    @Schema(description = "Branch ID to filter by (optional)")
    val branchId: UUID? = null,

    @Schema(description = "Search query (searches name, email, phone)", example = "ahmed")
    val query: String? = null,

    @Schema(description = "Filter by member status (optional)", example = "ACTIVE", allowableValues = ["ACTIVE", "INACTIVE", "SUSPENDED", "PENDING_APPROVAL"])
    val status: String? = null,

    @Schema(description = "Filter by gender (optional)", example = "MALE", allowableValues = ["MALE", "FEMALE"])
    val gender: String? = null,

    @Schema(description = "Filter by minimum age (optional)", example = "18")
    @field:Min(value = 0, message = "Minimum age cannot be negative")
    val minAge: Int? = null,

    @Schema(description = "Filter by maximum age (optional)", example = "65")
    @field:Max(value = 150, message = "Maximum age cannot exceed 150")
    val maxAge: Int? = null,

    @Schema(description = "Filter members created after this date (optional)", example = "2025-01-01")
    val createdAfter: LocalDate? = null,

    @Schema(description = "Filter members created before this date (optional)", example = "2025-12-31")
    val createdBefore: LocalDate? = null,

    @Schema(description = "Page number (zero-based)", example = "0")
    @field:Min(value = 0, message = "Page number cannot be negative")
    val page: Int = 0,

    @Schema(description = "Page size", example = "20")
    @field:Min(value = 1, message = "Page size must be at least 1")
    @field:Max(value = 100, message = "Page size cannot exceed 100")
    val size: Int = 20,

    @Schema(description = "Sort field", example = "createdAt", allowableValues = ["name", "email", "createdAt", "status"])
    val sortBy: String = "createdAt",

    @Schema(description = "Sort direction", example = "DESC", allowableValues = ["ASC", "DESC"])
    val sortDirection: String = "DESC"
)

/**
 * Request DTO for deleting a member (GDPR compliant)
 */
@Schema(description = "Request to delete a member (GDPR compliant)")
data class DeleteMemberRequest(
    @Schema(description = "Whether to export member data before deletion", example = "true")
    val exportDataBeforeDeletion: Boolean = true,

    @field:NotBlank(message = "Reason is required for deletion")
    @field:Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    @Schema(description = "Reason for deletion", example = "Member requested account deletion (GDPR)")
    val reason: String
)
