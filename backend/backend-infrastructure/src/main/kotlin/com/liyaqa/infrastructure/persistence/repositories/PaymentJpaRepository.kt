package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.PaymentMethod
import com.liyaqa.gym.domain.entities.PaymentStatus
import com.liyaqa.infrastructure.persistence.entities.PaymentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository for PaymentJpaEntity.
 */
@Repository
interface PaymentJpaRepository : JpaRepository<PaymentJpaEntity, UUID> {

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.memberId = :memberId AND p.isDeleted = false ORDER BY p.createdAt DESC")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<PaymentJpaEntity>

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.organizationId = :organizationId AND p.isDeleted = false")
    fun findByOrganizationId(@Param("organizationId") organizationId: UUID): List<PaymentJpaEntity>

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.branchId = :branchId AND p.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<PaymentJpaEntity>

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.status = :status AND p.isDeleted = false")
    fun findByStatus(@Param("status") status: PaymentStatus): List<PaymentJpaEntity>

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.method = :method AND p.isDeleted = false")
    fun findByMethod(@Param("method") method: PaymentMethod): List<PaymentJpaEntity>

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.invoiceNumber = :invoiceNumber AND p.isDeleted = false")
    fun findByInvoiceNumber(@Param("invoiceNumber") invoiceNumber: String): Optional<PaymentJpaEntity>

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.subscriptionId = :subscriptionId AND p.isDeleted = false")
    fun findBySubscriptionId(@Param("subscriptionId") subscriptionId: UUID): List<PaymentJpaEntity>

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.ptSessionId = :ptSessionId AND p.isDeleted = false")
    fun findByPTSessionId(@Param("ptSessionId") ptSessionId: UUID): List<PaymentJpaEntity>

    @Query(
        """
        SELECT p FROM PaymentJpaEntity p
        WHERE p.paidAt BETWEEN :startDate AND :endDate
        AND p.status = 'COMPLETED'
        AND p.isDeleted = false
        ORDER BY p.paidAt DESC
        """
    )
    fun findCompletedBetween(
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<PaymentJpaEntity>

    @Query(
        """
        SELECT p FROM PaymentJpaEntity p
        WHERE p.branchId = :branchId
        AND p.paidAt BETWEEN :startDate AND :endDate
        AND p.status = 'COMPLETED'
        AND p.isDeleted = false
        ORDER BY p.paidAt DESC
        """
    )
    fun findCompletedByBranchAndDateRange(
        @Param("branchId") branchId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<PaymentJpaEntity>
}
