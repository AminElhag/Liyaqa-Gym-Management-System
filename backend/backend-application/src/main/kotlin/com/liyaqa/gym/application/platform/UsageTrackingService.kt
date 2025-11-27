package com.liyaqa.gym.application.platform

import com.liyaqa.gym.domain.entities.tenant.PlatformFeature
import com.liyaqa.gym.domain.entities.tenant.TenantContextHolder
import com.liyaqa.gym.domain.entities.tenant.UsageEvent
import com.liyaqa.gym.domain.repositories.UsageEventRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service for tracking billable usage events.
 * Records usage for later billing calculations.
 */
@Service
class UsageTrackingService(
    private val usageEventRepository: UsageEventRepository
) {

    /**
     * Track an API call for a tenant.
     * Only tracks if the tenant has API_ACCESS feature enabled.
     *
     * @param tenantId The tenant making the API call
     * @param endpoint The API endpoint that was called
     */
    suspend fun trackApiCall(tenantId: UUID, endpoint: String): Result<UsageEvent> {
        return try {
            // Check if tenant has API access feature
            val tenantContext = TenantContextHolder.get()
            if (tenantContext != null && !tenantContext.hasFeature(PlatformFeature.API_ACCESS)) {
                // Tenant doesn't have API access, don't track
                return Result.failure(IllegalStateException("Tenant does not have API_ACCESS feature"))
            }

            val event = UsageEvent.createApiCall(
                tenantId = tenantId,
                endpoint = endpoint
            )

            usageEventRepository.save(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Track SMS messages sent by a tenant.
     *
     * @param tenantId The tenant sending SMS messages
     * @param recipientCount Number of recipients (each counts as one SMS)
     */
    suspend fun trackSmsMessage(tenantId: UUID, recipientCount: Int): Result<UsageEvent> {
        return try {
            require(recipientCount > 0) { "Recipient count must be positive" }

            val event = UsageEvent.createSmsMessage(
                tenantId = tenantId,
                recipientCount = recipientCount
            )

            usageEventRepository.save(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Track emails sent by a tenant.
     *
     * @param tenantId The tenant sending emails
     * @param recipientCount Number of email recipients
     */
    suspend fun trackEmailSent(tenantId: UUID, recipientCount: Int): Result<UsageEvent> {
        return try {
            require(recipientCount > 0) { "Recipient count must be positive" }

            val event = UsageEvent.createEmailSent(
                tenantId = tenantId,
                recipientCount = recipientCount
            )

            usageEventRepository.save(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Track storage usage for a tenant.
     * Records current storage size snapshot.
     *
     * @param tenantId The tenant using storage
     * @param sizeMB Storage size in megabytes
     */
    suspend fun trackStorageUsage(tenantId: UUID, sizeMB: Long): Result<UsageEvent> {
        return try {
            require(sizeMB > 0) { "Storage size must be positive" }

            val event = UsageEvent.createStorageUsage(
                tenantId = tenantId,
                sizeMB = sizeMB
            )

            usageEventRepository.save(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Track multiple usage events in batch.
     * More efficient when tracking multiple events at once.
     *
     * @param events List of usage events to track
     * @return Result containing the saved events
     */
    suspend fun trackBatch(events: List<UsageEvent>): Result<List<UsageEvent>> {
        return try {
            require(events.isNotEmpty()) { "Events list cannot be empty" }
            usageEventRepository.saveAll(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
