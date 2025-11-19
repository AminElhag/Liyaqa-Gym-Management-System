package com.liyaqa.infrastructure.payment.dto

import java.math.BigDecimal
import java.time.Instant

/**
 * Result of creating a recurring payment subscription
 */
data class RecurringPaymentResult(
    val success: Boolean,
    val subscriptionId: String?,
    val customerId: String?,
    val amount: BigDecimal,
    val currency: String,
    val schedule: RecurringSchedule,
    val status: RecurringPaymentStatus,
    val nextPaymentDate: Instant? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val createdAt: Instant = Instant.now(),
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun success(
            subscriptionId: String,
            customerId: String,
            amount: BigDecimal,
            currency: String,
            schedule: RecurringSchedule,
            nextPaymentDate: Instant,
            metadata: Map<String, String> = emptyMap()
        ): RecurringPaymentResult {
            return RecurringPaymentResult(
                success = true,
                subscriptionId = subscriptionId,
                customerId = customerId,
                amount = amount,
                currency = currency,
                schedule = schedule,
                status = RecurringPaymentStatus.ACTIVE,
                nextPaymentDate = nextPaymentDate,
                metadata = metadata
            )
        }

        fun failure(
            amount: BigDecimal,
            currency: String,
            schedule: RecurringSchedule,
            errorCode: String,
            errorMessage: String
        ): RecurringPaymentResult {
            return RecurringPaymentResult(
                success = false,
                subscriptionId = null,
                customerId = null,
                amount = amount,
                currency = currency,
                schedule = schedule,
                status = RecurringPaymentStatus.FAILED,
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        }
    }
}

enum class RecurringPaymentStatus {
    ACTIVE,
    PAUSED,
    CANCELLED,
    FAILED,
    EXPIRED
}

enum class RecurringSchedule {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}
