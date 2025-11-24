package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Booking
import com.liyaqa.gym.domain.entities.BookingStatus
import com.liyaqa.gym.domain.repositories.BookingRepository
import com.liyaqa.infrastructure.persistence.entities.BookingJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.BookingEntityMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for BookingJpaEntity.
 */
interface BookingJpaEntityRepository : JpaRepository<BookingJpaEntity, UUID> {

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

/**
 * Implementation of BookingRepository using Spring Data JPA.
 */
@Repository
class BookingJpaRepositoryImpl(
    private val jpaRepository: BookingJpaEntityRepository,
    private val mapper: BookingEntityMapper
) : BookingRepository {

    override fun findById(id: UUID): Result<Optional<Booking>> {
        return runCatching {
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByMember(memberId: UUID, page: Int, size: Int): Result<List<Booking>> {
        return runCatching {
            jpaRepository.findByMemberId(memberId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findBySchedule(scheduleId: UUID, page: Int, size: Int): Result<List<Booking>> {
        return runCatching {
            jpaRepository.findByScheduleId(scheduleId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun countBySchedule(scheduleId: UUID): Result<Long> {
        return runCatching {
            jpaRepository.countConfirmedByScheduleId(scheduleId)
        }
    }

    override fun save(booking: Booking): Result<Booking> {
        return runCatching {
            val entity = mapper.toEntity(booking)
            val saved = jpaRepository.save(entity)
            mapper.toDomain(saved)
        }
    }

    override fun delete(id: UUID): Result<Unit> {
        return runCatching {
            jpaRepository.findById(id).ifPresent { entity ->
                entity.isDeleted = true
                jpaRepository.save(entity)
            }
        }
    }
}
