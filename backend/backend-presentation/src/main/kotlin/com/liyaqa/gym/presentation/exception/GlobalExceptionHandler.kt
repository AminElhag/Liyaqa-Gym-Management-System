package com.liyaqa.gym.presentation.exception

import com.liyaqa.gym.common.exception.*
import com.liyaqa.gym.presentation.dto.common.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * Global exception handler for all REST controllers.
 * Provides consistent error response format across the API.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Validation error: {}", ex.message)

        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as? FieldError)?.field ?: "unknown"
            val errorMessage = error.defaultMessage ?: "Validation failed"
            fieldName to errorMessage
        }

        val response = ApiResponse.error<Unit>(
            message = "Validation failed",
            code = "VALIDATION_ERROR",
            details = errors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Handle validation exceptions from domain/application layer
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Validation error: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = ex.message ?: "Validation failed",
            code = "VALIDATION_ERROR"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Resource not found: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = ex.message ?: "Resource not found",
            code = "RESOURCE_NOT_FOUND"
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * Handle conflict exceptions (e.g., duplicate email)
     */
    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Conflict error: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = ex.message ?: "Resource conflict",
            code = "CONFLICT"
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException::class, BadCredentialsException::class)
    fun handleAuthenticationException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Authentication error: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = "Authentication failed",
            code = "AUTHENTICATION_ERROR"
        )

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    /**
     * Handle authorization exceptions
     */
    @ExceptionHandler(AuthorizationException::class, AccessDeniedException::class)
    fun handleAuthorizationException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Authorization error: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = "Access denied",
            code = "AUTHORIZATION_ERROR"
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }

    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Business logic error: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = ex.message ?: "Business logic error",
            code = "BUSINESS_ERROR"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Illegal argument: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = ex.message ?: "Invalid argument",
            code = "INVALID_ARGUMENT"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Handle illegal state exceptions
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Illegal state: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = ex.message ?: "Invalid state",
            code = "INVALID_STATE"
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    /**
     * Handle type mismatch exceptions (e.g., invalid UUID format)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Type mismatch error: {}", ex.message)

        val response = ApiResponse.error<Unit>(
            message = "Invalid parameter format: ${ex.name}",
            code = "TYPE_MISMATCH",
            details = mapOf(
                "parameter" to (ex.name ?: "unknown"),
                "expectedType" to (ex.requiredType?.simpleName ?: "unknown")
            )
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error("Unexpected error: {}", ex.message, ex)

        val response = ApiResponse.error<Unit>(
            message = "An unexpected error occurred",
            code = "INTERNAL_SERVER_ERROR"
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
