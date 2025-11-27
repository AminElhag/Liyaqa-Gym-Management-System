package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Branch
import com.liyaqa.gym.domain.valueobjects.Address
import com.liyaqa.infrastructure.persistence.entities.AddressEmbeddable
import com.liyaqa.infrastructure.persistence.entities.BranchJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Branch domain entity and BranchJpaEntity.
 */
@Component
class BranchEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Branch): BranchJpaEntity {
        return BranchJpaEntity(
            id = domain.id,
            tenantId = domain.tenantId,
            organizationId = domain.organizationId,
            name = domain.name,
            address = toEmbeddable(domain.address),
            facilityType = domain.facilityType,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: BranchJpaEntity): Branch {
        return Branch(
            id = entity.id,
            tenantId = entity.tenantId,
            organizationId = entity.organizationId,
            name = entity.name,
            address = toDomainAddress(entity.address),
            facilityType = entity.facilityType,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEmbeddable(address: Address): AddressEmbeddable {
        return AddressEmbeddable(
            street = address.street,
            city = address.city,
            state = address.state,
            country = address.country,
            postalCode = address.postalCode
        )
    }

    private fun toDomainAddress(embeddable: AddressEmbeddable): Address {
        return Address(
            street = embeddable.street,
            city = embeddable.city,
            state = embeddable.state,
            country = embeddable.country,
            postalCode = embeddable.postalCode
        )
    }
}
