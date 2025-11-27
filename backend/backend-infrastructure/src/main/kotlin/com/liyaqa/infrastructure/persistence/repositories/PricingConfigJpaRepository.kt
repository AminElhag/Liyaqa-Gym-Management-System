package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.PricingConfigJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JPA repository for PricingConfigJpaEntity.
 */
@Repository
interface PricingConfigJpaRepository : JpaRepository<PricingConfigJpaEntity, UUID> {

    @Query(
        """
        SELECT p FROM PricingConfigJpaEntity p
        WHERE p.isActive = true
        ORDER BY p.effectiveFrom DESC
        """
    )
    fun findAllActive(): List<PricingConfigJpaEntity>

    @Query(
        """
        SELECT p FROM PricingConfigJpaEntity p
        WHERE p.isActive = true
        AND p.effectiveFrom <= :now
        AND (p.effectiveUntil IS NULL OR p.effectiveUntil >= :now)
        ORDER BY p.effectiveFrom DESC
        """
    )
    fun findActivePricing(@Param("now") now: Instant = Instant.now()): List<PricingConfigJpaEntity>

    @Query(
        """
        SELECT p FROM PricingConfigJpaEntity p
        WHERE p.effectiveFrom <= :effectiveAt
        AND (p.effectiveUntil IS NULL OR p.effectiveUntil >= :effectiveAt)
        ORDER BY p.effectiveFrom DESC
        """
    )
    fun findByEffectiveDate(@Param("effectiveAt") effectiveAt: Instant): List<PricingConfigJpaEntity>

    @Modifying
    @Query("UPDATE PricingConfigJpaEntity p SET p.isActive = false WHERE p.isActive = true")
    fun deactivateAll(): Int

    @Query("SELECT COUNT(p) > 0 FROM PricingConfigJpaEntity p WHERE p.isActive = true")
    fun hasActivePricing(): Boolean
}
