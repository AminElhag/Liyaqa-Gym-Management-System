package com.liyaqa.gym.database.dao

import com.liyaqa.gym.domain.Member
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Member operations
 * Implements cache-aside pattern: check cache first, then network
 */
interface MemberDao {
    /**
     * Get all members from cache
     */
    suspend fun getAll(): List<Member>

    /**
     * Get member by ID from cache
     */
    suspend fun getById(id: String): Member?

    /**
     * Get members by branch ID
     */
    suspend fun getByBranchId(branchId: String): List<Member>

    /**
     * Get members by status
     */
    suspend fun getByStatus(status: String): List<Member>

    /**
     * Search members by name, email, or phone
     */
    suspend fun search(query: String): List<Member>

    /**
     * Observe all members as a flow
     */
    fun observeAll(): Flow<List<Member>>

    /**
     * Observe member by ID as a flow
     */
    fun observeById(id: String): Flow<Member?>

    /**
     * Save a single member to cache
     */
    suspend fun save(member: Member)

    /**
     * Save multiple members to cache
     */
    suspend fun saveAll(members: List<Member>)

    /**
     * Delete member by ID
     */
    suspend fun deleteById(id: String)

    /**
     * Clear all cached members
     */
    suspend fun clearAll()

    /**
     * Get count of cached members
     */
    suspend fun count(): Long
}
