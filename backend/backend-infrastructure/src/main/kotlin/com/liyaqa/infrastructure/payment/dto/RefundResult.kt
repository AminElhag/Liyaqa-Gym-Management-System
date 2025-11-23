package com.liyaqa.infrastructure.payment.dto

import com.liyaqa.gym.domain.payment.RefundResult as DomainRefundResult
import com.liyaqa.gym.domain.payment.RefundStatus as DomainRefundStatus

/**
 * Type aliases for backward compatibility.
 * All code should use the domain types.
 */
typealias RefundResult = DomainRefundResult
typealias RefundStatus = DomainRefundStatus
