package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Zone
import com.liyaqa.infrastructure.persistence.entities.ZoneJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Zone domain entity and ZoneJpaEntity.
 */
@Component
class ZoneEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Zone): ZoneJpaEntity {
        return ZoneJpaEntity(
            id = domain.id,
            branchId = domain.branchId,
            name = domain.name,
            description = domain.description,
            requiredFeatures = domain.requiredFeatures,
            capacity = domain.capacity,
            genderRestriction = domain.genderRestriction,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: ZoneJpaEntity): Zone {
        return Zone(
            id = entity.id,
            branchId = entity.branchId,
            name = entity.name,
            description = entity.description,
            requiredFeatures = entity.requiredFeatures,
            capacity = entity.capacity,
            genderRestriction = entity.genderRestriction,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
