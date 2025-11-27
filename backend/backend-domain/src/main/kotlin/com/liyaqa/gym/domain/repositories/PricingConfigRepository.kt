package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.PricingConfig
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for PricingConfig entity operations.
 * Manages persistence of usage-based pricing configurations.
 */
interface PricingConfigRepository {

    /**
     * Find pricing config by unique identifier.
     *
     * @param id The unique identifier of the pricing config
     * @return Optional containing the config if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<PricingConfig>>

    /**
     * Find the currently active pricing configuration.
     * Returns the configuration that is active and within its effective date range.
     *
     * @return Optional containing the active pricing config if found
     */
    fun findActivePricing(): Result<Optional<PricingConfig>>

    /**
     * Find pricing configuration effective at a specific time.
     *
     * @param effectiveAt The time to check for effective pricing
     * @return Optional containing the pricing config effective at that time
     */
    fun findByEffectiveDate(effectiveAt: Instant): Result<Optional<PricingConfig>>

    /**
     * Find all active pricing configurations.
     *
     * @return List of active pricing configs
     */
    fun findAllActive(): Result<List<PricingConfig>>

    /**
     * Find all pricing configurations (active and inactive).
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return List of pricing configs ordered by creation date descending
     */
    fun findAll(page: Int = 0, size: Int = 20): Result<List<PricingConfig>>

    /**
     * Save pricing configuration (create or update).
     *
     * @param config The pricing config to save
     * @return The saved pricing config
     */
    fun save(config: PricingConfig): Result<PricingConfig>

    /**
     * Deactivate all active pricing configurations.
     * Useful before activating a new pricing config.
     *
     * @return Number of deactivated configs
     */
    fun deactivateAll(): Result<Int>

    /**
     * Check if an active pricing configuration exists.
     *
     * @return True if an active config exists, false otherwise
     */
    fun hasActivePricing(): Result<Boolean>
}
