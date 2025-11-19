package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Certification
import com.liyaqa.gym.domain.entities.Trainer
import com.liyaqa.gym.domain.valueobjects.ContactInfo
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.infrastructure.persistence.entities.CertificationEmbeddable
import com.liyaqa.infrastructure.persistence.entities.ContactInfoEmbeddable
import com.liyaqa.infrastructure.persistence.entities.MoneyEmbeddable
import com.liyaqa.infrastructure.persistence.entities.TrainerJpaEntity
import org.springframework.stereotype.Component
import java.util.Currency

/**
 * Mapper between Trainer domain entity and TrainerJpaEntity.
 */
@Component
class TrainerEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Trainer): TrainerJpaEntity {
        return TrainerJpaEntity(
            id = domain.id,
            branchId = domain.branchId,
            name = domain.name,
            nameArabic = domain.nameArabic,
            contactInfo = toContactInfoEmbeddable(domain.contactInfo),
            specializations = domain.specializations.toMutableSet(),
            certifications = domain.certifications.map { toCertificationEmbeddable(it) },
            hourlyRate = toMoneyEmbeddable(domain.hourlyRate),
            biography = domain.biography,
            photoUrl = domain.photoUrl,
            hireDate = domain.hireDate,
            status = domain.status,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: TrainerJpaEntity): Trainer {
        return Trainer(
            id = entity.id,
            branchId = entity.branchId,
            name = entity.name,
            nameArabic = entity.nameArabic,
            contactInfo = toDomainContactInfo(entity.contactInfo),
            specializations = entity.specializations.toSet(),
            certifications = entity.certifications.map { toDomainCertification(it) },
            hourlyRate = toDomainMoney(entity.hourlyRate),
            biography = entity.biography,
            photoUrl = entity.photoUrl,
            hireDate = entity.hireDate,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toContactInfoEmbeddable(contactInfo: ContactInfo): ContactInfoEmbeddable {
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

    private fun toCertificationEmbeddable(certification: Certification): CertificationEmbeddable {
        return CertificationEmbeddable(
            name = certification.name,
            issuingOrganization = certification.issuingOrganization,
            issueDate = certification.issueDate,
            expiryDate = certification.expiryDate,
            certificateNumber = certification.certificateNumber
        )
    }

    private fun toDomainCertification(embeddable: CertificationEmbeddable): Certification {
        return Certification(
            name = embeddable.name,
            issuingOrganization = embeddable.issuingOrganization,
            issueDate = embeddable.issueDate,
            expiryDate = embeddable.expiryDate,
            certificateNumber = embeddable.certificateNumber
        )
    }

    private fun toMoneyEmbeddable(money: Money): MoneyEmbeddable {
        return MoneyEmbeddable(
            amount = money.amount,
            currency = money.currency.currencyCode
        )
    }

    private fun toDomainMoney(embeddable: MoneyEmbeddable): Money {
        return Money(
            amount = embeddable.amount,
            currency = Currency.getInstance(embeddable.currency)
        )
    }
}
