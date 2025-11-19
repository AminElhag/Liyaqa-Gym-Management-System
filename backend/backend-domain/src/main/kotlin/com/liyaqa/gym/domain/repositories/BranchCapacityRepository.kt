package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.BranchCapacity
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for BranchCapacity entity operations.
 * Supports real-time capacity monitoring with concurrent updates.
 */
interface BranchCapacityRepository {

    /**
     * Find branch capacity by branch identifier.
     *
     * @param branchId The branch identifier
     * @return Optional containing the branch capacity if found, empty otherwise
     */
    fun findByBranchId(branchId: UUID): Result<Optional<BranchCapacity>>

    /**
     * Save branch capacity (create or update).
     * Implementations should handle concurrent updates atomically.
     *
     * @param branchCapacity The branch capacity to save
     * @return Result containing the saved branch capacity
     */
    fun save(branchCapacity: BranchCapacity): Result<BranchCapacity>

    /**
     * Atomically increment occupancy for a branch.
     * This should be implemented with database-level atomic operations.
     *
     * @param branchId The branch identifier
     * @param isMale Whether the person checking in is male
     * @return Result containing the updated branch capacity
     */
    fun incrementOccupancy(branchId: UUID, isMale: Boolean): Result<BranchCapacity>

    /**
     * Atomically decrement occupancy for a branch.
     * This should be implemented with database-level atomic operations.
     *
     * @param branchId The branch identifier
     * @param isMale Whether the person checking out is male
     * @return Result containing the updated branch capacity
     */
    fun decrementOccupancy(branchId: UUID, isMale: Boolean): Result<BranchCapacity>
}
