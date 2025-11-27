package com.liyaqa.infrastructure.persistence.entities

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * JPA entity for PricingConfig.
 * Stores usage-based pricing configuration.
 */
@Entity
@Table(
    name = "pricing_configs",
    indexes = [
        Index(name = "idx_pricing_config_is_active", columnList = "is_active"),
        Index(name = "idx_pricing_config_effective_from", columnList = "effective_from"),
        Index(name = "idx_pricing_config_effective_until", columnList = "effective_until")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class PricingConfigJpaEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean,

    // SMS Pricing Configuration
    @Column(name = "sms_free_quota", nullable = false)
    var smsFreeQuota: Int,

    @Column(name = "sms_price_per_unit", nullable = false, precision = 19, scale = 4)
    var smsPricePerUnit: BigDecimal,

    @Column(name = "sms_unit", nullable = false, length = 50)
    var smsUnit: String,

    @Column(name = "sms_currency", nullable = false, length = 3)
    var smsCurrency: String,

    // Email Pricing Configuration
    @Column(name = "email_free_quota", nullable = false)
    var emailFreeQuota: Int,

    @Column(name = "email_price_per_unit", nullable = false, precision = 19, scale = 4)
    var emailPricePerUnit: BigDecimal,

    @Column(name = "email_unit", nullable = false, length = 50)
    var emailUnit: String,

    @Column(name = "email_currency", nullable = false, length = 3)
    var emailCurrency: String,

    // API Pricing Configuration
    @Column(name = "api_free_quota", nullable = false)
    var apiFreeQuota: Int,

    @Column(name = "api_price_per_unit", nullable = false, precision = 19, scale = 4)
    var apiPricePerUnit: BigDecimal,

    @Column(name = "api_unit", nullable = false, length = 50)
    var apiUnit: String,

    @Column(name = "api_currency", nullable = false, length = 3)
    var apiCurrency: String,

    // Storage Pricing Configuration
    @Column(name = "storage_free_quota", nullable = false)
    var storageFreeQuota: Int,

    @Column(name = "storage_price_per_unit", nullable = false, precision = 19, scale = 4)
    var storagePricePerUnit: BigDecimal,

    @Column(name = "storage_unit", nullable = false, length = 50)
    var storageUnit: String,

    @Column(name = "storage_currency", nullable = false, length = 3)
    var storageCurrency: String,

    @Column(name = "effective_from", nullable = false)
    var effectiveFrom: Instant,

    @Column(name = "effective_until")
    var effectiveUntil: Instant? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    constructor() : this(
        id = UUID.randomUUID(),
        name = "",
        isActive = false,
        smsFreeQuota = 0,
        smsPricePerUnit = BigDecimal.ZERO,
        smsUnit = "message",
        smsCurrency = "SAR",
        emailFreeQuota = 0,
        emailPricePerUnit = BigDecimal.ZERO,
        emailUnit = "email",
        emailCurrency = "SAR",
        apiFreeQuota = 0,
        apiPricePerUnit = BigDecimal.ZERO,
        apiUnit = "call",
        apiCurrency = "SAR",
        storageFreeQuota = 0,
        storagePricePerUnit = BigDecimal.ZERO,
        storageUnit = "GB",
        storageCurrency = "SAR",
        effectiveFrom = Instant.now()
    )
}
