package com.liyaqa.infrastructure.persistence.mappers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.liyaqa.gym.domain.entities.tenant.TenantOnboardingProgress
import com.liyaqa.infrastructure.persistence.entities.TenantOnboardingProgressJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper for TenantOnboardingProgress domain entity and JPA entity conversions.
 */
@Component
class TenantOnboardingMapper(
    private val objectMapper: ObjectMapper
) {

    /**
     * Convert JPA entity to domain entity
     */
    fun toDomain(entity: TenantOnboardingProgressJpaEntity): TenantOnboardingProgress {
        return TenantOnboardingProgress(
            id = entity.id,
            tenantId = entity.tenantId,
            step = entity.step,
            status = entity.status,
            completedAt = entity.completedAt,
            data = entity.dataJson?.let { json ->
                try {
                    objectMapper.readValue<Map<String, Any>>(json)
                } catch (e: Exception) {
                    null
                }
            },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * Convert domain entity to JPA entity
     */
    fun toEntity(domain: TenantOnboardingProgress): TenantOnboardingProgressJpaEntity {
        return TenantOnboardingProgressJpaEntity(
            id = domain.id,
            tenantId = domain.tenantId,
            step = domain.step,
            status = domain.status,
            completedAt = domain.completedAt,
            dataJson = domain.data?.let { data ->
                try {
                    objectMapper.writeValueAsString(data)
                } catch (e: Exception) {
                    null
                }
            },
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
