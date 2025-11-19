package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.MembershipPlan
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.infrastructure.persistence.entities.MembershipPlanJpaEntity
import com.liyaqa.infrastructure.persistence.entities.MoneyEmbeddable
import org.springframework.stereotype.Component
import java.util.Currency

/**
 * Mapper between MembershipPlan domain entity and MembershipPlanJpaEntity.
 */
@Component
class MembershipPlanEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: MembershipPlan): MembershipPlanJpaEntity {
        return MembershipPlanJpaEntity(
            id = domain.id,
            branchId = domain.branchId,
            name = domain.name,
            description = domain.description,
            type = domain.type,
            price = toEmbeddable(domain.price),
            durationDays = domain.durationDays,
            visitCount = domain.visitCount,
            allowedTimeSlots = domain.allowedTimeSlots,
            features = domain.features,
            isActive = domain.isActive,
            maxActiveSubscriptions = domain.maxActiveSubscriptions,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: MembershipPlanJpaEntity): MembershipPlan {
        return MembershipPlan(
            id = entity.id,
            branchId = entity.branchId,
            name = entity.name,
            description = entity.description,
            type = entity.type,
            price = toDomainMoney(entity.price),
            durationDays = entity.durationDays,
            visitCount = entity.visitCount,
            allowedTimeSlots = entity.allowedTimeSlots,
            features = entity.features,
            isActive = entity.isActive,
            maxActiveSubscriptions = entity.maxActiveSubscriptions,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEmbeddable(money: Money): MoneyEmbeddable {
        return MoneyEmbeddable(
            amount = money.amount,
            currency = money.currency.currencyCode
        )
    }

    fun toDomainMoney(embeddable: MoneyEmbeddable): Money {
        return Money(
            amount = embeddable.amount,
            currency = Currency.getInstance(embeddable.currency)
        )
    }
}
