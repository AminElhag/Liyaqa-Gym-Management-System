package com.liyaqa.infrastructure.payment.dto

import com.liyaqa.gym.domain.payment.PaymentResult as DomainPaymentResult
import com.liyaqa.gym.domain.payment.PaymentTransactionStatus as DomainPaymentTransactionStatus

/**
 * Type aliases for backward compatibility.
 * All code should use the domain types.
 */
typealias PaymentResult = DomainPaymentResult
typealias PaymentTransactionStatus = DomainPaymentTransactionStatus
