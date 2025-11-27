package com.liyaqa.gym.presentation.controller.platform

import com.liyaqa.gym.application.platform.PlatformHealthService
import com.liyaqa.gym.domain.entities.health.IssueType
import com.liyaqa.gym.domain.entities.health.Severity
import com.liyaqa.gym.presentation.dto.health.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Platform Health Controller
 * Provides health monitoring endpoints for platform administrators
 */
@RestController
@RequestMapping("/api/v1/platform/health")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@Tag(name = "Platform Health", description = "Platform health monitoring endpoints")
class PlatformHealthController(
    private val healthService: PlatformHealthService
) {

    private val logger = LoggerFactory.getLogger(PlatformHealthController::class.java)

    /**
     * Get overall platform health
     */
    @GetMapping
    @Operation(
        summary = "Get platform health",
        description = "Get overall platform health including infrastructure components and tenant summary"
    )
    fun getPlatformHealth(): ResponseEntity<PlatformHealthResponse> {
        logger.info("Platform health check requested")

        val health = runBlocking {
            healthService.getPlatformHealth()
        }

        return ResponseEntity.ok(health.toResponse())
    }

    /**
     * Get health for a specific tenant
     */
    @GetMapping("/tenants/{id}")
    @Operation(
        summary = "Get tenant health",
        description = "Get detailed health information for a specific tenant"
    )
    fun getTenantHealth(
        @Parameter(description = "Tenant ID")
        @PathVariable id: UUID
    ): ResponseEntity<TenantHealthResponse> {
        logger.info("Tenant health check requested for tenant: $id")

        val health = runBlocking {
            healthService.getTenantHealth(id)
        }

        return ResponseEntity.ok(health.toResponse())
    }

    /**
     * Get all tenant issues with optional filtering
     */
    @GetMapping("/issues")
    @Operation(
        summary = "Get tenant issues",
        description = "Get all tenant issues with optional filtering by severity and type"
    )
    fun getTenantIssues(
        @Parameter(description = "Filter by severity (LOW, MEDIUM, HIGH, CRITICAL)")
        @RequestParam(required = false) severity: String?,
        @Parameter(description = "Filter by issue type")
        @RequestParam(required = false) type: String?
    ): ResponseEntity<List<TenantIssueResponse>> {
        logger.info("Tenant issues requested - severity: $severity, type: $type")

        val severityEnum = severity?.let {
            try {
                Severity.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid severity value: $severity")
                null
            }
        }

        val typeEnum = type?.let {
            try {
                IssueType.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid issue type value: $type")
                null
            }
        }

        val issues = runBlocking {
            healthService.getTenantIssues(severityEnum, typeEnum)
        }

        return ResponseEntity.ok(issues.map { it.toResponse() })
    }
}
