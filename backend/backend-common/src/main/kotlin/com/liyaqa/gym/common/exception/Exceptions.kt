package com.liyaqa.gym.common.exception

/**
 * Base exception for all business logic exceptions in the application.
 */
open class BusinessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Thrown when a requested resource is not found.
 */
class ResourceNotFoundException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Thrown when a business rule validation fails.
 */
class ValidationException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Thrown when an operation conflicts with the current state.
 */
class ConflictException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Thrown when authentication fails.
 */
class AuthenticationException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Thrown when authorization fails.
 */
class AuthorizationException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)
