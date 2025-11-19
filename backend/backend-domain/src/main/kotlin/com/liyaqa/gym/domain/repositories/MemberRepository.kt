package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Member
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Member entity operations.
 * Follows the repository pattern for domain-driven design.
 */
interface MemberRepository {

    /**
     * Find a member by their unique identifier.
     *
     * @param id The unique identifier of the member
     * @return Optional containing the member if found, empty otherwise
     */
    fun findById(id: UUID): Result<Optional<Member>>

    /**
     * Find a member by their email address.
     *
     * @param email The email address to search for
     * @return Result containing Optional of member if found
     */
    fun findByEmail(email: String): Result<Optional<Member>>

    /**
     * Find all members belonging to a specific branch.
     *
     * @param branchId The branch identifier
     * @param page The page number (zero-based)
     * @param size The number of items per page
     * @return Result containing a list of members
     */
    fun findByBranch(branchId: UUID, page: Int = 0, size: Int = 20): Result<List<Member>>

    /**
     * Save a member (create or update).
     *
     * @param member The member to save
     * @return Result containing the saved member
     */
    fun save(member: Member): Result<Member>

    /**
     * Check if a member with the given email already exists.
     *
     * @param email The email address to check
     * @return Result containing true if exists, false otherwise
     */
    fun existsByEmail(email: String): Result<Boolean>
}
