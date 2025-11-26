package com.liyaqa.gym.domain.entities.tenant

import com.liyaqa.gym.domain.valueobjects.Address
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Tenant entity representing a gym business - B2B client.
 * Root entity for multi-tenancy. Each tenant is a separate gym business using the platform.
 */
data class Tenant(
    val id: UUID,
    val name: String,
    val nameArabic: String,
    val slug: String, // unique identifier for subdomain (e.g., "gold-gym" -> gold-gym.liyaqa.com)
    val businessType: BusinessType,
    val status: TenantStatus,
    val subscriptionPlan: TenantSubscriptionPlan,
    val subscriptionStartDate: LocalDate,
    val subscriptionEndDate: LocalDate,
    val billingCycle: BillingCycle,
    val maxBranches: Int,
    val maxMembers: Int,
    val maxStaff: Int,
    val features: Set<PlatformFeature>, // enabled features for this tenant
    val contactInfo: TenantContactInfo,
    val address: Address,
    val vatNumber: String?,
    val commercialRegistration: String?,
    val logo: String?,
    val brandColors: BrandColors?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: UUID, // platform admin who created this tenant
    val isDeleted: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Tenant name cannot be blank" }
        require(nameArabic.isNotBlank()) { "Tenant Arabic name cannot be blank" }
        require(slug.isNotBlank()) { "Tenant slug cannot be blank" }
        require(slug.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$"))) {
            "Slug must be lowercase alphanumeric with hyphens only"
        }
        require(maxBranches > 0) { "Max branches must be positive" }
        require(maxMembers > 0) { "Max members must be positive" }
        require(maxStaff > 0) { "Max staff must be positive" }
        require(!subscriptionEndDate.isBefore(subscriptionStartDate)) {
            "Subscription end date cannot be before start date"
        }
        vatNumber?.let {
            require(it.isNotBlank()) { "VAT number cannot be blank if provided" }
        }
        commercialRegistration?.let {
            require(it.isNotBlank()) { "Commercial registration cannot be blank if provided" }
        }
    }

    fun isActive(): Boolean = status == TenantStatus.ACTIVE

    fun isTrial(): Boolean = status == TenantStatus.TRIAL

    fun isSuspended(): Boolean = status == TenantStatus.SUSPENDED

    fun isCancelled(): Boolean = status == TenantStatus.CANCELLED

    fun isSubscriptionExpired(): Boolean = LocalDate.now().isAfter(subscriptionEndDate)

    fun hasFeature(feature: PlatformFeature): Boolean = features.contains(feature)

    fun canCreateBranches(currentCount: Int): Boolean = currentCount < maxBranches

    fun canAddMembers(currentCount: Int): Boolean = currentCount < maxMembers

    fun canAddStaff(currentCount: Int): Boolean = currentCount < maxStaff

    fun activate(): Tenant {
        require(status == TenantStatus.TRIAL || status == TenantStatus.SUSPENDED) {
            "Only trial or suspended tenants can be activated"
        }
        return copy(status = TenantStatus.ACTIVE, updatedAt = Instant.now())
    }

    fun suspend(reason: String? = null): Tenant {
        require(status == TenantStatus.ACTIVE || status == TenantStatus.TRIAL) {
            "Only active or trial tenants can be suspended"
        }
        return copy(status = TenantStatus.SUSPENDED, updatedAt = Instant.now())
    }

    fun cancel(): Tenant {
        require(!isCancelled()) { "Tenant is already cancelled" }
        return copy(status = TenantStatus.CANCELLED, updatedAt = Instant.now())
    }

    fun upgradePlan(newPlan: TenantSubscriptionPlan): Tenant {
        require(newPlan.ordinal > subscriptionPlan.ordinal) {
            "New plan must be higher tier than current plan"
        }
        return copy(
            subscriptionPlan = newPlan,
            maxBranches = newPlan.defaultMaxBranches,
            maxMembers = newPlan.defaultMaxMembers,
            maxStaff = newPlan.defaultMaxStaff,
            features = features + newPlan.includedFeatures,
            updatedAt = Instant.now()
        )
    }

    fun downgradePlan(newPlan: TenantSubscriptionPlan): Tenant {
        require(newPlan.ordinal < subscriptionPlan.ordinal) {
            "New plan must be lower tier than current plan"
        }
        return copy(
            subscriptionPlan = newPlan,
            maxBranches = newPlan.defaultMaxBranches,
            maxMembers = newPlan.defaultMaxMembers,
            maxStaff = newPlan.defaultMaxStaff,
            features = newPlan.includedFeatures,
            updatedAt = Instant.now()
        )
    }

    fun renewSubscription(endDate: LocalDate): Tenant {
        return copy(
            subscriptionEndDate = endDate,
            status = TenantStatus.ACTIVE,
            updatedAt = Instant.now()
        )
    }

    fun updateContactInfo(newContactInfo: TenantContactInfo): Tenant {
        return copy(contactInfo = newContactInfo, updatedAt = Instant.now())
    }

    fun updateBrandColors(newBrandColors: BrandColors): Tenant {
        return copy(brandColors = newBrandColors, updatedAt = Instant.now())
    }

    fun updateLogo(logoUrl: String): Tenant {
        return copy(logo = logoUrl, updatedAt = Instant.now())
    }

    fun softDelete(): Tenant {
        return copy(isDeleted = true, status = TenantStatus.CANCELLED, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            name: String,
            nameArabic: String,
            slug: String,
            businessType: BusinessType,
            subscriptionPlan: TenantSubscriptionPlan,
            billingCycle: BillingCycle,
            contactInfo: TenantContactInfo,
            address: Address,
            vatNumber: String?,
            commercialRegistration: String?,
            createdBy: UUID
        ): Tenant {
            val now = Instant.now()
            val today = LocalDate.now()
            val subscriptionEndDate = when (billingCycle) {
                BillingCycle.MONTHLY -> today.plusMonths(1)
                BillingCycle.QUARTERLY -> today.plusMonths(3)
                BillingCycle.ANNUAL -> today.plusYears(1)
            }

            return Tenant(
                id = UUID.randomUUID(),
                name = name,
                nameArabic = nameArabic,
                slug = slug,
                businessType = businessType,
                status = TenantStatus.TRIAL,
                subscriptionPlan = subscriptionPlan,
                subscriptionStartDate = today,
                subscriptionEndDate = today.plusDays(14), // 14-day trial
                billingCycle = billingCycle,
                maxBranches = subscriptionPlan.defaultMaxBranches,
                maxMembers = subscriptionPlan.defaultMaxMembers,
                maxStaff = subscriptionPlan.defaultMaxStaff,
                features = subscriptionPlan.includedFeatures,
                contactInfo = contactInfo,
                address = address,
                vatNumber = vatNumber,
                commercialRegistration = commercialRegistration,
                logo = null,
                brandColors = null,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy,
                isDeleted = false
            )
        }

        fun createEnterprise(
            name: String,
            nameArabic: String,
            slug: String,
            businessType: BusinessType,
            contactInfo: TenantContactInfo,
            address: Address,
            vatNumber: String?,
            commercialRegistration: String?,
            maxBranches: Int,
            maxMembers: Int,
            maxStaff: Int,
            features: Set<PlatformFeature>,
            createdBy: UUID
        ): Tenant {
            val now = Instant.now()
            val today = LocalDate.now()

            return Tenant(
                id = UUID.randomUUID(),
                name = name,
                nameArabic = nameArabic,
                slug = slug,
                businessType = businessType,
                status = TenantStatus.PENDING,
                subscriptionPlan = TenantSubscriptionPlan.ENTERPRISE,
                subscriptionStartDate = today,
                subscriptionEndDate = today.plusYears(1),
                billingCycle = BillingCycle.ANNUAL,
                maxBranches = maxBranches,
                maxMembers = maxMembers,
                maxStaff = maxStaff,
                features = features,
                contactInfo = contactInfo,
                address = address,
                vatNumber = vatNumber,
                commercialRegistration = commercialRegistration,
                logo = null,
                brandColors = null,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy,
                isDeleted = false
            )
        }
    }
}

/**
 * Tenant status representing the current state of the tenant subscription
 */
enum class TenantStatus {
    TRIAL,        // 14-day trial period
    ACTIVE,       // Paying customer with active subscription
    SUSPENDED,    // Payment failed or violation
    CANCELLED,    // Subscription ended
    PENDING       // Awaiting setup completion
}

/**
 * Tenant subscription plans with different pricing tiers
 */
enum class TenantSubscriptionPlan(
    val displayName: String,
    val defaultMaxBranches: Int,
    val defaultMaxMembers: Int,
    val defaultMaxStaff: Int,
    val monthlyPriceSAR: Int,
    val includedFeatures: Set<PlatformFeature>
) {
    STARTER(
        displayName = "Starter",
        defaultMaxBranches = 1,
        defaultMaxMembers = 500,
        defaultMaxStaff = 10,
        monthlyPriceSAR = 500,
        includedFeatures = setOf(
            PlatformFeature.BASIC_MEMBERSHIP,
            PlatformFeature.CLASS_BOOKING,
            PlatformFeature.ACCESS_CONTROL,
            PlatformFeature.FINANCIAL_REPORTS
        )
    ),
    PROFESSIONAL(
        displayName = "Professional",
        defaultMaxBranches = 3,
        defaultMaxMembers = 2000,
        defaultMaxStaff = 30,
        monthlyPriceSAR = 1500,
        includedFeatures = setOf(
            PlatformFeature.BASIC_MEMBERSHIP,
            PlatformFeature.CLASS_BOOKING,
            PlatformFeature.TRAINER_MANAGEMENT,
            PlatformFeature.ACCESS_CONTROL,
            PlatformFeature.FINANCIAL_REPORTS,
            PlatformFeature.ADVANCED_ANALYTICS,
            PlatformFeature.MULTI_BRANCH,
            PlatformFeature.ZATCA_INTEGRATION
        )
    ),
    ENTERPRISE(
        displayName = "Enterprise",
        defaultMaxBranches = 999,
        defaultMaxMembers = 999999,
        defaultMaxStaff = 999,
        monthlyPriceSAR = 0, // Custom pricing
        includedFeatures = PlatformFeature.entries.toSet() // All features
    )
}

/**
 * Platform features that can be enabled/disabled for tenants
 */
enum class PlatformFeature {
    BASIC_MEMBERSHIP,        // Basic member registration and management
    CLASS_BOOKING,           // Group class booking and scheduling
    TRAINER_MANAGEMENT,      // Personal trainer management and PT sessions
    ACCESS_CONTROL,          // QR code access control and check-in
    FINANCIAL_REPORTS,       // Basic financial reporting
    ADVANCED_ANALYTICS,      // Advanced analytics and insights
    CUSTOM_BRANDING,         // Custom logos and brand colors
    API_ACCESS,              // REST API access for integrations
    WHATSAPP_INTEGRATION,    // WhatsApp notifications
    MULTI_BRANCH,            // Multiple branch management
    ZATCA_INTEGRATION        // Saudi ZATCA e-invoicing compliance
}

/**
 * Billing cycle for tenant subscriptions
 */
enum class BillingCycle {
    MONTHLY,
    QUARTERLY,
    ANNUAL
}

/**
 * Business type for gym categorization
 */
enum class BusinessType {
    GYM,             // Traditional gym
    FITNESS_CENTER,  // Fitness and wellness center
    SPORTS_CLUB      // Sports club with multiple facilities
}

/**
 * Tenant contact information
 */
data class TenantContactInfo(
    val primaryContactName: String,
    val primaryContactEmail: String,
    val primaryContactPhone: String,
    val technicalContactEmail: String?,
    val billingContactEmail: String?
) {
    init {
        require(primaryContactName.isNotBlank()) { "Primary contact name cannot be blank" }
        require(primaryContactEmail.isNotBlank()) { "Primary contact email cannot be blank" }
        require(primaryContactPhone.isNotBlank()) { "Primary contact phone cannot be blank" }
        require(primaryContactEmail.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
            "Invalid primary contact email format"
        }
        technicalContactEmail?.let {
            require(it.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
                "Invalid technical contact email format"
            }
        }
        billingContactEmail?.let {
            require(it.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
                "Invalid billing contact email format"
            }
        }
    }
}

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
                primaryColor = "#1E40AF",
                secondaryColor = "#64748B",
                accentColor = "#10B981"
            )
        }
    }
}
