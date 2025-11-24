package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.ClassSchedule
import com.liyaqa.gym.domain.entities.ScheduleStatus
import com.liyaqa.gym.domain.repositories.ClassScheduleRepository
import com.liyaqa.infrastructure.persistence.entities.ClassScheduleJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.ClassScheduleEntityMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for ClassScheduleJpaEntity.
 */
interface ClassScheduleJpaEntityRepository : JpaRepository<ClassScheduleJpaEntity, UUID> {

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.classId = :classId AND cs.isDeleted = false")
    fun findByClassId(@Param("classId") classId: UUID): List<ClassScheduleJpaEntity>

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.classId IN (SELECT c.id FROM ClassJpaEntity c WHERE c.branchId = :branchId) AND cs.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<ClassScheduleJpaEntity>

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.trainerId = :trainerId AND cs.isDeleted = false")
    fun findByTrainerIdAndDeleted(@Param("trainerId") trainerId: UUID): List<ClassScheduleJpaEntity>

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

    @Query("SELECT cs FROM ClassScheduleJpaEntity cs WHERE cs.startDate BETWEEN :startDate AND :endDate AND cs.isDeleted = false ORDER BY cs.startDate")
    fun findByDateRange(
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

/**
 * Implementation of ClassScheduleRepository using Spring Data JPA.
 */
@Repository
class ClassScheduleJpaRepositoryImpl(
    private val jpaRepository: ClassScheduleJpaEntityRepository,
    private val mapper: ClassScheduleEntityMapper
) : ClassScheduleRepository {

    override fun findById(id: UUID): Result<Optional<ClassSchedule>> {
        return runCatching {
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByBranch(branchId: UUID, page: Int, size: Int): Result<List<ClassSchedule>> {
        return runCatching {
            jpaRepository.findByBranchId(branchId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        page: Int,
        size: Int
    ): Result<List<ClassSchedule>> {
        return runCatching {
            jpaRepository.findByDateRange(startDate, endDate)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByTrainer(trainerId: UUID, page: Int, size: Int): Result<List<ClassSchedule>> {
        return runCatching {
            jpaRepository.findByTrainerIdAndDeleted(trainerId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun save(schedule: ClassSchedule): Result<ClassSchedule> {
        return runCatching {
            val entity = mapper.toEntity(schedule)
            val saved = jpaRepository.save(entity)
            mapper.toDomain(saved)
        }
    }
}
