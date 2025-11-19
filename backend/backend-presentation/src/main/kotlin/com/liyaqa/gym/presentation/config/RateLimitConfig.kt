package com.liyaqa.gym.presentation.config

import com.liyaqa.gym.presentation.security.RateLimitInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Rate limiting configuration
 * Registers the rate limit interceptor and schedules cleanup
 */
@Configuration
@EnableScheduling
class RateLimitConfig(
    private val rateLimitInterceptor: RateLimitInterceptor
) : WebMvcConfigurer {

    /**
     * Register rate limit interceptor
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/v1/auth/**")
    }

    /**
     * Cleanup rate limit data every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    fun cleanupRateLimitData() {
        rateLimitInterceptor.cleanup()
    }
}
