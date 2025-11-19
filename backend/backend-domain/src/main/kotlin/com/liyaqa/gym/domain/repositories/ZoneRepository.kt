package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Zone
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Zone entity operations.
 */
interface ZoneRepository {

    /**
     * Find a zone by its unique identifier.
     *
     * @param id The unique identifier of the zone
     * @return Optional containing the zone if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Zone>>

    /**
     * Find all zones for a specific branch.
     *
     * @param branchId The branch identifier
     * @return Result containing a list of zones
     */
    fun findByBranch(branchId: UUID): Result<List<Zone>>

    /**
     * Find all active zones for a specific branch.
     *
     * @param branchId The branch identifier
     * @return Result containing a list of active zones
     */
    fun findActiveByBranch(branchId: UUID): Result<List<Zone>>

    /**
     * Save a zone (create or update).
     *
     * @param zone The zone to save
     * @return Result containing the saved zone
     */
    fun save(zone: Zone): Result<Zone>

    /**
     * Delete a zone by its identifier.
     *
     * @param id The unique identifier of the zone
     * @return Result indicating success or failure
     */
    fun deleteById(id: UUID): Result<Unit>
}
