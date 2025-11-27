package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.valueobjects.ContactInfo
import com.liyaqa.infrastructure.persistence.entities.ContactInfoEmbeddable
import com.liyaqa.infrastructure.persistence.entities.MemberJpaEntity
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Mapper between Member domain entity and MemberJpaEntity.
 */
@Component
class MemberEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Member): MemberJpaEntity {
        return MemberJpaEntity(
            id = domain.id,
            tenantId = domain.tenantId,
            organizationId = domain.organizationId,
            branchId = domain.branchId,
            name = domain.name,
            nameArabic = domain.nameArabic,
            contactInfo = toEmbeddable(domain.contactInfo),
            nationalId = domain.nationalId,
            gender = domain.gender,
            dateOfBirth = domain.dateOfBirth,
            status = domain.status,
            profilePhotoUrl = domain.profilePhotoUrl,
            emergencyContactName = domain.emergencyContactName,
            emergencyContactPhone = domain.emergencyContactPhone,
            notes = domain.notes,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: MemberJpaEntity): Member {
        return Member(
            id = entity.id,
            tenantId = entity.tenantId,
            organizationId = entity.organizationId,
            branchId = entity.branchId,
            name = entity.name,
            nameArabic = entity.nameArabic,
            contactInfo = toDomainContactInfo(entity.contactInfo),
            nationalId = entity.nationalId,
            gender = entity.gender,
            dateOfBirth = entity.dateOfBirth,
            status = entity.status,
            profilePhotoUrl = entity.profilePhotoUrl,
            emergencyContactName = entity.emergencyContactName,
            emergencyContactPhone = entity.emergencyContactPhone,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEmbeddable(contactInfo: ContactInfo): ContactInfoEmbeddable {
        return ContactInfoEmbeddable(
            email = contactInfo.email,
            phone = contactInfo.phone
        )
    }

    private fun toDomainContactInfo(embeddable: ContactInfoEmbeddable): ContactInfo {
        return ContactInfo(
            email = embeddable.email,
            phone = embeddable.phone
        )
    }
}
