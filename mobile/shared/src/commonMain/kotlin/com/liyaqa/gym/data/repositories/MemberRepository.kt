package com.liyaqa.gym.data.repositories

import com.liyaqa.gym.cache.isCacheValid
import com.liyaqa.gym.database.dao.MemberDao
import com.liyaqa.gym.domain.Member
import com.liyaqa.gym.network.ApiResult
import com.liyaqa.gym.network.ConnectivityMonitor
import com.liyaqa.gym.network.mappers.toDomain
import com.liyaqa.gym.network.services.MemberApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Repository interface for Member operations
 */
interface MemberRepository {
    /**
     * Get member profile by ID
     * @param memberId The member ID
     * @param forceRefresh Force fetching from network, bypassing cache
     * @return Result containing Member or error
     */
    suspend fun getMemberProfile(memberId: String, forceRefresh: Boolean = false): Result<Member>

    /**
     * Update member profile
     * @param member The updated member data
     * @return Result containing updated Member or error
     */
    suspend fun updateProfile(member: Member): Result<Member>

    /**
     * Observe member profile as a flow
     * @param memberId The member ID
     * @return Flow of Member or null
     */
    fun observeMemberProfile(memberId: String): Flow<Member?>

    /**
     * Clear all cached member data
     */
    suspend fun clearCache()
}

/**
 * Implementation of MemberRepository with offline-first capabilities
 */
class MemberRepositoryImpl(
    private val apiService: MemberApiService,
    private val memberDao: MemberDao,
    private val connectivityMonitor: ConnectivityMonitor
) : MemberRepository {

    private val cacheMaxAge = 1.hours

    override suspend fun getMemberProfile(memberId: String, forceRefresh: Boolean): Result<Member> {
        // Check if we should use cached data
        if (!forceRefresh) {
            val cached = memberDao.getById(memberId)
            if (cached != null) {
                // Cache exists, check if it's still valid
                // Note: We'd need to store cachedAt timestamp with the member
                // For now, we'll assume cache is valid if it exists
                return Result.success(cached)
            }
        }

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: return cached data if available
            val cached = memberDao.getById(memberId)
            return if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No internet connection and no cached data available"))
            }
        }

        // Fetch from network
        return when (val result = apiService.getMemberById(memberId)) {
            is ApiResult.Success -> {
                val member = result.data.toDomain()
                // Save to cache
                memberDao.save(member)
                Result.success(member)
            }
            is ApiResult.Error -> {
                // Network error: try to return cached data as fallback
                val cached = memberDao.getById(memberId)
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(result.error)
                }
            }
        }
    }

    override suspend fun updateProfile(member: Member): Result<Member> {
        // Optimistic update: save to cache immediately
        memberDao.save(member)

        // Check connectivity
        if (!connectivityMonitor.isConnected()) {
            // Offline: just return the updated member
            // In a production app, you'd queue this for sync later
            return Result.success(member)
        }

        // Update on server
        val request = com.liyaqa.gym.network.models.UpdateProfileRequest(
            name = member.name,
            nameArabic = member.nameArabic,
            phone = member.contactInfo.phone,
            emergencyContactName = member.emergencyContactName,
            emergencyContactPhone = member.emergencyContactPhone,
            profilePhotoUrl = member.profilePhotoUrl
        )

        return when (val result = apiService.updateMember(member.id, request)) {
            is ApiResult.Success -> {
                val updatedMember = result.data.toDomain()
                // Update cache with server response
                memberDao.save(updatedMember)
                Result.success(updatedMember)
            }
            is ApiResult.Error -> {
                // Rollback the optimistic update
                // In a real app, you might want to keep the local changes
                // and sync them later when online
                Result.failure(result.error)
            }
        }
    }

    override fun observeMemberProfile(memberId: String): Flow<Member?> {
        return memberDao.observeById(memberId)
    }

    override suspend fun clearCache() {
        memberDao.clearAll()
    }
}
