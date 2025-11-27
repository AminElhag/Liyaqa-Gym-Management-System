package com.liyaqa.gym.presentation.controller.tenant

import com.liyaqa.gym.application.platform.PlatformHealthService
import com.liyaqa.gym.domain.entities.tenant.TenantUser
import com.liyaqa.gym.presentation.dto.health.TenantHealthResponse
import com.liyaqa.gym.presentation.dto.health.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * Tenant Health Controller
 * Provides health monitoring endpoints for tenant owners and admins
 */
@RestController
@RequestMapping("/api/v1/tenant/health")
@PreAuthorize("hasAnyRole('TENANT_OWNER', 'TENANT_ADMIN')")
@Tag(name = "Tenant Health", description = "Tenant health self-service endpoints")
class TenantHealthController(
    private val healthService: PlatformHealthService
) {

    private val logger = LoggerFactory.getLogger(TenantHealthController::class.java)

    /**
     * Get health status for the authenticated tenant
     */
    @GetMapping
    @Operation(
        summary = "Get tenant health",
        description = "Get health status, subscription info, usage metrics, and alerts for your tenant"
    )
    fun getTenantHealth(
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<TenantHealthResponse> {
        logger.info("Tenant health check requested by tenant: ${user.tenantId}")

        val health = runBlocking {
            healthService.getTenantHealth(user.tenantId)
        }

        return ResponseEntity.ok(health.toResponse())
    }
}
