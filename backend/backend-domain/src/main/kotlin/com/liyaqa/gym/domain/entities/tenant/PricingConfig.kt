package com.liyaqa.gym.domain.entities.tenant

import com.liyaqa.gym.domain.valueobjects.Money
import java.time.Instant
import java.util.UUID

/**
 * PricingConfig entity representing usage-based pricing configuration.
 * Defines the pricing tiers and rates for billable usage events.
 */
data class PricingConfig(
    val id: UUID,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val smsConfig: UsagePricingTier,
    val emailConfig: UsagePricingTier,
    val apiConfig: UsagePricingTier,
    val storageConfig: UsagePricingTier,
    val effectiveFrom: Instant,
    val effectiveUntil: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Pricing config name cannot be blank" }
        effectiveUntil?.let {
            require(!it.isBefore(effectiveFrom)) {
                "Effective until cannot be before effective from"
            }
        }
    }

    fun isCurrentlyActive(): Boolean {
        val now = Instant.now()
        return isActive &&
               !effectiveFrom.isAfter(now) &&
               (effectiveUntil == null || !effectiveUntil.isBefore(now))
    }

    fun activate(): PricingConfig {
        return copy(isActive = true, updatedAt = Instant.now())
    }

    fun deactivate(): PricingConfig {
        return copy(isActive = false, updatedAt = Instant.now())
    }

    fun updatePricing(
        smsConfig: UsagePricingTier? = null,
        emailConfig: UsagePricingTier? = null,
        apiConfig: UsagePricingTier? = null,
        storageConfig: UsagePricingTier? = null
    ): PricingConfig {
        return copy(
            smsConfig = smsConfig ?: this.smsConfig,
            emailConfig = emailConfig ?: this.emailConfig,
            apiConfig = apiConfig ?: this.apiConfig,
            storageConfig = storageConfig ?: this.storageConfig,
            updatedAt = Instant.now()
        )
    }

    companion object {
        /**
         * Create default pricing configuration for Saudi Arabia (SAR).
         */
        fun createDefault(): PricingConfig {
            val now = Instant.now()
            return PricingConfig(
                id = UUID.randomUUID(),
                name = "Default Usage-Based Pricing",
                description = "Standard pricing for usage-based add-ons",
                isActive = true,
                smsConfig = UsagePricingTier(
                    freeQuota = 100,
                    pricePerUnit = Money.sar(0.10),
                    unit = "message"
                ),
                emailConfig = UsagePricingTier(
                    freeQuota = 1000,
                    pricePerUnit = Money.sar(0.01),
                    unit = "email"
                ),
                apiConfig = UsagePricingTier(
                    freeQuota = 10000,
                    pricePerUnit = Money.sar(0.001),
                    unit = "call"
                ),
                storageConfig = UsagePricingTier(
                    freeQuota = 0, // Storage is always charged per GB
                    pricePerUnit = Money.sar(0.50),
                    unit = "GB"
                ),
                effectiveFrom = now,
                effectiveUntil = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * UsagePricingTier defines pricing for a specific usage type.
 */
data class UsagePricingTier(
    val freeQuota: Int,           // Number of free units per month
    val pricePerUnit: Money,      // Price per unit after free quota
    val unit: String              // Unit name (e.g., "message", "call", "GB")
) {
    init {
        require(freeQuota >= 0) { "Free quota cannot be negative" }
        require(!pricePerUnit.isNegative()) { "Price per unit cannot be negative" }
        require(unit.isNotBlank()) { "Unit name cannot be blank" }
    }

    /**
     * Calculate charges for a given quantity based on this pricing tier.
     */
    fun calculateCharges(quantity: Int): Money {
        val billableQuantity = maxOf(0, quantity - freeQuota)
        return pricePerUnit * billableQuantity
    }

    /**
     * Check if quantity exceeds free quota.
     */
    fun exceedsFreeQuota(quantity: Int): Boolean {
        return quantity > freeQuota
    }

    /**
     * Calculate billable quantity after deducting free quota.
     */
    fun billableQuantity(quantity: Int): Int {
        return maxOf(0, quantity - freeQuota)
    }
}
