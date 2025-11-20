package com.liyaqa.gym.presentation.dto.analytics

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// ==================== RESPONSE DTOs ====================

/**
 * Dashboard KPIs response
 */
@Schema(description = "Dashboard key performance indicators")
data class DashboardKPIsResponse(
    @Schema(description = "Date for which KPIs are calculated")
    val date: LocalDate,

    @Schema(description = "Total members count", example = "1250")
    val totalMembers: Int,

    @Schema(description = "Active members count", example = "987")
    val activeMembers: Int,

    @Schema(description = "New members today", example = "5")
    val newMembersToday: Int,

    @Schema(description = "Check-ins today", example = "234")
    val todayCheckIns: Int,

    @Schema(description = "Current facility occupancy", example = "45")
    val currentOccupancy: Int,

    @Schema(description = "Revenue today in SAR", example = "12500.50")
    val todayRevenue: Double,

    @Schema(description = "Revenue this month in SAR", example = "245000.00")
    val monthRevenue: Double,

    @Schema(description = "Active subscriptions count", example = "876")
    val activeSubscriptions: Int,

    @Schema(description = "Subscriptions expiring within 7 days", example = "23")
    val expiringSubscriptions: Int,

    @Schema(description = "Class bookings today", example = "67")
    val todayClassBookings: Int,

    @Schema(description = "Pending payments count", example = "12")
    val pendingPayments: Int,

    @Schema(description = "Trend data compared to previous period")
    val trends: TrendsData
)

/**
 * Trends data for dashboard
 */
@Schema(description = "Trend indicators")
data class TrendsData(
    @Schema(description = "Member growth percentage", example = "5.2")
    val memberGrowth: Double,

    @Schema(description = "Revenue growth percentage", example = "8.7")
    val revenueGrowth: Double,

    @Schema(description = "Check-in growth percentage", example = "3.4")
    val checkInGrowth: Double
)

/**
 * Revenue analytics response
 */
@Schema(description = "Revenue analytics with detailed breakdown")
data class RevenueAnalyticsResponse(
    @Schema(description = "Analysis start date")
    val startDate: LocalDate,

    @Schema(description = "Analysis end date")
    val endDate: LocalDate,

    @Schema(description = "Total revenue in SAR", example = "450000.00")
    val totalRevenue: Double,

    @Schema(description = "Average transaction value in SAR", example = "750.50")
    val averageTransactionValue: Double,

    @Schema(description = "Number of transactions", example = "600")
    val transactionCount: Int,

    @Schema(description = "Revenue breakdown by subscription type")
    val revenueBySubscriptionType: Map<String, Double>,

    @Schema(description = "Revenue breakdown by payment method")
    val revenueByPaymentMethod: Map<String, Double>,

    @Schema(description = "Time series revenue data")
    val timeSeries: List<RevenueTimeSeriesPoint>,

    @Schema(description = "Top revenue-generating categories")
    val topRevenueCategories: List<RevenueCategory>
)

/**
 * Revenue time series data point
 */
@Schema(description = "Revenue at a specific time point")
data class RevenueTimeSeriesPoint(
    @Schema(description = "Date/time")
    val timestamp: LocalDate,

    @Schema(description = "Revenue amount in SAR")
    val amount: Double,

    @Schema(description = "Transaction count")
    val transactionCount: Int
)

/**
 * Revenue category summary
 */
@Schema(description = "Revenue category")
data class RevenueCategory(
    @Schema(description = "Category name")
    val name: String,

    @Schema(description = "Revenue amount in SAR")
    val amount: Double,

    @Schema(description = "Percentage of total revenue")
    val percentage: Double
)

/**
 * Retention analytics response
 */
@Schema(description = "Member retention analytics")
data class RetentionAnalyticsResponse(
    @Schema(description = "Analysis start date")
    val startDate: LocalDate,

    @Schema(description = "Analysis end date")
    val endDate: LocalDate,

    @Schema(description = "Overall retention rate percentage", example = "78.5")
    val overallRetentionRate: Double,

    @Schema(description = "Retention rate by month")
    val retentionByMonth: List<RetentionDataPoint>,

    @Schema(description = "Cohort analysis data")
    val cohortAnalysis: List<CohortData>,

    @Schema(description = "Average member lifetime in months", example = "14")
    val averageMemberLifetime: Int,

    @Schema(description = "Number of at-risk members", example = "45")
    val atRiskMembers: Int
)

/**
 * Retention data point for a specific month
 */
@Schema(description = "Retention rate at a specific month")
data class RetentionDataPoint(
    @Schema(description = "Month")
    val month: LocalDate,

    @Schema(description = "Retention rate percentage")
    val retentionRate: Double,

    @Schema(description = "Members retained count")
    val membersRetained: Int,

    @Schema(description = "Total members at start")
    val totalMembers: Int
)

/**
 * Cohort analysis data
 */
@Schema(description = "Cohort retention data")
data class CohortData(
    @Schema(description = "Cohort start month")
    val cohortMonth: LocalDate,

    @Schema(description = "Initial cohort size")
    val initialSize: Int,

    @Schema(description = "Retention rates by month since joining")
    val retentionByMonth: Map<Int, Double>
)

/**
 * Churn analytics response
 */
@Schema(description = "Member churn analytics")
data class ChurnAnalyticsResponse(
    @Schema(description = "Analysis start date")
    val startDate: LocalDate,

    @Schema(description = "Analysis end date")
    val endDate: LocalDate,

    @Schema(description = "Churn rate percentage", example = "12.5")
    val churnRate: Double,

    @Schema(description = "Number of churned members", example = "87")
    val churnedMembers: Int,

    @Schema(description = "Churn reasons breakdown")
    val churnReasons: Map<String, Int>,

    @Schema(description = "Churn by subscription type")
    val churnBySubscriptionType: Map<String, Int>,

    @Schema(description = "Lost revenue in SAR", example = "65000.00")
    val lostRevenue: Double,

    @Schema(description = "Churn trend over time")
    val churnTrend: List<ChurnDataPoint>
)

/**
 * Churn data point
 */
@Schema(description = "Churn data at a specific time")
data class ChurnDataPoint(
    @Schema(description = "Month")
    val month: LocalDate,

    @Schema(description = "Churn rate percentage")
    val churnRate: Double,

    @Schema(description = "Churned members count")
    val churnedCount: Int
)

/**
 * Class popularity analytics response
 */
@Schema(description = "Class popularity analytics")
data class ClassPopularityResponse(
    @Schema(description = "Analysis start date")
    val startDate: LocalDate,

    @Schema(description = "Analysis end date")
    val endDate: LocalDate,

    @Schema(description = "Most popular classes")
    val mostPopularClasses: List<ClassPopularityData>,

    @Schema(description = "Least popular classes")
    val leastPopularClasses: List<ClassPopularityData>,

    @Schema(description = "Peak booking times")
    val peakTimes: List<PeakTimeData>,

    @Schema(description = "Average attendance rate percentage", example = "85.5")
    val averageAttendanceRate: Double
)

/**
 * Class popularity data
 */
@Schema(description = "Class popularity metrics")
data class ClassPopularityData(
    @Schema(description = "Class ID")
    val classId: UUID,

    @Schema(description = "Class name")
    val className: String,

    @Schema(description = "Total bookings", example = "245")
    val totalBookings: Int,

    @Schema(description = "Average attendance rate percentage", example = "92.3")
    val attendanceRate: Double,

    @Schema(description = "Waitlist frequency percentage", example = "45.0")
    val waitlistFrequency: Double,

    @Schema(description = "Average rating", example = "4.7")
    val averageRating: Double?
)

/**
 * Peak time data
 */
@Schema(description = "Peak booking time")
data class PeakTimeData(
    @Schema(description = "Day of week (1-7, 1=Monday)")
    val dayOfWeek: Int,

    @Schema(description = "Hour of day (0-23)")
    val hour: Int,

    @Schema(description = "Booking count")
    val bookingCount: Int,

    @Schema(description = "Time period label", example = "Monday 6-7 PM")
    val label: String
)

/**
 * Facility utilization response
 */
@Schema(description = "Facility utilization analytics")
data class FacilityUtilizationResponse(
    @Schema(description = "Analysis start date")
    val startDate: LocalDate,

    @Schema(description = "Analysis end date")
    val endDate: LocalDate,

    @Schema(description = "Average occupancy percentage", example = "65.5")
    val averageOccupancy: Double,

    @Schema(description = "Peak occupancy count", example = "95")
    val peakOccupancy: Int,

    @Schema(description = "Peak occupancy time")
    val peakTime: LocalDateTime?,

    @Schema(description = "Average occupancy by hour of day")
    val utilizationByHour: Map<Int, Double>,

    @Schema(description = "Average occupancy by day of week")
    val utilizationByDayOfWeek: Map<String, Double>,

    @Schema(description = "Average visit duration in minutes", example = "78")
    val averageVisitDuration: Int,

    @Schema(description = "Total visits", example = "4567")
    val totalVisits: Int
)

/**
 * Trainer performance response
 */
@Schema(description = "Trainer performance metrics")
data class TrainerPerformanceResponse(
    @Schema(description = "Analysis start date")
    val startDate: LocalDate,

    @Schema(description = "Analysis end date")
    val endDate: LocalDate,

    @Schema(description = "Performance metrics for each trainer")
    val trainerMetrics: List<TrainerMetrics>,

    @Schema(description = "Top performing trainers")
    val topPerformers: List<TrainerMetrics>,

    @Schema(description = "Average rating across all trainers", example = "4.5")
    val averageRating: Double,

    @Schema(description = "Total classes taught", example = "567")
    val totalClassesTaught: Int
)

/**
 * Individual trainer metrics
 */
@Schema(description = "Trainer performance metrics")
data class TrainerMetrics(
    @Schema(description = "Trainer ID")
    val trainerId: UUID,

    @Schema(description = "Trainer name")
    val trainerName: String,

    @Schema(description = "Classes taught count", example = "45")
    val classesTaught: Int,

    @Schema(description = "Total students taught", example = "567")
    val totalStudents: Int,

    @Schema(description = "Average attendance per class", example = "12.5")
    val averageAttendance: Double,

    @Schema(description = "Average class rating", example = "4.8")
    val averageRating: Double,

    @Schema(description = "Booking rate percentage", example = "95.5")
    val bookingRate: Double,

    @Schema(description = "Member retention rate percentage", example = "88.0")
    val retentionRate: Double
)
