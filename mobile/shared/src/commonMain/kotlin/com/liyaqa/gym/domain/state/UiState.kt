package com.liyaqa.gym.domain.state

/**
 * Represents the UI state for async operations
 * @param T The type of data when successful
 */
sealed class UiState<out T> {
    /**
     * Initial state before any operation
     */
    data object Idle : UiState<Nothing>()

    /**
     * Loading state during async operation
     * @param message Optional loading message
     */
    data class Loading(val message: String? = null) : UiState<Nothing>()

    /**
     * Success state with data
     * @param data The successful result data
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error state with error information
     * @param error The error that occurred
     * @param message Human-readable error message
     */
    data class Error(
        val error: Throwable,
        val message: String = error.message ?: "An error occurred"
    ) : UiState<Nothing>()

    /**
     * Check if state is idle
     */
    fun isIdle(): Boolean = this is Idle

    /**
     * Check if state is loading
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Check if state is success
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Check if state is error
     */
    fun isError(): Boolean = this is Error

    /**
     * Get data if success, null otherwise
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Get error if error state, null otherwise
     */
    fun getErrorOrNull(): Throwable? = when (this) {
        is Error -> error
        else -> null
    }
}

/**
 * Transform the data in a Success state
 */
fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Idle -> UiState.Idle
        is UiState.Loading -> UiState.Loading(message)
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> UiState.Error(error, message)
    }
}

/**
 * Convert Result to UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> {
    return if (isSuccess) {
        UiState.Success(getOrThrow())
    } else {
        UiState.Error(exceptionOrNull() ?: Exception("Unknown error"))
    }
}
