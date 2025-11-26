package com.liyaqa.android.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.liyaqa.gym.domain.TenantBranding

/**
 * Branded theme composable that applies tenant-specific colors.
 * Use this instead of MaterialTheme for tenant-branded apps.
 */
@Composable
fun LiyaqaBrandedTheme(
    branding: TenantBranding?,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (branding != null) {
        // Parse tenant brand colors
        val primaryColor = parseHexColor(branding.brandColors.primaryColor)
        val secondaryColor = parseHexColor(branding.brandColors.secondaryColor)
        val accentColor = parseHexColor(branding.brandColors.accentColor)

        if (darkTheme) {
            darkColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                tertiary = accentColor,
                // Add complementary colors for dark theme
                primaryContainer = primaryColor.copy(alpha = 0.3f),
                secondaryContainer = secondaryColor.copy(alpha = 0.3f),
                tertiaryContainer = accentColor.copy(alpha = 0.3f)
            )
        } else {
            lightColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                tertiary = accentColor,
                // Add complementary colors for light theme
                primaryContainer = primaryColor.copy(alpha = 0.1f),
                secondaryContainer = secondaryColor.copy(alpha = 0.1f),
                tertiaryContainer = accentColor.copy(alpha = 0.1f)
            )
        }
    } else {
        // Use default theme colors
        if (darkTheme) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Parse hex color string to Compose Color
 */
private fun parseHexColor(hexColor: String): Color {
    return try {
        val colorInt = AndroidColor.parseColor(hexColor)
        Color(colorInt)
    } catch (e: Exception) {
        // Fallback to default blue if parsing fails
        Color(0xFF1976D2)
    }
}

/**
 * Remember tenant branding state
 */
@Composable
fun rememberTenantBranding(tenantSlug: String): State<TenantBranding?> {
    val branding = remember { mutableStateOf<TenantBranding?>(null) }

    // TODO: Load branding from repository
    // This should be implemented in a ViewModel or use case

    return branding
}

/**
 * Branded theme preview composable for testing
 */
@Composable
fun LiyaqaBrandedThemePreview(
    primaryColor: String = "#1976D2",
    secondaryColor: String = "#DC004E",
    accentColor: String = "#F50057",
    content: @Composable () -> Unit
) {
    val mockBranding = TenantBranding(
        tenantId = "preview",
        tenantName = "Preview Gym",
        tenantNameArabic = null,
        logo = null,
        favicon = null,
        brandColors = com.liyaqa.gym.domain.BrandColors(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            accentColor = accentColor
        ),
        customDomain = null,
        emailFromName = "Preview Gym",
        emailFromAddress = "noreply@preview.com",
        smsFromName = "Preview Gym",
        supportEmail = "support@preview.com",
        supportPhone = "+966 50 000 0000",
        socialLinks = null,
        mobileAppConfig = null
    )

    LiyaqaBrandedTheme(
        branding = mockBranding,
        content = content
    )
}
