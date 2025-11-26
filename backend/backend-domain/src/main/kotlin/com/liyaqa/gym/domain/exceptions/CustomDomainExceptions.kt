package com.liyaqa.gym.domain.exceptions

import java.util.UUID

/**
 * Exception thrown when a domain format is invalid.
 */
class InvalidDomainException(domain: String) :
    RuntimeException("Invalid domain format: $domain")

/**
 * Exception thrown when a custom domain is already in use by another tenant.
 */
class DomainAlreadyInUseException(domain: String) :
    RuntimeException("Custom domain already in use: $domain")

/**
 * Exception thrown when no custom domain is configured for a tenant.
 */
class NoDomainConfiguredException :
    RuntimeException("No custom domain configured for this tenant")

/**
 * Exception thrown when DNS verification fails.
 */
class DomainVerificationFailedException(domain: String, reason: String) :
    RuntimeException("Domain verification failed for $domain: $reason")

/**
 * Exception thrown when SSL certificate provisioning fails.
 */
class SSLProvisioningFailedException(domain: String, reason: String) :
    RuntimeException("SSL certificate provisioning failed for $domain: $reason")

/**
 * Exception thrown when attempting to verify a domain that is not in pending status.
 */
class InvalidDomainStatusException(domain: String, currentStatus: String) :
    RuntimeException("Cannot verify domain $domain with current status: $currentStatus")

/**
 * Exception thrown when DNS propagation is not complete.
 */
class DNSNotPropagatedException(domain: String) :
    RuntimeException("DNS records for $domain have not fully propagated yet. Please wait and try again.")
