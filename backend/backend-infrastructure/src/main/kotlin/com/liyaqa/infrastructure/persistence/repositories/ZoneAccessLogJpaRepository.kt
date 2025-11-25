package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.ZoneAccessLogJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for ZoneAccessLogJpaEntity.
 */
@Repository
interface ZoneAccessLogJpaRepository : JpaRepository<ZoneAccessLogJpaEntity, UUID> {

    /**
     * Find all zone access logs for a specific access log.
     */
    @Query("SELECT z FROM ZoneAccessLogJpaEntity z WHERE z.accessLogId = :accessLogId ORDER BY z.entryTime DESC")
    fun findByAccessLogId(@Param("accessLogId") accessLogId: UUID): List<ZoneAccessLogJpaEntity>

    /**
     * Find all zone access logs for a specific member.
     */
    @Query("SELECT z FROM ZoneAccessLogJpaEntity z WHERE z.memberId = :memberId ORDER BY z.entryTime DESC")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<ZoneAccessLogJpaEntity>

    /**
     * Find currently active (no exit time) zone access for a member.
     */
    @Query("SELECT z FROM ZoneAccessLogJpaEntity z WHERE z.memberId = :memberId AND z.exitTime IS NULL ORDER BY z.entryTime DESC")
    fun findActiveByMemberId(@Param("memberId") memberId: UUID): List<ZoneAccessLogJpaEntity>
}
