package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.BranchCapacity
import com.liyaqa.infrastructure.persistence.entities.BranchCapacityJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between BranchCapacity domain entity and BranchCapacityJpaEntity.
 */
@Component
class BranchCapacityEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: BranchCapacity): BranchCapacityJpaEntity {
        return BranchCapacityJpaEntity(
            branchId = domain.branchId,
            maxCapacity = domain.maxCapacity,
            currentOccupancy = domain.currentOccupancy,
            maleCount = domain.maleCount,
            femaleCount = domain.femaleCount,
            lastUpdated = domain.lastUpdated
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: BranchCapacityJpaEntity): BranchCapacity {
        return BranchCapacity(
            branchId = entity.branchId,
            maxCapacity = entity.maxCapacity,
            currentOccupancy = entity.currentOccupancy,
            maleCount = entity.maleCount,
            femaleCount = entity.femaleCount,
            lastUpdated = entity.lastUpdated
        )
    }
}
