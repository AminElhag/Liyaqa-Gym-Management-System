package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.Subscription
import com.liyaqa.gym.domain.entities.SubscriptionStatus
import com.liyaqa.gym.domain.repositories.SubscriptionRepository
import com.liyaqa.infrastructure.persistence.entities.SubscriptionJpaEntity
import com.liyaqa.infrastructure.persistence.mappers.SubscriptionEntityMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository interface for SubscriptionJpaEntity.
 */
interface SubscriptionJpaEntityRepository : JpaRepository<SubscriptionJpaEntity, UUID> {

    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.memberId = :memberId AND s.isDeleted = false")
    fun findByMemberId(@Param("memberId") memberId: UUID): List<SubscriptionJpaEntity>

    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.status = :status AND s.isDeleted = false")
    fun findByStatus(@Param("status") status: SubscriptionStatus): List<SubscriptionJpaEntity>

    @Query(
        """
        SELECT s FROM SubscriptionJpaEntity s
        WHERE s.endDate BETWEEN :startDate AND :endDate
        AND s.isDeleted = false
        ORDER BY s.endDate ASC
        """
    )
    fun findExpiringBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<SubscriptionJpaEntity>
}

/**
 * Implementation of SubscriptionRepository using Spring Data JPA.
 */
@Repository
class SubscriptionJpaRepositoryImpl(
    private val jpaRepository: SubscriptionJpaEntityRepository,
    private val mapper: SubscriptionEntityMapper
) : SubscriptionRepository {

    override fun findById(id: UUID): Result<Optional<Subscription>> {
        return runCatching {
            jpaRepository.findById(id)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByMember(memberId: UUID, page: Int, size: Int): Result<List<Subscription>> {
        return runCatching {
            jpaRepository.findByMemberId(memberId)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findByStatus(status: SubscriptionStatus, page: Int, size: Int): Result<List<Subscription>> {
        return runCatching {
            jpaRepository.findByStatus(status)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun findExpiringBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        page: Int,
        size: Int
    ): Result<List<Subscription>> {
        return runCatching {
            jpaRepository.findExpiringBetween(startDate, endDate)
                .drop(page * size)
                .take(size)
                .map { mapper.toDomain(it) }
        }
    }

    override fun save(subscription: Subscription): Result<Subscription> {
        return runCatching {
            val entity = mapper.toEntity(subscription)
            val saved = jpaRepository.save(entity)
            mapper.toDomain(saved)
        }
    }
}
