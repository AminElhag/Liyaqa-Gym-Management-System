package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.gym.domain.entities.ClassType
import com.liyaqa.gym.domain.entities.TrainerStatus
import com.liyaqa.infrastructure.persistence.entities.TrainerJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Spring Data JPA repository for TrainerJpaEntity.
 */
@Repository
interface TrainerJpaRepository : JpaRepository<TrainerJpaEntity, UUID> {

    @Query("SELECT t FROM TrainerJpaEntity t WHERE t.branchId = :branchId AND t.isDeleted = false")
    fun findByBranchId(@Param("branchId") branchId: UUID): List<TrainerJpaEntity>

    @Query("SELECT t FROM TrainerJpaEntity t WHERE t.status = :status AND t.isDeleted = false")
    fun findByStatus(@Param("status") status: TrainerStatus): List<TrainerJpaEntity>

    @Query("SELECT t FROM TrainerJpaEntity t WHERE t.branchId = :branchId AND t.status = 'ACTIVE' AND t.isDeleted = false")
    fun findActiveByBranchId(@Param("branchId") branchId: UUID): List<TrainerJpaEntity>

    @Query("SELECT t FROM TrainerJpaEntity t WHERE t.contactInfo.email = :email AND t.isDeleted = false")
    fun findByEmail(@Param("email") email: String): Optional<TrainerJpaEntity>

    @Query(
        """
        SELECT t FROM TrainerJpaEntity t
        JOIN t.specializations s
        WHERE s = :classType AND t.status = 'ACTIVE' AND t.isDeleted = false
        """
    )
    fun findBySpecialization(@Param("classType") classType: ClassType): List<TrainerJpaEntity>
}
