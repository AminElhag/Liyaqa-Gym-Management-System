package com.liyaqa.gym.presentation.controller.branding

import com.liyaqa.gym.application.branding.usecases.GetTenantBrandingUseCase
import com.liyaqa.gym.presentation.dto.branding.TenantBrandingResponse
import com.liyaqa.gym.presentation.dto.branding.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

/**
 * Public Branding Controller
 * Handles public branding endpoints (no authentication required)
 */
@RestController
@RequestMapping("/api/v1/public/branding")
@Tag(name = "Public Branding", description = "Public branding endpoints for mobile apps and web clients")
class PublicBrandingController(
    private val getBrandingUseCase: GetTenantBrandingUseCase
) {

    private val logger = LoggerFactory.getLogger(PublicBrandingController::class.java)

    /**
     * Get tenant branding by slug (public endpoint for mobile apps)
     */
    @GetMapping("/{slug}")
    @Operation(
        summary = "Get tenant branding by slug",
        description = "Retrieve tenant branding information by tenant slug. This endpoint is public and used by mobile apps and web clients to fetch tenant-specific theming."
    )
    fun getTenantBranding(
        @Parameter(description = "Tenant slug (subdomain identifier)")
        @PathVariable slug: String
    ): ResponseEntity<TenantBrandingResponse> {
        return runBlocking {
            logger.info("Public request for branding with slug: $slug")

            val branding = getBrandingUseCase.execute(slug)
                ?: return@runBlocking ResponseEntity.notFound().build()

            // Set cache control headers for 1 hour
            ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(branding.toResponse())
        }
    }

    /**
     * Get tenant branding by custom domain
     */
    @GetMapping("/domain/{domain}")
    @Operation(
        summary = "Get tenant branding by custom domain",
        description = "Retrieve tenant branding information by custom domain"
    )
    fun getTenantBrandingByDomain(
        @Parameter(description = "Custom domain")
        @PathVariable domain: String
    ): ResponseEntity<TenantBrandingResponse> {
        logger.info("Public request for branding with custom domain: $domain")

        // TODO: Implement custom domain lookup
        // For now, we'll extract the subdomain from the domain
        val slug = domain.split(".").firstOrNull() ?: return ResponseEntity.notFound().build()

        return runBlocking {
            val branding = getBrandingUseCase.execute(slug)
                ?: return@runBlocking ResponseEntity.notFound().build()

            ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(branding.toResponse())
        }
    }
}
