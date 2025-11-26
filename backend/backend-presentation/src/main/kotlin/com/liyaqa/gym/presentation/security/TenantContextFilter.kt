package com.liyaqa.gym.presentation.security

import com.liyaqa.gym.domain.entities.tenant.TenantContext
import com.liyaqa.gym.domain.entities.tenant.TenantContextHolder
import com.liyaqa.gym.domain.repositories.TenantRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that extracts tenant information from the request and sets it in the TenantContextHolder.
 *
 * This filter:
 * 1. Extracts the tenant slug from the subdomain or X-Tenant-Slug header
 * 2. Loads the tenant from the database
 * 3. Sets the tenant context in TenantContextHolder for the duration of the request
 * 4. Clears the context after the request completes
 *
 * Execution order: This filter should run BEFORE authentication filters to ensure
 * tenant context is available for authorization decisions.
 */
@Component
@Order(1) // Execute before other filters
class TenantContextFilter(
    private val tenantRepository: TenantRepository
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(TenantContextFilter::class.java)

    companion object {
        private const val TENANT_HEADER = "X-Tenant-Slug"
        private val EXCLUDED_PATHS = setOf(
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui",
            "/v3/api-docs",
            "/error"
        )
        private val PLATFORM_ADMIN_PATHS = setOf(
            "/api/v1/platform"
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestPath = request.requestURI

        try {
            // Skip tenant resolution for excluded paths
            if (shouldSkipTenantResolution(requestPath)) {
                logger.debug("Skipping tenant resolution for path: $requestPath")
                filterChain.doFilter(request, response)
                return
            }

            // Platform admin routes don't require tenant context
            if (isPlatformAdminPath(requestPath)) {
                logger.debug("Platform admin path detected, skipping tenant resolution: $requestPath")
                filterChain.doFilter(request, response)
                return
            }

            // Extract tenant slug from request
            val tenantSlug = extractTenantSlug(request)

            if (tenantSlug != null) {
                // Load tenant from database
                val tenantResult = tenantRepository.findBySlug(tenantSlug)

                tenantResult.fold(
                    onSuccess = { optionalTenant ->
                        if (optionalTenant.isPresent) {
                            val tenant = optionalTenant.get()

                            // Check if tenant is active
                            if (!tenant.isActive() && !tenant.isTrial()) {
                                logger.warn("Attempt to access suspended/cancelled tenant: ${tenant.slug}")
                                response.sendError(
                                    HttpServletResponse.SC_FORBIDDEN,
                                    "Tenant account is not active"
                                )
                                return
                            }

                            // Create and set tenant context
                            val tenantContext = TenantContext.fromTenant(tenant)
                            TenantContextHolder.set(tenantContext)

                            logger.debug(
                                "Tenant context set for: ${tenant.slug} (ID: ${tenant.id})"
                            )
                        } else {
                            logger.warn("Tenant not found with slug: $tenantSlug")
                            response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Tenant not found"
                            )
                            return
                        }
                    },
                    onFailure = { error ->
                        logger.error("Error loading tenant: ${error.message}", error)
                        response.sendError(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Error loading tenant"
                        )
                        return
                    }
                )
            } else {
                // No tenant slug found - for tenant-specific routes, this is an error
                if (isTenantSpecificPath(requestPath)) {
                    logger.warn("No tenant slug provided for tenant-specific path: $requestPath")
                    response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Tenant identifier required. Please provide subdomain or X-Tenant-Slug header."
                    )
                    return
                }
            }

            // Continue with the filter chain
            filterChain.doFilter(request, response)

        } finally {
            // Always clear the tenant context to prevent leakage between requests
            TenantContextHolder.clear()
            logger.debug("Tenant context cleared")
        }
    }

    /**
     * Extract tenant slug from request.
     * Tries custom domain first, then subdomain, then falls back to header.
     *
     * Examples:
     * - gym.example.com (custom domain) -> lookup by custom domain
     * - tenant1.liyaqa.com -> "tenant1"
     * - api.liyaqa.com with X-Tenant-Slug: tenant1 -> "tenant1"
     */
    private fun extractTenantSlug(request: HttpServletRequest): String? {
        val host = request.serverName

        // Check if this is a custom domain (not liyaqa.com)
        if (!host.endsWith(".liyaqa.com") && host != "liyaqa.com" && host != "localhost") {
            logger.debug("Custom domain detected: $host, looking up tenant by custom domain")
            val tenantResult = tenantRepository.findByCustomDomain(host)

            tenantResult.fold(
                onSuccess = { optionalTenant ->
                    if (optionalTenant.isPresent) {
                        val tenant = optionalTenant.get()
                        logger.debug("Found tenant by custom domain: ${tenant.slug}")
                        return tenant.slug
                    }
                },
                onFailure = { error ->
                    logger.error("Error looking up tenant by custom domain: ${error.message}", error)
                }
            )
        }

        // Try subdomain (e.g., tenant1.liyaqa.com)
        val subdomain = extractSubdomain(host)

        if (subdomain != null) {
            logger.debug("Extracted tenant slug from subdomain: $subdomain")
            return subdomain
        }

        // Fallback to header (for mobile apps and API clients)
        val headerSlug = request.getHeader(TENANT_HEADER)
        if (headerSlug != null) {
            logger.debug("Extracted tenant slug from header: $headerSlug")
            return headerSlug
        }

        return null
    }

    /**
     * Extract subdomain from host.
     * Returns null for reserved subdomains (www, api) or if not a subdomain.
     */
    private fun extractSubdomain(host: String): String? {
        val parts = host.split(".")

        // Need at least 3 parts for a subdomain (subdomain.domain.tld)
        if (parts.size < 3) {
            return null
        }

        val subdomain = parts[0]

        // Skip reserved subdomains
        if (subdomain in setOf("www", "api", "admin", "platform")) {
            return null
        }

        // Validate subdomain format (lowercase alphanumeric with hyphens)
        if (!subdomain.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$"))) {
            return null
        }

        return subdomain
    }

    /**
     * Check if the path should skip tenant resolution entirely.
     */
    private fun shouldSkipTenantResolution(path: String): Boolean {
        return EXCLUDED_PATHS.any { path.startsWith(it) }
    }

    /**
     * Check if the path is a platform admin path.
     */
    private fun isPlatformAdminPath(path: String): Boolean {
        return PLATFORM_ADMIN_PATHS.any { path.startsWith(it) }
    }

    /**
     * Check if the path requires tenant context.
     */
    private fun isTenantSpecificPath(path: String): Boolean {
        // Most API paths require tenant context
        return path.startsWith("/api/v1/") &&
               !isPlatformAdminPath(path) &&
               !path.startsWith("/api/v1/auth/") // Auth endpoints may not need tenant
    }
}
