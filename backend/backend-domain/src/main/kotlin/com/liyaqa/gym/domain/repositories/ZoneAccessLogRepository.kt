package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.ZoneAccessLog
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for ZoneAccessLog entity operations.
 */
interface ZoneAccessLogRepository {

    /**
     * Find a zone access log by its unique identifier.
     *
     * @param id The unique identifier of the zone access log
     * @return Optional containing the zone access log if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<ZoneAccessLog>>

    /**
     * Find all zone access logs for a specific access log.
     *
     * @param accessLogId The access log identifier
     * @return Result containing a list of zone access logs
     */
    fun findByAccessLog(accessLogId: UUID): Result<List<ZoneAccessLog>>

    /**
     * Find all zone access logs for a specific member.
     *
     * @param memberId The member identifier
     * @return Result containing a list of zone access logs
     */
    fun findByMember(memberId: UUID): Result<List<ZoneAccessLog>>

    /**
     * Find currently active (no exit time) zone access for a member.
     *
     * @param memberId The member identifier
     * @return Result containing a list of active zone access logs
     */
    fun findActiveByMember(memberId: UUID): Result<List<ZoneAccessLog>>

    /**
     * Save a zone access log (create or update).
     *
     * @param zoneAccessLog The zone access log to save
     * @return Result containing the saved zone access log
     */
    fun save(zoneAccessLog: ZoneAccessLog): Result<ZoneAccessLog>
}
