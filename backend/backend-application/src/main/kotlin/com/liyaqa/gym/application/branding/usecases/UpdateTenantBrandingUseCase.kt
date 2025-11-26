package com.liyaqa.gym.application.branding.usecases

import com.liyaqa.gym.application.branding.commands.UpdateBrandingCommand
import com.liyaqa.gym.domain.entities.branding.BrandColors
import com.liyaqa.gym.domain.entities.branding.MobileAppConfig
import com.liyaqa.gym.domain.entities.branding.TenantBranding
import com.liyaqa.gym.domain.entities.tenant.PlatformFeature
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.TenantBrandingUpdatedEvent
import com.liyaqa.gym.domain.exceptions.FeatureNotAvailableException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.services.CacheService
import com.liyaqa.gym.domain.services.EventPublisher
import com.liyaqa.gym.domain.services.StorageService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * Use case for updating tenant branding settings.
 * Handles logo/favicon upload, brand color validation, and cache invalidation.
 */
@Service
class UpdateTenantBrandingUseCase(
    private val tenantRepository: TenantRepository,
    private val storageService: StorageService,
    private val cacheService: CacheService,
    private val eventPublisher: EventPublisher
) {

    suspend fun execute(command: UpdateBrandingCommand): Result<TenantBranding> {
        return try {
            // Find tenant
            val tenantOptional = tenantRepository.findById(command.tenantId).getOrThrow()
            if (!tenantOptional.isPresent) {
                return Result.failure(TenantNotFoundException(command.tenantId))
            }

            val tenant = tenantOptional.get()

            // Check if custom branding is available in plan
            if (!tenant.hasFeature(PlatformFeature.CUSTOM_BRANDING)) {
                return Result.failure(FeatureNotAvailableException("Custom branding"))
            }

            // Upload logo if provided
            var logoUrl: String? = tenant.logo
            if (command.logoFile != null) {
                val uploadResult = storageService.upload(
                    file = command.logoFile,
                    path = "tenants/${tenant.id}/branding/logo",
                    allowedTypes = listOf("image/png", "image/jpeg", "image/svg+xml"),
                    maxSizeMB = 2
                ).getOrThrow()

                logoUrl = uploadResult.url

                // Delete old logo if exists
                if (tenant.logo != null) {
                    storageService.delete(tenant.logo)
                }
            }

            // Upload favicon if provided
            var faviconUrl: String? = tenant.favicon
            if (command.faviconFile != null) {
                val uploadResult = storageService.upload(
                    file = command.faviconFile,
                    path = "tenants/${tenant.id}/branding/favicon",
                    allowedTypes = listOf("image/x-icon", "image/png"),
                    maxSizeMB = 1
                ).getOrThrow()

                faviconUrl = uploadResult.url

                // Delete old favicon if exists
                if (tenant.favicon != null) {
                    storageService.delete(tenant.favicon)
                }
            }

            // Validate and normalize color codes
            val brandColors = command.brandColors?.let {
                validateAndNormalizeBrandColors(it)
            }

            // Update tenant
            var updatedTenant = tenant
            if (logoUrl != null && logoUrl != tenant.logo) {
                updatedTenant = updatedTenant.updateLogo(logoUrl)
            }
            if (brandColors != null) {
                updatedTenant = updatedTenant.updateBrandColors(brandColors)
            }

            // Save the updated favicon separately since we don't have a specific method
            if (faviconUrl != null && faviconUrl != tenant.favicon) {
                updatedTenant = updatedTenant.copy(
                    favicon = faviconUrl,
                    updatedAt = Instant.now()
                )
            }

            tenantRepository.save(updatedTenant).getOrThrow()

            // Invalidate branding cache
            cacheService.delete("tenant:branding:${tenant.id}")
            cacheService.delete("tenant:branding:slug:${tenant.slug}")

            // Publish event
            eventPublisher.publish(
                TenantBrandingUpdatedEvent(
                    tenantId = tenant.id,
                    occurredAt = Instant.now()
                )
            )

            val branding = TenantBranding(
                tenantId = updatedTenant.id,
                tenantName = updatedTenant.name,
                tenantNameArabic = updatedTenant.nameArabic,
                logo = updatedTenant.logo,
                favicon = faviconUrl,
                brandColors = updatedTenant.brandColors ?: BrandColors.default(),
                customDomain = updatedTenant.customDomain,
                emailFromName = updatedTenant.emailFromName ?: updatedTenant.name,
                emailFromAddress = updatedTenant.emailFromAddress
                    ?: "noreply@${updatedTenant.slug}.liyaqa.com",
                smsFromName = updatedTenant.smsFromName ?: updatedTenant.name,
                supportEmail = updatedTenant.contactInfo.primaryContactEmail,
                supportPhone = updatedTenant.contactInfo.primaryContactPhone,
                socialLinks = updatedTenant.socialLinks,
                mobileAppConfig = getMobileAppConfig(updatedTenant)
            )

            Result.success(branding)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateAndNormalizeBrandColors(
        colors: com.liyaqa.gym.domain.entities.tenant.BrandColors
    ): com.liyaqa.gym.domain.entities.tenant.BrandColors {
        require(isValidHexColor(colors.primaryColor)) { "Invalid primary color" }
        require(isValidHexColor(colors.secondaryColor)) { "Invalid secondary color" }
        require(isValidHexColor(colors.accentColor)) { "Invalid accent color" }

        return colors.copy(
            primaryColor = normalizeHexColor(colors.primaryColor),
            secondaryColor = normalizeHexColor(colors.secondaryColor),
            accentColor = normalizeHexColor(colors.accentColor)
        )
    }

    private fun isValidHexColor(color: String): Boolean {
        return color.matches(Regex("^#?[0-9A-Fa-f]{6}$"))
    }

    private fun normalizeHexColor(color: String): String {
        return if (color.startsWith("#")) color else "#$color"
    }

    private fun getMobileAppConfig(tenant: com.liyaqa.gym.domain.entities.tenant.Tenant): MobileAppConfig {
        return MobileAppConfig(
            appName = tenant.name,
            appNameArabic = tenant.nameArabic,
            appIconUrl = tenant.logo,
            splashScreenUrl = tenant.splashScreen,
            primaryColor = tenant.brandColors?.primaryColor ?: "#1976d2",
            androidPackageName = "com.liyaqa.${tenant.slug}",
            iosAppId = tenant.iosAppId,
            androidAppId = tenant.androidAppId
        )
    }
}
