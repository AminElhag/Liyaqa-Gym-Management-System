package com.liyaqa.gym.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured HttpClient instances
 */
object HttpClientFactory {

    fun create(
        enableLogging: Boolean = true,
        tokenProvider: (() -> String?)? = null
    ): HttpClient {
        return HttpClient {
            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            // Logging
            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }
            }

            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.TIMEOUT_MILLIS
                connectTimeoutMillis = ApiConfig.TIMEOUT_MILLIS
                socketTimeoutMillis = ApiConfig.TIMEOUT_MILLIS
            }

            // Default request configuration
            defaultRequest {
                url(ApiConfig.BASE_URL)
            }

            // Authentication
            tokenProvider?.let { provider ->
                install(Auth) {
                    bearer {
                        loadTokens {
                            provider()?.let { token ->
                                BearerTokens(token, token)
                            }
                        }
                    }
                }
            }

            // Retry configuration
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
        }
    }
}
