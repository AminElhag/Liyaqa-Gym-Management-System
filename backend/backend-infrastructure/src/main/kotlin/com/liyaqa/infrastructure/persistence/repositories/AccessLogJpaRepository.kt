package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.AccessType
import com.liyaqa.infrastructure.persistence.entities.AccessLogJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JPA repository for AccessLogJpaEntity.
 */
@Repository
interface AccessLogJpaRepository : JpaRepository<AccessLogJpaEntity, UUID> {

    @Query("SELECT a FROM AccessLogJpaEntity a WHERE a.memberId = :memberId AND a.isDeleted = false ORDER BY a.checkInTime DESC")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<AccessLogJpaEntity>

    @Query("SELECT a FROM AccessLogJpaEntity a WHERE a.branchId = :branchId AND a.isDeleted = false ORDER BY a.checkInTime DESC")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<AccessLogJpaEntity>

    @Query(
        """
        SELECT a FROM AccessLogJpaEntity a
        WHERE a.memberId = :memberId
        AND a.checkInTime BETWEEN :startDate AND :endDate
        AND a.isDeleted = false
        ORDER BY a.checkInTime DESC
        """
    )
    fun findByMemberAndDateRange(
        @Param("memberId") memberId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AccessLogJpaEntity>

    @Query(
        """
        SELECT a FROM AccessLogJpaEntity a
        WHERE a.branchId = :branchId
        AND a.checkInTime BETWEEN :startDate AND :endDate
        AND a.isDeleted = false
        ORDER BY a.checkInTime DESC
        """
    )
    fun findByBranchAndDateRange(
        @Param("branchId") branchId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AccessLogJpaEntity>

    @Query(
        """
        SELECT a FROM AccessLogJpaEntity a
        WHERE a.memberId = :memberId
        AND a.checkOutTime IS NULL
        AND a.isDeleted = false
        ORDER BY a.checkInTime DESC
        """
    )
    fun findActiveByMemberId(@Param("memberId") memberId: UUID): List<AccessLogJpaEntity>

    @Query(
        """
        SELECT a FROM AccessLogJpaEntity a
        WHERE a.branchId = :branchId
        AND a.checkOutTime IS NULL
        AND a.isDeleted = false
        ORDER BY a.checkInTime DESC
        """
    )
    fun findCurrentlyCheckedInByBranchId(@Param("branchId") branchId: UUID): List<AccessLogJpaEntity>

    @Query("SELECT a FROM AccessLogJpaEntity a WHERE a.accessType = :accessType AND a.isDeleted = false")
    fun findByAccessType(@Param("accessType") accessType: AccessType): List<AccessLogJpaEntity>

    @Query("SELECT a FROM AccessLogJpaEntity a WHERE a.subscriptionId = :subscriptionId AND a.isDeleted = false ORDER BY a.checkInTime DESC")
    fun findBySubscriptionId(@Param("subscriptionId") subscriptionId: UUID): List<AccessLogJpaEntity>

    @Query(
        """
        SELECT COUNT(a) FROM AccessLogJpaEntity a
        WHERE a.memberId = :memberId
        AND a.checkInTime BETWEEN :startDate AND :endDate
        AND a.isDeleted = false
        """
    )
    fun countByMemberAndDateRange(
        @Param("memberId") memberId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): Long
}
