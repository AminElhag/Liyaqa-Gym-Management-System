package com.liyaqa.gym.presentation.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Rate limiting interceptor for authentication endpoints
 * Simple in-memory implementation - for production use Redis or similar
 */
@Component
class RateLimitInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(RateLimitInterceptor::class.java)

    // In-memory store: key -> list of request timestamps
    private val requestCounts = ConcurrentHashMap<String, MutableList<Instant>>()

    companion object {
        private const val MAX_REQUESTS_PER_MINUTE = 5
        private const val WINDOW_SIZE_SECONDS = 60L
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val path = request.requestURI

        // Apply rate limiting only to authentication endpoints
        if (!shouldRateLimit(path)) {
            return true
        }

        val key = getRateLimitKey(request)
        val now = Instant.now()

        synchronized(this) {
            // Get or create request list for this key
            val timestamps = requestCounts.getOrPut(key) { mutableListOf() }

            // Remove old requests outside the time window
            timestamps.removeIf { it.isBefore(now.minusSeconds(WINDOW_SIZE_SECONDS)) }

            // Check if rate limit exceeded
            if (timestamps.size >= MAX_REQUESTS_PER_MINUTE) {
                logger.warn("Rate limit exceeded for key: $key, path: $path")
                response.status = 429 // Too Many Requests
                response.contentType = "application/json"
                response.writer.write(
                    """{"error":"Too Many Requests","message":"Rate limit exceeded. Please try again later."}"""
                )
                return false
            }

            // Add current request timestamp
            timestamps.add(now)
        }

        return true
    }

    /**
     * Determine if the path should be rate limited
     */
    private fun shouldRateLimit(path: String): Boolean {
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/forgot-password") ||
                path.startsWith("/api/v1/auth/reset-password")
    }

    /**
     * Generate rate limit key based on IP address and path
     */
    private fun getRateLimitKey(request: HttpServletRequest): String {
        val clientIp = getClientIp(request)
        val path = request.requestURI
        return "$clientIp:$path"
    }

    /**
     * Extract client IP address from request
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return when {
            xForwardedFor != null && xForwardedFor.isNotBlank() -> {
                // Take the first IP in the chain
                xForwardedFor.split(",").first().trim()
            }
            else -> request.remoteAddr
        }
    }

    /**
     * Cleanup old entries periodically
     * This should be called by a scheduled task in production
     */
    fun cleanup() {
        val now = Instant.now()
        synchronized(this) {
            requestCounts.forEach { (key, timestamps) ->
                timestamps.removeIf { it.isBefore(now.minusSeconds(WINDOW_SIZE_SECONDS * 2)) }
            }
            // Remove empty entries
            requestCounts.entries.removeIf { it.value.isEmpty() }
        }
        logger.debug("Rate limit cleanup completed. Active keys: ${requestCounts.size}")
    }
}
