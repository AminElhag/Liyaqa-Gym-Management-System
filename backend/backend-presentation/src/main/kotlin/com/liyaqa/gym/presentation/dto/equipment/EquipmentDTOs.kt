package com.liyaqa.gym.presentation.dto.equipment

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// ==================== REQUEST DTOs ====================

/**
 * Request to add new equipment
 */
@Schema(description = "Request to add new equipment to inventory")
data class AddEquipmentRequest(
    @field:NotNull(message = "Branch ID is required")
    @Schema(description = "Branch ID where equipment is located")
    val branchId: UUID,

    @field:NotBlank(message = "Equipment name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Equipment name", example = "Treadmill Pro 3000")
    val name: String,

    @field:NotBlank(message = "Category is required")
    @Schema(description = "Equipment category", example = "CARDIO", allowableValues = ["CARDIO", "STRENGTH", "FUNCTIONAL", "ACCESSORIES"])
    val category: String,

    @field:NotBlank(message = "Manufacturer is required")
    @field:Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    @Schema(description = "Manufacturer name", example = "TechnoGym")
    val manufacturer: String,

    @field:Size(max = 100, message = "Model must not exceed 100 characters")
    @Schema(description = "Model number/name", example = "Run Personal")
    val model: String? = null,

    @field:Size(max = 100, message = "Serial number must not exceed 100 characters")
    @Schema(description = "Serial number", example = "SN-12345-67890")
    val serialNumber: String? = null,

    @field:NotNull(message = "Purchase date is required")
    @field:PastOrPresent(message = "Purchase date cannot be in the future")
    @Schema(description = "Purchase date", example = "2025-01-15")
    val purchaseDate: LocalDate,

    @field:NotNull(message = "Purchase price is required")
    @field:DecimalMin(value = "0.0", message = "Purchase price must be positive")
    @Schema(description = "Purchase price in SAR", example = "15000.0")
    val purchasePrice: Double,

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Equipment location in facility", example = "Main Floor - Section A")
    val location: String? = null,

    @Schema(description = "Next scheduled maintenance date", example = "2025-04-15")
    @field:Future(message = "Next maintenance date must be in the future")
    val nextMaintenanceDate: LocalDate? = null,

    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Requires special cleaning solution")
    val notes: String? = null
)

/**
 * Request to update equipment
 */
@Schema(description = "Request to update equipment information")
data class UpdateEquipmentRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Equipment name", example = "Treadmill Pro 3000")
    val name: String? = null,

    @Schema(description = "Equipment category", example = "CARDIO")
    val category: String? = null,

    @field:Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    @Schema(description = "Manufacturer name", example = "TechnoGym")
    val manufacturer: String? = null,

    @field:Size(max = 100, message = "Model must not exceed 100 characters")
    @Schema(description = "Model number/name", example = "Run Personal")
    val model: String? = null,

    @field:Size(max = 100, message = "Serial number must not exceed 100 characters")
    @Schema(description = "Serial number", example = "SN-12345-67890")
    val serialNumber: String? = null,

    @field:PastOrPresent(message = "Purchase date cannot be in the future")
    @Schema(description = "Purchase date", example = "2025-01-15")
    val purchaseDate: LocalDate? = null,

    @field:DecimalMin(value = "0.0", message = "Purchase price must be positive")
    @Schema(description = "Purchase price in SAR", example = "15000.0")
    val purchasePrice: Double? = null,

    @Schema(description = "Equipment status", example = "AVAILABLE", allowableValues = ["AVAILABLE", "IN_USE", "MAINTENANCE", "OUT_OF_SERVICE"])
    val status: String? = null,

    @field:Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Equipment location in facility", example = "Main Floor - Section A")
    val location: String? = null,

    @field:PastOrPresent(message = "Last maintenance date cannot be in the future")
    @Schema(description = "Last maintenance date", example = "2025-10-15")
    val lastMaintenanceDate: LocalDate? = null,

    @Schema(description = "Next scheduled maintenance date", example = "2026-01-15")
    val nextMaintenanceDate: LocalDate? = null,

    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes")
    val notes: String? = null
)

/**
 * Request to report an equipment issue
 */
@Schema(description = "Request to report an equipment issue")
data class ReportIssueRequest(
    @field:NotBlank(message = "Description is required")
    @field:Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    @Schema(description = "Issue description", example = "Belt making unusual noise, speed fluctuates")
    val description: String,

    @field:NotBlank(message = "Severity is required")
    @Schema(description = "Issue severity", example = "MEDIUM", allowableValues = ["LOW", "MEDIUM", "HIGH", "CRITICAL"])
    val severity: String,

    @Schema(description = "Whether equipment is still usable", example = "false")
    val isUsable: Boolean = true,

    @Schema(description = "Photo URLs of the issue (optional)")
    val photoUrls: List<String>? = null
)

// ==================== RESPONSE DTOs ====================

/**
 * Equipment details response
 */
@Schema(description = "Equipment details")
data class EquipmentResponse(
    @Schema(description = "Equipment ID")
    val id: UUID,

    @Schema(description = "Branch ID")
    val branchId: UUID,

    @Schema(description = "Equipment name")
    val name: String,

    @Schema(description = "Category")
    val category: String,

    @Schema(description = "Manufacturer")
    val manufacturer: String,

    @Schema(description = "Model")
    val model: String?,

    @Schema(description = "Serial number")
    val serialNumber: String?,

    @Schema(description = "Purchase date")
    val purchaseDate: LocalDate,

    @Schema(description = "Purchase price in SAR")
    val purchasePrice: Double,

    @Schema(description = "Current status")
    val status: String,

    @Schema(description = "Location in facility")
    val location: String?,

    @Schema(description = "Last maintenance date")
    val lastMaintenanceDate: LocalDate?,

    @Schema(description = "Next maintenance date")
    val nextMaintenanceDate: LocalDate?,

    @Schema(description = "Maintenance history")
    val maintenanceHistory: List<MaintenanceRecord>,

    @Schema(description = "Notes")
    val notes: String?,

    @Schema(description = "Created at timestamp")
    val createdAt: Instant,

    @Schema(description = "Updated at timestamp")
    val updatedAt: Instant
)

/**
 * Equipment summary for list view
 */
@Schema(description = "Equipment summary")
data class EquipmentSummary(
    @Schema(description = "Equipment ID")
    val id: UUID,

    @Schema(description = "Equipment name")
    val name: String,

    @Schema(description = "Category")
    val category: String,

    @Schema(description = "Current status")
    val status: String,

    @Schema(description = "Location")
    val location: String?,

    @Schema(description = "Next maintenance date")
    val nextMaintenanceDate: LocalDate?,

    @Schema(description = "Days until next maintenance")
    val daysUntilMaintenance: Int?
)

/**
 * Paginated equipment list response
 */
@Schema(description = "Paginated equipment list")
data class EquipmentListResponse(
    @Schema(description = "Equipment items")
    val content: List<EquipmentSummary>,

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
 * Maintenance record
 */
@Schema(description = "Maintenance record")
data class MaintenanceRecord(
    @Schema(description = "Maintenance ID")
    val id: UUID,

    @Schema(description = "Maintenance date")
    val date: LocalDate,

    @Schema(description = "Type of maintenance")
    val type: String,

    @Schema(description = "Description of work performed")
    val description: String,

    @Schema(description = "Cost of maintenance in SAR")
    val cost: Double?,

    @Schema(description = "Performed by (staff name or vendor)")
    val performedBy: String
)

/**
 * Equipment issue response
 */
@Schema(description = "Equipment issue report response")
data class EquipmentIssueResponse(
    @Schema(description = "Issue ID")
    val issueId: UUID,

    @Schema(description = "Equipment ID")
    val equipmentId: UUID,

    @Schema(description = "Reported by user ID")
    val reportedBy: UUID,

    @Schema(description = "Severity level")
    val severity: String,

    @Schema(description = "Issue description")
    val description: String,

    @Schema(description = "Reported at timestamp")
    val reportedAt: Instant,

    @Schema(description = "Current status")
    val status: String,

    @Schema(description = "Response message")
    val message: String
)

/**
 * Maintenance schedule item
 */
@Schema(description = "Maintenance schedule item")
data class MaintenanceScheduleItem(
    @Schema(description = "Equipment ID")
    val equipmentId: UUID,

    @Schema(description = "Equipment name")
    val equipmentName: String,

    @Schema(description = "Category")
    val category: String,

    @Schema(description = "Location")
    val location: String?,

    @Schema(description = "Scheduled maintenance date")
    val scheduledDate: LocalDate,

    @Schema(description = "Days overdue (negative if not overdue)")
    val daysOverdue: Int,

    @Schema(description = "Last maintenance date")
    val lastMaintenanceDate: LocalDate?,

    @Schema(description = "Priority level")
    val priority: String
)

/**
 * Maintenance schedule response
 */
@Schema(description = "Maintenance schedule")
data class MaintenanceScheduleResponse(
    @Schema(description = "Overdue maintenance items")
    val overdue: List<MaintenanceScheduleItem>,

    @Schema(description = "Upcoming maintenance items")
    val upcoming: List<MaintenanceScheduleItem>,

    @Schema(description = "Total overdue count")
    val totalOverdue: Int,

    @Schema(description = "Total upcoming count")
    val totalUpcoming: Int
)
