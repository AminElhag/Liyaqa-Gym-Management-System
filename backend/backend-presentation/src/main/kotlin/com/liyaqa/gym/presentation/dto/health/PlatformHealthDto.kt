package com.liyaqa.gym.presentation.dto.health

import com.liyaqa.gym.domain.entities.health.*
import java.time.Instant
import java.util.UUID

/**
 * Platform health response DTO
 */
data class PlatformHealthResponse(
    val status: String,
    val timestamp: Instant,
    val components: Map<String, ComponentHealthResponse>,
    val tenants: TenantHealthSummaryResponse,
    val metrics: PlatformMetricsResponse
)

/**
 * Component health response
 */
data class ComponentHealthResponse(
    val status: String,
    val message: String? = null,
    val responseTimeMs: Long? = null,
    val details: Map<String, Any>? = null
)

/**
 * Tenant health summary response
 */
data class TenantHealthSummaryResponse(
    val total: Long,
    val active: Long,
    val trial: Long,
    val suspended: Long,
    val withIssues: Int,
    val issues: List<TenantIssueResponse>
)

/**
 * Tenant issue response
 */
data class TenantIssueResponse(
    val tenantId: UUID,
    val tenantName: String? = null,
    val severity: String,
    val type: String,
    val message: String,
    val occurredAt: Instant
)

/**
 * Platform metrics response
 */
data class PlatformMetricsResponse(
    val totalRevenueMRR: Double,
    val averageRevenuePerTenant: Double,
    val churnRate: Double,
    val growthRate: Double,
    val totalMembers: Long,
    val totalBookings: Long
)

/**
 * Tenant health response
 */
data class TenantHealthResponse(
    val status: String,
    val subscription: SubscriptionHealthResponse,
    val usage: UsageHealthResponse,
    val alerts: List<TenantAlertResponse>
)

/**
 * Subscription health response
 */
data class SubscriptionHealthResponse(
    val status: String,
    val nextBillingDate: String,
    val paymentMethodOnFile: Boolean,
    val daysUntilDue: Long
)

/**
 * Usage health response with individual metrics
 */
data class UsageHealthResponse(
    val members: UsageMetric,
    val branches: UsageMetric,
    val storage: UsageMetric
)

/**
 * Individual usage metric
 */
data class UsageMetric(
    val current: Long,
    val limit: Long,
    val percentage: Float
)

/**
 * Tenant alert response
 */
data class TenantAlertResponse(
    val severity: String,
    val type: String,
    val message: String,
    val timestamp: Instant
)

// Extension functions to convert domain models to DTOs
fun PlatformHealth.toResponse(): PlatformHealthResponse {
    return PlatformHealthResponse(
        status = status.name,
        timestamp = timestamp,
        components = components.mapValues { it.value.toResponse() },
        tenants = tenants.toResponse(),
        metrics = metrics.toResponse()
    )
}

fun ComponentHealth.toResponse(): ComponentHealthResponse {
    return ComponentHealthResponse(
        status = status.name,
        message = message,
        responseTimeMs = responseTimeMs,
        details = details
    )
}

fun TenantHealthSummary.toResponse(): TenantHealthSummaryResponse {
    return TenantHealthSummaryResponse(
        total = total,
        active = active,
        trial = trial,
        suspended = suspended,
        withIssues = withIssues,
        issues = issues.map { it.toResponse() }
    )
}

fun TenantIssue.toResponse(): TenantIssueResponse {
    return TenantIssueResponse(
        tenantId = tenantId,
        severity = severity.name,
        type = type.name,
        message = message,
        occurredAt = occurredAt
    )
}

fun PlatformMetrics.toResponse(): PlatformMetricsResponse {
    return PlatformMetricsResponse(
        totalRevenueMRR = totalRevenueMRR,
        averageRevenuePerTenant = averageRevenuePerTenant,
        churnRate = churnRate,
        growthRate = growthRate,
        totalMembers = totalMembers,
        totalBookings = totalBookings
    )
}

fun TenantHealth.toResponse(): TenantHealthResponse {
    return TenantHealthResponse(
        status = status.name,
        subscription = subscription.toResponse(),
        usage = usage.toResponse(),
        alerts = alerts.map { it.toResponse() }
    )
}

fun SubscriptionHealth.toResponse(): SubscriptionHealthResponse {
    return SubscriptionHealthResponse(
        status = status,
        nextBillingDate = nextBillingDate,
        paymentMethodOnFile = paymentMethodOnFile,
        daysUntilDue = daysUntilDue
    )
}

fun UsageHealth.toResponse(): UsageHealthResponse {
    return UsageHealthResponse(
        members = UsageMetric(
            current = memberCount,
            limit = memberLimit.toLong(),
            percentage = memberPercentage
        ),
        branches = UsageMetric(
            current = branchCount,
            limit = branchLimit.toLong(),
            percentage = branchPercentage
        ),
        storage = UsageMetric(
            current = storageMB,
            limit = storageLimit,
            percentage = storagePercentage
        )
    )
}

fun TenantAlert.toResponse(): TenantAlertResponse {
    return TenantAlertResponse(
        severity = severity.name,
        type = type.name,
        message = message,
        timestamp = timestamp
    )
}
