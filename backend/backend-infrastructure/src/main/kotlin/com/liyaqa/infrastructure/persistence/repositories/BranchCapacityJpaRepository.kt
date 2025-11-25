package com.liyaqa.infrastructure.persistence.repositories

import com.liyaqa.infrastructure.persistence.entities.BranchCapacityJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for BranchCapacityJpaEntity.
 * Provides atomic operations for real-time capacity management.
 */
@Repository
interface BranchCapacityJpaRepository : JpaRepository<BranchCapacityJpaEntity, UUID> {

    @Query("SELECT bc FROM BranchCapacityJpaEntity bc WHERE bc.branchId = :branchId")
    fun findByBranchId(@Param("branchId") branchId: UUID): BranchCapacityJpaEntity?

    /**
     * Atomically increment male occupancy.
     * Uses database-level atomic operations to ensure thread-safety.
     */
    @Modifying
    @Query(
        """
        UPDATE BranchCapacityJpaEntity bc
        SET bc.currentOccupancy = bc.currentOccupancy + 1,
            bc.maleCount = bc.maleCount + 1,
            bc.lastUpdated = CURRENT_TIMESTAMP
        WHERE bc.branchId = :branchId
        AND bc.currentOccupancy < bc.maxCapacity
        """
    )
    fun incrementMaleOccupancy(@Param("branchId") branchId: UUID): Int

    /**
     * Atomically increment female occupancy.
     * Uses database-level atomic operations to ensure thread-safety.
     */
    @Modifying
    @Query(
        """
        UPDATE BranchCapacityJpaEntity bc
        SET bc.currentOccupancy = bc.currentOccupancy + 1,
            bc.femaleCount = bc.femaleCount + 1,
            bc.lastUpdated = CURRENT_TIMESTAMP
        WHERE bc.branchId = :branchId
        AND bc.currentOccupancy < bc.maxCapacity
        """
    )
    fun incrementFemaleOccupancy(@Param("branchId") branchId: UUID): Int

    /**
     * Atomically decrement male occupancy.
     * Uses database-level atomic operations to ensure thread-safety.
     */
    @Modifying
    @Query(
        """
        UPDATE BranchCapacityJpaEntity bc
        SET bc.currentOccupancy = bc.currentOccupancy - 1,
            bc.maleCount = bc.maleCount - 1,
            bc.lastUpdated = CURRENT_TIMESTAMP
        WHERE bc.branchId = :branchId
        AND bc.currentOccupancy > 0
        AND bc.maleCount > 0
        """
    )
    fun decrementMaleOccupancy(@Param("branchId") branchId: UUID): Int

    /**
     * Atomically decrement female occupancy.
     * Uses database-level atomic operations to ensure thread-safety.
     */
    @Modifying
    @Query(
        """
        UPDATE BranchCapacityJpaEntity bc
        SET bc.currentOccupancy = bc.currentOccupancy - 1,
            bc.femaleCount = bc.femaleCount - 1,
            bc.lastUpdated = CURRENT_TIMESTAMP
        WHERE bc.branchId = :branchId
        AND bc.currentOccupancy > 0
        AND bc.femaleCount > 0
        """
    )
    fun decrementFemaleOccupancy(@Param("branchId") branchId: UUID): Int
}
