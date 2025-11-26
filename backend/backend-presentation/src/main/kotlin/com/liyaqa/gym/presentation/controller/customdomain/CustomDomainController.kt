package com.liyaqa.gym.presentation.controller.customdomain

import com.liyaqa.gym.application.customdomain.CustomDomainService
import com.liyaqa.gym.domain.entities.tenant.TenantUser
import com.liyaqa.gym.presentation.dto.customdomain.AddCustomDomainRequest
import com.liyaqa.gym.presentation.dto.customdomain.CustomDomainStatusResponse
import com.liyaqa.gym.presentation.dto.customdomain.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * Custom Domain Controller
 * Handles custom domain management for tenants (authenticated endpoints)
 */
@RestController
@RequestMapping("/api/v1/tenant/custom-domain")
@PreAuthorize("hasRole('TENANT_OWNER')")
@Tag(name = "Custom Domain", description = "Custom domain management endpoints")
class CustomDomainController(
    private val customDomainService: CustomDomainService
) {

    private val logger = LoggerFactory.getLogger(CustomDomainController::class.java)

    /**
     * Add a custom domain to the tenant
     */
    @PostMapping
    @Operation(
        summary = "Add custom domain",
        description = "Add a custom domain to your tenant and get DNS verification instructions"
    )
    fun addCustomDomain(
        @RequestBody request: AddCustomDomainRequest,
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<CustomDomainStatusResponse> {
        return runBlocking {
            logger.info("Adding custom domain '${request.domain}' for tenant: ${user.tenantId}")

            val result = customDomainService.addCustomDomain(user.tenantId, request.domain)

            result.fold(
                onSuccess = { status ->
                    logger.info("Successfully added custom domain '${request.domain}' for tenant: ${user.tenantId}")
                    ResponseEntity.ok(status.toResponse())
                },
                onFailure = { error ->
                    logger.error("Failed to add custom domain '${request.domain}' for tenant: ${user.tenantId}", error)
                    throw error
                }
            )
        }
    }

    /**
     * Verify the custom domain
     */
    @PostMapping("/verify")
    @Operation(
        summary = "Verify custom domain",
        description = "Verify DNS records and provision SSL certificate for the custom domain"
    )
    fun verifyDomain(
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<CustomDomainStatusResponse> {
        return runBlocking {
            logger.info("Verifying custom domain for tenant: ${user.tenantId}")

            val result = customDomainService.verifyCustomDomain(user.tenantId)

            result.fold(
                onSuccess = { status ->
                    logger.info("Custom domain verification completed for tenant: ${user.tenantId}, status: ${status.status}")
                    ResponseEntity.ok(status.toResponse())
                },
                onFailure = { error ->
                    logger.error("Failed to verify custom domain for tenant: ${user.tenantId}", error)
                    throw error
                }
            )
        }
    }

    /**
     * Get custom domain status
     */
    @GetMapping("/status")
    @Operation(
        summary = "Get custom domain status",
        description = "Get the current status of the custom domain configuration"
    )
    fun getDomainStatus(
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<CustomDomainStatusResponse> {
        return runBlocking {
            logger.info("Getting custom domain status for tenant: ${user.tenantId}")

            val result = customDomainService.getCustomDomainStatus(user.tenantId)

            result.fold(
                onSuccess = { status ->
                    if (status == null) {
                        ResponseEntity.noContent().build()
                    } else {
                        ResponseEntity.ok(status.toResponse())
                    }
                },
                onFailure = { error ->
                    logger.error("Failed to get custom domain status for tenant: ${user.tenantId}", error)
                    throw error
                }
            )
        }
    }

    /**
     * Remove the custom domain
     */
    @DeleteMapping
    @Operation(
        summary = "Remove custom domain",
        description = "Remove the custom domain from your tenant"
    )
    fun removeCustomDomain(
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<Unit> {
        return runBlocking {
            logger.info("Removing custom domain for tenant: ${user.tenantId}")

            val result = customDomainService.removeCustomDomain(user.tenantId)

            result.fold(
                onSuccess = {
                    logger.info("Successfully removed custom domain for tenant: ${user.tenantId}")
                    ResponseEntity.noContent().build()
                },
                onFailure = { error ->
                    logger.error("Failed to remove custom domain for tenant: ${user.tenantId}", error)
                    throw error
                }
            )
        }
    }
}
