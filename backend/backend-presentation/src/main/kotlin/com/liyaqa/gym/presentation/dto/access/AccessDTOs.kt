package com.liyaqa.gym.presentation.dto.access

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.Instant
import java.util.UUID

// ==================== REQUEST DTOs ====================

/**
 * Request to check in a member
 */
@Schema(description = "Request to check in a member to the facility")
data class CheckInRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @Schema(description = "Access method (CARD, QR_CODE, MANUAL)", example = "CARD")
    val accessMethod: String? = "MANUAL",

    @Schema(description = "Notes about check-in (optional)", example = "Guest accompanied")
    val notes: String? = null
)

/**
 * Request to check out a member
 */
@Schema(description = "Request to check out a member from the facility")
data class CheckOutRequest(
    @field:NotNull(message = "Member ID is required")
    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @Schema(description = "Notes about check-out (optional)", example = "Equipment returned")
    val notes: String? = null
)

/**
 * Request to create a guest pass
 */
@Schema(description = "Request to create a temporary guest pass")
data class GuestPassRequest(
    @field:NotBlank(message = "Guest name is required")
    @field:Size(min = 2, max = 100, message = "Guest name must be between 2 and 100 characters")
    @Schema(description = "Guest's full name", example = "Sarah Ahmed")
    val guestName: String,

    @field:NotBlank(message = "Guest phone is required")
    @field:Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Phone must be 10-15 digits, optionally starting with +"
    )
    @Schema(description = "Guest's phone number", example = "+966501234567")
    val guestPhone: String,

    @Schema(description = "Guest email (optional)", example = "sarah@example.com")
    @field:Email(message = "Invalid email format")
    val guestEmail: String? = null,

    @field:NotNull(message = "Valid until time is required")
    @field:Future(message = "Valid until must be in the future")
    @Schema(description = "When the guest pass expires", example = "2025-11-20T18:00:00Z")
    val validUntil: Instant,

    @Schema(description = "Sponsored by member ID (optional)", example = "123e4567-e89b-12d3-a456-426614174000")
    val sponsoredBy: UUID? = null,

    @field:Size(max = 500, message = "Purpose must not exceed 500 characters")
    @Schema(description = "Purpose of visit (optional)", example = "Trial session")
    val purpose: String? = null
)

/**
 * Request to revoke member access
 */
@Schema(description = "Request to revoke facility access for a member")
data class RevokeAccessRequest(
    @field:NotBlank(message = "Reason is required")
    @field:Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    @Schema(description = "Reason for revoking access", example = "Membership suspended due to payment issues")
    val reason: String,

    @Schema(description = "Whether to notify the member", example = "true")
    val notifyMember: Boolean = true,

    @Schema(description = "Revocation is temporary (can be reinstated)", example = "true")
    val temporary: Boolean = true
)

// ==================== RESPONSE DTOs ====================

/**
 * Response after checking in a member
 */
@Schema(description = "Response after member check-in")
data class CheckInResponse(
    @Schema(description = "Access log ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val accessLogId: UUID,

    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @Schema(description = "Member name", example = "Ahmed Mohammed")
    val memberName: String,

    @Schema(description = "Branch ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val branchId: UUID,

    @Schema(description = "Check-in timestamp")
    val checkInTime: Instant,

    @Schema(description = "Whether access was granted", example = "true")
    val accessGranted: Boolean,

    @Schema(description = "Response message", example = "Check-in successful")
    val message: String
)

/**
 * Response after checking out a member
 */
@Schema(description = "Response after member check-out")
data class CheckOutResponse(
    @Schema(description = "Access log ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val accessLogId: UUID,

    @Schema(description = "Member ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val memberId: UUID,

    @Schema(description = "Check-out timestamp")
    val checkOutTime: Instant,

    @Schema(description = "Duration of visit (formatted)", example = "2h 30m")
    val duration: String,

    @Schema(description = "Response message", example = "Check-out successful")
    val message: String
)

/**
 * Current facility occupancy information
 */
@Schema(description = "Current facility occupancy information")
data class CurrentOccupancyResponse(
    @Schema(description = "Current number of checked-in members", example = "45")
    val currentCount: Int,

    @Schema(description = "Facility capacity", example = "100")
    val capacity: Int,

    @Schema(description = "Occupancy percentage", example = "45.0")
    val occupancyPercentage: Double,

    @Schema(description = "List of currently checked-in members")
    val checkedInMembers: List<CheckedInMemberSummary>
)

/**
 * Summary of a checked-in member
 */
@Schema(description = "Summary of a checked-in member")
data class CheckedInMemberSummary(
    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member name")
    val memberName: String,

    @Schema(description = "Check-in time")
    val checkInTime: Instant,

    @Schema(description = "Duration so far (in minutes)")
    val durationMinutes: Int
)

/**
 * Access log entry
 */
@Schema(description = "Access log entry")
data class AccessLogEntry(
    @Schema(description = "Access log ID")
    val id: UUID,

    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member name")
    val memberName: String,

    @Schema(description = "Branch ID")
    val branchId: UUID,

    @Schema(description = "Check-in time")
    val checkInTime: Instant,

    @Schema(description = "Check-out time (null if still checked in)")
    val checkOutTime: Instant?,

    @Schema(description = "Duration in minutes (null if still checked in)")
    val durationMinutes: Int?,

    @Schema(description = "Access method")
    val accessMethod: String
)

/**
 * Paginated access logs response
 */
@Schema(description = "Paginated access logs")
data class PagedAccessLogsResponse(
    @Schema(description = "Access log entries")
    val content: List<AccessLogEntry>,

    @Schema(description = "Current page number")
    val page: Int,

    @Schema(description = "Page size")
    val size: Int,

    @Schema(description = "Total number of elements")
    val totalElements: Long,

    @Schema(description = "Total number of pages")
    val totalPages: Int,

    @Schema(description = "Whether there is a next page")
    val hasNext: Boolean,

    @Schema(description = "Whether there is a previous page")
    val hasPrevious: Boolean
)

/**
 * Guest pass response
 */
@Schema(description = "Guest pass information")
data class GuestPassResponse(
    @Schema(description = "Guest pass ID")
    val guestPassId: UUID,

    @Schema(description = "Guest name")
    val guestName: String,

    @Schema(description = "Access code for guest")
    val accessCode: String,

    @Schema(description = "Valid from timestamp")
    val validFrom: Instant,

    @Schema(description = "Valid until timestamp")
    val validUntil: Instant,

    @Schema(description = "Issued by staff/admin ID")
    val issuedBy: UUID,

    @Schema(description = "Response message")
    val message: String
)

/**
 * Response after revoking access
 */
@Schema(description = "Response after revoking member access")
data class RevokeAccessResponse(
    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Whether access was revoked")
    val accessRevoked: Boolean,

    @Schema(description = "Reason for revocation")
    val reason: String,

    @Schema(description = "Revocation timestamp")
    val revokedAt: Instant,

    @Schema(description = "Revoked by staff/admin ID")
    val revokedBy: UUID,

    @Schema(description = "Response message")
    val message: String
)
