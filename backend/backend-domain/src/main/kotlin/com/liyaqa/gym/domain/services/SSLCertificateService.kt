package com.liyaqa.gym.domain.services

import java.time.Instant

/**
 * Service interface for SSL/TLS certificate management.
 * Handles certificate provisioning and renewal for custom domains.
 */
interface SSLCertificateService {

    /**
     * Provision an SSL certificate for a domain.
     * Uses Let's Encrypt or similar certificate authority.
     *
     * @param domain The domain to provision a certificate for
     * @return Result containing the certificate information
     */
    suspend fun provisionCertificate(domain: String): Result<SSLCertificate>

    /**
     * Renew an existing SSL certificate.
     *
     * @param certificateId The ID of the certificate to renew
     * @return Result containing the renewed certificate information
     */
    suspend fun renewCertificate(certificateId: String): Result<SSLCertificate>

    /**
     * Revoke an SSL certificate.
     *
     * @param certificateId The ID of the certificate to revoke
     * @return Result indicating success or failure
     */
    suspend fun revokeCertificate(certificateId: String): Result<Unit>

    /**
     * Get certificate information.
     *
     * @param certificateId The ID of the certificate
     * @return Result containing the certificate information
     */
    suspend fun getCertificate(certificateId: String): Result<SSLCertificate>

    /**
     * Check if a certificate is about to expire (within 30 days).
     *
     * @param certificateId The ID of the certificate to check
     * @return true if certificate expires soon, false otherwise
     */
    suspend fun isCertificateExpiringSoon(certificateId: String): Boolean
}

/**
 * Represents an SSL/TLS certificate.
 */
data class SSLCertificate(
    val certificateId: String,
    val domain: String,
    val issuer: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val status: CertificateStatus
)

/**
 * SSL certificate status.
 */
enum class CertificateStatus {
    PENDING,    // Certificate provisioning in progress
    ACTIVE,     // Certificate is active and valid
    EXPIRED,    // Certificate has expired
    REVOKED     // Certificate has been revoked
}
