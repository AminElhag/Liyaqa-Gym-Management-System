package com.liyaqa.gym.domain.entities.health

import java.time.Instant
import java.util.UUID

/**
 * Platform health snapshot representing overall system health
 */
data class PlatformHealth(
    val status: HealthStatus,
    val timestamp: Instant,
    val components: Map<String, ComponentHealth>,
    val tenants: TenantHealthSummary,
    val metrics: PlatformMetrics
)

/**
 * Component health status (database, redis, kafka, etc.)
 */
data class ComponentHealth(
    val status: HealthStatus,
    val message: String? = null,
    val responseTimeMs: Long? = null,
    val details: Map<String, Any>? = null
)

/**
 * Summary of tenant health across the platform
 */
data class TenantHealthSummary(
    val total: Long,
    val active: Long,
    val trial: Long,
    val suspended: Long,
    val withIssues: Int,
    val issues: List<TenantIssue>
)

/**
 * Individual tenant health issue
 */
data class TenantIssue(
    val tenantId: UUID,
    val severity: Severity,
    val type: IssueType,
    val message: String,
    val occurredAt: Instant
)

/**
 * Platform-wide metrics for monitoring
 */
data class PlatformMetrics(
    val totalRevenueMRR: Double,
    val averageRevenuePerTenant: Double,
    val churnRate: Double,
    val growthRate: Double,
    val totalMembers: Long,
    val totalBookings: Long
)

/**
 * Detailed health information for a specific tenant
 */
data class TenantHealth(
    val tenantId: UUID,
    val status: HealthStatus,
    val subscription: SubscriptionHealth,
    val usage: UsageHealth,
    val alerts: List<TenantAlert>
)

/**
 * Subscription health for a tenant
 */
data class SubscriptionHealth(
    val status: String,
    val nextBillingDate: String,
    val paymentMethodOnFile: Boolean,
    val daysUntilDue: Long,
    val paymentFailureCount: Int = 0
)

/**
 * Usage metrics for a tenant
 */
data class UsageHealth(
    val memberCount: Long,
    val memberLimit: Int,
    val memberPercentage: Float,
    val branchCount: Long,
    val branchLimit: Int,
    val branchPercentage: Float,
    val storageMB: Long,
    val storageLimit: Long,
    val storagePercentage: Float
)

/**
 * Alert for a specific tenant
 */
data class TenantAlert(
    val severity: Severity,
    val type: IssueType,
    val message: String,
    val timestamp: Instant
)

/**
 * Health status enumeration
 */
enum class HealthStatus {
    HEALTHY,
    DEGRADED,
    UNHEALTHY
}

/**
 * Issue severity levels
 */
enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Types of tenant issues
 */
enum class IssueType {
    PAYMENT_FAILURE,
    TRIAL_EXPIRING,
    APPROACHING_LIMIT,
    OVER_LIMIT,
    SUBSCRIPTION_EXPIRED,
    NO_PAYMENT_METHOD,
    PAYMENT_PAST_DUE
}
