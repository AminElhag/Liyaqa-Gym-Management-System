package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.Member
import com.liyaqa.gym.domain.entities.MemberStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
     * Search members with dynamic criteria.
     *
     * @param branchId Filter by branch ID (optional)
     * @param name Search by name (partial match, case-insensitive)
     * @param email Search by email (partial match, case-insensitive)
     * @param phone Search by phone (partial match)
     * @param nationalId Search by national ID (exact match)
     * @param status Filter by status (optional)
     * @param gender Filter by gender (optional)
     * @param pageable Pagination and sorting parameters
     * @return Result containing a page of members
     */
    fun search(
        branchId: UUID?,
        name: String?,
        email: String?,
        phone: String?,
        nationalId: String?,
        status: MemberStatus?,
        gender: Gender?,
        pageable: Pageable
    ): Result<Page<Member>>

    /**
     * Count total members by criteria.
     *
     * @param branchId Filter by branch ID (optional)
     * @param status Filter by status (optional)
     * @return Result containing count of members
     */
    fun countByBranchAndStatus(branchId: UUID?, status: MemberStatus?): Result<Long>

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

    /**
     * Check if a member with the given email exists (excluding a specific member ID).
     * Useful for update operations to check uniqueness.
     *
     * @param email The email address to check
     * @param excludeMemberId The member ID to exclude from the check
     * @return Result containing true if exists, false otherwise
     */
    fun existsByEmailExcludingMember(email: String, excludeMemberId: UUID): Result<Boolean>

    /**
     * Soft delete a member by marking them as deleted.
     *
     * @param memberId The member ID to delete
     * @return Result indicating success
     */
    fun softDelete(memberId: UUID): Result<Unit>
}
