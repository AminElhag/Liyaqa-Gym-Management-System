package com.liyaqa.infrastructure.cache

import com.liyaqa.gym.domain.entities.Member
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Cache service for Member entities.
 * Implements cache-aside pattern with get, put, and invalidate operations.
 */
@Service
class MemberCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(MemberCacheService::class.java)

    /**
     * Get member from cache.
     * Returns null if not found in cache (cache miss).
     *
     * @param branchId The branch ID
     * @param memberId The member ID
     * @return The cached member or null if not found
     */
    @Cacheable(
        value = ["member"],
        key = "#branchId + ':' + #memberId",
        unless = "#result == null"
    )
    fun getMember(branchId: UUID, memberId: UUID): Member? {
        val key = RedisConfig.memberKey(branchId.toString(), memberId.toString())
        logger.debug("Cache miss for member key: {}", key)
        return null
    }

    /**
     * Get member from cache using manual Redis operations.
     * This provides more control over cache operations.
     *
     * @param branchId The branch ID
     * @param memberId The member ID
     * @return The cached member or null if not found
     */
    fun getMemberDirect(branchId: UUID, memberId: UUID): Member? {
        return try {
            val key = RedisConfig.memberKey(branchId.toString(), memberId.toString())
            val cached = redisTemplate.opsForValue().get(key)
            if (cached != null) {
                logger.debug("Cache hit for member key: {}", key)
                cached as Member
            } else {
                logger.debug("Cache miss for member key: {}", key)
                null
            }
        } catch (e: Exception) {
            logger.error("Error retrieving member from cache", e)
            null
        }
    }

    /**
     * Put member into cache.
     * Uses cache-aside pattern to store member data with TTL.
     *
     * @param member The member to cache
     * @return The cached member
     */
    @CachePut(
        value = ["member"],
        key = "#member.branchId + ':' + #member.id"
    )
    fun putMember(member: Member): Member {
        val key = RedisConfig.memberKey(member.branchId.toString(), member.id.toString())
        logger.debug("Caching member with key: {}", key)
        return member
    }

    /**
     * Put member into cache using manual Redis operations.
     * Allows explicit TTL control.
     *
     * @param member The member to cache
     * @param ttlMinutes TTL in minutes (default: 5 minutes)
     */
    fun putMemberDirect(member: Member, ttlMinutes: Long = 5) {
        try {
            val key = RedisConfig.memberKey(member.branchId.toString(), member.id.toString())
            redisTemplate.opsForValue().set(key, member, ttlMinutes, TimeUnit.MINUTES)
            logger.debug("Cached member with key: {} (TTL: {} minutes)", key, ttlMinutes)
        } catch (e: Exception) {
            logger.error("Error caching member", e)
        }
    }

    /**
     * Invalidate member cache.
     * Removes member from cache when data is updated or deleted.
     *
     * @param branchId The branch ID
     * @param memberId The member ID
     */
    @CacheEvict(
        value = ["member"],
        key = "#branchId + ':' + #memberId"
    )
    fun invalidateMember(branchId: UUID, memberId: UUID) {
        val key = RedisConfig.memberKey(branchId.toString(), memberId.toString())
        logger.debug("Invalidated member cache with key: {}", key)
    }

    /**
     * Invalidate member cache using manual Redis operations.
     *
     * @param branchId The branch ID
     * @param memberId The member ID
     */
    fun invalidateMemberDirect(branchId: UUID, memberId: UUID) {
        try {
            val key = RedisConfig.memberKey(branchId.toString(), memberId.toString())
            redisTemplate.delete(key)
            logger.debug("Invalidated member cache with key: {}", key)
        } catch (e: Exception) {
            logger.error("Error invalidating member cache", e)
        }
    }

    /**
     * Invalidate all members for a specific branch.
     *
     * @param branchId The branch ID
     */
    fun invalidateBranchMembers(branchId: UUID) {
        try {
            val pattern = "member:$branchId:*"
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
                logger.debug("Invalidated {} member cache entries for branch: {}", keys.size, branchId)
            }
        } catch (e: Exception) {
            logger.error("Error invalidating branch members cache", e)
        }
    }

    /**
     * Check if member exists in cache.
     *
     * @param branchId The branch ID
     * @param memberId The member ID
     * @return true if member is cached, false otherwise
     */
    fun isMemberCached(branchId: UUID, memberId: UUID): Boolean {
        return try {
            val key = RedisConfig.memberKey(branchId.toString(), memberId.toString())
            redisTemplate.hasKey(key) ?: false
        } catch (e: Exception) {
            logger.error("Error checking member cache existence", e)
            false
        }
    }

    /**
     * Get or fetch member using cache-aside pattern.
     * If member is in cache, returns it. Otherwise, fetches from DB using the provided function.
     *
     * @param branchId The branch ID
     * @param memberId The member ID
     * @param fetchFromDb Function to fetch member from database
     * @return The member from cache or database
     */
    fun getOrFetch(branchId: UUID, memberId: UUID, fetchFromDb: () -> Member?): Member? {
        // Check cache first
        val cached = getMemberDirect(branchId, memberId)
        if (cached != null) {
            return cached
        }

        // Cache miss - fetch from DB
        val member = fetchFromDb()

        // Store in cache if found
        if (member != null) {
            putMemberDirect(member)
        }

        return member
    }

}
