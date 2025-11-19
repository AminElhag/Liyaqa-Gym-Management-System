package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.AccessLog
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for AccessLog entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface AccessLogRepository {

    /**
     * Find an access log by its unique identifier.
     *
     * @param id The unique identifier of the access log
     * @return Optional containing the access log if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<AccessLog>>

    /**
     * Find all access logs for a specific member.
     *
     * @param memberId The member identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of access logs
     */
    fun findByMember(memberId: UUID, page: Int = 0, size: Int = 20): Result<List<AccessLog>>

    /**
     * Find all access logs for a specific branch.
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of access logs
     */
    fun findByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<AccessLog>>

    /**
     * Find access logs within a date range.
     *
     * @param startDate The start date/time of the range
     * @param endDate The end date/time of the range
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of access logs in the range
     */
    fun findByDateRange(
        startDate: Instant,
        endDate: Instant,
        page: Int = 0,
        size: Int = 20
    ): Result<List<AccessLog>>

    /**
     * Save an access log (create or update).
     *
     * @param accessLog The access log to save
     * @return Result containing the saved access log
     */
    fun save(accessLog: AccessLog): Result<AccessLog>
}
