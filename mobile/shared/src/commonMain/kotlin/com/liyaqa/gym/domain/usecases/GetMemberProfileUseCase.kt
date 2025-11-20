package com.liyaqa.gym.domain.usecases

import com.liyaqa.gym.data.repositories.MemberRepository
import com.liyaqa.gym.domain.Member

/**
 * Use case to get member profile
 * Encapsulates the business logic for fetching member data
 */
class GetMemberProfileUseCase(
    private val memberRepository: MemberRepository
) {
    /**
     * Execute the use case
     * @param memberId The member ID to fetch
     * @param forceRefresh Force refresh from network
     * @return Result containing Member or error
     */
    suspend operator fun invoke(
        memberId: String,
        forceRefresh: Boolean = false
    ): Result<Member> {
        return try {
            memberRepository.getMemberProfile(memberId, forceRefresh)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
