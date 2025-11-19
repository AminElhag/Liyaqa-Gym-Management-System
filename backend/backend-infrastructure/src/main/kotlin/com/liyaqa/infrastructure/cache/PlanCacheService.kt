package com.liyaqa.infrastructure.cache

import com.liyaqa.gym.domain.entities.MembershipPlan
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Cache service for MembershipPlan entities.
 * Implements cache-aside pattern with get, put, and invalidate operations.
 * Plans are cached with 1-hour TTL as they change infrequently.
 */
@Service
class PlanCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(PlanCacheService::class.java)

    /**
     * Get plan from cache by plan ID.
     *
     * @param planId The plan ID
     * @return The cached plan or null if not found
     */
    @Cacheable(
        value = ["plan"],
        key = "#planId",
        unless = "#result == null"
    )
    fun getPlan(planId: UUID): MembershipPlan? {
        val key = RedisConfig.planKey(planId.toString())
        logger.debug("Cache miss for plan key: {}", key)
        return null
    }

    /**
     * Get plan from cache using manual Redis operations.
     *
     * @param planId The plan ID
     * @return The cached plan or null if not found
     */
    fun getPlanDirect(planId: UUID): MembershipPlan? {
        return try {
            val key = RedisConfig.planKey(planId.toString())
            val cached = redisTemplate.opsForValue().get(key)
            if (cached != null) {
                logger.debug("Cache hit for plan key: {}", key)
                cached as MembershipPlan
            } else {
                logger.debug("Cache miss for plan key: {}", key)
                null
            }
        } catch (e: Exception) {
            logger.error("Error retrieving plan from cache", e)
            null
        }
    }

    /**
     * Put plan into cache.
     *
     * @param plan The plan to cache
     * @return The cached plan
     */
    @CachePut(
        value = ["plan"],
        key = "#plan.id"
    )
    fun putPlan(plan: MembershipPlan): MembershipPlan {
        val key = RedisConfig.planKey(plan.id.toString())
        logger.debug("Caching plan with key: {}", key)
        return plan
    }

    /**
     * Put plan into cache using manual Redis operations.
     * Uses 1-hour TTL for stable plan data.
     *
     * @param plan The plan to cache
     * @param ttlHours TTL in hours (default: 1 hour)
     */
    fun putPlanDirect(plan: MembershipPlan, ttlHours: Long = 1) {
        try {
            val key = RedisConfig.planKey(plan.id.toString())
            redisTemplate.opsForValue().set(key, plan, ttlHours, TimeUnit.HOURS)
            logger.debug("Cached plan with key: {} (TTL: {} hours)", key, ttlHours)
        } catch (e: Exception) {
            logger.error("Error caching plan", e)
        }
    }

    /**
     * Invalidate plan cache.
     * Called when plan is updated or deleted.
     *
     * @param planId The plan ID
     */
    @CacheEvict(
        value = ["plan"],
        key = "#planId"
    )
    fun invalidatePlan(planId: UUID) {
        val key = RedisConfig.planKey(planId.toString())
        logger.debug("Invalidated plan cache with key: {}", key)
    }

    /**
     * Invalidate plan cache using manual Redis operations.
     *
     * @param planId The plan ID
     */
    fun invalidatePlanDirect(planId: UUID) {
        try {
            val key = RedisConfig.planKey(planId.toString())
            redisTemplate.delete(key)
            logger.debug("Invalidated plan cache with key: {}", key)

            // Also invalidate branch-specific plan lists that might contain this plan
            // This is a simple approach; for production, consider more sophisticated invalidation
        } catch (e: Exception) {
            logger.error("Error invalidating plan cache", e)
        }
    }

    /**
     * Get all active plans for a branch from cache.
     *
     * @param branchId The branch ID
     * @return List of cached active plans or null if not found
     */
    @Cacheable(
        value = ["active_plans"],
        key = "#branchId",
        unless = "#result == null"
    )
    fun getActivePlans(branchId: UUID): List<MembershipPlan>? {
        val key = RedisConfig.activePlansKey(branchId.toString())
        logger.debug("Cache miss for active plans key: {}", key)
        return null
    }

    /**
     * Get active plans from cache using manual Redis operations.
     *
     * @param branchId The branch ID
     * @return List of cached active plans or null if not found
     */
    @Suppress("UNCHECKED_CAST")
    fun getActivePlansDirect(branchId: UUID): List<MembershipPlan>? {
        return try {
            val key = RedisConfig.activePlansKey(branchId.toString())
            val cached = redisTemplate.opsForValue().get(key)
            if (cached != null) {
                logger.debug("Cache hit for active plans key: {}", key)
                cached as List<MembershipPlan>
            } else {
                logger.debug("Cache miss for active plans key: {}", key)
                null
            }
        } catch (e: Exception) {
            logger.error("Error retrieving active plans from cache", e)
            null
        }
    }

    /**
     * Put active plans into cache.
     *
     * @param branchId The branch ID
     * @param plans List of active plans to cache
     * @return The cached plans
     */
    @CachePut(
        value = ["active_plans"],
        key = "#branchId"
    )
    fun putActivePlans(branchId: UUID, plans: List<MembershipPlan>): List<MembershipPlan> {
        val key = RedisConfig.activePlansKey(branchId.toString())
        logger.debug("Caching {} active plans with key: {}", plans.size, key)
        return plans
    }

    /**
     * Put active plans into cache using manual Redis operations.
     *
     * @param branchId The branch ID
     * @param plans List of active plans to cache
     * @param ttlHours TTL in hours (default: 1 hour)
     */
    fun putActivePlansDirect(branchId: UUID, plans: List<MembershipPlan>, ttlHours: Long = 1) {
        try {
            val key = RedisConfig.activePlansKey(branchId.toString())
            redisTemplate.opsForValue().set(key, plans, ttlHours, TimeUnit.HOURS)
            logger.debug("Cached {} active plans with key: {} (TTL: {} hours)", plans.size, key, ttlHours)
        } catch (e: Exception) {
            logger.error("Error caching active plans", e)
        }
    }

    /**
     * Invalidate active plans cache for a branch.
     * Should be called when any plan is created, updated, or deleted.
     *
     * @param branchId The branch ID
     */
    @CacheEvict(
        value = ["active_plans"],
        key = "#branchId"
    )
    fun invalidateActivePlans(branchId: UUID) {
        val key = RedisConfig.activePlansKey(branchId.toString())
        logger.debug("Invalidated active plans cache with key: {}", key)
    }

    /**
     * Invalidate active plans cache using manual Redis operations.
     *
     * @param branchId The branch ID
     */
    fun invalidateActivePlansDirect(branchId: UUID) {
        try {
            val key = RedisConfig.activePlansKey(branchId.toString())
            redisTemplate.delete(key)
            logger.debug("Invalidated active plans cache with key: {}", key)
        } catch (e: Exception) {
            logger.error("Error invalidating active plans cache", e)
        }
    }

    /**
     * Invalidate all plan-related cache for a branch.
     * This includes individual plans and active plans list.
     *
     * @param branchId The branch ID
     */
    fun invalidateBranchPlans(branchId: UUID) {
        try {
            // Invalidate active plans list
            invalidateActivePlansDirect(branchId)

            // Invalidate individual plan caches for this branch
            val pattern = "plan:*"
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
                logger.debug("Invalidated {} plan cache entries for branch: {}", keys.size, branchId)
            }
        } catch (e: Exception) {
            logger.error("Error invalidating branch plans cache", e)
        }
    }

    /**
     * Check if plan is cached.
     *
     * @param planId The plan ID
     * @return true if plan is cached, false otherwise
     */
    fun isPlanCached(planId: UUID): Boolean {
        return try {
            val key = RedisConfig.planKey(planId.toString())
            redisTemplate.hasKey(key) ?: false
        } catch (e: Exception) {
            logger.error("Error checking plan cache existence", e)
            false
        }
    }

    /**
     * Get or fetch plan using cache-aside pattern.
     * If plan is in cache, returns it. Otherwise, fetches from DB.
     *
     * @param planId The plan ID
     * @param fetchFromDb Function to fetch plan from database
     * @return The plan from cache or database
     */
    fun getOrFetch(planId: UUID, fetchFromDb: () -> MembershipPlan?): MembershipPlan? {
        // Check cache first
        val cached = getPlanDirect(planId)
        if (cached != null) {
            return cached
        }

        // Cache miss - fetch from DB
        val plan = fetchFromDb()

        // Store in cache if found
        if (plan != null) {
            putPlanDirect(plan)
        }

        return plan
    }

    /**
     * Get or fetch active plans using cache-aside pattern.
     *
     * @param branchId The branch ID
     * @param fetchFromDb Function to fetch active plans from database
     * @return List of active plans from cache or database
     */
    fun getOrFetchActivePlans(branchId: UUID, fetchFromDb: () -> List<MembershipPlan>): List<MembershipPlan> {
        // Check cache first
        val cached = getActivePlansDirect(branchId)
        if (cached != null) {
            return cached
        }

        // Cache miss - fetch from DB
        val plans = fetchFromDb()

        // Store in cache
        if (plans.isNotEmpty()) {
            putActivePlansDirect(branchId, plans)
        }

        return plans
    }

    /**
     * Invalidate cache when a plan is modified.
     * Invalidates both the individual plan cache and the active plans list.
     *
     * @param plan The modified plan
     */
    fun invalidateOnUpdate(plan: MembershipPlan) {
        invalidatePlanDirect(plan.id)
        invalidateActivePlansDirect(plan.branchId)
    }

    /**
     * Warm up cache with all active plans for a branch.
     * Useful for preloading frequently accessed data.
     *
     * @param branchId The branch ID
     * @param fetchPlans Function to fetch all plans for the branch
     */
    fun warmUpCache(branchId: UUID, fetchPlans: () -> List<MembershipPlan>) {
        try {
            val plans = fetchPlans()

            // Cache individual plans
            plans.forEach { plan ->
                putPlanDirect(plan)
            }

            // Cache active plans list
            val activePlans = plans.filter { it.isActive }
            if (activePlans.isNotEmpty()) {
                putActivePlansDirect(branchId, activePlans)
            }

            logger.info("Warmed up plan cache for branch {} with {} plans ({} active)",
                branchId, plans.size, activePlans.size)
        } catch (e: Exception) {
            logger.error("Error warming up plan cache", e)
        }
    }
}
