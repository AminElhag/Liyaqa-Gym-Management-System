package com.liyaqa.gym.application.platform

import com.liyaqa.gym.domain.entities.health.*
import com.liyaqa.gym.domain.entities.tenant.TenantStatus
import com.liyaqa.gym.domain.repositories.*
import com.liyaqa.infrastructure.health.DatabaseHealthChecker
import com.liyaqa.infrastructure.health.KafkaHealthChecker
import com.liyaqa.infrastructure.health.RedisHealthChecker
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Platform Health Service
 * Monitors overall platform health including infrastructure components and tenant health
 */
@Service
class PlatformHealthService(
    private val tenantRepository: TenantRepository,
    private val subscriptionRepository: TenantSubscriptionRepository,
    private val memberRepository: MemberRepository,
    private val bookingRepository: BookingRepository,
    private val branchRepository: BranchRepository,
    private val databaseHealthChecker: DatabaseHealthChecker,
    private val redisHealthChecker: RedisHealthChecker,
    private val kafkaHealthChecker: KafkaHealthChecker
) {

    private val logger = LoggerFactory.getLogger(PlatformHealthService::class.java)

    /**
     * Get overall platform health
     */
    suspend fun getPlatformHealth(): PlatformHealth = coroutineScope {
        logger.info("Performing platform health check")

        // Run all component health checks in parallel
        val dbHealthDeferred = async { databaseHealthChecker.check() }
        val redisHealthDeferred = async { redisHealthChecker.check() }
        val kafkaHealthDeferred = async { kafkaHealthChecker.check() }

        val components = mapOf(
            "database" to dbHealthDeferred.await(),
            "redis" to redisHealthDeferred.await(),
            "kafka" to kafkaHealthDeferred.await()
        )

        val tenants = getTenantHealthSummary()
        val metrics = getPlatformMetrics()

        PlatformHealth(
            status = calculateOverallStatus(components, tenants),
            timestamp = Instant.now(),
            components = components,
            tenants = tenants,
            metrics = metrics
        )
    }

    /**
     * Get health for a specific tenant
     */
    suspend fun getTenantHealth(tenantId: UUID): TenantHealth {
        logger.info("Getting health for tenant: $tenantId")

        val tenant = tenantRepository.findById(tenantId).getOrNull()?.orElse(null)
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")

        val subscription = subscriptionRepository.findActiveByTenant(tenantId).getOrNull()?.orElse(null)

        val memberCount = memberRepository.countByBranchAndStatus(null, null).getOrNull() ?: 0L
        val branchCount = branchRepository.countByOrganization(tenantId).getOrNull() ?: 0L

        val subscriptionHealth = if (subscription != null) {
            SubscriptionHealth(
                status = subscription.status.name,
                nextBillingDate = subscription.nextBillingDate.toString(),
                paymentMethodOnFile = subscription.paymentMethod != null,
                daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), subscription.nextBillingDate),
                paymentFailureCount = subscription.paymentFailureCount
            )
        } else {
            SubscriptionHealth(
                status = "NONE",
                nextBillingDate = "",
                paymentMethodOnFile = false,
                daysUntilDue = 0,
                paymentFailureCount = 0
            )
        }

        val usageHealth = UsageHealth(
            memberCount = memberCount,
            memberLimit = tenant.maxMembers,
            memberPercentage = memberCount.toFloat() / tenant.maxMembers,
            branchCount = branchCount,
            branchLimit = tenant.maxBranches,
            branchPercentage = branchCount.toFloat() / tenant.maxBranches,
            storageMB = 0L, // TODO: Implement storage tracking
            storageLimit = 1000L, // Default 1GB
            storagePercentage = 0.0f
        )

        val alerts = generateTenantAlerts(tenant, subscription, usageHealth)

        return TenantHealth(
            tenantId = tenantId,
            status = calculateTenantHealthStatus(alerts),
            subscription = subscriptionHealth,
            usage = usageHealth,
            alerts = alerts
        )
    }

    /**
     * Get tenant issues filtered by severity and type
     */
    suspend fun getTenantIssues(severity: Severity?, type: IssueType?): List<TenantIssue> {
        val allIssues = findTenantsWithIssues()

        return allIssues.filter { issue ->
            (severity == null || issue.severity == severity) &&
            (type == null || issue.type == type)
        }
    }

    private suspend fun getTenantHealthSummary(): TenantHealthSummary {
        logger.debug("Getting tenant health summary")

        val totalTenants = tenantRepository.count().getOrNull() ?: 0L
        val activeTenants = tenantRepository.countByStatus(TenantStatus.ACTIVE).getOrNull() ?: 0L
        val trialTenants = tenantRepository.countByStatus(TenantStatus.TRIAL).getOrNull() ?: 0L
        val suspendedTenants = tenantRepository.countByStatus(TenantStatus.SUSPENDED).getOrNull() ?: 0L

        val tenantsWithIssues = findTenantsWithIssues()

        return TenantHealthSummary(
            total = totalTenants,
            active = activeTenants,
            trial = trialTenants,
            suspended = suspendedTenants,
            withIssues = tenantsWithIssues.size,
            issues = tenantsWithIssues
        )
    }

    private suspend fun findTenantsWithIssues(): List<TenantIssue> {
        logger.debug("Finding tenants with issues")

        val issues = mutableListOf<TenantIssue>()

        // Check for payment failures
        val paymentFailures = subscriptionRepository.findWithPaymentFailures().getOrNull() ?: emptyList()
        issues.addAll(paymentFailures.map {
            TenantIssue(
                tenantId = it.tenantId,
                severity = if (it.paymentFailureCount >= 3) Severity.CRITICAL else Severity.HIGH,
                type = IssueType.PAYMENT_FAILURE,
                message = "Payment failed ${it.paymentFailureCount} times",
                occurredAt = it.lastPaymentFailureAt ?: Instant.now()
            )
        })

        // Check for expiring trials
        val expiringTrials = subscriptionRepository.findTrialsExpiringInDays(3).getOrNull() ?: emptyList()
        issues.addAll(expiringTrials.map {
            val daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), it.trialEndsAt)
            TenantIssue(
                tenantId = it.tenantId,
                severity = if (daysUntilExpiry <= 1) Severity.HIGH else Severity.MEDIUM,
                type = IssueType.TRIAL_EXPIRING,
                message = "Trial expires in $daysUntilExpiry days",
                occurredAt = Instant.now()
            )
        })

        // Check for near-limit tenants
        val nearLimitTenants = findTenantsNearLimit()
        issues.addAll(nearLimitTenants)

        return issues
    }

    private suspend fun findTenantsNearLimit(): List<TenantIssue> {
        logger.debug("Finding tenants near limits")

        val issues = mutableListOf<TenantIssue>()

        val pageable = PageRequest.of(0, 10000)
        val tenants = tenantRepository.findAll(pageable).getOrNull()?.content ?: emptyList()

        tenants.forEach { tenant ->
            val memberCount = memberRepository.countByBranchAndStatus(null, null).getOrNull() ?: 0L
            val memberUsage = memberCount.toFloat() / tenant.maxMembers

            if (memberUsage >= 0.9) {
                issues.add(
                    TenantIssue(
                        tenantId = tenant.id,
                        severity = if (memberUsage >= 0.95) Severity.HIGH else Severity.MEDIUM,
                        type = if (memberUsage >= 1.0) IssueType.OVER_LIMIT else IssueType.APPROACHING_LIMIT,
                        message = "Using ${(memberUsage * 100).toInt()}% of member limit (${memberCount}/${tenant.maxMembers})",
                        occurredAt = Instant.now()
                    )
                )
            }
        }

        return issues
    }

    private suspend fun getPlatformMetrics(): PlatformMetrics {
        logger.debug("Calculating platform metrics")

        val mrr = calculateMRR()
        val activeTenants = tenantRepository.countByStatus(TenantStatus.ACTIVE).getOrNull() ?: 0L

        return PlatformMetrics(
            totalRevenueMRR = mrr,
            averageRevenuePerTenant = if (activeTenants > 0) mrr / activeTenants else 0.0,
            churnRate = calculateChurnRate(),
            growthRate = calculateGrowthRate(),
            totalMembers = memberRepository.countAll().getOrNull() ?: 0L,
            totalBookings = bookingRepository.countAll().getOrNull() ?: 0L
        )
    }

    private suspend fun calculateMRR(): Double {
        val pageable = PageRequest.of(0, 10000)
        val subscriptions = subscriptionRepository.findAll(pageable).getOrNull()?.content ?: emptyList()

        return subscriptions
            .filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
            .sumOf { subscription ->
                when (subscription.billingCycle.name) {
                    "MONTHLY" -> subscription.amount.amount
                    "QUARTERLY" -> subscription.amount.amount / java.math.BigDecimal("3.0")
                    "ANNUAL" -> subscription.amount.amount / java.math.BigDecimal("12.0")
                    else -> java.math.BigDecimal.ZERO
                }
            }.toDouble()
    }

    private suspend fun calculateChurnRate(): Double {
        val totalTenants = tenantRepository.count().getOrNull() ?: 0L
        val cancelledTenants = tenantRepository.countByStatus(TenantStatus.CANCELLED).getOrNull() ?: 0L

        return if (totalTenants > 0) {
            (cancelledTenants.toDouble() / totalTenants.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private suspend fun calculateGrowthRate(): Double {
        val pageable = PageRequest.of(0, 10000)
        val tenants = tenantRepository.findAll(pageable).getOrNull()?.content ?: emptyList()

        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1)
        val newTenantsThisMonth = tenants.count {
            LocalDate.ofInstant(it.createdAt, java.time.ZoneOffset.UTC).isAfter(startOfMonth)
        }

        val totalTenants = tenants.size
        return if (totalTenants > 0) {
            (newTenantsThisMonth.toDouble() / totalTenants.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private fun calculateOverallStatus(
        components: Map<String, ComponentHealth>,
        tenants: TenantHealthSummary
    ): HealthStatus {
        // If any critical component is unhealthy, platform is unhealthy
        if (components["database"]?.status == HealthStatus.UNHEALTHY ||
            components["redis"]?.status == HealthStatus.UNHEALTHY
        ) {
            return HealthStatus.UNHEALTHY
        }

        // If we have critical tenant issues, mark as degraded
        val criticalIssues = tenants.issues.count { it.severity == Severity.CRITICAL }
        if (criticalIssues > 5) {
            return HealthStatus.DEGRADED
        }

        // If any component is degraded, platform is degraded
        if (components.values.any { it.status == HealthStatus.DEGRADED }) {
            return HealthStatus.DEGRADED
        }

        return HealthStatus.HEALTHY
    }

    private fun calculateTenantHealthStatus(alerts: List<TenantAlert>): HealthStatus {
        val criticalAlerts = alerts.count { it.severity == Severity.CRITICAL }
        val highAlerts = alerts.count { it.severity == Severity.HIGH }

        return when {
            criticalAlerts > 0 -> HealthStatus.UNHEALTHY
            highAlerts > 0 -> HealthStatus.DEGRADED
            alerts.isNotEmpty() -> HealthStatus.DEGRADED
            else -> HealthStatus.HEALTHY
        }
    }

    private fun generateTenantAlerts(
        tenant: com.liyaqa.gym.domain.entities.tenant.Tenant,
        subscription: com.liyaqa.gym.domain.entities.tenant.TenantSubscription?,
        usage: UsageHealth
    ): List<TenantAlert> {
        val alerts = mutableListOf<TenantAlert>()

        // Check subscription status
        if (subscription == null) {
            alerts.add(
                TenantAlert(
                    severity = Severity.CRITICAL,
                    type = IssueType.NO_PAYMENT_METHOD,
                    message = "No active subscription found",
                    timestamp = Instant.now()
                )
            )
        } else {
            // Check payment failures
            if (subscription.paymentFailureCount > 0) {
                alerts.add(
                    TenantAlert(
                        severity = if (subscription.paymentFailureCount >= 3) Severity.CRITICAL else Severity.HIGH,
                        type = IssueType.PAYMENT_FAILURE,
                        message = "Payment failed ${subscription.paymentFailureCount} times",
                        timestamp = subscription.lastPaymentFailureAt ?: Instant.now()
                    )
                )
            }

            // Check if subscription is past due
            if (subscription.status.name == "PAST_DUE") {
                alerts.add(
                    TenantAlert(
                        severity = Severity.CRITICAL,
                        type = IssueType.PAYMENT_PAST_DUE,
                        message = "Subscription payment is past due",
                        timestamp = Instant.now()
                    )
                )
            }

            // Check trial expiration
            if (subscription.isTrial() && subscription.trialEndsAt != null) {
                val daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), subscription.trialEndsAt)
                if (daysUntilExpiry <= 3) {
                    alerts.add(
                        TenantAlert(
                            severity = if (daysUntilExpiry <= 1) Severity.HIGH else Severity.MEDIUM,
                            type = IssueType.TRIAL_EXPIRING,
                            message = "Trial expires in $daysUntilExpiry days",
                            timestamp = Instant.now()
                        )
                    )
                }
            }
        }

        // Check usage limits
        if (usage.memberPercentage >= 0.9) {
            alerts.add(
                TenantAlert(
                    severity = if (usage.memberPercentage >= 0.95) Severity.HIGH else Severity.MEDIUM,
                    type = if (usage.memberPercentage >= 1.0) IssueType.OVER_LIMIT else IssueType.APPROACHING_LIMIT,
                    message = "Using ${(usage.memberPercentage * 100).toInt()}% of member limit",
                    timestamp = Instant.now()
                )
            )
        }

        if (usage.branchPercentage >= 0.9) {
            alerts.add(
                TenantAlert(
                    severity = if (usage.branchPercentage >= 1.0) Severity.HIGH else Severity.MEDIUM,
                    type = if (usage.branchPercentage >= 1.0) IssueType.OVER_LIMIT else IssueType.APPROACHING_LIMIT,
                    message = "Using ${(usage.branchPercentage * 100).toInt()}% of branch limit",
                    timestamp = Instant.now()
                )
            )
        }

        return alerts
    }
}
