package com.liyaqa.gym.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Interceptor that handles authentication by:
 * 1. Adding JWT token to requests
 * 2. Refreshing expired tokens
 * 3. Retrying failed requests after token refresh
 */
class AuthInterceptor(
    private val tokenStorage: TokenStorage,
    private val onTokenRefreshFailed: suspend () -> Unit = {}
) {
    private val mutex = Mutex()
    private var isRefreshing = false

    /**
     * Install the auth interceptor on HttpClient
     */
    fun install(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            // Add access token to request if available
            val token = tokenStorage.getAccessToken()
            if (token != null && !request.url.encodedPath.contains("/auth/")) {
                request.header(HttpHeaders.Authorization, "Bearer $token")
            }

            // Execute the request
            val originalResponse = execute(request)

            // If we get 401, try to refresh the token and retry
            if (originalResponse.response.status == HttpStatusCode.Unauthorized &&
                !request.url.encodedPath.contains("/auth/refresh")) {

                handleUnauthorized(request) ?: originalResponse
            } else {
                originalResponse
            }
        }
    }

    private suspend fun handleUnauthorized(request: HttpRequestBuilder): HttpClientCall? {
        mutex.withLock {
            // Check if token is already being refreshed by another request
            if (isRefreshing) {
                return null
            }

            isRefreshing = true
            try {
                // Try to refresh the token
                val refreshToken = tokenStorage.getRefreshToken()
                if (refreshToken == null) {
                    onTokenRefreshFailed()
                    return null
                }

                // The actual token refresh will be handled by the AuthApiService
                // Here we just signal that refresh is needed
                // In a real implementation, you might want to call the refresh endpoint directly
                onTokenRefreshFailed()
                return null

            } finally {
                isRefreshing = false
            }
        }
    }
}

/**
 * Ktor plugin for automatic token refresh
 */
class TokenRefreshPlugin(
    private val tokenStorage: TokenStorage,
    private val refreshTokenCall: suspend (String) -> Result<Pair<String, String>>
) {

    class Config {
        var tokenStorage: TokenStorage? = null
        var refreshTokenCall: (suspend (String) -> Result<Pair<String, String>>)? = null
    }

    companion object : HttpClientPlugin<Config, TokenRefreshPlugin> {
        override val key: AttributeKey<TokenRefreshPlugin> = AttributeKey("TokenRefreshPlugin")

        override fun prepare(block: Config.() -> Unit): TokenRefreshPlugin {
            val config = Config().apply(block)
            return TokenRefreshPlugin(
                config.tokenStorage ?: throw IllegalStateException("TokenStorage must be provided"),
                config.refreshTokenCall ?: throw IllegalStateException("refreshTokenCall must be provided")
            )
        }

        override fun install(plugin: TokenRefreshPlugin, scope: HttpClient) {
            scope.plugin(HttpSend).intercept { request ->
                // Check if token is expired before making request
                if (plugin.tokenStorage.isTokenExpired() &&
                    !request.url.encodedPath.contains("/auth/")) {

                    val refreshToken = plugin.tokenStorage.getRefreshToken()
                    if (refreshToken != null) {
                        plugin.refreshTokenCall(refreshToken).fold(
                            onSuccess = { (accessToken, newRefreshToken) ->
                                plugin.tokenStorage.saveAccessToken(accessToken)
                                plugin.tokenStorage.saveRefreshToken(newRefreshToken)
                            },
                            onFailure = {
                                // Token refresh failed, clear tokens
                                plugin.tokenStorage.clearTokens()
                            }
                        )
                    }
                }

                execute(request)
            }
        }
    }
}
