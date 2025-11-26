package com.liyaqa.gym.domain.entities.tenant

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.time.YearMonth
import java.util.UUID

/**
 * TenantUsageMetrics entity for tracking usage and billing metrics per tenant.
 * Collected monthly for usage-based billing and analytics.
 */
data class TenantUsageMetrics(
    val id: UUID,
    val tenantId: UUID,
    val period: YearMonth,
    val totalMembers: Int,
    val activeMembers: Int,
    val totalBranches: Int,
    val totalStaff: Int,
    val totalBookings: Int,
    val totalRevenue: Money,
    val storageUsedMB: Long,
    val apiCallsCount: Long,
    val smsMessagesSent: Int,
    val emailsSent: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(totalMembers >= 0) { "Total members cannot be negative" }
        require(activeMembers >= 0) { "Active members cannot be negative" }
        require(activeMembers <= totalMembers) { "Active members cannot exceed total members" }
        require(totalBranches >= 0) { "Total branches cannot be negative" }
        require(totalStaff >= 0) { "Total staff cannot be negative" }
        require(totalBookings >= 0) { "Total bookings cannot be negative" }
        require(!totalRevenue.isNegative()) { "Total revenue cannot be negative" }
        require(storageUsedMB >= 0) { "Storage used cannot be negative" }
        require(apiCallsCount >= 0) { "API calls count cannot be negative" }
        require(smsMessagesSent >= 0) { "SMS messages sent cannot be negative" }
        require(emailsSent >= 0) { "Emails sent cannot be negative" }
    }

    fun memberUtilizationRate(): Double {
        return if (totalMembers > 0) {
            (activeMembers.toDouble() / totalMembers.toDouble()) * 100
        } else {
            0.0
        }
    }

    fun averageRevenuePerMember(): Money {
        return if (activeMembers > 0) {
            totalRevenue / activeMembers.toBigDecimal()
        } else {
            Money.zero(totalRevenue.currency.currencyCode)
        }
    }

    fun storageUsedGB(): Double {
        return storageUsedMB / 1024.0
    }

    fun updateMetrics(
        totalMembers: Int? = null,
        activeMembers: Int? = null,
        totalBranches: Int? = null,
        totalStaff: Int? = null,
        totalBookings: Int? = null,
        totalRevenue: Money? = null,
        storageUsedMB: Long? = null,
        apiCallsCount: Long? = null,
        smsMessagesSent: Int? = null,
        emailsSent: Int? = null
    ): TenantUsageMetrics {
        return copy(
            totalMembers = totalMembers ?: this.totalMembers,
            activeMembers = activeMembers ?: this.activeMembers,
            totalBranches = totalBranches ?: this.totalBranches,
            totalStaff = totalStaff ?: this.totalStaff,
            totalBookings = totalBookings ?: this.totalBookings,
            totalRevenue = totalRevenue ?: this.totalRevenue,
            storageUsedMB = storageUsedMB ?: this.storageUsedMB,
            apiCallsCount = apiCallsCount ?: this.apiCallsCount,
            smsMessagesSent = smsMessagesSent ?: this.smsMessagesSent,
            emailsSent = emailsSent ?: this.emailsSent,
            updatedAt = Instant.now()
        )
    }

    fun incrementBookings(count: Int = 1): TenantUsageMetrics {
        require(count > 0) { "Count must be positive" }
        return copy(
            totalBookings = totalBookings + count,
            updatedAt = Instant.now()
        )
    }

    fun incrementApiCalls(count: Long = 1): TenantUsageMetrics {
        require(count > 0) { "Count must be positive" }
        return copy(
            apiCallsCount = apiCallsCount + count,
            updatedAt = Instant.now()
        )
    }

    fun incrementSms(count: Int = 1): TenantUsageMetrics {
        require(count > 0) { "Count must be positive" }
        return copy(
            smsMessagesSent = smsMessagesSent + count,
            updatedAt = Instant.now()
        )
    }

    fun incrementEmails(count: Int = 1): TenantUsageMetrics {
        require(count > 0) { "Count must be positive" }
        return copy(
            emailsSent = emailsSent + count,
            updatedAt = Instant.now()
        )
    }

    fun addRevenue(amount: Money): TenantUsageMetrics {
        require(amount.isPositive()) { "Revenue amount must be positive" }
        require(amount.currency == totalRevenue.currency) {
            "Revenue currency must match existing currency"
        }
        return copy(
            totalRevenue = totalRevenue + amount,
            updatedAt = Instant.now()
        )
    }

    fun updateStorage(newStorageMB: Long): TenantUsageMetrics {
        require(newStorageMB >= 0) { "Storage cannot be negative" }
        return copy(
            storageUsedMB = newStorageMB,
            updatedAt = Instant.now()
        )
    }

    companion object {
        fun create(
            tenantId: UUID,
            period: YearMonth
        ): TenantUsageMetrics {
            val now = Instant.now()
            return TenantUsageMetrics(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                period = period,
                totalMembers = 0,
                activeMembers = 0,
                totalBranches = 0,
                totalStaff = 0,
                totalBookings = 0,
                totalRevenue = Money.zero("SAR"),
                storageUsedMB = 0,
                apiCallsCount = 0,
                smsMessagesSent = 0,
                emailsSent = 0,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createForCurrentMonth(tenantId: UUID): TenantUsageMetrics {
            return create(tenantId, YearMonth.now())
        }

        fun createWithInitialData(
            tenantId: UUID,
            period: YearMonth,
            totalMembers: Int,
            activeMembers: Int,
            totalBranches: Int,
            totalStaff: Int,
            totalRevenue: Money
        ): TenantUsageMetrics {
            val now = Instant.now()
            return TenantUsageMetrics(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                period = period,
                totalMembers = totalMembers,
                activeMembers = activeMembers,
                totalBranches = totalBranches,
                totalStaff = totalStaff,
                totalBookings = 0,
                totalRevenue = totalRevenue,
                storageUsedMB = 0,
                apiCallsCount = 0,
                smsMessagesSent = 0,
                emailsSent = 0,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Summary statistics for tenant usage over a period
 */
data class TenantUsageSummary(
    val tenantId: UUID,
    val startPeriod: YearMonth,
    val endPeriod: YearMonth,
    val averageMembers: Double,
    val averageActiveMembers: Double,
    val peakMembers: Int,
    val totalBookings: Int,
    val totalRevenue: Money,
    val totalStorageGB: Double,
    val totalApiCalls: Long,
    val totalSmsSent: Int,
    val totalEmailsSent: Int,
    val growthRate: Double // Percentage growth in members
) {
    companion object {
        fun fromMetrics(metrics: List<TenantUsageMetrics>): TenantUsageSummary {
            require(metrics.isNotEmpty()) { "Metrics list cannot be empty" }
            require(metrics.map { it.tenantId }.distinct().size == 1) {
                "All metrics must belong to the same tenant"
            }

            val tenantId = metrics.first().tenantId
            val sortedMetrics = metrics.sortedBy { it.period }
            val startPeriod = sortedMetrics.first().period
            val endPeriod = sortedMetrics.last().period

            val averageMembers = metrics.map { it.totalMembers }.average()
            val averageActiveMembers = metrics.map { it.activeMembers }.average()
            val peakMembers = metrics.maxOf { it.totalMembers }
            val totalBookings = metrics.sumOf { it.totalBookings }
            val totalRevenue = metrics.fold(Money.zero("SAR")) { acc, m -> acc + m.totalRevenue }
            val totalStorageGB = metrics.sumOf { it.storageUsedMB } / 1024.0
            val totalApiCalls = metrics.sumOf { it.apiCallsCount }
            val totalSmsSent = metrics.sumOf { it.smsMessagesSent }
            val totalEmailsSent = metrics.sumOf { it.emailsSent }

            // Calculate growth rate
            val firstMembers = sortedMetrics.first().totalMembers
            val lastMembers = sortedMetrics.last().totalMembers
            val growthRate = if (firstMembers > 0) {
                ((lastMembers - firstMembers).toDouble() / firstMembers.toDouble()) * 100
            } else {
                0.0
            }

            return TenantUsageSummary(
                tenantId = tenantId,
                startPeriod = startPeriod,
                endPeriod = endPeriod,
                averageMembers = averageMembers,
                averageActiveMembers = averageActiveMembers,
                peakMembers = peakMembers,
                totalBookings = totalBookings,
                totalRevenue = totalRevenue,
                totalStorageGB = totalStorageGB,
                totalApiCalls = totalApiCalls,
                totalSmsSent = totalSmsSent,
                totalEmailsSent = totalEmailsSent,
                growthRate = growthRate
            )
        }
    }
}
