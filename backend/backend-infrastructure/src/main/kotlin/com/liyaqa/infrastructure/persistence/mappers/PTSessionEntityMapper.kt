package com.liyaqa.infrastructure.persistence.mappers

import com.liyaqa.gym.domain.entities.PTSession
import com.liyaqa.gym.domain.valueobjects.Money
import com.liyaqa.infrastructure.persistence.entities.MoneyEmbeddable
import com.liyaqa.infrastructure.persistence.entities.PTSessionJpaEntity
import org.springframework.stereotype.Component
import java.util.Currency

/**
 * Mapper between PTSession domain entity and PTSessionJpaEntity.
 */
@Component
class PTSessionEntityMapper {

    /**
     * Convert domain entity to JPA entity.
     */
    fun toEntity(domain: PTSession): PTSessionJpaEntity {
        return PTSessionJpaEntity(
            id = domain.id,
            trainerId = domain.trainerId,
            memberId = domain.memberId,
            scheduledAt = domain.scheduledAt,
            durationMinutes = domain.durationMinutes,
            status = domain.status,
            sessionType = domain.sessionType,
            price = toEmbeddable(domain.price),
            notes = domain.notes,
            memberGoals = domain.memberGoals,
            trainerNotes = domain.trainerNotes,
            completedAt = domain.completedAt,
            cancelledAt = domain.cancelledAt,
            cancellationReason = domain.cancellationReason,
            noShowMarkedAt = domain.noShowMarkedAt,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Convert JPA entity to domain entity.
     */
    fun toDomain(entity: PTSessionJpaEntity): PTSession {
        return PTSession(
            id = entity.id,
            trainerId = entity.trainerId,
            memberId = entity.memberId,
            scheduledAt = entity.scheduledAt,
            durationMinutes = entity.durationMinutes,
            status = entity.status,
            sessionType = entity.sessionType,
            price = toDomainMoney(entity.price),
            notes = entity.notes,
            memberGoals = entity.memberGoals,
            trainerNotes = entity.trainerNotes,
            completedAt = entity.completedAt,
            cancelledAt = entity.cancelledAt,
            cancellationReason = entity.cancellationReason,
            noShowMarkedAt = entity.noShowMarkedAt,
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
