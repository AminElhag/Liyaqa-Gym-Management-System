package com.liyaqa.gym.domain.entities.branding

import java.util.UUID

/**
 * Tenant branding information for white-label customization.
 * Contains all branding-related data for tenant applications.
 */
data class TenantBranding(
    val tenantId: UUID,
    val tenantName: String,
    val tenantNameArabic: String?,
    val logo: String?,
    val favicon: String?,
    val brandColors: BrandColors,
    val customDomain: String?,
    val emailFromName: String,
    val emailFromAddress: String,
    val smsFromName: String,
    val supportEmail: String,
    val supportPhone: String,
    val socialLinks: SocialLinks?,
    val mobileAppConfig: MobileAppConfig?
)

/**
 * Brand colors for tenant customization
 */
data class BrandColors(
    val primaryColor: String,
    val secondaryColor: String,
    val accentColor: String
) {
    init {
        require(isValidHexColor(primaryColor)) { "Primary color must be a valid hex color" }
        require(isValidHexColor(secondaryColor)) { "Secondary color must be a valid hex color" }
        require(isValidHexColor(accentColor)) { "Accent color must be a valid hex color" }
    }

    private fun isValidHexColor(color: String): Boolean {
        return color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))
    }

    companion object {
        fun default(): BrandColors {
            return BrandColors(
                primaryColor = "#1976d2",
                secondaryColor = "#dc004e",
                accentColor = "#f50057"
            )
        }
    }
}

/**
 * Social media links for tenant
 */
data class SocialLinks(
    val facebook: String?,
    val instagram: String?,
    val twitter: String?,
    val linkedin: String?,
    val tiktok: String?,
    val youtube: String?,
    val website: String?
)

/**
 * Mobile app configuration for tenant
 */
data class MobileAppConfig(
    val appName: String,
    val appNameArabic: String?,
    val appIconUrl: String?,
    val splashScreenUrl: String?,
    val primaryColor: String,
    val androidPackageName: String,
    val iosAppId: String?,
    val androidAppId: String?
)
