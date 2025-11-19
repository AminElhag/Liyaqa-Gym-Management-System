package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.AccessLog
import com.liyaqa.infrastructure.persistence.entities.AccessLogJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between AccessLog domain entity and AccessLogJpaEntity.
 */
@Component
class AccessLogEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: AccessLog): AccessLogJpaEntity {
        return AccessLogJpaEntity(
            id = domain.id,
            memberId = domain.memberId,
            branchId = domain.branchId,
            subscriptionId = domain.subscriptionId,
            checkInTime = domain.checkInTime,
            checkOutTime = domain.checkOutTime,
            accessType = domain.accessType,
            accessPoint = domain.accessPoint,
            deviceId = domain.deviceId,
            notes = domain.notes,
            validatedBy = domain.validatedBy,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: AccessLogJpaEntity): AccessLog {
        return AccessLog(
            id = entity.id,
            memberId = entity.memberId,
            branchId = entity.branchId,
            subscriptionId = entity.subscriptionId,
            checkInTime = entity.checkInTime,
            checkOutTime = entity.checkOutTime,
            accessType = entity.accessType,
            accessPoint = entity.accessPoint,
            deviceId = entity.deviceId,
            notes = entity.notes,
            validatedBy = entity.validatedBy,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
