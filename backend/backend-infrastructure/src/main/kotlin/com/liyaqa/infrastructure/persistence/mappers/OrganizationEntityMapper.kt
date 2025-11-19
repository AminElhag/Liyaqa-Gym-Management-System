package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Organization
import com.liyaqa.gym.domain.entities.OrganizationSettings
import com.liyaqa.infrastructure.persistence.entities.OrganizationJpaEntity
import com.liyaqa.infrastructure.persistence.entities.OrganizationSettingsEmbeddable
import org.springframework.stereotype.Component
import java.time.ZoneId

/**
 * Mapper between Organization domain entity and OrganizationJpaEntity.
 */
@Component
class OrganizationEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Organization): OrganizationJpaEntity {
        return OrganizationJpaEntity(
            id = domain.id,
            name = domain.name,
            timezone = domain.timezone.id,
            settings = toEmbeddable(domain.settings),
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: OrganizationJpaEntity): Organization {
        return Organization(
            id = entity.id,
            name = entity.name,
            timezone = ZoneId.of(entity.timezone),
            settings = toDomainSettings(entity.settings),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEmbeddable(settings: OrganizationSettings): OrganizationSettingsEmbeddable {
        return OrganizationSettingsEmbeddable(
            defaultCurrency = settings.defaultCurrency,
            defaultLanguage = settings.defaultLanguage,
            workingHoursStart = settings.workingHoursStart,
            workingHoursEnd = settings.workingHoursEnd,
            allowOnlineBooking = settings.allowOnlineBooking,
            requireMemberApproval = settings.requireMemberApproval,
            enableWaitingList = settings.enableWaitingList,
            maxAdvanceBookingDays = settings.maxAdvanceBookingDays,
            cancellationPolicyHours = settings.cancellationPolicyHours,
            enableNotifications = settings.enableNotifications,
            notificationEmail = settings.notificationEmail
        )
    }

    private fun toDomainSettings(embeddable: OrganizationSettingsEmbeddable): OrganizationSettings {
        return OrganizationSettings(
            defaultCurrency = embeddable.defaultCurrency,
            defaultLanguage = embeddable.defaultLanguage,
            workingHoursStart = embeddable.workingHoursStart,
            workingHoursEnd = embeddable.workingHoursEnd,
            allowOnlineBooking = embeddable.allowOnlineBooking,
            requireMemberApproval = embeddable.requireMemberApproval,
            enableWaitingList = embeddable.enableWaitingList,
            maxAdvanceBookingDays = embeddable.maxAdvanceBookingDays,
            cancellationPolicyHours = embeddable.cancellationPolicyHours,
            enableNotifications = embeddable.enableNotifications,
            notificationEmail = embeddable.notificationEmail
        )
    }
}
