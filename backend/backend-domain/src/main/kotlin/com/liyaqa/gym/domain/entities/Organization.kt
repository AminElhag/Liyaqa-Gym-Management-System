package com.liyaqa.gym.domain.entities

import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/**
 * Organization entity representing a gym business.
 * Root entity for the multi-tenant system.
 */
data class Organization(
    val id: UUID,
    val name: String,
    val timezone: ZoneId,
    val settings: OrganizationSettings,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Organization name cannot be blank" }
    }

    fun updateSettings(newSettings: OrganizationSettings): Organization {
        return copy(settings = newSettings, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            name: String,
            timezone: ZoneId = ZoneId.of("Asia/Riyadh"),
            settings: OrganizationSettings = OrganizationSettings.default()
        ): Organization {
            val now = Instant.now()
            return Organization(
                id = UUID.randomUUID(),
                name = name,
                timezone = timezone,
                settings = settings,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Organization-level settings and preferences
 */
data class OrganizationSettings(
    val defaultCurrency: String,
    val defaultLanguage: String,
    val workingHoursStart: String, // HH:mm format
    val workingHoursEnd: String,   // HH:mm format
    val allowOnlineBooking: Boolean,
    val requireMemberApproval: Boolean,
    val enableWaitingList: Boolean,
    val maxAdvanceBookingDays: Int,
    val cancellationPolicyHours: Int,
    val enableNotifications: Boolean,
    val notificationEmail: String?
) {
    init {
        require(defaultCurrency.isNotBlank()) { "Default currency cannot be blank" }
        require(maxAdvanceBookingDays > 0) { "Max advance booking days must be positive" }
        require(cancellationPolicyHours >= 0) { "Cancellation policy hours cannot be negative" }
    }

    companion object {
        fun default(): OrganizationSettings {
            return OrganizationSettings(
                defaultCurrency = "SAR",
                defaultLanguage = "ar",
                workingHoursStart = "06:00",
                workingHoursEnd = "23:00",
                allowOnlineBooking = true,
                requireMemberApproval = false,
                enableWaitingList = true,
                maxAdvanceBookingDays = 14,
                cancellationPolicyHours = 24,
                enableNotifications = true,
                notificationEmail = null
            )
        }
    }
}
