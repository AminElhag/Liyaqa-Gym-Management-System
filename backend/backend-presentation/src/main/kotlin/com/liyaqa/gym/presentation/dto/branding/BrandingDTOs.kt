package com.liyaqa.gym.presentation.dto.branding

import com.liyaqa.gym.domain.entities.branding.BrandColors
import com.liyaqa.gym.domain.entities.branding.MobileAppConfig
import com.liyaqa.gym.domain.entities.branding.SocialLinks
import com.liyaqa.gym.domain.entities.branding.TenantBranding
import java.util.UUID

/**
 * Response DTO for tenant branding information.
 */
data class TenantBrandingResponse(
    val tenantId: UUID,
    val tenantName: String,
    val tenantNameArabic: String?,
    val logo: String?,
    val favicon: String?,
    val brandColors: BrandColorsDTO,
    val customDomain: String?,
    val emailFromName: String,
    val emailFromAddress: String,
    val smsFromName: String,
    val supportEmail: String,
    val supportPhone: String,
    val socialLinks: SocialLinksDTO?,
    val mobileAppConfig: MobileAppConfigDTO?
)

/**
 * DTO for brand colors.
 */
data class BrandColorsDTO(
    val primaryColor: String,
    val secondaryColor: String,
    val accentColor: String
)

/**
 * DTO for social media links.
 */
data class SocialLinksDTO(
    val facebook: String?,
    val instagram: String?,
    val twitter: String?,
    val linkedin: String?,
    val tiktok: String?,
    val youtube: String?,
    val website: String?
)

/**
 * DTO for mobile app configuration.
 */
data class MobileAppConfigDTO(
    val appName: String,
    val appNameArabic: String?,
    val appIconUrl: String?,
    val splashScreenUrl: String?,
    val primaryColor: String,
    val androidPackageName: String,
    val iosAppId: String?,
    val androidAppId: String?
)

/**
 * Request DTO for previewing branding changes.
 */
data class PreviewBrandingRequest(
    val primaryColor: String?,
    val secondaryColor: String?,
    val accentColor: String?,
    val logoUrl: String?,
    val faviconUrl: String?
)

/**
 * Response DTO for branding preview.
 */
data class BrandingPreviewResponse(
    val previewUrl: String,
    val brandColors: BrandColorsDTO,
    val logo: String?,
    val favicon: String?
)

/**
 * Extension function to convert TenantBranding to TenantBrandingResponse.
 */
fun TenantBranding.toResponse(): TenantBrandingResponse {
    return TenantBrandingResponse(
        tenantId = this.tenantId,
        tenantName = this.tenantName,
        tenantNameArabic = this.tenantNameArabic,
        logo = this.logo,
        favicon = this.favicon,
        brandColors = BrandColorsDTO(
            primaryColor = this.brandColors.primaryColor,
            secondaryColor = this.brandColors.secondaryColor,
            accentColor = this.brandColors.accentColor
        ),
        customDomain = this.customDomain,
        emailFromName = this.emailFromName,
        emailFromAddress = this.emailFromAddress,
        smsFromName = this.smsFromName,
        supportEmail = this.supportEmail,
        supportPhone = this.supportPhone,
        socialLinks = this.socialLinks?.let {
            SocialLinksDTO(
                facebook = it.facebook,
                instagram = it.instagram,
                twitter = it.twitter,
                linkedin = it.linkedin,
                tiktok = it.tiktok,
                youtube = it.youtube,
                website = it.website
            )
        },
        mobileAppConfig = this.mobileAppConfig?.let {
            MobileAppConfigDTO(
                appName = it.appName,
                appNameArabic = it.appNameArabic,
                appIconUrl = it.appIconUrl,
                splashScreenUrl = it.splashScreenUrl,
                primaryColor = it.primaryColor,
                androidPackageName = it.androidPackageName,
                iosAppId = it.iosAppId,
                androidAppId = it.androidAppId
            )
        }
    )
}
