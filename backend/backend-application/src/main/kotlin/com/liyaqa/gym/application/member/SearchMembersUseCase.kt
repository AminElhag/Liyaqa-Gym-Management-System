package com.liyaqa.gym.application.member

import com.liyaqa.gym.application.member.dto.MemberMapper
import com.liyaqa.gym.application.member.dto.MemberSummaryDTO
import com.liyaqa.gym.application.member.dto.PageResult
import com.liyaqa.gym.application.member.queries.SearchMembersQuery
import com.liyaqa.gym.application.member.queries.SortDirection
import com.liyaqa.gym.domain.repositories.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for searching members with various criteria.
 *
 * This use case handles:
 * - Dynamic search criteria
 * - Pagination
 * - Sorting
 * - DTO conversion
 *
 * @property memberRepository Repository for member persistence
 * @property memberMapper Mapper for DTO conversion
 */
@Service
@Transactional(readOnly = true)
class SearchMembersUseCase(
    private val memberRepository: MemberRepository,
    private val memberMapper: MemberMapper
) {

    private val logger = LoggerFactory.getLogger(SearchMembersUseCase::class.java)

    /**
     * Executes the member search use case.
     *
     * @param query The search query containing criteria and pagination
     * @return Result containing a page of member summary DTOs or error
     */
    fun execute(query: SearchMembersQuery): Result<PageResult<MemberSummaryDTO>> {
        return runCatching {
            logger.debug("Searching members with query: $query")

            // Build pageable with sorting
            val pageable = buildPageable(query)

            // Execute search
            val membersPage = memberRepository.search(
                branchId = query.branchId,
                name = query.name?.trim(),
                email = query.email?.trim()?.lowercase(),
                phone = query.phone?.trim(),
                nationalId = query.nationalId?.trim(),
                status = query.status,
                gender = query.gender,
                pageable = pageable
            ).getOrElse { error ->
                logger.error("Failed to search members: ${error.message}", error)
                throw error
            }

            logger.info(
                "Search completed: found ${membersPage.totalElements} members, " +
                        "returning page ${query.page + 1} of ${membersPage.totalPages}"
            )

            // Convert to DTOs
            val summaryDTOs = memberMapper.toSummaryDTOList(membersPage.content)

            // Build page result
            PageResult.of(
                content = summaryDTOs,
                page = query.page,
                size = query.size,
                totalElements = membersPage.totalElements
            )

        }.onFailure { error ->
            logger.error("Failed to execute member search: ${error.message}", error)
        }
    }

    /**
     * Builds a Spring Data Pageable object from the search query.
     *
     * @param query The search query
     * @return Pageable for repository query
     */
    private fun buildPageable(query: SearchMembersQuery): PageRequest {
        val sort = when (query.sortDirection) {
            SortDirection.ASC -> Sort.by(query.sortBy).ascending()
            SortDirection.DESC -> Sort.by(query.sortBy).descending()
        }

        return PageRequest.of(query.page, query.size, sort)
    }

    /**
     * Gets the total count of members matching the criteria.
     *
     * @param branchId Optional branch ID filter
     * @param status Optional status filter
     * @return Result containing the count
     */
    fun count(branchId: java.util.UUID?, status: com.liyaqa.gym.domain.entities.MemberStatus?): Result<Long> {
        return runCatching {
            logger.debug("Counting members with branchId=$branchId, status=$status")

            val count = memberRepository.countByBranchAndStatus(branchId, status)
                .getOrElse { error ->
                    logger.error("Failed to count members: ${error.message}", error)
                    throw error
                }

            logger.debug("Member count: $count")
            count

        }.onFailure { error ->
            logger.error("Failed to count members: ${error.message}", error)
        }
    }
}
