package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Refund
import com.liyaqa.gym.domain.entities.RefundStatus
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Refund entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface RefundRepository {

    /**
     * Find a refund by its unique identifier.
     *
     * @param id The unique identifier of the refund
     * @return Result containing Optional with the refund if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Refund>>

    /**
     * Find all refunds for a specific payment.
     *
     * @param paymentId The payment identifier
     * @return Result containing a list of refunds for the payment
     */
    fun findByPayment(paymentId: UUID): Result<List<Refund>>

    /**
     * Find all refunds for a specific member.
     *
     * @param memberId The member identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of refunds
     */
    fun findByMember(memberId: UUID, page: Int = 0, size: Int = 20): Result<List<Refund>>

    /**
     * Find refunds within a date range.
     *
     * @param startDate The start date/time of the range
     * @param endDate The end date/time of the range
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of refunds in the range
     */
    fun findByDateRange(
        startDate: Instant,
        endDate: Instant,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Refund>>

    /**
     * Find refunds by status.
     *
     * @param status The refund status to filter by
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of refunds with the specified status
     */
    fun findByStatus(status: RefundStatus, page: Int = 0, size: Int = 20): Result<List<Refund>>

    /**
     * Find all pending refunds (for processing).
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of pending refunds
     */
    fun findPending(page: Int = 0, size: Int = 20): Result<List<Refund>>

    /**
     * Find a refund by payment gateway refund ID.
     *
     * @param gatewayRefundId The payment gateway refund transaction ID
     * @return Result containing Optional with the refund if found, empty otherwise
     */
    fun findByGatewayRefundId(gatewayRefundId: String): Result<Optional<Refund>>

    /**
     * Save a refund (create or update).
     *
     * @param refund The refund to save
     * @return Result containing the saved refund
     */
    fun save(refund: Refund): Result<Refund>

    /**
     * Calculate total refunded amount for a payment.
     *
     * @param paymentId The payment identifier
     * @return Result containing the total refunded amount
     */
    fun getTotalRefundedAmount(paymentId: UUID): Result<java.math.BigDecimal>
}
