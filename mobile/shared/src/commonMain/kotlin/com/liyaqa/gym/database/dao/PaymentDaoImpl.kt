package com.liyaqa.gym.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.database.PaymentEntity
import com.liyaqa.gym.domain.Payment
import com.liyaqa.gym.network.models.PaymentMethod
import com.liyaqa.gym.network.models.PaymentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implementation of PaymentDao using SQLDelight
 */
class PaymentDaoImpl(
    private val database: LiyaqaDatabase
) : PaymentDao {

    private val queries = database.paymentEntityQueries

    override suspend fun getAll(): List<Payment> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getById(id: String): Payment? = withContext(Dispatchers.Default) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getByMemberId(memberId: String): List<Payment> =
        withContext(Dispatchers.Default) {
            queries.selectByMemberId(memberId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getBySubscriptionId(subscriptionId: String): List<Payment> =
        withContext(Dispatchers.Default) {
            queries.selectBySubscriptionId(subscriptionId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByStatus(status: String): List<Payment> =
        withContext(Dispatchers.Default) {
            queries.selectByStatus(status).executeAsList().map { it.toDomain() }
        }

    override suspend fun getRecent(): List<Payment> = withContext(Dispatchers.Default) {
        queries.selectRecent().executeAsList().map { it.toDomain() }
    }

    override fun observeAll(): Flow<List<Payment>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<Payment?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override fun observeByMemberId(memberId: String): Flow<List<Payment>> {
        return queries.selectByMemberId(memberId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(payment: Payment) = withContext(Dispatchers.Default) {
        database.transaction {
            payment.toEntity().let { entity ->
                queries.insert(
                    id = entity.id,
                    memberId = entity.memberId,
                    subscriptionId = entity.subscriptionId,
                    amount = entity.amount,
                    paymentMethod = entity.paymentMethod,
                    status = entity.status,
                    transactionId = entity.transactionId,
                    description = entity.description,
                    paidAt = entity.paidAt,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    cachedAt = entity.cachedAt
                )
            }
        }
    }

    override suspend fun saveAll(payments: List<Payment>) = withContext(Dispatchers.Default) {
        database.transaction {
            payments.forEach { payment ->
                save(payment)
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

    /**
     * Convert database entity to domain model
     */
    private fun PaymentEntity.toDomain(): Payment {
        return Payment(
            id = id,
            memberId = memberId,
            subscriptionId = subscriptionId,
            amount = amount,
            paymentMethod = PaymentMethod.valueOf(paymentMethod),
            status = PaymentStatus.valueOf(status),
            transactionId = transactionId,
            description = description,
            paidAt = paidAt?.let { Instant.parse(it) },
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt)
        )
    }

    /**
     * Convert domain model to database entity
     */
    private fun Payment.toEntity(): PaymentEntity {
        return PaymentEntity(
            id = id,
            memberId = memberId,
            subscriptionId = subscriptionId,
            amount = amount,
            paymentMethod = paymentMethod.name,
            status = status.name,
            transactionId = transactionId,
            description = description,
            paidAt = paidAt?.toString(),
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            cachedAt = Clock.System.now().toString()
        )
    }
}
