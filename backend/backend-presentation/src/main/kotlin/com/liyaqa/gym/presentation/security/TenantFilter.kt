package com.liyaqa.gym.presentation.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Tenant Filter - Extracts tenant context from authenticated user
 * This filter runs after JWT authentication to populate TenantContext
 */
@Component
@Order(2) // Run after JWT filter
class TenantFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(TenantFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Extract tenant information from authenticated user
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication != null && authentication.isAuthenticated) {
                val principal = authentication.principal

                if (principal is GymUserDetails) {
                    // Set tenant context from user details
                    TenantContext.setOrganizationId(principal.getOrganizationId())
                    TenantContext.setBranchId(principal.getBranchId())

                    logger.debug(
                        "Tenant context set - Organization: ${principal.getOrganizationId()}, " +
                                "Branch: ${principal.getBranchId()}"
                    )
                }
            }

            filterChain.doFilter(request, response)
        } finally {
            // Always clear tenant context after request processing
            TenantContext.clear()
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI

        // Skip tenant filter for public endpoints
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/api/v1/auth/forgot-password") ||
                path.startsWith("/api/v1/auth/reset-password") ||
                path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/error")
    }
}
