package com.liyaqa.gym.domain

import kotlinx.serialization.Serializable

/**
 * Tenant branding domain model for mobile apps
 */
@Serializable
data class TenantBranding(
    val tenantId: String,
    val tenantName: String,
    val tenantNameArabic: String? = null,
    val logo: String? = null,
    val favicon: String? = null,
    val brandColors: BrandColors,
    val customDomain: String? = null,
    val emailFromName: String,
    val emailFromAddress: String,
    val smsFromName: String,
    val supportEmail: String,
    val supportPhone: String,
    val socialLinks: SocialLinks? = null,
    val mobileAppConfig: MobileAppConfig? = null
)

/**
 * Brand colors for tenant
 */
@Serializable
data class BrandColors(
    val primaryColor: String,
    val secondaryColor: String,
    val accentColor: String
)

/**
 * Social media links
 */
@Serializable
data class SocialLinks(
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null,
    val website: String? = null
)

/**
 * Mobile app configuration
 */
@Serializable
data class MobileAppConfig(
    val appName: String,
    val appNameArabic: String? = null,
    val appIconUrl: String? = null,
    val splashScreenUrl: String? = null,
    val primaryColor: String,
    val androidPackageName: String,
    val iosAppId: String? = null,
    val androidAppId: String? = null
)
