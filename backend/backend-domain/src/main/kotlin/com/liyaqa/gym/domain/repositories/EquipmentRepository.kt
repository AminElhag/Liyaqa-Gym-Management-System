package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Equipment
import com.liyaqa.gym.domain.entities.EquipmentStatus
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Equipment entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface EquipmentRepository {

    /**
     * Find equipment by its unique identifier.
     *
     * @param id The unique identifier of the equipment
     * @return Optional containing the equipment if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Equipment>>

    /**
     * Find all equipment belonging to a specific branch.
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of equipment
     */
    fun findByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<Equipment>>

    /**
     * Find equipment by status.
     *
     * @param status The equipment status to filter by
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of equipment with the specified status
     */
    fun findByStatus(status: EquipmentStatus, page: Int = 0, size: Int = 20): Result<List<Equipment>>

    /**
     * Find equipment requiring maintenance.
     * This includes equipment where nextMaintenanceDate is in the past or today.
     *
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of equipment requiring maintenance
     */
    fun findRequiringMaintenance(page: Int = 0, size: Int = 20): Result<List<Equipment>>

    /**
     * Save equipment (create or update).
     *
     * @param equipment The equipment to save
     * @return Result containing the saved equipment
     */
    fun save(equipment: Equipment): Result<Equipment>
}
