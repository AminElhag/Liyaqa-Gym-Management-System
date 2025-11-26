package com.liyaqa.gym.presentation.controller.branding

import com.liyaqa.gym.application.branding.commands.UpdateBrandingCommand
import com.liyaqa.gym.application.branding.usecases.GetTenantBrandingUseCase
import com.liyaqa.gym.application.branding.usecases.UpdateTenantBrandingUseCase
import com.liyaqa.gym.domain.entities.tenant.BrandColors
import com.liyaqa.gym.domain.entities.tenant.TenantUser
import com.liyaqa.gym.presentation.dto.branding.BrandColorsDTO
import com.liyaqa.gym.presentation.dto.branding.BrandingPreviewResponse
import com.liyaqa.gym.presentation.dto.branding.PreviewBrandingRequest
import com.liyaqa.gym.presentation.dto.branding.TenantBrandingResponse
import com.liyaqa.gym.presentation.dto.branding.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Tenant Branding Controller
 * Handles tenant branding management (authenticated endpoints)
 */
@RestController
@RequestMapping("/api/v1/tenant/branding")
@PreAuthorize("hasAnyRole('TENANT_OWNER', 'TENANT_ADMIN')")
@Tag(name = "Tenant Branding", description = "Tenant branding management endpoints")
class TenantBrandingController(
    private val updateBrandingUseCase: UpdateTenantBrandingUseCase,
    private val getBrandingUseCase: GetTenantBrandingUseCase
) {

    private val logger = LoggerFactory.getLogger(TenantBrandingController::class.java)

    /**
     * Get current tenant branding
     */
    @GetMapping
    @Operation(summary = "Get tenant branding", description = "Retrieve current tenant branding settings")
    fun getBranding(@AuthenticationPrincipal user: TenantUser): ResponseEntity<TenantBrandingResponse> {
        return runBlocking {
            logger.info("Getting branding for tenant: ${user.tenantId}")

            val branding = getBrandingUseCase.executeById(user.tenantId)
                ?: return@runBlocking ResponseEntity.notFound().build()

            ResponseEntity.ok(branding.toResponse())
        }
    }

    /**
     * Update tenant branding
     */
    @PutMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Update tenant branding",
        description = "Update tenant branding including logo, favicon, and brand colors"
    )
    fun updateBranding(
        @RequestParam(required = false) logo: MultipartFile?,
        @RequestParam(required = false) favicon: MultipartFile?,
        @RequestParam(required = false) primaryColor: String?,
        @RequestParam(required = false) secondaryColor: String?,
        @RequestParam(required = false) accentColor: String?,
        @AuthenticationPrincipal user: TenantUser
    ): ResponseEntity<TenantBrandingResponse> {
        return runBlocking {
            logger.info("Updating branding for tenant: ${user.tenantId}")

            val brandColors = if (primaryColor != null && secondaryColor != null && accentColor != null) {
                BrandColors(primaryColor, secondaryColor, accentColor)
            } else null

            val command = UpdateBrandingCommand(
                tenantId = user.tenantId,
                logoFile = logo,
                faviconFile = favicon,
                brandColors = brandColors
            )

            val result = updateBrandingUseCase.execute(command)

            result.fold(
                onSuccess = { branding ->
                    logger.info("Successfully updated branding for tenant: ${user.tenantId}")
                    ResponseEntity.ok(branding.toResponse())
                },
                onFailure = { error ->
                    logger.error("Failed to update branding for tenant: ${user.tenantId}", error)
                    throw error
                }
            )
        }
    }

    /**
     * Preview branding changes without saving
     */
    @PostMapping("/preview")
    @Operation(
        summary = "Preview branding",
        description = "Generate a preview of branding changes without saving them"
    )
    fun previewBranding(
        @RequestBody request: PreviewBrandingRequest
    ): ResponseEntity<BrandingPreviewResponse> {
        logger.info("Generating branding preview")

        // Generate preview with temporary branding
        val preview = BrandingPreviewResponse(
            previewUrl = "/api/v1/tenant/branding/preview/${java.util.UUID.randomUUID()}",
            brandColors = BrandColorsDTO(
                primaryColor = request.primaryColor ?: "#1976d2",
                secondaryColor = request.secondaryColor ?: "#dc004e",
                accentColor = request.accentColor ?: "#f50057"
            ),
            logo = request.logoUrl,
            favicon = request.faviconUrl
        )

        return ResponseEntity.ok(preview)
    }
}
