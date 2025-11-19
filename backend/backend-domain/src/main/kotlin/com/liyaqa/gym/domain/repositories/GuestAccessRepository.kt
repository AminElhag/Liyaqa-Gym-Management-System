package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.GuestAccess
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for GuestAccess entity operations.
 */
interface GuestAccessRepository {

    /**
     * Find a guest access by its unique identifier.
     *
     * @param id The unique identifier of the guest access
     * @return Optional containing the guest access if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<GuestAccess>>

    /**
     * Find a guest access by QR code.
     *
     * @param qrCode The QR code
     * @return Optional containing the guest access if found, empty otherwise
     */
    fun findByQRCode(qrCode: String): Result<Optional<GuestAccess>>

    /**
     * Find all guest accesses created by a host member.
     *
     * @param hostMemberId The host member identifier
     * @return Result containing a list of guest accesses
     */
    fun findByHostMember(hostMemberId: UUID): Result<List<GuestAccess>>

    /**
     * Find active (unused and valid) guest accesses for a host member.
     *
     * @param hostMemberId The host member identifier
     * @return Result containing a list of active guest accesses
     */
    fun findActiveByHostMember(hostMemberId: UUID): Result<List<GuestAccess>>

    /**
     * Count active guest accesses for a host member.
     *
     * @param hostMemberId The host member identifier
     * @return Result containing the count of active guest accesses
     */
    fun countActiveByHostMember(hostMemberId: UUID): Result<Int>

    /**
     * Save a guest access (create or update).
     *
     * @param guestAccess The guest access to save
     * @return Result containing the saved guest access
     */
    fun save(guestAccess: GuestAccess): Result<GuestAccess>
}
