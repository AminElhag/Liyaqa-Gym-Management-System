package com.liyaqa.gym.presentation.security

import com.liyaqa.gym.domain.entities.User
import com.liyaqa.gym.domain.entities.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

/**
 * Custom UserDetails implementation for gym management system
 * Includes multi-tenant context (organization and branch)
 */
class GymUserDetails(
    private val user: User
) : UserDetails {

    fun getUserId(): UUID = user.id

    fun getRole(): UserRole = user.role

    fun getOrganizationId(): UUID = user.organizationId

    fun getBranchId(): UUID? = user.branchId

    fun getMemberId(): UUID? = user.memberId

    fun getStaffId(): UUID? = user.staffId

    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()

        // Add role as authority
        authorities.add(SimpleGrantedAuthority("ROLE_${user.role.name}"))

        // Add all permissions as authorities
        user.role.permissions.forEach { permission ->
            authorities.add(SimpleGrantedAuthority(permission))
        }

        return authorities
    }

    override fun getPassword(): String = user.passwordHash

    override fun getUsername(): String = user.email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = user.isActive

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = user.isActive

    /**
     * Check if user has a specific permission
     */
    fun hasPermission(permission: String): Boolean {
        return user.hasPermission(permission)
    }

    /**
     * Check if user has access to a specific branch
     */
    fun hasAccessToBranch(branchId: UUID): Boolean {
        // Admins without specific branch can access all branches
        if (user.role == UserRole.ADMIN && user.branchId == null) {
            return true
        }

        // Otherwise, check if the branch matches
        return user.branchId == branchId
    }

    /**
     * Check if user has access to a specific organization
     */
    fun hasAccessToOrganization(organizationId: UUID): Boolean {
        return user.organizationId == organizationId
    }

    /**
     * Get the underlying User entity
     */
    fun getUser(): User = user

    companion object {
        fun create(user: User): GymUserDetails {
            return GymUserDetails(user)
        }
    }
}
