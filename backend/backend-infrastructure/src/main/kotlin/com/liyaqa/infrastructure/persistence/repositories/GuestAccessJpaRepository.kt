package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.GuestAccessJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JPA repository for GuestAccessJpaEntity.
 */
@Repository
interface GuestAccessJpaRepository : JpaRepository<GuestAccessJpaEntity, UUID> {

    @Query("SELECT g FROM GuestAccessJpaEntity g WHERE g.qrCode = :qrCode")
    fun findByQRCode(@Param("qrCode") qrCode: String): GuestAccessJpaEntity?

    @Query("SELECT g FROM GuestAccessJpaEntity g WHERE g.hostMemberId = :hostMemberId ORDER BY g.createdAt DESC")
    fun findByHostMemberId(@Param("hostMemberId") hostMemberId: UUID): List<GuestAccessJpaEntity>

    @Query(
        """
        SELECT g FROM GuestAccessJpaEntity g
        WHERE g.hostMemberId = :hostMemberId
        AND g.isUsed = false
        AND g.validFrom <= :now
        AND g.validUntil > :now
        ORDER BY g.createdAt DESC
        """
    )
    fun findActiveByHostMemberId(
        @Param("hostMemberId") hostMemberId: UUID,
        @Param("now") now: Instant = Instant.now()
    ): List<GuestAccessJpaEntity>

    @Query(
        """
        SELECT COUNT(g) FROM GuestAccessJpaEntity g
        WHERE g.hostMemberId = :hostMemberId
        AND g.isUsed = false
        AND g.validFrom <= :now
        AND g.validUntil > :now
        """
    )
    fun countActiveByHostMemberId(
        @Param("hostMemberId") hostMemberId: UUID,
        @Param("now") now: Instant = Instant.now()
    ): Int
}
