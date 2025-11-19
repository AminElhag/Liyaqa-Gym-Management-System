package com.liyaqa.gym.presentation.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
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
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val userDetailsService: GymUserDetailsService,
    private val corsConfigurationSource: CorsConfigurationSource
) {

    /**
     * Configure security filter chain
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Disable CSRF for stateless API
            .csrf { it.disable() }

            // Configure CORS
            .cors { it.configurationSource(corsConfigurationSource) }

            // Configure session management (stateless)
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // Configure authorization rules
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints - authentication
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password"
                    ).permitAll()

                    // Public endpoints - health check and documentation
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/error"
                    ).permitAll()

                    // Public endpoints - webhook (should be secured with signature verification)
                    .requestMatchers(HttpMethod.POST, "/api/v1/webhooks/**").permitAll()

                    // Protected endpoints - require authentication
                    .requestMatchers("/api/v1/**").authenticated()

                    // All other requests require authentication
                    .anyRequest().authenticated()
            }

            // Configure authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            // Configure exception handling
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, authException ->
                        response.sendError(
                            401,
                            "Unauthorized: ${authException.message}"
                        )
                    }
                    .accessDeniedHandler { _, response, accessDeniedException ->
                        response.sendError(
                            403,
                            "Forbidden: ${accessDeniedException.message}"
                        )
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
