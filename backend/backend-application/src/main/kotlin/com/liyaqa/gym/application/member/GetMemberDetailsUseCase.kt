package com.liyaqa.gym.application.member

import com.liyaqa.gym.application.member.dto.MemberDTO
import com.liyaqa.gym.application.member.dto.MemberMapper
import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.domain.repositories.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Use case for retrieving member details.
 *
 * This use case handles:
 * - Cache-first lookup
 * - Repository fallback
 * - DTO conversion
 *
 * @property memberRepository Repository for member persistence
 * @property memberMapper Mapper for DTO conversion
 */
@Service
@Transactional(readOnly = true)
class GetMemberDetailsUseCase(
    private val memberRepository: MemberRepository,
    private val memberMapper: MemberMapper
) {

    private val logger = LoggerFactory.getLogger(GetMemberDetailsUseCase::class.java)

    /**
     * Executes the get member details use case.
     * Results are cached for subsequent requests.
     *
     * @param memberId The ID of the member to retrieve
     * @return Result containing the member DTO or error
     */
    @Cacheable(
        value = ["member"],
        key = "#result.getOrNull()?.branchId + ':' + #memberId",
        unless = "#result == null || #result.isFailure"
    )
    fun execute(memberId: UUID): Result<MemberDTO> {
        return runCatching {
            logger.debug("Fetching member details for member ID: $memberId")

            // Fetch from repository
            val member = memberRepository.findById(memberId)
                .getOrElse { error ->
                    logger.error("Failed to fetch member from repository: ${error.message}", error)
                    throw error
                }
                .orElseThrow {
                    logger.warn("Member not found with ID: $memberId")
                    ResourceNotFoundException("Member not found with ID: $memberId")
                }

            logger.info("Member details retrieved successfully for member ID: $memberId")

            // Convert to DTO
            memberMapper.toDTO(member)

        }.onFailure { error ->
            logger.error("Failed to get member details for ID $memberId: ${error.message}", error)
        }
    }

    /**
     * Batch retrieves multiple members by their IDs.
     * This is useful for fetching multiple members in a single operation.
     *
     * @param memberIds List of member IDs to retrieve
     * @return Result containing a list of member DTOs
     */
    fun getMultiple(memberIds: List<UUID>): Result<List<MemberDTO>> {
        return runCatching {
            logger.debug("Fetching ${memberIds.size} members")

            if (memberIds.isEmpty()) {
                return@runCatching emptyList()
            }

            // Fetch each member individually
            // Note: In a production system, you might want to add a batch fetch method to the repository
            val members = memberIds.mapNotNull { memberId ->
                memberRepository.findById(memberId)
                    .getOrNull()
                    ?.orElse(null)
            }

            logger.info("Retrieved ${members.size} out of ${memberIds.size} requested members")

            // Convert to DTOs
            memberMapper.toDTOList(members)

        }.onFailure { error ->
            logger.error("Failed to get multiple members: ${error.message}", error)
        }
    }
}
