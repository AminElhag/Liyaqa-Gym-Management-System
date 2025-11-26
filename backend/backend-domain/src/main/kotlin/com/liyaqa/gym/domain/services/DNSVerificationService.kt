package com.liyaqa.gym.domain.services

/**
 * Service interface for DNS verification operations.
 * Used to verify domain ownership through DNS records.
 */
interface DNSVerificationService {

    /**
     * Verify that a TXT record exists with the expected value.
     *
     * @param domain The domain to verify
     * @param expectedValue The expected TXT record value
     * @return true if verification succeeds, false otherwise
     */
    suspend fun verifyTxtRecord(domain: String, expectedValue: String): Boolean

    /**
     * Verify that a CNAME record exists and points to the expected target.
     *
     * @param domain The domain to verify
     * @param expectedTarget The expected CNAME target
     * @return true if verification succeeds, false otherwise
     */
    suspend fun verifyCnameRecord(domain: String, expectedTarget: String): Boolean

    /**
     * Get all DNS records for a domain.
     *
     * @param domain The domain to query
     * @return List of DNS records
     */
    suspend fun getDnsRecords(domain: String): Result<List<DNSRecord>>

    /**
     * Check if DNS propagation is complete for a domain.
     *
     * @param domain The domain to check
     * @return true if DNS has propagated, false otherwise
     */
    suspend fun isDnsPropagated(domain: String): Boolean
}

/**
 * Represents a DNS record configuration.
 */
data class DNSRecord(
    val type: String,       // TXT, CNAME, A, AAAA, etc.
    val name: String,       // Record name/hostname
    val value: String,      // Record value/target
    val ttl: Int = 3600    // Time to live in seconds
)
