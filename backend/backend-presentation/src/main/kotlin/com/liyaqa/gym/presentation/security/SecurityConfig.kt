package com.liyaqa.gym.presentation.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

/**
 * Spring Security Configuration
 * - JWT-based authentication
 * - Stateless session management
 * - CORS configuration
 * - Method-level security with @PreAuthorize
 * - BCrypt password encoding
 * - Multi-tenant support with TenantContextFilter
 * - Role-based authorization for platform admins and tenant users
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Order(1)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val tenantContextFilter: TenantContextFilter,
    private val userDetailsService: GymUserDetailsService,
    private val corsConfigurationSource: CorsConfigurationSource
) {

    /**
     * Configure security filter chain with multi-tenant support
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf(AbstractHttpConfigurer<*, *>::disable)
            .cors { it.configurationSource(corsConfigurationSource) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public authentication endpoints
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password"
                    ).permitAll()

                    // Public endpoints
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/error"
                    ).permitAll()

                    // Webhooks (payment gateways, etc.)
                    .requestMatchers(HttpMethod.POST, "/api/v1/webhooks/**").permitAll()

                    // Platform admin routes - only accessible by PLATFORM_ADMIN role
                    .requestMatchers("/api/v1/platform/**")
                        .hasRole("PLATFORM_ADMIN")

                    // Tenant onboarding and management routes
                    .requestMatchers("/api/v1/tenant/onboarding/**")
                        .hasAnyRole("TENANT_OWNER", "TENANT_ADMIN")

                    .requestMatchers("/api/v1/tenant/**")
                        .hasAnyRole("TENANT_OWNER", "TENANT_ADMIN", "TENANT_MANAGER")

                    // Tenant-specific API routes (filtered by tenant context)
                    // All authenticated users within a tenant can access
                    .requestMatchers("/api/v1/members/**", "/api/v1/subscriptions/**")
                        .hasAnyRole("MEMBER", "TRAINER", "STAFF", "ADMIN", "TENANT_OWNER")

                    .requestMatchers("/api/v1/classes/**", "/api/v1/bookings/**")
                        .hasAnyRole("MEMBER", "TRAINER", "STAFF", "ADMIN", "TENANT_OWNER")

                    .requestMatchers("/api/v1/branches/**")
                        .hasAnyRole("STAFF", "ADMIN", "TENANT_OWNER", "TENANT_ADMIN")

                    .requestMatchers("/api/v1/analytics/**", "/api/v1/reports/**")
                        .hasAnyRole("ADMIN", "TENANT_OWNER", "TENANT_ADMIN")

                    // All other API requests require authentication
                    .requestMatchers("/api/v1/**").authenticated()
                    .anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider())
            // Add filters in correct order: Tenant context -> JWT authentication
            .addFilterBefore(tenantContextFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, authException ->
                        response.sendError(401, "Unauthorized: ${authException.message}")
                    }
                    .accessDeniedHandler { _, response, accessDeniedException ->
                        response.sendError(403, "Forbidden: ${accessDeniedException.message}")
                    }
            }

        return http.build()
    }

    /**
     * Configure authentication provider
     */
    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    /**
     * Configure password encoder (BCrypt)
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12) // strength 12
    }

    /**
     * Configure authentication manager
     */
    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}
