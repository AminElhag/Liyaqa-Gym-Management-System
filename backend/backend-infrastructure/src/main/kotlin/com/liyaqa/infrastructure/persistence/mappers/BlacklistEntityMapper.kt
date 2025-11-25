package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Blacklist
import com.liyaqa.infrastructure.persistence.entities.BlacklistJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Blacklist domain entity and BlacklistJpaEntity.
 */
@Component
class BlacklistEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Blacklist): BlacklistJpaEntity {
        return BlacklistJpaEntity(
            id = domain.id,
            memberId = domain.memberId,
            branchId = domain.branchId,
            reason = domain.reason,
            blacklistedAt = domain.blacklistedAt,
            blacklistedBy = domain.blacklistedBy,
            expiresAt = domain.expiresAt,
            notes = domain.notes,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: BlacklistJpaEntity): Blacklist {
        return Blacklist(
            id = entity.id,
            memberId = entity.memberId,
            branchId = entity.branchId,
            reason = entity.reason,
            blacklistedAt = entity.blacklistedAt,
            blacklistedBy = entity.blacklistedBy,
            expiresAt = entity.expiresAt,
            notes = entity.notes,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
