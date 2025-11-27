package com.liyaqa.gym.application.branding.commands

import com.liyaqa.gym.domain.entities.tenant.BrandColors
import com.liyaqa.gym.domain.services.FileUpload
import java.util.UUID

/**
 * Command for updating tenant branding settings.
 */
data class UpdateBrandingCommand(
    val tenantId: UUID,
    val logoFile: FileUpload?,
    val faviconFile: FileUpload?,
    val brandColors: BrandColors?
)
