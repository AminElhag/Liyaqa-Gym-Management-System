package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Invoice
import com.liyaqa.gym.domain.entities.InvoiceStatus
import com.liyaqa.gym.domain.entities.ZATCAStatus
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Invoice entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface InvoiceRepository {

    /**
     * Find an invoice by its unique identifier.
     *
     * @param id The unique identifier of the invoice
     * @return Result containing Optional with the invoice if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Invoice>>

    /**
     * Find an invoice by its invoice number.
     *
     * @param invoiceNumber The invoice number
     * @return Result containing Optional with the invoice if found, empty otherwise
     */
    fun findByInvoiceNumber(invoiceNumber: String): Result<Optional<Invoice>>

    /**
     * Find all invoices for a specific member.
     *
     * @param memberId The member identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of invoices
     */
    fun findByMember(memberId: UUID, page: Int = 0, size: Int = 20): Result<List<Invoice>>

    /**
     * Find invoices within a date range.
     *
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @param branchId Optional branch filter
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of invoices in the range
     */
    fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        branchId: UUID? = null,
        page: Int = 0,
        size: Int = 100
    ): Result<List<Invoice>>

    /**
     * Find invoices by status.
     *
     * @param status The invoice status to filter by
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of invoices with the specified status
     */
    fun findByStatus(status: InvoiceStatus, page: Int = 0, size: Int = 20): Result<List<Invoice>>

    /**
     * Find invoices by ZATCA status.
     *
     * @param zatcaStatus The ZATCA status to filter by
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of invoices with the specified ZATCA status
     */
    fun findByZATCAStatus(zatcaStatus: ZATCAStatus, page: Int = 0, size: Int = 20): Result<List<Invoice>>

    /**
     * Find overdue invoices (invoices with due date in the past and status = PENDING).
     *
     * @param asOfDate The date to check against (defaults to today)
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of overdue invoices
     */
    fun findOverdue(asOfDate: LocalDate = LocalDate.now(), page: Int = 0, size: Int = 20): Result<List<Invoice>>

    /**
     * Find invoices for a branch within a date range (used for VAT reporting).
     *
     * @param branchId The branch identifier
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Result containing a list of all invoices in the range
     */
    fun findByBranchAndDateRange(
        branchId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<Invoice>>

    /**
     * Get the next invoice sequence number for generating invoice numbers.
     * This should be implemented to ensure uniqueness across distributed systems.
     *
     * @return Result containing the next sequence number
     */
    fun getNextInvoiceSequence(): Result<Long>

    /**
     * Save an invoice (create or update).
     *
     * @param invoice The invoice to save
     * @return Result containing the saved invoice
     */
    fun save(invoice: Invoice): Result<Invoice>

    /**
     * Check if an invoice number already exists.
     *
     * @param invoiceNumber The invoice number to check
     * @return Result containing true if exists, false otherwise
     */
    fun existsByInvoiceNumber(invoiceNumber: String): Result<Boolean>
}
