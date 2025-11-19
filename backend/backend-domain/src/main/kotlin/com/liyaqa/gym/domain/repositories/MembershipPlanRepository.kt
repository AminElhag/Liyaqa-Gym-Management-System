package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.MembershipPlan
import com.liyaqa.gym.domain.entities.PlanType
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for MembershipPlan entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface MembershipPlanRepository {

    /**
     * Find a membership plan by its unique identifier.
     *
     * @param id The unique identifier of the plan
     * @return Result containing Optional with the plan if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<MembershipPlan>>

    /**
     * Find all active membership plans for a specific branch.
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of active plans
     */
    fun findActiveByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<MembershipPlan>>

    /**
     * Find all membership plans for a specific branch (including inactive).
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of plans
     */
    fun findByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<MembershipPlan>>

    /**
     * Find membership plans by type.
     *
     * @param type The plan type to filter by
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of plans
     */
    fun findByType(type: PlanType, page: Int = 0, size: Int = 20): Result<List<MembershipPlan>>

    /**
     * Save a membership plan (create or update).
     *
     * @param plan The plan to save
     * @return Result containing the saved plan
     */
    fun save(plan: MembershipPlan): Result<MembershipPlan>

    /**
     * Check if a plan exists by id.
     *
     * @param id The plan identifier
     * @return Result containing true if plan exists, false otherwise
     */
    fun existsById(id: UUID): Result<Boolean>
}
