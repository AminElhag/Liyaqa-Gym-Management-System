package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.BlacklistJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JPA repository for BlacklistJpaEntity.
 */
@Repository
interface BlacklistJpaRepository : JpaRepository<BlacklistJpaEntity, UUID> {

    @Query("SELECT b FROM BlacklistJpaEntity b WHERE b.memberId = :memberId ORDER BY b.blacklistedAt DESC")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<BlacklistJpaEntity>

    @Query(
        """
        SELECT b FROM BlacklistJpaEntity b
        WHERE b.memberId = :memberId
        AND b.isActive = true
        AND (b.expiresAt IS NULL OR b.expiresAt > :now)
        ORDER BY b.blacklistedAt DESC
        """
    )
    fun findActiveByMemberId(
        @Param("memberId") memberId: UUID,
        @Param("now") now: Instant = Instant.now()
    ): List<BlacklistJpaEntity>

    @Query(
        """
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM BlacklistJpaEntity b
        WHERE b.memberId = :memberId
        AND b.isActive = true
        AND (b.expiresAt IS NULL OR b.expiresAt > :now)
        AND (:branchId IS NULL OR b.branchId IS NULL OR b.branchId = :branchId)
        """
    )
    fun existsActiveBlacklistForMember(
        @Param("memberId") memberId: UUID,
        @Param("branchId") branchId: UUID?,
        @Param("now") now: Instant = Instant.now()
    ): Boolean

    @Query("SELECT b FROM BlacklistJpaEntity b WHERE b.branchId = :branchId ORDER BY b.blacklistedAt DESC")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<BlacklistJpaEntity>

    @Query(
        """
        SELECT b FROM BlacklistJpaEntity b
        WHERE b.branchId = :branchId
        AND b.isActive = true
        AND (b.expiresAt IS NULL OR b.expiresAt > :now)
        ORDER BY b.blacklistedAt DESC
        """
    )
    fun findActiveByBranchId(
        @Param("branchId") branchId: UUID,
        @Param("now") now: Instant = Instant.now()
    ): List<BlacklistJpaEntity>
}
