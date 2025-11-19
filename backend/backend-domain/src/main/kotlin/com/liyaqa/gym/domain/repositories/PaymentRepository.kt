package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Payment
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Payment entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface PaymentRepository {

    /**
     * Find a payment by its unique identifier.
     *
     * @param id The unique identifier of the payment
     * @return Optional containing the payment if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Payment>>

    /**
     * Find all payments for a specific member.
     *
     * @param memberId The member identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of payments
     */
    fun findByMember(memberId: UUID, page: Int = 0, size: Int = 20): Result<List<Payment>>

    /**
     * Find payments within a date range.
     *
     * @param startDate The start date/time of the range
     * @param endDate The end date/time of the range
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of payments in the range
     */
    fun findByDateRange(
        startDate: Instant,
        endDate: Instant,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Payment>>

    /**
     * Find all pending payments.
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of pending payments
     */
    fun findPending(page: Int = 0, size: Int = 20): Result<List<Payment>>

    /**
     * Find a payment by payment gateway ID.
     *
     * @param paymentGatewayId The payment gateway transaction ID
     * @return The payment if found, null otherwise
     */
    fun findByPaymentGatewayId(paymentGatewayId: String): Payment?

    /**
     * Save a payment (create or update).
     *
     * @param payment The payment to save
     * @return Result containing the saved payment
     */
    fun save(payment: Payment): Result<Payment>
}
