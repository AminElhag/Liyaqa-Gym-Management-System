package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.ZoneAccessLog
import com.liyaqa.infrastructure.persistence.entities.ZoneAccessLogJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between ZoneAccessLog domain entity and ZoneAccessLogJpaEntity.
 */
@Component
class ZoneAccessLogEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: ZoneAccessLog): ZoneAccessLogJpaEntity {
        return ZoneAccessLogJpaEntity(
            id = domain.id,
            accessLogId = domain.accessLogId,
            zoneId = domain.zoneId,
            memberId = domain.memberId,
            entryTime = domain.entryTime,
            exitTime = domain.exitTime,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: ZoneAccessLogJpaEntity): ZoneAccessLog {
        return ZoneAccessLog(
            id = entity.id,
            accessLogId = entity.accessLogId,
            zoneId = entity.zoneId,
            memberId = entity.memberId,
            entryTime = entity.entryTime,
            exitTime = entity.exitTime,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
