package com.liyaqa.gym.presentation.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web configuration for CORS and other web-related settings.
 */
@Configuration
class WebConfig : WebMvcConfigurer {

    @Value("\${app.cors.allowed-origins}")
    private lateinit var allowedOrigins: String

    @Value("\${app.cors.allowed-methods}")
    private lateinit var allowedMethods: String

    @Value("\${app.cors.allowed-headers}")
    private lateinit var allowedHeaders: String

    @Value("\${app.cors.allow-credentials}")
    private var allowCredentials: Boolean = true

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = allowedOrigins.split(",").map { it.trim() }
            allowedMethods = this@WebConfig.allowedMethods.split(",").map { it.trim() }
            allowedHeaders = this@WebConfig.allowedHeaders.split(",").map { it.trim() }
            allowCredentials = this@WebConfig.allowCredentials
            maxAge = 3600L
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
