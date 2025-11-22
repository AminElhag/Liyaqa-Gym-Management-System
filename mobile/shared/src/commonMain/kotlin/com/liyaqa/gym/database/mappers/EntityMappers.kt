package com.liyaqa.gym.database.mappers

import com.liyaqa.gym.database.*
import com.liyaqa.gym.domain.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Extension functions to map database entities to domain models
 */

/**
 * Convert SubscriptionEntity to Subscription domain model
 */
fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        id = id,
        memberId = memberId,
        planId = planId,
        planName = planName,
        startDate = LocalDate.parse(startDate),
        endDate = endDate?.let { LocalDate.parse(it) },
        status = SubscriptionStatus.valueOf(status),
        autoRenew = autoRenew != 0L,
        remainingVisits = remainingVisits?.toInt(),
        pausedAt = pausedAt?.let { LocalDate.parse(it) },
        pausedUntil = pausedUntil?.let { LocalDate.parse(it) },
        cancelledAt = cancelledAt?.let { Instant.parse(it) },
        cancellationReason = cancellationReason,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * Convert Subscription domain model to SubscriptionEntity
 */
fun Subscription.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        id = id,
        memberId = memberId,
        planId = planId,
        planName = planName,
        startDate = startDate.toString(),
        endDate = endDate?.toString(),
        status = status.name,
        autoRenew = if (autoRenew) 1L else 0L,
        remainingVisits = remainingVisits?.toLong(),
        pausedAt = pausedAt?.toString(),
        pausedUntil = pausedUntil?.toString(),
        cancelledAt = cancelledAt?.toString(),
        cancellationReason = cancellationReason,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        cachedAt = Clock.System.now().toString()
    )
}
