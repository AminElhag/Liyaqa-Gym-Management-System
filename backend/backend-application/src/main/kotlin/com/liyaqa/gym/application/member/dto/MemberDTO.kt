package com.liyaqa.gym.application.member.dto

import com.liyaqa.gym.domain.entities.Gender
import com.liyaqa.gym.domain.entities.MemberStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Data Transfer Object for complete member details.
 * Used when returning full member information.
 */
data class MemberDTO(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val nameArabic: String?,
    val email: String,
    val phone: String,
    val nationalId: String?,
    val gender: Gender,
    val dateOfBirth: LocalDate?,
    val age: Int?,
    val status: MemberStatus,
    val profilePhotoUrl: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Data Transfer Object for member summary information.
 * Used in list views and search results for better performance.
 */
data class MemberSummaryDTO(
    val id: UUID,
    val branchId: UUID,
    val name: String,
    val email: String,
    val phone: String,
    val status: MemberStatus,
    val gender: Gender,
    val profilePhotoUrl: String?,
    val createdAt: Instant
)

/**
 * Generic paginated result wrapper.
 *
 * @param T The type of items in the page
 * @property content List of items in the current page
 * @property page Current page number (zero-based)
 * @property size Number of items per page
 * @property totalElements Total number of elements across all pages
 * @property totalPages Total number of pages
 * @property hasNext Whether there is a next page
 * @property hasPrevious Whether there is a previous page
 */
data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun <T> of(
            content: List<T>,
            page: Int,
            size: Int,
            totalElements: Long
        ): PageResult<T> {
            val totalPages = if (size > 0) ((totalElements + size - 1) / size).toInt() else 0
            return PageResult(
                content = content,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                hasNext = page < totalPages - 1,
                hasPrevious = page > 0
            )
        }

        fun <T> empty(): PageResult<T> {
            return PageResult(
                content = emptyList(),
                page = 0,
                size = 0,
                totalElements = 0,
                totalPages = 0,
                hasNext = false,
                hasPrevious = false
            )
        }
    }
}

/**
 * Result wrapper for member deletion operations.
 */
data class MemberDeletionResult(
    val memberId: UUID,
    val exportedDataPath: String?,
    val deletedAt: Instant,
    val success: Boolean,
    val message: String
)
