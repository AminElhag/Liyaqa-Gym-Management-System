package com.liyaqa.infrastructure.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Redis configuration for caching.
 * Configures connection factory, Redis template, and cache manager with TTL settings.
 */
@Configuration
@EnableCaching
class RedisConfig(
    private val redisProperties: RedisProperties
) {

    /**
     * Cache names enumeration with their TTL configurations.
     */
    enum class CacheName(val cacheName: String, val ttl: Duration) {
        MEMBER("member", Duration.ofMinutes(5)),
        MEMBER_BY_BRANCH("member_by_branch", Duration.ofMinutes(5)),
        SCHEDULE("schedule", Duration.ofMinutes(5)),
        SCHEDULE_BY_BRANCH("schedule_by_branch", Duration.ofMinutes(5)),
        PLAN("plan", Duration.ofHours(1)),
        PLAN_BY_BRANCH("plan_by_branch", Duration.ofHours(1)),
        ACTIVE_PLANS("active_plans", Duration.ofHours(1));

        companion object {
            fun fromString(name: String): CacheName? {
                return values().find { it.cacheName == name }
            }
        }
    }

    /**
     * Configure Redis connection factory using Lettuce client.
     */
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val factory = LettuceConnectionFactory()
        factory.hostName = redisProperties.host
        factory.port = redisProperties.port

        redisProperties.password?.let {
            factory.setPassword(it)
        }

        redisProperties.database?.let {
            factory.database = it
        }

        return factory
    }

    /**
     * Configure RedisTemplate with JSON serialization using Jackson.
     * Used for manual cache operations.
     */
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        // Use String serializer for keys
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()

        // Use JSON serializer for values
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper())
        template.valueSerializer = jsonSerializer
        template.hashValueSerializer = jsonSerializer

        template.afterPropertiesSet()
        return template
    }

    /**
     * Configure ObjectMapper for JSON serialization.
     * Includes Kotlin and Java Time modules.
     */
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        }
    }

    /**
     * Configure cache manager with different TTL settings for each cache.
     */
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper())
                )
            )
            .disableCachingNullValues()

        // Configure cache-specific TTLs
        val cacheConfigurations = CacheName.values().associate { cache ->
            cache.cacheName to defaultConfig.entryTtl(cache.ttl)
        }

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    companion object {
        /**
         * Generate cache key for members.
         * Format: "member:{branchId}:{memberId}"
         */
        fun memberKey(branchId: String, memberId: String): String {
            return "member:$branchId:$memberId"
        }

        /**
         * Generate cache key for schedules.
         * Format: "schedule:{branchId}:{date}"
         */
        fun scheduleKey(branchId: String, date: String): String {
            return "schedule:$branchId:$date"
        }

        /**
         * Generate cache key for plans.
         * Format: "plan:{planId}"
         */
        fun planKey(planId: String): String {
            return "plan:$planId"
        }

        /**
         * Generate cache key for branch-specific plans.
         * Format: "plan:branch:{branchId}"
         */
        fun plansByBranchKey(branchId: String): String {
            return "plan:branch:$branchId"
        }

        /**
         * Generate cache key for active plans.
         * Format: "plan:active:{branchId}"
         */
        fun activePlansKey(branchId: String): String {
            return "plan:active:$branchId"
        }
    }
}
