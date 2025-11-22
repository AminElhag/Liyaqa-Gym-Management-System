package com.liyaqa.gym.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.database.SubscriptionEntity
import com.liyaqa.gym.database.mappers.toDomain
import com.liyaqa.gym.database.mappers.toEntity
import com.liyaqa.gym.domain.Subscription
import com.liyaqa.gym.domain.SubscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementation of SubscriptionDao using SQLDelight
 */
class SubscriptionDaoImpl(
    private val database: LiyaqaDatabase
) : SubscriptionDao {

    private val queries = database.subscriptionEntityQueries

    override suspend fun getAll(): List<Subscription> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getById(id: String): Subscription? = withContext(Dispatchers.Default) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getByMemberId(memberId: String): List<Subscription> =
        withContext(Dispatchers.Default) {
            queries.selectByMemberId(memberId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getActiveByMemberId(memberId: String): List<Subscription> =
        withContext(Dispatchers.Default) {
            queries.selectActiveByMemberId(memberId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByStatus(status: String): List<Subscription> =
        withContext(Dispatchers.Default) {
            queries.selectByStatus(status).executeAsList().map { it.toDomain() }
        }

    override suspend fun getExpiringSoon(): List<Subscription> =
        withContext(Dispatchers.Default) {
            queries.selectExpiringSoon().executeAsList().map { it.toDomain() }
        }

    override fun observeAll(): Flow<List<Subscription>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<Subscription?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override fun observeByMemberId(memberId: String): Flow<List<Subscription>> {
        return queries.selectByMemberId(memberId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(subscription: Subscription) = withContext(Dispatchers.Default) {
        database.transaction {
            subscription.toEntity().let { entity ->
                queries.insert(
                    id = entity.id,
                    memberId = entity.memberId,
                    planId = entity.planId,
                    planName = entity.planName,
                    startDate = entity.startDate,
                    endDate = entity.endDate,
                    status = entity.status,
                    autoRenew = entity.autoRenew,
                    remainingVisits = entity.remainingVisits,
                    pausedAt = entity.pausedAt,
                    pausedUntil = entity.pausedUntil,
                    cancelledAt = entity.cancelledAt,
                    cancellationReason = entity.cancellationReason,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    cachedAt = entity.cachedAt
                )
            }
        }
    }

    override suspend fun saveAll(subscriptions: List<Subscription>) = withContext(Dispatchers.Default) {
        database.transaction {
            subscriptions.forEach { subscription ->
                subscription.toEntity().let { entity ->
                    queries.insert(
                        id = entity.id,
                        memberId = entity.memberId,
                        planId = entity.planId,
                        planName = entity.planName,
                        startDate = entity.startDate,
                        endDate = entity.endDate,
                        status = entity.status,
                        autoRenew = entity.autoRenew,
                        remainingVisits = entity.remainingVisits,
                        pausedAt = entity.pausedAt,
                        pausedUntil = entity.pausedUntil,
                        cancelledAt = entity.cancelledAt,
                        cancellationReason = entity.cancellationReason,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt,
                        cachedAt = entity.cachedAt
                    )
                }
            }
        }
    }

    override suspend fun updateStatus(id: String, status: String) = withContext(Dispatchers.Default) {
        queries.updateStatus(status, Clock.System.now().toString(), id)
    }

    override suspend fun deleteById(id: String) = withContext(Dispatchers.Default) {
        queries.deleteById(id)
    }

    override suspend fun deleteByMemberId(memberId: String) = withContext(Dispatchers.Default) {
        queries.deleteByMemberId(memberId)
    }

    override suspend fun clearAll() = withContext(Dispatchers.Default) {
        queries.deleteAll()
    }

    override suspend fun count(): Long = withContext(Dispatchers.Default) {
        queries.count().executeAsOne()
    }
}
