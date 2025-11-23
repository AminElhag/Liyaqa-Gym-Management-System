package com.liyaqa.infrastructure.payment.dto

import com.liyaqa.gym.domain.payment.RecurringPaymentResult as DomainRecurringPaymentResult
import com.liyaqa.gym.domain.payment.RecurringPaymentStatus as DomainRecurringPaymentStatus
import com.liyaqa.gym.domain.payment.RecurringSchedule as DomainRecurringSchedule

/**
 * Type aliases for backward compatibility.
 * All code should use the domain types.
 */
typealias RecurringPaymentResult = DomainRecurringPaymentResult
typealias RecurringPaymentStatus = DomainRecurringPaymentStatus
typealias RecurringSchedule = DomainRecurringSchedule
