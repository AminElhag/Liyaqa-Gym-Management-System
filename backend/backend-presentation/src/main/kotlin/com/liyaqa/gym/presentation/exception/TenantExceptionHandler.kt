package com.liyaqa.gym.presentation.exception

import com.liyaqa.gym.domain.entities.tenant.FeatureNotEnabledException
import com.liyaqa.gym.domain.entities.tenant.TenantLimitExceededException
import com.liyaqa.gym.domain.exceptions.TenantContextNotSetException
import com.liyaqa.gym.domain.exceptions.TenantNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

/**
 * Global exception handler for tenant-related exceptions.
 *
 * This handler provides consistent error responses for tenant access,
 * feature restrictions, and limit violations.
 */
@RestControllerAdvice
class TenantExceptionHandler {

    private val logger = LoggerFactory.getLogger(TenantExceptionHandler::class.java)

    /**
     * Handle feature not enabled exceptions.
     * Returns 403 Forbidden with details about the missing feature.
     */
    @ExceptionHandler(FeatureNotEnabledException::class)
    fun handleFeatureNotEnabled(ex: FeatureNotEnabledException): ProblemDetail {
        logger.warn("Feature access denied: ${ex.message}")

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "This feature is not available on your current subscription plan"
        )

        problemDetail.title = "Feature Not Available"
        problemDetail.type = URI.create("https://liyaqa.com/errors/feature-not-available")
        problemDetail.setProperty("feature", ex.feature.name)
        problemDetail.setProperty("tenantId", ex.tenantId.toString())
        problemDetail.setProperty(
            "upgradeMessage",
            "Please upgrade your subscription plan to access this feature"
        )

        return problemDetail
    }

    /**
     * Handle tenant limit exceeded exceptions.
     * Returns 429 Too Many Requests with details about the limit.
     */
    @ExceptionHandler(TenantLimitExceededException::class)
    fun handleLimitExceeded(ex: TenantLimitExceededException): ProblemDetail {
        logger.warn("Tenant limit exceeded: ${ex.message}")

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "You have reached the limit for ${ex.limitType} on your current plan"
        )

        problemDetail.title = "Limit Exceeded"
        problemDetail.type = URI.create("https://liyaqa.com/errors/limit-exceeded")
        problemDetail.setProperty("limitType", ex.limitType)
        problemDetail.setProperty("currentValue", ex.currentValue)
        problemDetail.setProperty("maxValue", ex.maxValue)
        problemDetail.setProperty("tenantId", ex.tenantId.toString())
        problemDetail.setProperty(
            "upgradeMessage",
            "Please upgrade your subscription plan to increase your limits"
        )

        return problemDetail
    }

    /**
     * Handle tenant not found exceptions.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(TenantNotFoundException::class)
    fun handleTenantNotFound(ex: TenantNotFoundException): ProblemDetail {
        logger.warn("Tenant not found: ${ex.message}")

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: "Tenant not found"
        )

        problemDetail.title = "Tenant Not Found"
        problemDetail.type = URI.create("https://liyaqa.com/errors/tenant-not-found")

        return problemDetail
    }

    /**
     * Handle tenant context not set exceptions.
     * Returns 500 Internal Server Error (this should not happen in production).
     */
    @ExceptionHandler(TenantContextNotSetException::class)
    fun handleTenantContextNotSet(ex: TenantContextNotSetException): ProblemDetail {
        logger.error("Tenant context not set: ${ex.message}", ex)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Tenant context is not properly configured"
        )

        problemDetail.title = "Internal Configuration Error"
        problemDetail.type = URI.create("https://liyaqa.com/errors/tenant-context-error")
        problemDetail.setProperty(
            "message",
            "Please contact support if this error persists"
        )

        return problemDetail
    }
}
