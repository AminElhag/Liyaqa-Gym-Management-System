package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Class
import com.liyaqa.infrastructure.persistence.entities.ClassJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Class domain entity and ClassJpaEntity.
 */
@Component
class ClassEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Class): ClassJpaEntity {
        return ClassJpaEntity(
            id = domain.id,
            branchId = domain.branchId,
            name = domain.name,
            nameArabic = domain.nameArabic,
            description = domain.description,
            type = domain.type,
            level = domain.level,
            capacity = domain.capacity,
            durationMinutes = domain.durationMinutes,
            genderRestriction = domain.genderRestriction,
            imageUrl = domain.imageUrl,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: ClassJpaEntity): Class {
        return Class(
            id = entity.id,
            branchId = entity.branchId,
            name = entity.name,
            nameArabic = entity.nameArabic,
            description = entity.description,
            type = entity.type,
            level = entity.level,
            capacity = entity.capacity,
            durationMinutes = entity.durationMinutes,
            genderRestriction = entity.genderRestriction,
            imageUrl = entity.imageUrl,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
