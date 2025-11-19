package com.liyaqa.gym.domain.repositories

import com.liyaqa.gym.domain.entities.User
import java.util.UUID

/**
 * Repository interface for User entity
 */
interface UserRepository {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun findByEmailAndOrganizationId(email: String, organizationId: UUID): User?
    fun save(user: User): User
    fun delete(id: UUID)
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndOrganizationId(email: String, organizationId: UUID): Boolean
    fun findAllByOrganizationId(organizationId: UUID): List<User>
    fun findAllByBranchId(branchId: UUID): List<User>
}
