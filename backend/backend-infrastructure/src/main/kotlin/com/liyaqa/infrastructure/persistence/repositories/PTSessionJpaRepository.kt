package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.PTSessionStatus
import com.liyaqa.gym.domain.entities.SessionType
import com.liyaqa.infrastructure.persistence.entities.PTSessionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

/**
 * Spring Data JPA repository for PTSessionJpaEntity.
 */
@Repository
interface PTSessionJpaRepository : JpaRepository<PTSessionJpaEntity, UUID> {

    @Query("SELECT p FROM PTSessionJpaEntity p WHERE p.trainerId = :trainerId AND p.isDeleted = false ORDER BY p.scheduledAt DESC")
    fun findByTrainerId(@Param("trainerId") trainerId: UUID): List<PTSessionJpaEntity>

    @Query("SELECT p FROM PTSessionJpaEntity p WHERE p.memberId = :memberId AND p.isDeleted = false ORDER BY p.scheduledAt DESC")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<PTSessionJpaEntity>

    @Query(
        """
        SELECT p FROM PTSessionJpaEntity p
        WHERE p.trainerId = :trainerId
        AND p.scheduledAt BETWEEN :startDate AND :endDate
        AND p.isDeleted = false
        ORDER BY p.scheduledAt ASC
        """
    )
    fun findByTrainerAndDateRange(
        @Param("trainerId") trainerId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<PTSessionJpaEntity>

    @Query("SELECT p FROM PTSessionJpaEntity p WHERE p.status = :status AND p.isDeleted = false")
    fun findByStatus(@Param("status") status: PTSessionStatus): List<PTSessionJpaEntity>

    @Query("SELECT p FROM PTSessionJpaEntity p WHERE p.sessionType = :sessionType AND p.isDeleted = false")
    fun findBySessionType(@Param("sessionType") sessionType: SessionType): List<PTSessionJpaEntity>

    @Query(
        """
        SELECT p FROM PTSessionJpaEntity p
        WHERE p.scheduledAt >= :fromDate
        AND p.status = 'SCHEDULED'
        AND p.isDeleted = false
        ORDER BY p.scheduledAt ASC
        """
    )
    fun findUpcomingSessions(@Param("fromDate") fromDate: LocalDateTime): List<PTSessionJpaEntity>
}
