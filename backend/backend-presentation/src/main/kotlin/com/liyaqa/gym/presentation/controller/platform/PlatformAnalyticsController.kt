package com.liyaqa.gym.presentation.controller.platform

import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.repositories.TenantSubscriptionRepository
import com.liyaqa.gym.presentation.dto.platform.MonthlyRevenue
import com.liyaqa.gym.presentation.dto.platform.PlatformDashboardMetrics
import com.liyaqa.gym.presentation.dto.platform.RevenueAnalytics
import com.liyaqa.gym.presentation.dto.platform.TenantGrowthData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.YearMonth

/**
 * Platform Analytics Controller
 * Provides analytics and metrics for platform administrators
 */
@RestController
@RequestMapping("/api/v1/platform/analytics")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@Tag(name = "Platform Analytics", description = "Platform analytics and metrics endpoints")
class PlatformAnalyticsController(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository
) {

    private val logger = LoggerFactory.getLogger(PlatformAnalyticsController::class.java)

    /**
     * Get platform dashboard metrics
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard metrics", description = "Get overall platform metrics for dashboard")
    fun getDashboardMetrics(): ResponseEntity<PlatformDashboardMetrics> {
        logger.info("Getting platform dashboard metrics")

        // Use large page size to get all tenants (in production, use proper aggregation queries)
        val pageable = PageRequest.of(0, 10000)

        val tenants = runBlocking {
            tenantRepository.findAll(pageable).getOrNull()?.content ?: emptyList()
        }

        val subscriptions = runBlocking {
            subscriptionRepository.findAll(pageable).getOrNull()?.content ?: emptyList()
        }

        val totalTenants = tenants.size
        val activeTenants = tenants.count { it.status == TenantStatus.ACTIVE }
        val trialTenants = tenants.count { it.status == TenantStatus.TRIAL }
        val suspendedTenants = tenants.count { it.status == TenantStatus.SUSPENDED }
        val cancelledTenants = tenants.count { it.status == TenantStatus.CANCELLED }

        // Calculate MRR (Monthly Recurring Revenue)
        val mrr = subscriptions
            .filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
            .sumOf { subscription ->
                when (subscription.billingCycle.name) {
                    "MONTHLY" -> subscription.amount.amount
                    "QUARTERLY" -> subscription.amount.amount / java.math.BigDecimal("3.0")
                    "ANNUAL" -> subscription.amount.amount / java.math.BigDecimal("12.0")
                    else -> java.math.BigDecimal.ZERO
                }
            }.toDouble()

        val arr = mrr * 12 // Annual Recurring Revenue

        // Calculate average revenue per tenant
        val arpt = if (activeTenants > 0) mrr / activeTenants else 0.0

        // Calculate new tenants this month
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1)
        val newTenantsThisMonth = tenants.count {
            it.createdAt.isAfter(startOfMonth.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
        }

        // Calculate new tenants this quarter
        val startOfQuarter = now.withDayOfMonth(1).minusMonths((now.monthValue - 1) % 3.toLong())
        val newTenantsThisQuarter = tenants.count {
            it.createdAt.isAfter(startOfQuarter.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
        }

        // Calculate churn rate (simplified)
        val churnRate = if (totalTenants > 0) {
            (cancelledTenants.toDouble() / totalTenants.toDouble()) * 100.0
        } else {
            0.0
        }

        val metrics = PlatformDashboardMetrics(
            totalTenants = totalTenants,
            activeTenants = activeTenants,
            trialTenants = trialTenants,
            suspendedTenants = suspendedTenants,
            cancelledTenants = cancelledTenants,
            monthlyRecurringRevenue = mrr,
            annualRecurringRevenue = arr,
            churnRate = churnRate,
            averageRevenuePerTenant = arpt,
            newTenantsThisMonth = newTenantsThisMonth,
            newTenantsThisQuarter = newTenantsThisQuarter
        )

        return ResponseEntity.ok(metrics)
    }

    /**
     * Get revenue analytics
     */
    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics", description = "Get revenue analytics for a date range")
    fun getRevenueAnalytics(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<RevenueAnalytics> {
        logger.info("Getting revenue analytics from $startDate to $endDate")

        val pageable = PageRequest.of(0, 10000)

        val subscriptions = runBlocking {
            subscriptionRepository.findAll(pageable).getOrNull()?.content ?: emptyList()
        }

        val tenants = runBlocking {
            tenantRepository.findAll(pageable).getOrNull()?.content ?: emptyList()
        }

        // Calculate total revenue
        val totalRevenue = subscriptions
            .filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
            .sumOf { it.amount.amount }
            .toDouble()

        // Revenue by plan
        val revenueByPlan = subscriptions
            .filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
            .groupBy { it.plan.displayName }
            .mapValues { (_, subs) -> subs.sumOf { it.amount.amount }.toDouble() }

        // Revenue by month (simplified - using creation dates)
        val monthlyRevenues = mutableListOf<MonthlyRevenue>()
        var currentMonth = YearMonth.from(startDate)
        val endMonth = YearMonth.from(endDate)

        while (!currentMonth.isAfter(endMonth)) {
            val monthStart = currentMonth.atDay(1)
            val monthEnd = currentMonth.atEndOfMonth()

            val monthSubscriptions = subscriptions.count {
                val subDate = LocalDate.ofInstant(it.createdAt, java.time.ZoneOffset.UTC)
                !subDate.isBefore(monthStart) && !subDate.isAfter(monthEnd)
            }

            val monthNewTenants = tenants.count {
                val tenantDate = LocalDate.ofInstant(it.createdAt, java.time.ZoneOffset.UTC)
                !tenantDate.isBefore(monthStart) && !tenantDate.isAfter(monthEnd)
            }

            val monthRevenue = subscriptions
                .filter {
                    val subDate = LocalDate.ofInstant(it.createdAt, java.time.ZoneOffset.UTC)
                    !subDate.isBefore(monthStart) && !subDate.isAfter(monthEnd)
                }
                .sumOf { it.amount.amount }
                .toDouble()

            monthlyRevenues.add(
                MonthlyRevenue(
                    month = currentMonth,
                    revenue = monthRevenue,
                    subscriptions = monthSubscriptions,
                    newTenants = monthNewTenants
                )
            )

            currentMonth = currentMonth.plusMonths(1)
        }

        val analytics = RevenueAnalytics(
            totalRevenue = totalRevenue,
            revenueByPlan = revenueByPlan,
            revenueByMonth = monthlyRevenues
        )

        return ResponseEntity.ok(analytics)
    }

    /**
     * Get tenant growth analytics
     */
    @GetMapping("/tenant-growth")
    @Operation(summary = "Get tenant growth", description = "Get tenant growth metrics by period")
    fun getTenantGrowth(
        @Parameter(description = "Period: month, quarter, or year")
        @RequestParam period: String
    ): ResponseEntity<List<TenantGrowthData>> {
        logger.info("Getting tenant growth analytics for period: $period")

        val pageable = PageRequest.of(0, 10000)

        val tenants = runBlocking {
            tenantRepository.findAll(pageable).getOrNull()?.content ?: emptyList()
        }

        val growthData = mutableListOf<TenantGrowthData>()

        // Calculate growth for the last 12 periods
        for (i in 11 downTo 0) {
            val periodStart: LocalDate
            val periodEnd: LocalDate
            val periodLabel: String

            when (period.lowercase()) {
                "month" -> {
                    periodStart = LocalDate.now().minusMonths(i.toLong()).withDayOfMonth(1)
                    periodEnd = periodStart.plusMonths(1).minusDays(1)
                    periodLabel = periodStart.toString().substring(0, 7) // YYYY-MM
                }
                "quarter" -> {
                    val quarterStart = LocalDate.now().minusMonths((i * 3).toLong())
                    periodStart = quarterStart.withDayOfMonth(1).minusMonths((quarterStart.monthValue - 1) % 3.toLong())
                    periodEnd = periodStart.plusMonths(3).minusDays(1)
                    periodLabel = "Q${(periodStart.monthValue - 1) / 3 + 1} ${periodStart.year}"
                }
                "year" -> {
                    periodStart = LocalDate.now().minusYears(i.toLong()).withDayOfYear(1)
                    periodEnd = periodStart.plusYears(1).minusDays(1)
                    periodLabel = periodStart.year.toString()
                }
                else -> throw IllegalArgumentException("Invalid period: $period. Must be 'month', 'quarter', or 'year'")
            }

            val newTenants = tenants.count {
                val tenantDate = LocalDate.ofInstant(it.createdAt, java.time.ZoneOffset.UTC)
                !tenantDate.isBefore(periodStart) && !tenantDate.isAfter(periodEnd)
            }

            val churnedTenants = tenants.count {
                it.status == TenantStatus.CANCELLED &&
                        it.updatedAt.let { updated ->
                            val updatedDate = LocalDate.ofInstant(updated, java.time.ZoneOffset.UTC)
                            !updatedDate.isBefore(periodStart) && !updatedDate.isAfter(periodEnd)
                        }
            }

            val totalAtEnd = tenants.count {
                val tenantDate = LocalDate.ofInstant(it.createdAt, java.time.ZoneOffset.UTC)
                !tenantDate.isAfter(periodEnd) && it.status != TenantStatus.CANCELLED
            }

            val netGrowth = newTenants - churnedTenants
            val growthRate = if (totalAtEnd > 0) {
                (netGrowth.toDouble() / totalAtEnd) * 100
            } else {
                0.0
            }

            growthData.add(
                TenantGrowthData(
                    period = periodLabel,
                    newTenants = newTenants,
                    churnedTenants = churnedTenants,
                    netGrowth = netGrowth,
                    totalTenants = totalAtEnd,
                    growthRate = growthRate
                )
            )
        }

        return ResponseEntity.ok(growthData)
    }
}
