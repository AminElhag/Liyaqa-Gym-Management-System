package com.liyaqa.gym.presentation.dto.platform

import com.liyaqa.gym.domain.entities.tenant.BillingCycle
import com.liyaqa.gym.domain.entities.tenant.BusinessType
import com.liyaqa.gym.domain.entities.tenant.TenantContactInfo
import com.liyaqa.gym.domain.entities.tenant.TenantSubscriptionPlan
import com.liyaqa.gym.domain.valueobjects.Address
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Request DTO for creating a new tenant
 */
data class CreateTenantRequest(
    @field:NotBlank(message = "Tenant name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String,

    @field:NotBlank(message = "Arabic name is required")
    @field:Size(min = 3, max = 100, message = "Arabic name must be between 3 and 100 characters")
    val nameArabic: String,

    @field:NotBlank(message = "Slug is required")
    @field:Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens only")
    @field:Size(min = 3, max = 50, message = "Slug must be between 3 and 50 characters")
    val slug: String,

    val businessType: BusinessType,

    val plan: TenantSubscriptionPlan,

    val billingCycle: BillingCycle,

    val startWithTrial: Boolean = true,

    val contactInfo: TenantContactInfoRequest,

    val address: AddressRequest,

    val vatNumber: String? = null,

    val commercialRegistration: String? = null,

    @field:NotBlank(message = "Owner name is required")
    val ownerName: String,

    @field:NotBlank(message = "Owner email is required")
    @field:Email(message = "Invalid owner email format")
    val ownerEmail: String,

    @field:NotBlank(message = "Owner phone is required")
    val ownerPhone: String
)

/**
 * Request DTO for updating tenant information
 */
data class UpdateTenantRequest(
    val name: String? = null,
    val nameArabic: String? = null,
    val businessType: BusinessType? = null,
    val contactInfo: TenantContactInfoRequest? = null,
    val address: AddressRequest? = null,
    val vatNumber: String? = null,
    val commercialRegistration: String? = null,
    val logo: String? = null,
    val brandColors: BrandColorsRequest? = null
)

/**
 * Request DTO for suspending a tenant
 */
data class SuspendTenantRequest(
    @field:NotBlank(message = "Suspension reason is required")
    val reason: String
)

/**
 * Request DTO for tenant contact information
 */
data class TenantContactInfoRequest(
    @field:NotBlank(message = "Primary contact name is required")
    val primaryContactName: String,

    @field:NotBlank(message = "Primary contact email is required")
    @field:Email(message = "Invalid email format")
    val primaryContactEmail: String,

    @field:NotBlank(message = "Primary contact phone is required")
    val primaryContactPhone: String,

    @field:Email(message = "Invalid email format")
    val technicalContactEmail: String? = null,

    @field:Email(message = "Invalid email format")
    val billingContactEmail: String? = null
) {
    fun toDomain(): TenantContactInfo {
        return TenantContactInfo(
            primaryContactName = primaryContactName,
            primaryContactEmail = primaryContactEmail,
            primaryContactPhone = primaryContactPhone,
            technicalContactEmail = technicalContactEmail,
            billingContactEmail = billingContactEmail
        )
    }
}

/**
 * Request DTO for address
 */
data class AddressRequest(
    @field:NotBlank(message = "Street is required")
    val street: String,

    @field:NotBlank(message = "City is required")
    val city: String,

    @field:NotBlank(message = "State/Province is required")
    val state: String,

    @field:NotBlank(message = "Postal code is required")
    val postalCode: String,

    @field:NotBlank(message = "Country is required")
    val country: String
) {
    fun toDomain(): Address {
        return Address(
            street = street,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country
        )
    }
}

/**
 * Request DTO for brand colors
 */
data class BrandColorsRequest(
    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Primary color must be a valid hex color")
    val primaryColor: String,

    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Secondary color must be a valid hex color")
    val secondaryColor: String,

    @field:Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Accent color must be a valid hex color")
    val accentColor: String
)

/**
 * Request DTO for updating payment method
 */
data class UpdatePaymentMethodRequest(
    @field:NotBlank(message = "Payment method ID is required")
    val paymentMethodId: String
)
