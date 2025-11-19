package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Blacklist
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Blacklist entity operations.
 */
interface BlacklistRepository {

    /**
     * Find a blacklist entry by its unique identifier.
     *
     * @param id The unique identifier of the blacklist entry
     * @return Optional containing the blacklist entry if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Blacklist>>

    /**
     * Find active blacklist entries for a member.
     *
     * @param memberId The member identifier
     * @return Result containing a list of active blacklist entries
     */
    fun findActiveByMember(memberId: UUID): Result<List<Blacklist>>

    /**
     * Find blacklist entries for a member (active and inactive).
     *
     * @param memberId The member identifier
     * @return Result containing a list of blacklist entries
     */
    fun findByMember(memberId: UUID): Result<List<Blacklist>>

    /**
     * Check if a member is currently blacklisted.
     *
     * @param memberId The member identifier
     * @param branchId The branch identifier (null to check system-wide)
     * @return Result containing true if blacklisted, false otherwise
     */
    fun isBlacklisted(memberId: UUID, branchId: UUID?): Result<Boolean>

    /**
     * Save a blacklist entry (create or update).
     *
     * @param blacklist The blacklist entry to save
     * @return Result containing the saved blacklist entry
     */
    fun save(blacklist: Blacklist): Result<Blacklist>
}
