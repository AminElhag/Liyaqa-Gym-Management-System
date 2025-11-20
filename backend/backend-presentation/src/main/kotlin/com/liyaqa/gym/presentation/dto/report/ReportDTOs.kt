package com.liyaqa.gym.presentation.dto.report

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// ==================== REQUEST DTOs ====================

/**
 * Request to generate a custom report
 */
@Schema(description = "Request to generate a custom report")
data class CustomReportRequest(
    @field:NotBlank(message = "Report name is required")
    @field:Size(min = 3, max = 100, message = "Report name must be between 3 and 100 characters")
    @Schema(description = "Report name", example = "Monthly Revenue by Branch")
    val reportName: String,

    @field:NotEmpty(message = "At least one metric is required")
    @Schema(description = "Metrics to include in report", example = "[\"revenue\", \"member_count\", \"class_attendance\"]")
    val metrics: List<String>,

    @field:NotEmpty(message = "At least one dimension is required")
    @Schema(description = "Dimensions to group by", example = "[\"branch\", \"date\", \"subscription_type\"]")
    val dimensions: List<String>,

    @field:NotNull(message = "Start date is required")
    @Schema(description = "Report start date", example = "2025-01-01")
    val startDate: LocalDate,

    @field:NotNull(message = "End date is required")
    @Schema(description = "Report end date", example = "2025-01-31")
    val endDate: LocalDate,

    @Schema(description = "Filters to apply")
    val filters: Map<String, Any>? = null,

    @field:NotBlank(message = "Format is required")
    @Schema(description = "Export format", example = "PDF", allowableValues = ["JSON", "PDF", "EXCEL", "CSV"])
    val format: String = "JSON",

    @Schema(description = "Sort field (optional)", example = "date")
    val sortBy: String? = null,

    @Schema(description = "Sort direction", example = "DESC", allowableValues = ["ASC", "DESC"])
    val sortDirection: String = "DESC"
)

// ==================== RESPONSE DTOs ====================

/**
 * VAT report response
 */
@Schema(description = "VAT (Value Added Tax) report")
data class VATReportResponse(
    @Schema(description = "Report start date")
    val startDate: LocalDate,

    @Schema(description = "Report end date")
    val endDate: LocalDate,

    @Schema(description = "Total revenue including VAT in SAR", example = "115000.00")
    val totalRevenue: Double,

    @Schema(description = "Total VAT amount in SAR (15%)", example = "15000.00")
    val totalVAT: Double,

    @Schema(description = "Net revenue excluding VAT in SAR", example = "100000.00")
    val netRevenue: Double,

    @Schema(description = "VAT rate applied", example = "0.15")
    val vatRate: Double,

    @Schema(description = "Detailed transactions")
    val transactions: List<VATTransaction>,

    @Schema(description = "VAT summary by category")
    val summary: VATSummary
)

/**
 * VAT transaction detail
 */
@Schema(description = "Individual VAT transaction")
data class VATTransaction(
    @Schema(description = "Transaction ID")
    val transactionId: UUID,

    @Schema(description = "Transaction date")
    val date: Instant,

    @Schema(description = "Customer/member name")
    val customerName: String,

    @Schema(description = "Description")
    val description: String,

    @Schema(description = "Net amount in SAR")
    val netAmount: Double,

    @Schema(description = "VAT amount in SAR")
    val vatAmount: Double,

    @Schema(description = "Gross amount in SAR")
    val grossAmount: Double,

    @Schema(description = "VAT category")
    val vatCategory: String,

    @Schema(description = "Invoice number")
    val invoiceNumber: String?
)

/**
 * VAT summary by category
 */
@Schema(description = "VAT summary breakdown")
data class VATSummary(
    @Schema(description = "Standard rated sales (15% VAT) in SAR", example = "100000.00")
    val standardRatedSales: Double,

    @Schema(description = "Zero-rated sales (0% VAT) in SAR", example = "0.00")
    val zeroRatedSales: Double,

    @Schema(description = "Exempt sales (no VAT) in SAR", example = "0.00")
    val exemptSales: Double,

    @Schema(description = "Total VAT collected in SAR", example = "15000.00")
    val totalVATCollected: Double
)

/**
 * Financial report response
 */
@Schema(description = "Comprehensive financial report")
data class FinancialReportResponse(
    @Schema(description = "Report start date")
    val startDate: LocalDate,

    @Schema(description = "Report end date")
    val endDate: LocalDate,

    @Schema(description = "Total revenue in SAR", example = "450000.00")
    val totalRevenue: Double,

    @Schema(description = "Total expenses in SAR", example = "180000.00")
    val totalExpenses: Double,

    @Schema(description = "Net profit in SAR", example = "270000.00")
    val netProfit: Double,

    @Schema(description = "Profit margin percentage", example = "60.0")
    val profitMargin: Double,

    @Schema(description = "Revenue breakdown by source")
    val revenueBySource: Map<String, Double>,

    @Schema(description = "Expenses breakdown by category")
    val expensesByCategory: Map<String, Double>,

    @Schema(description = "Top revenue streams")
    val topRevenueStreams: List<RevenueStream>,

    @Schema(description = "Payment method breakdown")
    val paymentMethodBreakdown: Map<String, Double>
)

/**
 * Revenue stream details
 */
@Schema(description = "Revenue stream")
data class RevenueStream(
    @Schema(description = "Stream name", example = "Monthly Subscriptions")
    val name: String,

    @Schema(description = "Amount in SAR", example = "125000.00")
    val amount: Double,

    @Schema(description = "Percentage of total", example = "35.5")
    val percentage: Double,

    @Schema(description = "Growth compared to previous period", example = "8.5")
    val growth: Double?
)

/**
 * Attendance report response
 */
@Schema(description = "Attendance report")
data class AttendanceReportResponse(
    @Schema(description = "Report start date")
    val startDate: LocalDate,

    @Schema(description = "Report end date")
    val endDate: LocalDate,

    @Schema(description = "Total check-ins", example = "4567")
    val totalCheckIns: Int,

    @Schema(description = "Unique members who attended", example = "876")
    val uniqueMembers: Int,

    @Schema(description = "Average check-ins per day", example = "147.3")
    val averageCheckInsPerDay: Double,

    @Schema(description = "Class attendance rate percentage", example = "85.5")
    val classAttendanceRate: Double,

    @Schema(description = "Top attending members")
    val topAttendingMembers: List<MemberAttendanceSummary>,

    @Schema(description = "Attendance by day of week")
    val attendanceByDayOfWeek: Map<String, Int>,

    @Schema(description = "Attendance by class")
    val attendanceByClass: List<ClassAttendanceSummary>
)

/**
 * Member attendance summary
 */
@Schema(description = "Member attendance summary")
data class MemberAttendanceSummary(
    @Schema(description = "Member ID")
    val memberId: UUID,

    @Schema(description = "Member name")
    val memberName: String,

    @Schema(description = "Check-in count", example = "45")
    val checkInCount: Int,

    @Schema(description = "Class attendance count", example = "23")
    val classAttendanceCount: Int,

    @Schema(description = "Average visits per week", example = "3.2")
    val averageVisitsPerWeek: Double
)

/**
 * Class attendance summary
 */
@Schema(description = "Class attendance summary")
data class ClassAttendanceSummary(
    @Schema(description = "Class ID")
    val classId: UUID,

    @Schema(description = "Class name")
    val className: String,

    @Schema(description = "Total sessions held", example = "30")
    val totalSessions: Int,

    @Schema(description = "Total attendance", example = "345")
    val totalAttendance: Int,

    @Schema(description = "Average attendance per session", example = "11.5")
    val averageAttendance: Double,

    @Schema(description = "Attendance rate percentage", example = "92.5")
    val attendanceRate: Double
)

/**
 * Membership report response
 */
@Schema(description = "Membership statistics report")
data class MembershipReportResponse(
    @Schema(description = "Report start date")
    val startDate: LocalDate,

    @Schema(description = "Report end date")
    val endDate: LocalDate,

    @Schema(description = "Total active members", example = "987")
    val totalActiveMembers: Int,

    @Schema(description = "New members in period", example = "87")
    val newMembers: Int,

    @Schema(description = "Cancelled members in period", example = "23")
    val cancelledMembers: Int,

    @Schema(description = "Retention rate percentage", example = "78.5")
    val retentionRate: Double,

    @Schema(description = "Membership breakdown by type")
    val membershipByType: Map<String, Int>,

    @Schema(description = "Member growth trend")
    val memberGrowthTrend: List<MemberGrowthPoint>,

    @Schema(description = "Average membership duration in months", example = "14")
    val averageMembershipDuration: Int
)

/**
 * Member growth data point
 */
@Schema(description = "Member growth at a specific time")
data class MemberGrowthPoint(
    @Schema(description = "Date")
    val date: LocalDate,

    @Schema(description = "Active members count")
    val activeMembersCount: Int,

    @Schema(description = "New members")
    val newMembers: Int,

    @Schema(description = "Cancelled members")
    val cancelledMembers: Int,

    @Schema(description = "Net growth")
    val netGrowth: Int
)

/**
 * Custom report response
 */
@Schema(description = "Custom report result")
data class CustomReportResponse(
    @Schema(description = "Report ID")
    val reportId: UUID,

    @Schema(description = "Report name")
    val reportName: String,

    @Schema(description = "Generated at date")
    val generatedAt: LocalDate,

    @Schema(description = "Report data (flexible structure)")
    val data: Map<String, Any>,

    @Schema(description = "Report metadata")
    val metadata: ReportMetadata
)

/**
 * Report metadata
 */
@Schema(description = "Report metadata")
data class ReportMetadata(
    @Schema(description = "Total rows in report", example = "150")
    val totalRows: Int,

    @Schema(description = "Columns included")
    val columns: List<String>,

    @Schema(description = "Filters applied")
    val filters: Map<String, Any>?
)
