package com.liyaqa.gym.domain.exceptions

import java.util.UUID

/**
 * Exception thrown when a feature is not available for the tenant's plan.
 */
class FeatureNotAvailableException(
    val featureName: String,
    message: String = "Feature '$featureName' is not available in your current plan"
) : RuntimeException(message)

/**
 * Exception thrown when file upload fails validation.
 */
class FileUploadException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception thrown when tenant branding is not found.
 */
class TenantBrandingNotFoundException(
    val tenantId: UUID,
    message: String = "Branding not found for tenant: $tenantId"
) : RuntimeException(message)
