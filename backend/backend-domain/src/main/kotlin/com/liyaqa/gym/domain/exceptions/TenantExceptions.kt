package com.liyaqa.gym.domain.exceptions

import java.util.UUID

/**
 * Exception thrown when a tenant is not found.
 */
class TenantNotFoundException(tenantId: UUID) :
    RuntimeException("Tenant not found with ID: $tenantId")

/**
 * Exception thrown when a tenant slug already exists.
 */
class SlugAlreadyExistsException(slug: String) :
    RuntimeException("Tenant slug already exists: $slug")

/**
 * Exception thrown when no active subscription is found for a tenant.
 */
class NoActiveSubscriptionException(tenantId: UUID) :
    RuntimeException("No active subscription found for tenant: $tenantId")

/**
 * Exception thrown when a tenant has no payment method on file.
 */
class NoPaymentMethodException(tenantId: UUID) :
    RuntimeException("No payment method on file for tenant: $tenantId")

/**
 * Exception thrown when a tenant subscription is not found.
 */
class SubscriptionNotFoundException(subscriptionId: UUID) :
    RuntimeException("Subscription not found with ID: $subscriptionId")

/**
 * Exception thrown when a plan change is invalid.
 */
class InvalidPlanChangeException(message: String) :
    RuntimeException(message)

/**
 * Exception thrown when a tenant cannot be reactivated.
 */
class CannotReactivateTenantException(tenantId: UUID, reason: String) :
    RuntimeException("Cannot reactivate tenant $tenantId: $reason")

/**
 * Exception thrown when a tenant operation is invalid due to current status.
 */
class InvalidTenantStatusException(tenantId: UUID, currentStatus: String, message: String) :
    RuntimeException("Invalid operation for tenant $tenantId with status $currentStatus: $message")

/**
 * Exception thrown when no tenant context has been set for the current request.
 */
class TenantContextNotSetException(message: String = "Tenant context not set") :
    RuntimeException(message)
