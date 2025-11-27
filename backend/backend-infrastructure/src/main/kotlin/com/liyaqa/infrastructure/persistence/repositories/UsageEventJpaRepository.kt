package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.tenant.UsageEventType
import com.liyaqa.infrastructure.persistence.entities.UsageEventJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JPA repository for UsageEventJpaEntity.
 */
@Repository
interface UsageEventJpaRepository : JpaRepository<UsageEventJpaEntity, UUID> {

    @Query(
        """
        SELECT e FROM UsageEventJpaEntity e
        WHERE e.tenantId = :tenantId
        AND EXTRACT(YEAR FROM e.occurredAt) = :year
        AND EXTRACT(MONTH FROM e.occurredAt) = :month
        ORDER BY e.occurredAt DESC
        """
    )
    fun findByTenantAndPeriod(
        @Param("tenantId") tenantId: UUID,
        @Param("year") year: Int,
        @Param("month") month: Int
    ): List<UsageEventJpaEntity>

    @Query(
        """
        SELECT e FROM UsageEventJpaEntity e
        WHERE e.tenantId = :tenantId
        AND e.eventType = :eventType
        AND EXTRACT(YEAR FROM e.occurredAt) = :year
        AND EXTRACT(MONTH FROM e.occurredAt) = :month
        ORDER BY e.occurredAt DESC
        """
    )
    fun findByTenantPeriodAndType(
        @Param("tenantId") tenantId: UUID,
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("eventType") eventType: UsageEventType
    ): List<UsageEventJpaEntity>

    @Query(
        """
        SELECT e FROM UsageEventJpaEntity e
        WHERE e.tenantId = :tenantId
        AND e.occurredAt BETWEEN :startDate AND :endDate
        ORDER BY e.occurredAt DESC
        """
    )
    fun findByTenantAndDateRange(
        @Param("tenantId") tenantId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<UsageEventJpaEntity>

    @Query(
        """
        SELECT COUNT(e) FROM UsageEventJpaEntity e
        WHERE e.tenantId = :tenantId
        AND e.eventType = :eventType
        AND EXTRACT(YEAR FROM e.occurredAt) = :year
        AND EXTRACT(MONTH FROM e.occurredAt) = :month
        """
    )
    fun countByTenantPeriodAndType(
        @Param("tenantId") tenantId: UUID,
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("eventType") eventType: UsageEventType
    ): Long

    @Query(
        """
        SELECT SUM(e.quantity) FROM UsageEventJpaEntity e
        WHERE e.tenantId = :tenantId
        AND e.eventType = :eventType
        AND EXTRACT(YEAR FROM e.occurredAt) = :year
        AND EXTRACT(MONTH FROM e.occurredAt) = :month
        """
    )
    fun sumQuantityByTenantPeriodAndType(
        @Param("tenantId") tenantId: UUID,
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("eventType") eventType: UsageEventType
    ): Long?

    @Modifying
    @Query("DELETE FROM UsageEventJpaEntity e WHERE e.occurredAt < :beforeDate")
    fun deleteByOccurredAtBefore(@Param("beforeDate") beforeDate: Instant): Int
}
