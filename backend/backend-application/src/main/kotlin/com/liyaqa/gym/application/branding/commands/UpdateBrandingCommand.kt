package com.liyaqa.gym.application.branding.commands

import com.liyaqa.gym.domain.entities.tenant.BrandColors
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Command for updating tenant branding settings.
 */
data class UpdateBrandingCommand(
    val tenantId: UUID,
    val logoFile: MultipartFile?,
    val faviconFile: MultipartFile?,
    val brandColors: BrandColors?
)
