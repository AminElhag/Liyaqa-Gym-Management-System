package com.liyaqa.gym.application.platform

import com.liyaqa.gym.domain.entities.health.HealthStatus
import com.liyaqa.gym.domain.entities.health.IssueType
import com.liyaqa.gym.domain.entities.health.Severity
import com.liyaqa.gym.domain.services.EmailService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Platform Alert Service
 * Monitors platform health and sends alerts for critical issues
 */
@Component
class PlatformAlertService(
    private val healthService: PlatformHealthService,
    private val emailService: EmailService
) {

    private val logger = LoggerFactory.getLogger(PlatformAlertService::class.java)

    @Value("\${platform.alerts.email:ops@liyaqa.com}")
    private val alertEmail: String = "ops@liyaqa.com"

    @Value("\${platform.alerts.enabled:true}")
    private val alertsEnabled: Boolean = true

    /**
     * Scheduled health check and alerting
     * Runs every 15 minutes
     */
    @Scheduled(cron = "0 */15 * * * *")
    fun checkAndAlert() {
        if (!alertsEnabled) {
            logger.debug("Platform alerts are disabled")
            return
        }

        logger.info("Running scheduled platform health check")

        runBlocking {
            try {
                val health = healthService.getPlatformHealth()

                // Check for critical platform health issues
                if (health.status == HealthStatus.UNHEALTHY) {
                    sendCriticalAlert(
                        "Platform Health Critical",
                        buildPlatformHealthMessage(health)
                    )
                }

                // Check for high-severity tenant issues
                val criticalIssues = health.tenants.issues
                    .filter { it.severity == Severity.CRITICAL || it.severity == Severity.HIGH }

                if (criticalIssues.isNotEmpty()) {
                    sendAlert(
                        "Tenant Issues Detected",
                        buildTenantIssuesMessage(criticalIssues)
                    )
                }

                // Check for multiple payment failures
                val paymentFailures = health.tenants.issues
                    .filter { it.type == IssueType.PAYMENT_FAILURE }

                if (paymentFailures.size >= 5) {
                    sendAlert(
                        "Multiple Payment Failures",
                        "${paymentFailures.size} tenants are experiencing payment failures. Immediate attention required."
                    )
                }

                // Check for degraded infrastructure
                val degradedComponents = health.components
                    .filter { it.value.status == HealthStatus.DEGRADED || it.value.status == HealthStatus.UNHEALTHY }

                if (degradedComponents.isNotEmpty()) {
                    sendAlert(
                        "Infrastructure Components Degraded",
                        buildComponentMessage(degradedComponents)
                    )
                }

                logger.info("Platform health check completed - Status: ${health.status}, Issues: ${health.tenants.withIssues}")

            } catch (e: Exception) {
                logger.error("Error during scheduled health check", e)
                sendCriticalAlert(
                    "Platform Health Check Failed",
                    "The scheduled platform health check encountered an error: ${e.message}"
                )
            }
        }
    }

    /**
     * Send a critical alert (highest priority)
     */
    private suspend fun sendCriticalAlert(subject: String, message: String) {
        logger.error("CRITICAL ALERT: $subject - $message")

        try {
            emailService.sendTenantWelcomeEmail(
                to = alertEmail,
                tenantName = "Platform Operations",
                username = "",
                temporaryPassword = "",
                loginUrl = "",
                trialEndsAt = null
            )
            // Note: Using welcome email as a workaround. In production, create a dedicated alert email method.
            logger.info("Critical alert email sent to: $alertEmail")
        } catch (e: Exception) {
            logger.error("Failed to send critical alert email", e)
        }

        // Also log to monitoring systems
        logToMonitoring(subject, message, "CRITICAL")
    }

    /**
     * Send a standard alert
     */
    private suspend fun sendAlert(subject: String, message: String) {
        logger.warn("ALERT: $subject - $message")

        try {
            // In production, implement proper email template for alerts
            logger.info("Alert logged: $subject")
        } catch (e: Exception) {
            logger.error("Failed to send alert", e)
        }

        // Log to monitoring systems
        logToMonitoring(subject, message, "WARNING")
    }

    /**
     * Build detailed platform health message
     */
    private fun buildPlatformHealthMessage(health: com.liyaqa.gym.domain.entities.health.PlatformHealth): String {
        return buildString {
            appendLine("Platform Health Status: ${health.status}")
            appendLine()
            appendLine("Component Status:")
            health.components.forEach { (name, component) ->
                appendLine("  - $name: ${component.status}")
                component.message?.let { appendLine("    Message: $it") }
                component.responseTimeMs?.let { appendLine("    Response Time: ${it}ms") }
            }
            appendLine()
            appendLine("Tenant Summary:")
            appendLine("  - Total: ${health.tenants.total}")
            appendLine("  - Active: ${health.tenants.active}")
            appendLine("  - With Issues: ${health.tenants.withIssues}")
            appendLine()
            appendLine("Platform Metrics:")
            appendLine("  - MRR: $${String.format("%.2f", health.metrics.totalRevenueMRR)}")
            appendLine("  - Total Members: ${health.metrics.totalMembers}")
            appendLine("  - Total Bookings: ${health.metrics.totalBookings}")
        }
    }

    /**
     * Build tenant issues message
     */
    private fun buildTenantIssuesMessage(issues: List<com.liyaqa.gym.domain.entities.health.TenantIssue>): String {
        return buildString {
            appendLine("Found ${issues.size} high-severity tenant issues:")
            appendLine()

            val groupedIssues = issues.groupBy { it.type }
            groupedIssues.forEach { (type, typeIssues) ->
                appendLine("${type.name} (${typeIssues.size}):")
                typeIssues.take(5).forEach { issue ->
                    appendLine("  - Tenant ${issue.tenantId}: ${issue.message} [${issue.severity}]")
                }
                if (typeIssues.size > 5) {
                    appendLine("  ... and ${typeIssues.size - 5} more")
                }
                appendLine()
            }
        }
    }

    /**
     * Build component health message
     */
    private fun buildComponentMessage(components: Map<String, com.liyaqa.gym.domain.entities.health.ComponentHealth>): String {
        return buildString {
            appendLine("The following infrastructure components are experiencing issues:")
            appendLine()
            components.forEach { (name, component) ->
                appendLine("$name: ${component.status}")
                component.message?.let { appendLine("  Message: $it") }
                component.responseTimeMs?.let { appendLine("  Response Time: ${it}ms") }
                appendLine()
            }
        }
    }

    /**
     * Log to external monitoring systems
     * This would integrate with services like Datadog, New Relic, etc.
     */
    private fun logToMonitoring(subject: String, message: String, level: String) {
        // Placeholder for monitoring integration
        logger.info("Monitoring log [$level]: $subject")
        // TODO: Integrate with monitoring service (Datadog, New Relic, CloudWatch, etc.)
    }
}
