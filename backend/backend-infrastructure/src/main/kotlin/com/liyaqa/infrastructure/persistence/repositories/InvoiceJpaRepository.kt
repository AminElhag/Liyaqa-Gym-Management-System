package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.InvoiceStatus
import com.liyaqa.gym.domain.entities.ZATCAStatus
import com.liyaqa.infrastructure.persistence.entities.InvoiceJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository for InvoiceJpaEntity.
 */
@Repository
interface InvoiceJpaRepository : JpaRepository<InvoiceJpaEntity, UUID> {

    /**
     * Find an invoice by its invoice number.
     */
    fun findByInvoiceNumber(invoiceNumber: String): Optional<InvoiceJpaEntity>

    /**
     * Find all invoices for a specific member.
     */
    fun findByMemberId(memberId: UUID): List<InvoiceJpaEntity>

    /**
     * Find invoices within a date range.
     */
    @Query("SELECT i FROM InvoiceJpaEntity i WHERE i.issueDate >= :startDate AND i.issueDate <= :endDate")
    fun findByDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<InvoiceJpaEntity>

    /**
     * Find invoices within a date range for a specific branch.
     */
    @Query("SELECT i FROM InvoiceJpaEntity i WHERE i.issueDate >= :startDate AND i.issueDate <= :endDate AND i.branchId = :branchId")
    fun findByDateRangeAndBranch(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("branchId") branchId: UUID
    ): List<InvoiceJpaEntity>

    /**
     * Find invoices by status.
     */
    fun findByStatus(status: InvoiceStatus): List<InvoiceJpaEntity>

    /**
     * Find invoices by ZATCA status.
     */
    fun findByZatcaStatus(zatcaStatus: ZATCAStatus): List<InvoiceJpaEntity>

    /**
     * Find overdue invoices (due date in the past and status = PENDING).
     */
    @Query("SELECT i FROM InvoiceJpaEntity i WHERE i.dueDate < :asOfDate AND i.status = 'PENDING'")
    fun findOverdue(@Param("asOfDate") asOfDate: LocalDate): List<InvoiceJpaEntity>

    /**
     * Find invoices for a branch within a date range.
     */
    @Query("SELECT i FROM InvoiceJpaEntity i WHERE i.branchId = :branchId AND i.issueDate >= :startDate AND i.issueDate <= :endDate")
    fun findByBranchAndDateRange(
        @Param("branchId") branchId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<InvoiceJpaEntity>

    /**
     * Check if an invoice number already exists.
     */
    fun existsByInvoiceNumber(invoiceNumber: String): Boolean
}
