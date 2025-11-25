package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Invoice
import com.liyaqa.gym.domain.entities.InvoiceStatus
import com.liyaqa.gym.domain.entities.ZATCAStatus
import com.liyaqa.gym.domain.repositories.InvoiceRepository
import com.liyaqa.infrastructure.persistence.mappers.InvoiceEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * Implementation of InvoiceRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to InvoiceJpaRepository.
 */
@Repository
class InvoiceRepositoryImpl(
    private val jpaRepository: InvoiceJpaRepository,
    private val mapper: InvoiceEntityMapper
) : InvoiceRepository {

    private val logger = LoggerFactory.getLogger(InvoiceRepositoryImpl::class.java)

    // Simple sequence generator for invoice numbers (in production, use database sequence)
    private val sequenceGenerator = AtomicLong(1000000L)

    /**
     * Find an invoice by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<Invoice>> {
        return runCatching {
            logger.debug("Finding invoice by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find invoice by ID: {}", id, error)
        }
    }

    /**
     * Find an invoice by its invoice number.
     */
    override fun findByInvoiceNumber(invoiceNumber: String): Result<Optional<Invoice>> {
        return runCatching {
            logger.debug("Finding invoice by invoice number: {}", invoiceNumber)
            val entity = jpaRepository.findByInvoiceNumber(invoiceNumber)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find invoice by invoice number: {}", invoiceNumber, error)
        }
    }

    /**
     * Find all invoices for a specific member.
     */
    override fun findByMember(memberId: UUID, page: Int, size: Int): Result<List<Invoice>> {
        return runCatching {
            logger.debug("Finding invoices for member: {}, page: {}, size: {}", memberId, page, size)
            val entities = jpaRepository.findByMemberId(memberId)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find invoices for member: {}", memberId, error)
        }
    }

    /**
     * Find invoices within a date range.
     */
    override fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        branchId: UUID?,
        page: Int,
        size: Int
    ): Result<List<Invoice>> {
        return runCatching {
            logger.debug(
                "Finding invoices by date range: {} - {}, branchId: {}, page: {}, size: {}",
                startDate, endDate, branchId, page, size
            )

            val entities = if (branchId != null) {
                jpaRepository.findByDateRangeAndBranch(startDate, endDate, branchId)
            } else {
                jpaRepository.findByDateRange(startDate, endDate)
            }

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find invoices by date range", error)
        }
    }

    /**
     * Find invoices by status.
     */
    override fun findByStatus(status: InvoiceStatus, page: Int, size: Int): Result<List<Invoice>> {
        return runCatching {
            logger.debug("Finding invoices by status: {}, page: {}, size: {}", status, page, size)
            val entities = jpaRepository.findByStatus(status)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find invoices by status: {}", status, error)
        }
    }

    /**
     * Find invoices by ZATCA status.
     */
    override fun findByZATCAStatus(zatcaStatus: ZATCAStatus, page: Int, size: Int): Result<List<Invoice>> {
        return runCatching {
            logger.debug("Finding invoices by ZATCA status: {}, page: {}, size: {}", zatcaStatus, page, size)
            val entities = jpaRepository.findByZatcaStatus(zatcaStatus)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find invoices by ZATCA status: {}", zatcaStatus, error)
        }
    }

    /**
     * Find overdue invoices.
     */
    override fun findOverdue(asOfDate: LocalDate, page: Int, size: Int): Result<List<Invoice>> {
        return runCatching {
            logger.debug("Finding overdue invoices as of: {}, page: {}, size: {}", asOfDate, page, size)
            val entities = jpaRepository.findOverdue(asOfDate)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find overdue invoices", error)
        }
    }

    /**
     * Find invoices for a branch within a date range.
     */
    override fun findByBranchAndDateRange(
        branchId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<Invoice>> {
        return runCatching {
            logger.debug(
                "Finding invoices by branch and date range: branchId={}, {} - {}",
                branchId, startDate, endDate
            )
            val entities = jpaRepository.findByBranchAndDateRange(branchId, startDate, endDate)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find invoices by branch and date range", error)
        }
    }

    /**
     * Get the next invoice sequence number.
     */
    override fun getNextInvoiceSequence(): Result<Long> {
        return runCatching {
            val nextSequence = sequenceGenerator.incrementAndGet()
            logger.debug("Generated next invoice sequence: {}", nextSequence)
            nextSequence
        }.onFailure { error ->
            logger.error("Failed to generate invoice sequence", error)
        }
    }

    /**
     * Save an invoice (create or update).
     */
    override fun save(invoice: Invoice): Result<Invoice> {
        return runCatching {
            logger.debug("Saving invoice: {}", invoice.id)
            val entity = mapper.toEntity(invoice)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save invoice: {}", invoice.id, error)
        }
    }

    /**
     * Check if an invoice number already exists.
     */
    override fun existsByInvoiceNumber(invoiceNumber: String): Result<Boolean> {
        return runCatching {
            logger.debug("Checking if invoice number exists: {}", invoiceNumber)
            jpaRepository.existsByInvoiceNumber(invoiceNumber)
        }.onFailure { error ->
            logger.error("Failed to check if invoice number exists: {}", invoiceNumber, error)
        }
    }
}
