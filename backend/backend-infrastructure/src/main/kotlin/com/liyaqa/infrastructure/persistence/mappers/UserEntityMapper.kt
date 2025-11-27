package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.User
import com.liyaqa.infrastructure.persistence.entities.UserJpaEntity
import org.springframework.stereotype.Component

/**
 * Mapper between User domain entity and UserJpaEntity.
 */
@Component
class UserEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: User): UserJpaEntity {
        return UserJpaEntity(
            id = domain.id,
            email = domain.email,
            passwordHash = domain.passwordHash,
            role = domain.role,
            organizationId = domain.organizationId,
            branchId = domain.branchId,
            memberId = domain.memberId,
            staffId = domain.staffId,
            isActive = domain.isActive,
            isEmailVerified = domain.isEmailVerified,
            mustChangePassword = domain.mustChangePassword,
            lastLoginAt = domain.lastLoginAt,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     * Note: User's tenantId is derived from their organizationId
     */
    fun toDomain(entity: UserJpaEntity): User {
        return User(
            id = entity.id,
            tenantId = entity.organizationId,  // User's tenant is their organization
            email = entity.email,
            passwordHash = entity.passwordHash,
            role = entity.role,
            organizationId = entity.organizationId,
            branchId = entity.branchId,
            memberId = entity.memberId,
            staffId = entity.staffId,
            isActive = entity.isActive,
            isEmailVerified = entity.isEmailVerified,
            mustChangePassword = entity.mustChangePassword,
            lastLoginAt = entity.lastLoginAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}