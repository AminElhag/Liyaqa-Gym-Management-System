package com.liyaqa.gym.presentation.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT Authentication Filter
 * Extracts JWT from Authorization header, validates it, and sets authentication in SecurityContext
 */
@Component
class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val userDetailsService: GymUserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = extractJwtFromRequest(request)

            if (jwt != null && tokenProvider.validateToken(jwt)) {
                authenticateUser(jwt, request)
            } else if (jwt != null) {
                logger.debug("Invalid JWT token for request: ${request.requestURI}")
            }
        } catch (e: ExpiredJwtException) {
            logger.error("JWT token is expired: ${e.message}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token has expired")
            return
        } catch (e: JwtException) {
            logger.error("JWT authentication error: ${e.message}")
        } catch (e: Exception) {
            logger.error("Could not set user authentication in security context", e)
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extract JWT token from Authorization header
     */
    private fun extractJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)

        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    /**
     * Authenticate user and set in SecurityContext
     */
    private fun authenticateUser(jwt: String, request: HttpServletRequest) {
        try {
            val userId = tokenProvider.getUserIdFromToken(jwt)
            val userDetails = userDetailsService.loadUserById(userId)

            // Create authentication token
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities
            )

            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().authentication = authentication

            logger.debug(
                "Successfully authenticated user: ${userDetails.username}, " +
                        "role: ${userDetails.getRole()}, " +
                        "organizationId: ${userDetails.getOrganizationId()}"
            )
        } catch (e: Exception) {
            logger.error("Failed to authenticate user from JWT: ${e.message}", e)
            throw e
        }
    }

    /**
     * Override to specify which requests should not be filtered
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI

        // Skip JWT validation for public endpoints
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/api/v1/auth/forgot-password") ||
                path.startsWith("/api/v1/auth/reset-password") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/error")
    }
}
