package com.liyaqa.gym.network

import kotlinx.serialization.Serializable

/**
 * Sealed class representing different types of network errors
 */
sealed class NetworkError : Exception() {
    data class HttpError(
        val statusCode: Int,
        val errorResponse: ErrorResponse?
    ) : NetworkError() {
        override val message: String
            get() = errorResponse?.message ?: "HTTP Error: $statusCode"
    }

    data class NetworkException(
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkError()

    data object Unauthorized : NetworkError() {
        override val message: String = "Unauthorized access"
    }

    data object Timeout : NetworkError() {
        override val message: String = "Request timed out"
    }

    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkError()
}

/**
 * Standard error response from the API
 */
@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null,
    val details: Map<String, String>? = null,
    val timestamp: String? = null
)

/**
 * Result wrapper for API calls
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val error: NetworkError) : ApiResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(error)
        }
    }

    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (NetworkError) -> Unit): ApiResult<T> {
        if (this is Error) action(error)
        return this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw error
    }
}
