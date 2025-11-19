package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.ScheduleStatus
import com.liyaqa.infrastructure.persistence.entities.ClassScheduleJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.UUID

/**
 * Spring Data JPA repository for ClassScheduleJpaEntity.
 */
@Repository
interface ClassScheduleJpaRepository : JpaRepository<ClassScheduleJpaEntity, UUID> {

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.classId = :classId AND cs.isDeleted = false")
    fun findByClassId(@Param("classId") classId: UUID): List<ClassScheduleJpaEntity>

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.trainerId = :trainerId AND cs.isDeleted = false")
    fun findByTrainerId(@Param("trainerId") trainerId: UUID): List<ClassScheduleJpaEntity>

    @Query(
        """
        SELECT cs FROM ClassScheduleJpaEntity cs
        WHERE cs.trainerId = :trainerId
        AND cs.startDate BETWEEN :startDate AND :endDate
        AND cs.status = 'SCHEDULED'
        AND cs.isDeleted = false
        """
    )
    fun findByTrainerAndDateRange(
        @Param("trainerId") trainerId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<ClassScheduleJpaEntity>

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.dayOfWeek = :dayOfWeek AND cs.status = 'SCHEDULED' AND cs.isDeleted = false")
    fun findByDayOfWeek(@Param("dayOfWeek") dayOfWeek: DayOfWeek): List<ClassScheduleJpaEntity>

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.status = :status AND cs.isDeleted = false")
    fun findByStatus(@Param("status") status: ScheduleStatus): List<ClassScheduleJpaEntity>

    @Query(
        """
        SELECT cs FROM ClassScheduleJpaEntity cs
        WHERE cs.startDate >= :fromDate
        AND cs.status = 'SCHEDULED'
        AND cs.isDeleted = false
        ORDER BY cs.startDate ASC
        """
    )
    fun findUpcomingSchedules(@Param("fromDate") fromDate: LocalDateTime): List<ClassScheduleJpaEntity>
}
