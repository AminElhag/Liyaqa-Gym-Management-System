package com.liyaqa.gym.presentation.dto.platform

import com.liyaqa.gym.domain.entities.tenant.*
import com.liyaqa.gym.domain.valueobjects.Address
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Tenant response DTO with basic information
 */
data class TenantResponse(
    val id: UUID,
    val name: String,
    val nameArabic: String,
    val slug: String,
    val businessType: String,
    val status: String,
    val subscriptionPlan: String,
    val subscriptionStartDate: LocalDate,
    val subscriptionEndDate: LocalDate,
    val billingCycle: String,
    val contactEmail: String,
    val contactPhone: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Tenant summary response DTO for list views
 */
data class TenantSummaryResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val status: String,
    val subscriptionPlan: String,
    val subscriptionEndDate: LocalDate,
    val contactEmail: String,
    val createdAt: Instant
)

/**
 * Tenant detail response DTO with complete information
 */
data class TenantDetailResponse(
    val id: UUID,
    val name: String,
    val nameArabic: String,
    val slug: String,
    val businessType: String,
    val status: String,
    val subscriptionPlan: String,
    val subscriptionStartDate: LocalDate,
    val subscriptionEndDate: LocalDate,
    val billingCycle: String,
    val maxBranches: Int,
    val maxMembers: Int,
    val maxStaff: Int,
    val currentBranches: Int = 0,
    val currentMembers: Int = 0,
    val currentStaff: Int = 0,
    val features: List<String>,
    val contactInfo: TenantContactInfoResponse,
    val address: AddressResponse,
    val vatNumber: String?,
    val commercialRegistration: String?,
    val logo: String?,
    val brandColors: BrandColorsResponse?,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Tenant contact information response
 */
data class TenantContactInfoResponse(
    val primaryContactName: String,
    val primaryContactEmail: String,
    val primaryContactPhone: String,
    val technicalContactEmail: String?,
    val billingContactEmail: String?
)

/**
 * Address response DTO
 */
data class AddressResponse(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String
)

/**
 * Brand colors response DTO
 */
data class BrandColorsResponse(
    val primaryColor: String,
    val secondaryColor: String,
    val accentColor: String
)

/**
 * Invoice response DTO
 */
data class InvoiceResponse(
    val id: UUID,
    val tenantId: UUID,
    val invoiceNumber: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val dueDate: LocalDate,
    val paidAt: Instant?,
    val createdAt: Instant
)

/**
 * Subscription details response DTO
 */
data class SubscriptionDetailsResponse(
    val id: UUID,
    val plan: String,
    val status: String,
    val billingCycle: String,
    val amount: Double,
    val currency: String,
    val nextBillingDate: LocalDate,
    val autoRenew: Boolean,
    val trialEndsAt: LocalDate?,
    val paymentFailureCount: Int,
    val lastPaymentFailureAt: Instant?
)

/**
 * Tenant search criteria
 */
data class TenantSearchCriteria(
    val status: TenantStatus? = null,
    val plan: TenantSubscriptionPlan? = null,
    val searchTerm: String? = null
)

/**
 * Extension functions to convert domain entities to response DTOs
 */
fun Tenant.toResponse(): TenantResponse {
    return TenantResponse(
        id = this.id,
        name = this.name,
        nameArabic = this.nameArabic,
        slug = this.slug,
        businessType = this.businessType.name,
        status = this.status.name,
        subscriptionPlan = this.subscriptionPlan.displayName,
        subscriptionStartDate = this.subscriptionStartDate,
        subscriptionEndDate = this.subscriptionEndDate,
        billingCycle = this.billingCycle.name,
        contactEmail = this.contactInfo.primaryContactEmail,
        contactPhone = this.contactInfo.primaryContactPhone,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun Tenant.toSummaryResponse(): TenantSummaryResponse {
    return TenantSummaryResponse(
        id = this.id,
        name = this.name,
        slug = this.slug,
        status = this.status.name,
        subscriptionPlan = this.subscriptionPlan.displayName,
        subscriptionEndDate = this.subscriptionEndDate,
        contactEmail = this.contactInfo.primaryContactEmail,
        createdAt = this.createdAt
    )
}

fun Tenant.toDetailResponse(currentBranches: Int = 0, currentMembers: Int = 0, currentStaff: Int = 0): TenantDetailResponse {
    return TenantDetailResponse(
        id = this.id,
        name = this.name,
        nameArabic = this.nameArabic,
        slug = this.slug,
        businessType = this.businessType.name,
        status = this.status.name,
        subscriptionPlan = this.subscriptionPlan.displayName,
        subscriptionStartDate = this.subscriptionStartDate,
        subscriptionEndDate = this.subscriptionEndDate,
        billingCycle = this.billingCycle.name,
        maxBranches = this.maxBranches,
        maxMembers = this.maxMembers,
        maxStaff = this.maxStaff,
        currentBranches = currentBranches,
        currentMembers = currentMembers,
        currentStaff = currentStaff,
        features = this.features.map { it.name },
        contactInfo = this.contactInfo.toResponse(),
        address = this.address.toResponse(),
        vatNumber = this.vatNumber,
        commercialRegistration = this.commercialRegistration,
        logo = this.logo,
        brandColors = this.brandColors?.toResponse(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun TenantContactInfo.toResponse(): TenantContactInfoResponse {
    return TenantContactInfoResponse(
        primaryContactName = this.primaryContactName,
        primaryContactEmail = this.primaryContactEmail,
        primaryContactPhone = this.primaryContactPhone,
        technicalContactEmail = this.technicalContactEmail,
        billingContactEmail = this.billingContactEmail
    )
}

fun Address.toResponse(): AddressResponse {
    return AddressResponse(
        street = this.street,
        city = this.city,
        state = this.state,
        postalCode = this.postalCode,
        country = this.country
    )
}

fun BrandColors.toResponse(): BrandColorsResponse {
    return BrandColorsResponse(
        primaryColor = this.primaryColor,
        secondaryColor = this.secondaryColor,
        accentColor = this.accentColor
    )
}
