package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.BookingStatus
import com.liyaqa.infrastructure.persistence.entities.BookingJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JPA repository for BookingJpaEntity.
 */
@Repository
interface BookingJpaRepository : JpaRepository<BookingJpaEntity, UUID> {

    @Query("SELECT b FROM BookingJpaEntity b WHERE b.memberId = :memberId AND b.isDeleted = false ORDER BY b.bookedAt DESC")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<BookingJpaEntity>

    @Query("SELECT b FROM BookingJpaEntity b WHERE b.scheduleId = :scheduleId AND b.isDeleted = false")
    fun findByScheduleId(@Param("scheduleId") scheduleId: UUID): List<BookingJpaEntity>

    @Query(
        """
        SELECT b FROM BookingJpaEntity b
        WHERE b.scheduleId = :scheduleId
        AND b.status = :status
        AND b.isDeleted = false
        """
    )
    fun findByScheduleIdAndStatus(
        @Param("scheduleId") scheduleId: UUID,
        @Param("status") status: BookingStatus
    ): List<BookingJpaEntity>

    @Query("SELECT b FROM BookingJpaEntity b WHERE b.status = :status AND b.isDeleted = false")
    fun findByStatus(@Param("status") status: BookingStatus): List<BookingJpaEntity>

    @Query(
        """
        SELECT b FROM BookingJpaEntity b
        WHERE b.memberId = :memberId
        AND b.scheduleId = :scheduleId
        AND b.status IN ('CONFIRMED', 'WAITLISTED')
        AND b.isDeleted = false
        """
    )
    fun findActiveByMemberAndSchedule(
        @Param("memberId") memberId: UUID,
        @Param("scheduleId") scheduleId: UUID
    ): List<BookingJpaEntity>

    @Query(
        """
        SELECT COUNT(b) FROM BookingJpaEntity b
        WHERE b.scheduleId = :scheduleId
        AND b.status = 'CONFIRMED'
        AND b.isDeleted = false
        """
    )
    fun countConfirmedByScheduleId(@Param("scheduleId") scheduleId: UUID): Long
}
