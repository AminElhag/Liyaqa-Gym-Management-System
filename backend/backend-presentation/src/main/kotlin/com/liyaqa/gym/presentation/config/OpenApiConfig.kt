package com.liyaqa.gym.presentation.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
class OpenApiConfig {

    @Value("\${server.port:8080}")
    private var serverPort: Int = 8080

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Liyaqa Gym Management API")
                    .description("RESTful API for managing gym operations including members, classes, trainers, and bookings")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Liyaqa Team")
                            .email("support@liyaqa.com")
                            .url("https://liyaqa.com")
                    )
                    .license(
                        License()
                            .name("Proprietary")
                            .url("https://liyaqa.com/license")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:$serverPort")
                        .description("Development server"),
                    Server()
                        .url("https://api.liyaqa.com")
                        .description("Production server")
                )
            )
    }
}
