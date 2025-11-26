package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.tenant.InvoiceStatus
import com.liyaqa.gym.domain.entities.tenant.PlatformInvoice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for PlatformInvoice entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface PlatformInvoiceRepository {

    /**
     * Find an invoice by its unique identifier.
     *
     * @param id The unique identifier of the invoice
     * @return Optional containing the invoice if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<PlatformInvoice>>

    /**
     * Find an invoice by invoice number.
     *
     * @param invoiceNumber The invoice number
     * @return Optional containing the invoice if found, empty otherwise
     */
    fun findByInvoiceNumber(invoiceNumber: String): Result<Optional<PlatformInvoice>>

    /**
     * Find all invoices for a tenant.
     *
     * @param tenantId The tenant identifier
     * @param pageable Pagination and sorting parameters
     * @return Page of invoices
     */
    fun findByTenant(tenantId: UUID, pageable: Pageable): Result<Page<PlatformInvoice>>

    /**
     * Find invoices by status.
     *
     * @param status The invoice status
     * @param pageable Pagination and sorting parameters
     * @return Page of invoices
     */
    fun findByStatus(status: InvoiceStatus, pageable: Pageable): Result<Page<PlatformInvoice>>

    /**
     * Find invoices by tenant and status.
     *
     * @param tenantId The tenant identifier
     * @param status The invoice status
     * @param pageable Pagination and sorting parameters
     * @return Page of invoices
     */
    fun findByTenantAndStatus(
        tenantId: UUID,
        status: InvoiceStatus,
        pageable: Pageable
    ): Result<Page<PlatformInvoice>>

    /**
     * Find overdue invoices for a tenant.
     *
     * @param tenantId The tenant identifier
     * @param currentDate The current date to compare against
     * @param pageable Pagination and sorting parameters
     * @return Page of overdue invoices
     */
    fun findOverdueByTenant(
        tenantId: UUID,
        currentDate: LocalDate,
        pageable: Pageable
    ): Result<Page<PlatformInvoice>>

    /**
     * Find invoices with due date within a range.
     *
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @param pageable Pagination and sorting parameters
     * @return Page of invoices
     */
    fun findWithDueDateBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Result<Page<PlatformInvoice>>

    /**
     * Save an invoice (create or update).
     *
     * @param invoice The invoice to save
     * @return The saved invoice
     */
    fun save(invoice: PlatformInvoice): Result<PlatformInvoice>

    /**
     * Count invoices by status.
     *
     * @param status The invoice status
     * @return Count of invoices
     */
    fun countByStatus(status: InvoiceStatus): Result<Long>

    /**
     * Count invoices by tenant and status.
     *
     * @param tenantId The tenant identifier
     * @param status The invoice status
     * @return Count of invoices
     */
    fun countByTenantAndStatus(tenantId: UUID, status: InvoiceStatus): Result<Long>
}
