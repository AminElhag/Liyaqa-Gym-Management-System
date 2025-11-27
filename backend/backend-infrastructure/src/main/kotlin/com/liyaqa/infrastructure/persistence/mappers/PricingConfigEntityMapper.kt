package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.tenant.PricingConfig
import com.liyaqa.gym.domain.entities.tenant.UsagePricingTier
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.infrastructure.persistence.entities.PricingConfigJpaEntity
import org.springframework.stereotype.Component
import java.util.Currency

/**
 * Mapper between PricingConfig domain entity and PricingConfigJpaEntity.
 */
@Component
class PricingConfigEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: PricingConfig): PricingConfigJpaEntity {
        return PricingConfigJpaEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            isActive = domain.isActive,
            // SMS pricing
            smsFreeQuota = domain.smsConfig.freeQuota,
            smsPricePerUnit = domain.smsConfig.pricePerUnit.amount,
            smsUnit = domain.smsConfig.unit,
            smsCurrency = domain.smsConfig.pricePerUnit.currency.currencyCode,
            // Email pricing
            emailFreeQuota = domain.emailConfig.freeQuota,
            emailPricePerUnit = domain.emailConfig.pricePerUnit.amount,
            emailUnit = domain.emailConfig.unit,
            emailCurrency = domain.emailConfig.pricePerUnit.currency.currencyCode,
            // API pricing
            apiFreeQuota = domain.apiConfig.freeQuota,
            apiPricePerUnit = domain.apiConfig.pricePerUnit.amount,
            apiUnit = domain.apiConfig.unit,
            apiCurrency = domain.apiConfig.pricePerUnit.currency.currencyCode,
            // Storage pricing
            storageFreeQuota = domain.storageConfig.freeQuota,
            storagePricePerUnit = domain.storageConfig.pricePerUnit.amount,
            storageUnit = domain.storageConfig.unit,
            storageCurrency = domain.storageConfig.pricePerUnit.currency.currencyCode,
            effectiveFrom = domain.effectiveFrom,
            effectiveUntil = domain.effectiveUntil,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: PricingConfigJpaEntity): PricingConfig {
        return PricingConfig(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            isActive = entity.isActive,
            smsConfig = UsagePricingTier(
                freeQuota = entity.smsFreeQuota,
                pricePerUnit = Money(entity.smsPricePerUnit, Currency.getInstance(entity.smsCurrency)),
                unit = entity.smsUnit
            ),
            emailConfig = UsagePricingTier(
                freeQuota = entity.emailFreeQuota,
                pricePerUnit = Money(entity.emailPricePerUnit, Currency.getInstance(entity.emailCurrency)),
                unit = entity.emailUnit
            ),
            apiConfig = UsagePricingTier(
                freeQuota = entity.apiFreeQuota,
                pricePerUnit = Money(entity.apiPricePerUnit, Currency.getInstance(entity.apiCurrency)),
                unit = entity.apiUnit
            ),
            storageConfig = UsagePricingTier(
                freeQuota = entity.storageFreeQuota,
                pricePerUnit = Money(entity.storagePricePerUnit, Currency.getInstance(entity.storageCurrency)),
                unit = entity.storageUnit
            ),
            effectiveFrom = entity.effectiveFrom,
            effectiveUntil = entity.effectiveUntil,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * Convert list of JPA entities to domain entities.
     */
    fun toDomainList(entities: List<PricingConfigJpaEntity>): List<PricingConfig> {
        return entities.map { toDomain(it) }
    }

    /**
     * Convert list of domain entities to JPA entities.
     */
    fun toEntityList(domains: List<PricingConfig>): List<PricingConfigJpaEntity> {
        return domains.map { toEntity(it) }
    }
}
