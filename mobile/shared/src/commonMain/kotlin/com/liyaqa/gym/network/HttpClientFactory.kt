package com.liyaqa.gym.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured HttpClient instances with retry logic and auth support
 */
object HttpClientFactory {

    /**
     * Create HttpClient with full configuration including auth interceptor
     */
    fun create(
        enableLogging: Boolean = true,
        tokenStorage: TokenStorage? = null,
        onTokenRefreshFailed: suspend () -> Unit = {}
    ): HttpClient {
        return HttpClient(platformEngine()) {
            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            // Logging (only in debug mode)
            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                    filter { request ->
                        // Don't log sensitive auth endpoints
                        !request.url.encodedPath.contains("/auth/login")
                    }
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
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            // Authentication with Bearer tokens
            tokenStorage?.let { storage ->
                install(Auth) {
                    bearer {
                        loadTokens {
                            storage.getAccessToken()?.let { token ->
                                BearerTokens(
                                    accessToken = token,
                                    refreshToken = storage.getRefreshToken() ?: ""
                                )
                            }
                        }

                        refreshTokens {
                            val refreshToken = storage.getRefreshToken()
                            if (refreshToken != null) {
                                try {
                                    // Note: Actual refresh logic should be implemented via AuthApiService
                                    // This is a placeholder that signals token refresh is needed
                                    BearerTokens(
                                        accessToken = storage.getAccessToken() ?: "",
                                        refreshToken = refreshToken
                                    )
                                } catch (e: Exception) {
                                    onTokenRefreshFailed()
                                    null
                                }
                            } else {
                                onTokenRefreshFailed()
                                null
                            }
                        }
                    }
                }
            }

            // Retry configuration for transient network failures
            install(HttpRequestRetry) {
                // Retry on server errors (5xx)
                retryOnServerErrors(maxRetries = 3)

                // Retry on network exceptions
                retryOnException(maxRetries = 3, retryOnTimeout = true)

                // Retry on specific status codes
                retryIf(maxRetries = 3) { _, response ->
                    response.status.value in listOf(
                        HttpStatusCode.RequestTimeout.value,
                        HttpStatusCode.TooManyRequests.value,
                        HttpStatusCode.ServiceUnavailable.value,
                        HttpStatusCode.GatewayTimeout.value
                    )
                }

                // Exponential backoff delay
                exponentialDelay(base = 2.0, maxDelayMs = 10_000)

                // Modify request before retry (e.g., refresh token)
                modifyRequest { request ->
                    tokenStorage?.let { storage ->
                        storage.getAccessToken()?.let { token ->
                            request.headers[HttpHeaders.Authorization] = "Bearer $token"
                        }
                    }
                }
            }
        }
    }

    /**
     * Create a simple HttpClient without auth (for login/public endpoints)
     */
    fun createPublic(enableLogging: Boolean = false): HttpClient {
        return HttpClient(platformEngine()) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.TIMEOUT_MILLIS
                connectTimeoutMillis = ApiConfig.TIMEOUT_MILLIS
                socketTimeoutMillis = ApiConfig.TIMEOUT_MILLIS
            }

            defaultRequest {
                url(ApiConfig.BASE_URL)
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                retryOnException(maxRetries = 3, retryOnTimeout = true)
                exponentialDelay(base = 2.0, maxDelayMs = 10_000)
            }
        }
    }
}

/**
 * Expect declaration for platform-specific HTTP engine
 */
expect fun platformEngine(): HttpClientEngineFactory<*>
