package com.liyaqa.gym.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.liyaqa.gym.database.InvoiceEntity
import com.liyaqa.gym.database.InvoiceItemEntity
import com.liyaqa.gym.database.LiyaqaDatabase
import com.liyaqa.gym.domain.Invoice
import com.liyaqa.gym.domain.InvoiceItem
import com.liyaqa.gym.network.models.InvoiceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Implementation of InvoiceDao using SQLDelight
 */
class InvoiceDaoImpl(
    private val database: LiyaqaDatabase
) : InvoiceDao {

    private val queries = database.invoiceEntityQueries

    override suspend fun getAll(): List<Invoice> = withContext(Dispatchers.Default) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getById(id: String): Invoice? = withContext(Dispatchers.Default) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getByMemberId(memberId: String): List<Invoice> =
        withContext(Dispatchers.Default) {
            queries.selectByMemberId(memberId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getBySubscriptionId(subscriptionId: String): List<Invoice> =
        withContext(Dispatchers.Default) {
            queries.selectBySubscriptionId(subscriptionId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getByStatus(status: String): List<Invoice> =
        withContext(Dispatchers.Default) {
            queries.selectByStatus(status).executeAsList().map { it.toDomain() }
        }

    override suspend fun getOverdue(): List<Invoice> = withContext(Dispatchers.Default) {
        queries.selectOverdue().executeAsList().map { it.toDomain() }
    }

    override fun observeAll(): Flow<List<Invoice>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<Invoice?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override fun observeByMemberId(memberId: String): Flow<List<Invoice>> {
        return queries.selectByMemberId(memberId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(invoice: Invoice) = withContext(Dispatchers.Default) {
        database.transaction {
            // First, delete existing items for this invoice
            queries.deleteItemsByInvoiceId(invoice.id)

            // Save the invoice
            invoice.toEntity().let { entity ->
                queries.insert(
                    id = entity.id,
                    memberId = entity.memberId,
                    subscriptionId = entity.subscriptionId,
                    amount = entity.amount,
                    paidAmount = entity.paidAmount,
                    status = entity.status,
                    dueDate = entity.dueDate,
                    paidAt = entity.paidAt,
                    notes = entity.notes,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    cachedAt = entity.cachedAt
                )
            }

            // Save the invoice items
            invoice.items.forEach { item ->
                queries.insertItem(
                    invoiceId = invoice.id,
                    description = item.description,
                    quantity = item.quantity.toLong(),
                    unitPrice = item.unitPrice,
                    amount = item.amount
                )
            }
        }
    }

    override suspend fun saveAll(invoices: List<Invoice>) = withContext(Dispatchers.Default) {
        database.transaction {
            invoices.forEach { invoice ->
                save(invoice)
            }
        }
    }

    override suspend fun updateStatus(id: String, status: String) = withContext(Dispatchers.Default) {
        queries.updateStatus(status, Clock.System.now().toString(), id)
    }

    override suspend fun updatePaidAmount(id: String, paidAmount: Double, status: String) =
        withContext(Dispatchers.Default) {
            queries.updatePaidAmount(paidAmount, status, Clock.System.now().toString(), id)
        }

    override suspend fun deleteById(id: String) = withContext(Dispatchers.Default) {
        database.transaction {
            queries.deleteItemsByInvoiceId(id)
            queries.deleteById(id)
        }
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
    private fun InvoiceEntity.toDomain(): Invoice {
        // Fetch invoice items
        val items = queries.selectItemsByInvoiceId(id).executeAsList().map { it.toDomain() }

        return Invoice(
            id = id,
            memberId = memberId,
            subscriptionId = subscriptionId,
            amount = amount,
            paidAmount = paidAmount,
            status = InvoiceStatus.valueOf(status),
            dueDate = LocalDate.parse(dueDate),
            paidAt = paidAt?.let { Instant.parse(it) },
            items = items,
            notes = notes,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt)
        )
    }

    /**
     * Convert InvoiceItemEntity to InvoiceItem domain model
     */
    private fun InvoiceItemEntity.toDomain(): InvoiceItem {
        return InvoiceItem(
            description = description,
            quantity = quantity.toInt(),
            unitPrice = unitPrice,
            amount = amount
        )
    }

    /**
     * Convert domain model to database entity
     */
    private fun Invoice.toEntity(): InvoiceEntity {
        return InvoiceEntity(
            id = id,
            memberId = memberId,
            subscriptionId = subscriptionId,
            amount = amount,
            paidAmount = paidAmount,
            status = status.name,
            dueDate = dueDate.toString(),
            paidAt = paidAt?.toString(),
            notes = notes,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            cachedAt = Clock.System.now().toString()
        )
    }
}
