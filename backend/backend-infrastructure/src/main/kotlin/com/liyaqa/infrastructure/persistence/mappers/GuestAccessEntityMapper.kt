package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.GuestAccess
import com.liyaqa.infrastructure.persistence.entities.GuestAccessJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between GuestAccess domain entity and GuestAccessJpaEntity.
 */
@Component
class GuestAccessEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: GuestAccess): GuestAccessJpaEntity {
        return GuestAccessJpaEntity(
            id = domain.id,
            hostMemberId = domain.hostMemberId,
            branchId = domain.branchId,
            guestName = domain.guestName,
            guestPhone = domain.guestPhone,
            qrCode = domain.qrCode,
            validFrom = domain.validFrom,
            validUntil = domain.validUntil,
            isUsed = domain.isUsed,
            usedAt = domain.usedAt,
            accessLogId = domain.accessLogId,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: GuestAccessJpaEntity): GuestAccess {
        return GuestAccess(
            id = entity.id,
            hostMemberId = entity.hostMemberId,
            branchId = entity.branchId,
            guestName = entity.guestName,
            guestPhone = entity.guestPhone,
            qrCode = entity.qrCode,
            validFrom = entity.validFrom,
            validUntil = entity.validUntil,
            isUsed = entity.isUsed,
            usedAt = entity.usedAt,
            accessLogId = entity.accessLogId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
