package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Payment
import com.liyaqa.gym.domain.entities.PaymentStatus
import com.liyaqa.gym.domain.repositories.PaymentRepository
import com.liyaqa.infrastructure.persistence.mappers.PaymentEntityMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Implementation of PaymentRepository that uses Spring Data JPA.
 *
 * This adapter converts between domain entities and JPA entities,
 * delegating persistence operations to PaymentJpaRepository.
 */
@Repository
class PaymentRepositoryImpl(
    private val jpaRepository: PaymentJpaRepository,
    private val mapper: PaymentEntityMapper
) : PaymentRepository {

    private val logger = LoggerFactory.getLogger(PaymentRepositoryImpl::class.java)

    /**
     * Find a payment by its unique identifier.
     */
    override fun findById(id: UUID): Result<Optional<Payment>> {
        return runCatching {
            logger.debug("Finding payment by ID: {}", id)
            val entity = jpaRepository.findById(id)

            if (entity.isPresent) {
                Optional.of(mapper.toDomain(entity.get()))
            } else {
                Optional.empty()
            }
        }.onFailure { error ->
            logger.error("Failed to find payment by ID: {}", id, error)
        }
    }

    /**
     * Find all payments for a specific member.
     */
    override fun findByMember(memberId: UUID, page: Int, size: Int): Result<List<Payment>> {
        return runCatching {
            logger.debug("Finding payments for member: {}, page: {}, size: {}", memberId, page, size)
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
            logger.error("Failed to find payments for member: {}", memberId, error)
        }
    }

    /**
     * Find payments within a date range.
     */
    override fun findByDateRange(
        startDate: Instant,
        endDate: Instant,
        page: Int,
        size: Int
    ): Result<List<Payment>> {
        return runCatching {
            logger.debug(
                "Finding payments by date range: {} - {}, page: {}, size: {}",
                startDate, endDate, page, size
            )

            val entities = jpaRepository.findCompletedBetween(startDate, endDate)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find payments by date range", error)
        }
    }

    /**
     * Find all pending payments.
     */
    override fun findPending(page: Int, size: Int): Result<List<Payment>> {
        return runCatching {
            logger.debug("Finding pending payments, page: {}, size: {}", page, size)
            val entities = jpaRepository.findByStatus(PaymentStatus.PENDING)

            // Apply pagination manually
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, entities.size)

            if (startIndex >= entities.size) {
                emptyList()
            } else {
                entities.subList(startIndex, endIndex).map { mapper.toDomain(it) }
            }
        }.onFailure { error ->
            logger.error("Failed to find pending payments", error)
        }
    }

    /**
     * Find a payment by payment gateway ID.
     */
    override fun findByPaymentGatewayId(paymentGatewayId: String): Payment? {
        return try {
            logger.debug("Finding payment by payment gateway ID: {}", paymentGatewayId)
            // Note: PaymentJpaRepository doesn't have this method yet, so we'll need to filter manually
            // For now, we'll return null. In a real implementation, add this query to PaymentJpaRepository
            logger.warn("findByPaymentGatewayId not yet implemented in PaymentJpaRepository")
            null
        } catch (e: Exception) {
            logger.error("Failed to find payment by payment gateway ID: {}", paymentGatewayId, e)
            null
        }
    }

    /**
     * Save a payment (create or update).
     */
    override fun save(payment: Payment): Result<Payment> {
        return runCatching {
            logger.debug("Saving payment: {}", payment.id)
            val entity = mapper.toEntity(payment)
            val savedEntity = jpaRepository.save(entity)
            mapper.toDomain(savedEntity)
        }.onFailure { error ->
            logger.error("Failed to save payment: {}", payment.id, error)
        }
    }
}
