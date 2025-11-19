package com.liyaqa.gym.domain.repositories

import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Invoice entity operations.
 * Follows the repository pattern for domain-driven design.
 *
 * Note: This interface is created for future Invoice entity implementation.
 * Currently, invoice functionality may be handled through the Payment entity.
 */
interface InvoiceRepository {

    /**
     * Find an invoice by its unique identifier.
     *
     * @param id The unique identifier of the invoice
     * @return Optional containing the invoice if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Any>>

    /**
     * Find all invoices for a specific member.
     *
     * @param memberId The member identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of invoices
     */
    fun findByMember(memberId: UUID, page: Int = 0, size: Int = 20): Result<List<Any>>

    /**
     * Find invoices within a date range.
     *
     * @param startDate The start date/time of the range
     * @param endDate The end date/time of the range
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of invoices in the range
     */
    fun findByDateRange(
        startDate: Instant,
        endDate: Instant,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Any>>

    /**
     * Find all unpaid invoices.
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of unpaid invoices
     */
    fun findUnpaid(page: Int = 0, size: Int = 20): Result<List<Any>>

    /**
     * Save an invoice (create or update).
     *
     * @param invoice The invoice to save
     * @return Result containing the saved invoice
     */
    fun save(invoice: Any): Result<Any>
}
