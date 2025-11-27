package com.liyaqa.gym.application.branding.usecases

import com.liyaqa.gym.domain.entities.branding.BrandColors
import com.liyaqa.gym.domain.entities.branding.MobileAppConfig
import com.liyaqa.gym.domain.entities.branding.TenantBranding
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.services.CacheService
import org.springframework.stereotype.Service

/**
 * Use case for retrieving tenant branding information.
 * Implements caching for better performance.
 */
@Service
class GetTenantBrandingUseCase(
    private val tenantRepository: TenantRepository,
    private val cacheService: CacheService
) {

    suspend fun execute(tenantSlug: String): TenantBranding? {
        // Try cache first
        val cacheKey = "tenant:branding:slug:$tenantSlug"
        val cached = cacheService.get<TenantBranding>(cacheKey)
        if (cached != null) {
            return cached
        }

        // Load from database
        val tenantOptional = tenantRepository.findBySlug(tenantSlug).getOrNull()
            ?: return null

        if (!tenantOptional.isPresent) {
            return null
        }

        val tenant = tenantOptional.get()

        val branding = TenantBranding(
            tenantId = tenant.id,
            tenantName = tenant.name,
            tenantNameArabic = tenant.nameArabic,
            logo = tenant.logo,
            favicon = tenant.favicon,
            brandColors = tenant.brandColors?.let { convertBrandColors(it) } ?: getDefaultBrandColors(),
            customDomain = tenant.customDomain,
            emailFromName = tenant.emailFromName ?: tenant.name,
            emailFromAddress = tenant.emailFromAddress ?: "noreply@${tenant.slug}.liyaqa.com",
            smsFromName = tenant.smsFromName ?: tenant.name,
            supportEmail = tenant.contactInfo.primaryContactEmail,
            supportPhone = tenant.contactInfo.primaryContactPhone,
            socialLinks = tenant.socialLinks?.let { convertSocialLinks(it) },
            mobileAppConfig = getMobileAppConfig(tenant)
        )

        // Cache for 1 hour
        cacheService.set(cacheKey, branding, ttlSeconds = 3600)

        return branding
    }

    suspend fun executeById(tenantId: java.util.UUID): TenantBranding? {
        // Try cache first
        val cacheKey = "tenant:branding:$tenantId"
        val cached = cacheService.get<TenantBranding>(cacheKey)
        if (cached != null) {
            return cached
        }

        // Load from database
        val tenantOptional = tenantRepository.findById(tenantId).getOrNull()
            ?: return null

        if (!tenantOptional.isPresent) {
            return null
        }

        val tenant = tenantOptional.get()

        val branding = TenantBranding(
            tenantId = tenant.id,
            tenantName = tenant.name,
            tenantNameArabic = tenant.nameArabic,
            logo = tenant.logo,
            favicon = tenant.favicon,
            brandColors = tenant.brandColors?.let { convertBrandColors(it) } ?: getDefaultBrandColors(),
            customDomain = tenant.customDomain,
            emailFromName = tenant.emailFromName ?: tenant.name,
            emailFromAddress = tenant.emailFromAddress ?: "noreply@${tenant.slug}.liyaqa.com",
            smsFromName = tenant.smsFromName ?: tenant.name,
            supportEmail = tenant.contactInfo.primaryContactEmail,
            supportPhone = tenant.contactInfo.primaryContactPhone,
            socialLinks = tenant.socialLinks?.let { convertSocialLinks(it) },
            mobileAppConfig = getMobileAppConfig(tenant)
        )

        // Cache for 1 hour
        cacheService.set(cacheKey, branding, ttlSeconds = 3600)

        return branding
    }

    private fun convertBrandColors(tenantColors: com.liyaqa.gym.domain.entities.tenant.BrandColors): BrandColors {
        return BrandColors(
            primaryColor = tenantColors.primaryColor,
            secondaryColor = tenantColors.secondaryColor,
            accentColor = tenantColors.accentColor
        )
    }

    private fun convertSocialLinks(tenantLinks: com.liyaqa.gym.domain.entities.tenant.SocialLinks): com.liyaqa.gym.domain.entities.branding.SocialLinks {
        return com.liyaqa.gym.domain.entities.branding.SocialLinks(
            facebook = tenantLinks.facebook,
            instagram = tenantLinks.instagram,
            twitter = tenantLinks.twitter,
            linkedin = tenantLinks.linkedin,
            tiktok = tenantLinks.tiktok,
            youtube = tenantLinks.youtube,
            website = tenantLinks.website
        )
    }

    private fun getDefaultBrandColors(): BrandColors {
        return BrandColors(
            primaryColor = "#1976d2",
            secondaryColor = "#dc004e",
            accentColor = "#f50057"
        )
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
