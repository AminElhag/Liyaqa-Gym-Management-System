package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.RefundStatus
import com.liyaqa.infrastructure.persistence.entities.RefundJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository for RefundJpaEntity.
 */
@Repository
interface RefundJpaRepository : JpaRepository<RefundJpaEntity, UUID> {

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.paymentId = :paymentId AND r.isDeleted = false ORDER BY r.createdAt DESC")
    fun findByPaymentId(@Param("paymentId") paymentId: UUID): List<RefundJpaEntity>

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.memberId = :memberId AND r.isDeleted = false ORDER BY r.createdAt DESC")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<RefundJpaEntity>

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.status = :status AND r.isDeleted = false ORDER BY r.createdAt DESC")
    fun findByStatus(@Param("status") status: RefundStatus): List<RefundJpaEntity>

    @Query(
        """
        SELECT r FROM RefundJpaEntity r
        WHERE r.createdAt BETWEEN :startDate AND :endDate
        AND r.isDeleted = false
        ORDER BY r.createdAt DESC
        """
    )
    fun findByDateRange(
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<RefundJpaEntity>

    @Query("SELECT r FROM RefundJpaEntity r WHERE r.paymentGatewayRefundId = :gatewayRefundId AND r.isDeleted = false")
    fun findByPaymentGatewayRefundId(@Param("gatewayRefundId") gatewayRefundId: String): Optional<RefundJpaEntity>
}
