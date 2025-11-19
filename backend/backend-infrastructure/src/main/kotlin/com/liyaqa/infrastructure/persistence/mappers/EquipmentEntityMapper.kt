package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.Equipment
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.infrastructure.persistence.entities.EquipmentJpaEntity
import com.liyaqa.infrastructure.persistence.entities.MoneyEmbeddable
import org.springframework.stereotype.Component
import java.util.Currency

/**
 * Mapper between Equipment domain entity and EquipmentJpaEntity.
 */
@Component
class EquipmentEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: Equipment): EquipmentJpaEntity {
        return EquipmentJpaEntity(
            id = domain.id,
            branchId = domain.branchId,
            name = domain.name,
            nameArabic = domain.nameArabic,
            category = domain.category,
            manufacturer = domain.manufacturer,
            model = domain.model,
            serialNumber = domain.serialNumber,
            purchaseDate = domain.purchaseDate,
            purchasePrice = domain.purchasePrice?.let { toEmbeddable(it) },
            warrantyExpiryDate = domain.warrantyExpiryDate,
            status = domain.status,
            location = domain.location,
            qrCode = domain.qrCode,
            notes = domain.notes,
            lastMaintenanceDate = domain.lastMaintenanceDate,
            nextMaintenanceDate = domain.nextMaintenanceDate,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: EquipmentJpaEntity): Equipment {
        return Equipment(
            id = entity.id,
            branchId = entity.branchId,
            name = entity.name,
            nameArabic = entity.nameArabic,
            category = entity.category,
            manufacturer = entity.manufacturer,
            model = entity.model,
            serialNumber = entity.serialNumber,
            purchaseDate = entity.purchaseDate,
            purchasePrice = entity.purchasePrice?.let { toDomainMoney(it) },
            warrantyExpiryDate = entity.warrantyExpiryDate,
            status = entity.status,
            location = entity.location,
            qrCode = entity.qrCode,
            notes = entity.notes,
            lastMaintenanceDate = entity.lastMaintenanceDate,
            nextMaintenanceDate = entity.nextMaintenanceDate,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEmbeddable(money: Money): MoneyEmbeddable {
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
