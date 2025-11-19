package com.liyaqa.gym.application.member.queries

import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.MemberStatus
import java.util.UUID

/**
 * Query object for searching members with various criteria.
 *
 * @property branchId Filter by branch ID (optional)
 * @property name Search by member name (partial match, case-insensitive)
 * @property email Search by email (partial match, case-insensitive)
 * @property phone Search by phone number (partial match)
 * @property nationalId Search by national ID (exact match)
 * @property status Filter by member status (optional)
 * @property gender Filter by gender (optional)
 * @property page Page number (zero-based)
 * @property size Page size
 * @property sortBy Field to sort by (default: name)
 * @property sortDirection Sort direction (asc or desc)
 */
data class SearchMembersQuery(
    val branchId: UUID? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val nationalId: String? = null,
    val status: MemberStatus? = null,
    val gender: Gender? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "name",
    val sortDirection: SortDirection = SortDirection.ASC
) {
    init {
        require(page >= 0) { "Page number must be non-negative" }
        require(size > 0) { "Page size must be positive" }
        require(size <= 100) { "Page size cannot exceed 100" }
        require(sortBy.isNotBlank()) { "Sort field cannot be blank" }
    }

    /**
     * Checks if any search criteria is specified
     */
    fun hasSearchCriteria(): Boolean {
        return listOfNotNull(
            branchId, name, email, phone, nationalId, status, gender
        ).isNotEmpty()
    }
}

enum class SortDirection {
    ASC,
    DESC
}
