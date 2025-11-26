package com.liyaqa.gym.application.customdomain

import com.liyaqa.gym.domain.entities.tenant.DomainVerificationStatus
import com.liyaqa.gym.domain.exceptions.*
import com.liyaqa.gym.domain.repositories.TenantRepository
import com.liyaqa.gym.domain.services.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * Service for managing custom domains for tenants.
 * Handles domain verification, SSL provisioning, and domain lifecycle.
 */
@Service
class CustomDomainService(
    private val tenantRepository: TenantRepository,
    private val dnsVerificationService: DNSVerificationService,
    private val sslCertificateService: SSLCertificateService,
    private val eventPublisher: EventPublisher
) {

    /**
     * Add a custom domain to a tenant.
     * Generates verification token and provides DNS configuration instructions.
     */
    suspend fun addCustomDomain(
        tenantId: UUID,
        domain: String
    ): Result<CustomDomainStatus> {
        return try {
            // Find tenant
            val tenantOptional = tenantRepository.findById(tenantId).getOrThrow()
            if (!tenantOptional.isPresent) {
                return Result.failure(TenantNotFoundException(tenantId))
            }

            val tenant = tenantOptional.get()

            // Validate domain format
            if (!isValidDomain(domain)) {
                return Result.failure(InvalidDomainException(domain))
            }

            // Check if domain already in use
            val domainExists = tenantRepository.existsByCustomDomain(domain).getOrThrow()
            if (domainExists) {
                return Result.failure(DomainAlreadyInUseException(domain))
            }

            // Generate verification token
            val verificationToken = generateVerificationToken()

            // Update tenant with pending domain
            val updatedTenant = tenant.setCustomDomain(domain, verificationToken)
            tenantRepository.save(updatedTenant).getOrThrow()

            // Return domain status with verification instructions
            val domainStatus = CustomDomainStatus(
                domain = domain,
                status = DomainVerificationStatus.PENDING,
                verificationMethod = "DNS",
                dnsRecords = listOf(
                    DNSRecord(
                        type = "TXT",
                        name = "_liyaqa-verification",
                        value = verificationToken,
                        ttl = 3600
                    ),
                    DNSRecord(
                        type = "CNAME",
                        name = domain,
                        value = "${tenant.slug}.liyaqa.com",
                        ttl = 3600
                    )
                ),
                instructions = """
                    1. Add the TXT record to your DNS settings:
                       Type: TXT
                       Name: _liyaqa-verification
                       Value: $verificationToken

                    2. Add the CNAME record:
                       Type: CNAME
                       Name: @ (or your domain)
                       Value: ${tenant.slug}.liyaqa.com

                    3. Wait for DNS propagation (up to 48 hours)
                    4. Click "Verify Domain" to complete setup
                """.trimIndent(),
                verifiedAt = null,
                sslEnabled = false
            )

            Result.success(domainStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify a custom domain by checking DNS records.
     * Provisions SSL certificate if verification succeeds.
     */
    suspend fun verifyCustomDomain(tenantId: UUID): Result<CustomDomainStatus> {
        return try {
            // Find tenant
            val tenantOptional = tenantRepository.findById(tenantId).getOrThrow()
            if (!tenantOptional.isPresent) {
                return Result.failure(TenantNotFoundException(tenantId))
            }

            val tenant = tenantOptional.get()

            if (tenant.customDomain == null) {
                return Result.failure(NoDomainConfiguredException())
            }

            if (tenant.domainVerificationStatus != DomainVerificationStatus.PENDING) {
                return Result.failure(
                    InvalidDomainStatusException(
                        tenant.customDomain!!,
                        tenant.domainVerificationStatus?.name ?: "NONE"
                    )
                )
            }

            // Verify DNS records
            val txtVerified = dnsVerificationService.verifyTxtRecord(
                domain = tenant.customDomain!!,
                expectedValue = tenant.domainVerificationToken!!
            )

            val cnameVerified = dnsVerificationService.verifyCnameRecord(
                domain = tenant.customDomain!!,
                expectedTarget = "${tenant.slug}.liyaqa.com"
            )

            if (txtVerified && cnameVerified) {
                // Provision SSL certificate
                val sslResult = sslCertificateService.provisionCertificate(tenant.customDomain!!)

                if (sslResult.isSuccess) {
                    val sslCert = sslResult.getOrNull()
                    val updatedTenant = tenant.verifyCustomDomain(sslCert?.certificateId)
                    tenantRepository.save(updatedTenant).getOrThrow()

                    // Publish domain verified event
                    eventPublisher.publish(
                        CustomDomainVerifiedEvent(
                            tenantId = tenantId,
                            domain = tenant.customDomain!!,
                            occurredAt = Instant.now()
                        )
                    )

                    return Result.success(
                        CustomDomainStatus(
                            domain = tenant.customDomain!!,
                            status = DomainVerificationStatus.VERIFIED,
                            verifiedAt = updatedTenant.domainVerifiedAt,
                            sslEnabled = true,
                            verificationMethod = "DNS",
                            dnsRecords = null,
                            instructions = null
                        )
                    )
                } else {
                    // SSL provisioning failed
                    val error = sslResult.exceptionOrNull()
                    val updatedTenant = tenant.failDomainVerification()
                    tenantRepository.save(updatedTenant).getOrThrow()

                    eventPublisher.publish(
                        CustomDomainVerificationFailedEvent(
                            tenantId = tenantId,
                            domain = tenant.customDomain!!,
                            reason = "SSL provisioning failed: ${error?.message}",
                            occurredAt = Instant.now()
                        )
                    )

                    return Result.failure(
                        SSLProvisioningFailedException(
                            tenant.customDomain!!,
                            error?.message ?: "Unknown error"
                        )
                    )
                }
            } else {
                // DNS verification failed
                val errorMessage = when {
                    !txtVerified && !cnameVerified -> "Both TXT and CNAME records not found"
                    !txtVerified -> "TXT record not found or incorrect"
                    else -> "CNAME record not found or incorrect"
                }

                eventPublisher.publish(
                    CustomDomainVerificationFailedEvent(
                        tenantId = tenantId,
                        domain = tenant.customDomain!!,
                        reason = errorMessage,
                        occurredAt = Instant.now()
                    )
                )

                return Result.success(
                    CustomDomainStatus(
                        domain = tenant.customDomain!!,
                        status = DomainVerificationStatus.PENDING,
                        error = "DNS records not properly configured: $errorMessage",
                        verifiedAt = null,
                        sslEnabled = false,
                        verificationMethod = "DNS",
                        dnsRecords = null,
                        instructions = null
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the current status of a tenant's custom domain.
     */
    suspend fun getCustomDomainStatus(tenantId: UUID): Result<CustomDomainStatus?> {
        return try {
            val tenantOptional = tenantRepository.findById(tenantId).getOrThrow()
            if (!tenantOptional.isPresent) {
                return Result.failure(TenantNotFoundException(tenantId))
            }

            val tenant = tenantOptional.get()

            if (tenant.customDomain == null) {
                return Result.success(null)
            }

            val status = CustomDomainStatus(
                domain = tenant.customDomain!!,
                status = tenant.domainVerificationStatus ?: DomainVerificationStatus.PENDING,
                verifiedAt = tenant.domainVerifiedAt,
                sslEnabled = tenant.sslCertificateId != null,
                verificationMethod = "DNS",
                dnsRecords = if (tenant.domainVerificationStatus == DomainVerificationStatus.PENDING) {
                    listOf(
                        DNSRecord(
                            type = "TXT",
                            name = "_liyaqa-verification",
                            value = tenant.domainVerificationToken ?: "",
                            ttl = 3600
                        ),
                        DNSRecord(
                            type = "CNAME",
                            name = tenant.customDomain!!,
                            value = "${tenant.slug}.liyaqa.com",
                            ttl = 3600
                        )
                    )
                } else null,
                instructions = null
            )

            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove a custom domain from a tenant.
     */
    suspend fun removeCustomDomain(tenantId: UUID): Result<Unit> {
        return try {
            val tenantOptional = tenantRepository.findById(tenantId).getOrThrow()
            if (!tenantOptional.isPresent) {
                return Result.failure(TenantNotFoundException(tenantId))
            }

            val tenant = tenantOptional.get()

            if (tenant.customDomain == null) {
                return Result.failure(NoDomainConfiguredException())
            }

            val domain = tenant.customDomain!!

            // Revoke SSL certificate if exists
            if (tenant.sslCertificateId != null) {
                sslCertificateService.revokeCertificate(tenant.sslCertificateId!!)
                    .onFailure { e ->
                        // Log the error but continue with domain removal
                        println("Warning: Failed to revoke SSL certificate: ${e.message}")
                    }
            }

            // Remove domain from tenant
            val updatedTenant = tenant.removeCustomDomain()
            tenantRepository.save(updatedTenant).getOrThrow()

            // Publish domain removed event
            eventPublisher.publish(
                CustomDomainRemovedEvent(
                    tenantId = tenantId,
                    domain = domain,
                    occurredAt = Instant.now()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate domain format.
     */
    private fun isValidDomain(domain: String): Boolean {
        return domain.matches(Regex("^[a-z0-9][a-z0-9-]{0,61}[a-z0-9]\\.[a-z]{2,}$"))
    }

    /**
     * Generate a unique verification token.
     */
    private fun generateVerificationToken(): String {
        return "liyaqa-verify-${UUID.randomUUID()}"
    }
}

/**
 * Represents the status of a custom domain configuration.
 */
data class CustomDomainStatus(
    val domain: String,
    val status: DomainVerificationStatus,
    val verificationMethod: String? = null,
    val dnsRecords: List<DNSRecord>? = null,
    val instructions: String? = null,
    val verifiedAt: Instant? = null,
    val sslEnabled: Boolean = false,
    val error: String? = null
)
