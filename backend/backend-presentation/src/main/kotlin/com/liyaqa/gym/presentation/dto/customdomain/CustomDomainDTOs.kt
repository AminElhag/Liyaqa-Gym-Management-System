package com.liyaqa.gym.presentation.dto.customdomain

import com.liyaqa.gym.application.customdomain.CustomDomainStatus
import com.liyaqa.gym.domain.entities.tenant.DomainVerificationStatus
import java.time.Instant

/**
 * Request DTO for adding a custom domain.
 */
data class AddCustomDomainRequest(
    val domain: String
)

/**
 * Response DTO for custom domain status.
 */
data class CustomDomainStatusResponse(
    val domain: String,
    val status: DomainVerificationStatus,
    val verificationMethod: String? = null,
    val dnsRecords: List<DNSRecordDTO>? = null,
    val instructions: String? = null,
    val verifiedAt: Instant? = null,
    val sslEnabled: Boolean = false,
    val error: String? = null
)

/**
 * DTO for DNS record information.
 */
data class DNSRecordDTO(
    val type: String,
    val name: String,
    val value: String,
    val ttl: Int
)

/**
 * Response DTO for domain availability check.
 */
data class DomainAvailabilityResponse(
    val domain: String,
    val available: Boolean,
    val message: String?
)

/**
 * Extension function to convert CustomDomainStatus to CustomDomainStatusResponse.
 */
fun CustomDomainStatus.toResponse(): CustomDomainStatusResponse {
    return CustomDomainStatusResponse(
        domain = this.domain,
        status = this.status,
        verificationMethod = this.verificationMethod,
        dnsRecords = this.dnsRecords?.map {
            DNSRecordDTO(
                type = it.type,
                name = it.name,
                value = it.value,
                ttl = it.ttl
            )
        },
        instructions = this.instructions,
        verifiedAt = this.verifiedAt,
        sslEnabled = this.sslEnabled,
        error = this.error
    )
}
