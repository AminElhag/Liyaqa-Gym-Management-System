package com.liyaqa.infrastructure.payment.gateway

import com.liyaqa.gym.domain.payment.PaymentGateway as DomainPaymentGateway

/**
 * Type alias for backward compatibility.
 * All implementations should use the domain interface.
 */
typealias PaymentGateway = DomainPaymentGateway
