package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Refund
import com.liyaqa.gym.domain.entities.RefundStatus
import com.liyaqa.gym.domain.repositories.RefundRepository
import com.liyaqa.infrastructure.persistence.mappers.RefundEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Implementation of RefundRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to RefundJpaRepository.
 */
@Repository
class RefundRepositoryImpl(
    private val jpaRepository: RefundJpaRepository,
    private val mapper: RefundEntityMapper
) : RefundRepository {

    private val logger = LoggerFactory.getLogger(RefundRepositoryImpl::class.java)

    /**
     * Find a refund by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<Refund>> {
        return runCatching {
            logger.debug("Finding refund by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find refund by ID: {}", id, error)
        }
    }

    /**
     * Find all refunds for a specific payment.
     */
    override fun findByPayment(paymentId: UUID): Result<List<Refund>> {
        return runCatching {
            logger.debug("Finding refunds for payment: {}", paymentId)
            val entities = jpaRepository.findByPaymentId(paymentId)
            entities.map { mapper.toDomain(it) }
        }.onFailure { error ->
            logger.error("Failed to find refunds for payment: {}", paymentId, error)
        }
    }

    /**
     * Find all refunds for a specific member.
     */
    override fun findByMember(memberId: UUID, page: Int, size: Int): Result<List<Refund>> {
        return runCatching {
            logger.debug("Finding refunds for member: {}, page: {}, size: {}", memberId, page, size)
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
            logger.error("Failed to find refunds for member: {}", memberId, error)
        }
    }

    /**
     * Find refunds within a date range.
     */
    override fun findByDateRange(
        startDate: Instant,
        endDate: Instant,
        page: Int,
        size: Int
    ): Result<List<Refund>> {
        return runCatching {
            logger.debug(
                "Finding refunds by date range: {} - {}, page: {}, size: {}",
                startDate, endDate, page, size
            )

            val entities = jpaRepository.findByDateRange(startDate, endDate)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find refunds by date range", error)
        }
    }

    /**
     * Find refunds by status.
     */
    override fun findByStatus(status: RefundStatus, page: Int, size: Int): Result<List<Refund>> {
        return runCatching {
            logger.debug("Finding refunds by status: {}, page: {}, size: {}", status, page, size)
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
            logger.error("Failed to find refunds by status: {}", status, error)
        }
    }

    /**
     * Find all pending refunds (for processing).
     */
    override fun findPending(page: Int, size: Int): Result<List<Refund>> {
        return findByStatus(RefundStatus.PENDING, page, size)
    }

    /**
     * Find a refund by payment gateway refund ID.
     */
    override fun findByGatewayRefundId(gatewayRefundId: String): Result<Optional<Refund>> {
        return runCatching {
            logger.debug("Finding refund by gateway refund ID: {}", gatewayRefundId)
            val entity = jpaRepository.findByPaymentGatewayRefundId(gatewayRefundId)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find refund by gateway refund ID: {}", gatewayRefundId, error)
        }
    }

    /**
     * Save a refund (create or update).
     */
    override fun save(refund: Refund): Result<Refund> {
        return runCatching {
            logger.debug("Saving refund: {}", refund.id)
            val entity = mapper.toEntity(refund)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save refund: {}", refund.id, error)
        }
    }

    /**
     * Calculate total refunded amount for a payment.
     */
    override fun getTotalRefundedAmount(paymentId: UUID): Result<BigDecimal> {
        return runCatching {
            logger.debug("Calculating total refunded amount for payment: {}", paymentId)
            val refunds = jpaRepository.findByPaymentId(paymentId)
                .map { mapper.toDomain(it) }
                .filter { it.isProcessed() }

            val totalAmount = refunds
                .map { it.amount.amount }
                .fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }

            logger.debug("Total refunded amount for payment {}: {}", paymentId, totalAmount)
            totalAmount
        }.onFailure { error ->
            logger.error("Failed to calculate total refunded amount for payment: {}", paymentId, error)
        }
    }
}
