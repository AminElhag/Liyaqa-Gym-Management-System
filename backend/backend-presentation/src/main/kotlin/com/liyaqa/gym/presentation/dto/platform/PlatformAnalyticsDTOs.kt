package com.liyaqa.gym.presentation.dto.platform

import java.time.LocalDate
import java.time.YearMonth

/**
 * Platform dashboard metrics overview
 */
data class PlatformDashboardMetrics(
    val totalTenants: Int,
    val activeTenants: Int,
    val trialTenants: Int,
    val suspendedTenants: Int,
    val cancelledTenants: Int,
    val monthlyRecurringRevenue: Double,
    val annualRecurringRevenue: Double,
    val churnRate: Double,
    val averageRevenuePerTenant: Double,
    val newTenantsThisMonth: Int,
    val newTenantsThisQuarter: Int
)

/**
 * Revenue analytics data
 */
data class RevenueAnalytics(
    val totalRevenue: Double,
    val revenueByPlan: Map<String, Double>,
    val revenueByMonth: List<MonthlyRevenue>,
    val currency: String = "SAR"
)

/**
 * Monthly revenue breakdown
 */
data class MonthlyRevenue(
    val month: YearMonth,
    val revenue: Double,
    val subscriptions: Int,
    val newTenants: Int
)

/**
 * Tenant growth analytics
 */
data class TenantGrowthData(
    val period: String,
    val newTenants: Int,
    val churnedTenants: Int,
    val netGrowth: Int,
    val totalTenants: Int,
    val growthRate: Double
)

/**
 * Plan distribution data
 */
data class PlanDistribution(
    val plan: String,
    val count: Int,
    val percentage: Double,
    val revenue: Double
)

/**
 * Usage metrics summary
 */
data class UsageMetricsSummary(
    val totalBranches: Int,
    val totalMembers: Int,
    val totalStaff: Int,
    val averageMembersPerTenant: Double,
    val averageBranchesPerTenant: Double
)
