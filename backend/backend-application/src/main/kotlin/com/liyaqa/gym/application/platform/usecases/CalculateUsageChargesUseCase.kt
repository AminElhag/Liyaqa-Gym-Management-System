package com.liyaqa.gym.application.platform.usecases

import com.liyaqa.gym.domain.entities.tenant.PricingConfig
import com.liyaqa.gym.domain.entities.tenant.UsageEventType
import com.liyaqa.gym.domain.exceptions.NoPricingConfigException
import com.liyaqa.gym.domain.repositories.PricingConfigRepository
import com.liyaqa.gym.domain.repositories.UsageEventRepository
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.gym.domain.valueobjects.UsageCharges
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.util.UUID

/**
 * Use case for calculating usage-based charges for a tenant.
 * Aggregates usage events for a billing period and calculates charges based on pricing configuration.
 */
@Service
class CalculateUsageChargesUseCase(
    private val usageEventRepository: UsageEventRepository,
    private val pricingConfigRepository: PricingConfigRepository
) {

    /**
     * Calculate usage charges for a tenant and billing period.
     *
     * @param tenantId The tenant identifier
     * @param period The billing period (year-month)
     * @return UsageCharges containing breakdown and total charges
     */
    suspend fun execute(tenantId: UUID, period: YearMonth): Result<UsageCharges> {
        return try {
            // Fetch all usage events for the period
            val events = usageEventRepository.findByTenantAndPeriod(tenantId, period).getOrThrow()

            // Get active pricing configuration
            val pricingOpt = pricingConfigRepository.findActivePricing().getOrThrow()
            if (!pricingOpt.isPresent) {
                return Result.failure(NoPricingConfigException())
            }
            val pricing = pricingOpt.get()

            // Aggregate usage by type
            val smsCount = events
                .filter { it.eventType == UsageEventType.SMS_MESSAGE }
                .sumOf { it.quantity }

            val emailCount = events
                .filter { it.eventType == UsageEventType.EMAIL_SENT }
                .sumOf { it.quantity }

            val apiCallsCount = events
                .filter { it.eventType == UsageEventType.API_CALL }
                .size

            // For storage, take the maximum value (peak usage)
            val storageMaxMB = events
                .filter { it.eventType == UsageEventType.STORAGE_USAGE }
                .maxOfOrNull { it.quantity } ?: 0

            // Convert MB to GB for billing (round up)
            val storageGB = (storageMaxMB + 1023) / 1024

            // Calculate charges using pricing tiers
            val smsCharges = calculateSmsCharges(smsCount, pricing)
            val emailCharges = calculateEmailCharges(emailCount, pricing)
            val apiCharges = calculateApiCharges(apiCallsCount, pricing)
            val storageCharges = calculateStorageCharges(storageGB, pricing)

            // Create usage charges result
            val usageCharges = UsageCharges.create(
                smsCount = smsCount,
                smsCharges = smsCharges,
                emailCount = emailCount,
                emailCharges = emailCharges,
                apiCallsCount = apiCallsCount,
                apiCharges = apiCharges,
                storageGB = storageGB,
                storageCharges = storageCharges
            )

            Result.success(usageCharges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate SMS charges based on pricing config.
     */
    private fun calculateSmsCharges(count: Int, pricing: PricingConfig): Money {
        return pricing.smsConfig.calculateCharges(count)
    }

    /**
     * Calculate email charges based on pricing config.
     */
    private fun calculateEmailCharges(count: Int, pricing: PricingConfig): Money {
        return pricing.emailConfig.calculateCharges(count)
    }

    /**
     * Calculate API call charges based on pricing config.
     */
    private fun calculateApiCharges(count: Int, pricing: PricingConfig): Money {
        return pricing.apiConfig.calculateCharges(count)
    }

    /**
     * Calculate storage charges based on pricing config.
     */
    private fun calculateStorageCharges(storageGB: Int, pricing: PricingConfig): Money {
        return pricing.storageConfig.calculateCharges(storageGB)
    }

    /**
     * Get usage summary for a tenant without calculating charges.
     * Useful for displaying usage statistics.
     *
     * @param tenantId The tenant identifier
     * @param period The billing period
     * @return Map of usage type to quantity
     */
    suspend fun getUsageSummary(tenantId: UUID, period: YearMonth): Result<Map<UsageEventType, Int>> {
        return try {
            val events = usageEventRepository.findByTenantAndPeriod(tenantId, period).getOrThrow()

            val summary = mutableMapOf<UsageEventType, Int>()
            summary[UsageEventType.SMS_MESSAGE] = events
                .filter { it.eventType == UsageEventType.SMS_MESSAGE }
                .sumOf { it.quantity }

            summary[UsageEventType.EMAIL_SENT] = events
                .filter { it.eventType == UsageEventType.EMAIL_SENT }
                .sumOf { it.quantity }

            summary[UsageEventType.API_CALL] = events
                .filter { it.eventType == UsageEventType.API_CALL }
                .size

            summary[UsageEventType.STORAGE_USAGE] = events
                .filter { it.eventType == UsageEventType.STORAGE_USAGE }
                .maxOfOrNull { it.quantity } ?: 0

            Result.success(summary.toMap())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
