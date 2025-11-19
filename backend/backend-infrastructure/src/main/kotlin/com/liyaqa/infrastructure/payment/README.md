# Payment Gateway Integration

This module provides a comprehensive payment gateway integration system for the Liyaqa Gym Management System, supporting multiple payment methods including international and Saudi-specific payment options.

## Supported Payment Gateways

### 1. Stripe
- **Payment Methods**: Credit cards, Debit cards, Apple Pay, Google Pay
- **Region**: International
- **Features**: One-time payments, recurring subscriptions, refunds
- **Documentation**: https://stripe.com/docs/api

### 2. Mada (via HyperPay)
- **Payment Methods**: Mada debit cards (Saudi national scheme)
- **Region**: Saudi Arabia
- **Features**: One-time payments, recurring payments, refunds
- **Documentation**: https://wordpresshyperpay.docs.oppwa.com/

### 3. STC Pay
- **Payment Methods**: STC Pay digital wallet
- **Region**: Saudi Arabia
- **Features**: Wallet payments, recurring subscriptions, refunds
- **Documentation**: Contact STC Pay for API documentation

## Architecture

### Core Components

1. **PaymentGateway Interface**: Defines the contract for all payment gateway implementations
2. **Gateway Implementations**:
   - `StripePaymentGateway`
   - `MadaPaymentGateway`
   - `STCPayPaymentGateway`
3. **PaymentGatewayFactory**: Routes payment requests to the appropriate gateway
4. **WebhookController**: Handles async payment notifications from gateways
5. **DTOs**: Payment result objects (`PaymentResult`, `RefundResult`, `RecurringPaymentResult`)

### Project Structure

```
payment/
├── dto/
│   ├── PaymentResult.kt
│   ├── RefundResult.kt
│   └── RecurringPaymentResult.kt
├── gateway/
│   ├── PaymentGateway.kt
│   ├── PaymentGatewayException.kt
│   ├── PaymentGatewayFactory.kt
│   ├── StripePaymentGateway.kt
│   ├── MadaPaymentGateway.kt
│   └── STCPayPaymentGateway.kt
└── config/
    └── PaymentGatewayProperties.kt
```

## Configuration

### Application Configuration

Add to your `application.yml`:

```yaml
payment:
  testMode: true  # Set to false in production

  stripe:
    enabled: true
    apiKey: ${STRIPE_API_KEY}
    webhookSecret: ${STRIPE_WEBHOOK_SECRET}

  mada:
    enabled: true
    merchantId: ${MADA_MERCHANT_ID}
    apiKey: ${MADA_API_KEY}
    entityId: ${MADA_ENTITY_ID}

  stcPay:
    enabled: true
    merchantId: ${STCPAY_MERCHANT_ID}
    apiKey: ${STCPAY_API_KEY}
    apiSecret: ${STCPAY_API_SECRET}
```

### Environment Variables

Set these environment variables:

```bash
# Stripe
export STRIPE_API_KEY=sk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...

# Mada
export MADA_MERCHANT_ID=...
export MADA_API_KEY=...
export MADA_ENTITY_ID=...

# STC Pay
export STCPAY_MERCHANT_ID=...
export STCPAY_API_KEY=...
export STCPAY_API_SECRET=...
```

## Usage

### Processing a Payment

```kotlin
@Service
class PaymentService(
    private val gatewayFactory: PaymentGatewayFactory,
    private val paymentRepository: PaymentRepository
) {
    fun processPayment(
        amount: Money,
        method: PaymentMethod,
        memberId: UUID
    ): PaymentResult {
        // Get the appropriate gateway
        val gateway = gatewayFactory.getGateway(method.name.lowercase())

        // Process payment
        val result = gateway.processPayment(
            amount = amount.amount,
            currency = amount.currency.currencyCode,
            method = method.name.lowercase(),
            metadata = mapOf(
                "memberId" to memberId.toString(),
                "description" to "Gym membership payment"
            )
        )

        return result
    }
}
```

### Processing a Refund

```kotlin
fun refundPayment(paymentId: String, amount: BigDecimal): RefundResult {
    val gateway = gatewayFactory.getGateway("stripe")
    return gateway.refund(paymentId, amount)
}
```

### Creating a Recurring Payment

```kotlin
fun createSubscription(
    amount: Money,
    schedule: RecurringSchedule,
    customerId: String
): RecurringPaymentResult {
    val gateway = gatewayFactory.getGateway("stripe")

    return gateway.createRecurringPayment(
        amount = amount.amount,
        currency = amount.currency.currencyCode,
        schedule = schedule,
        metadata = mapOf("customerId" to customerId)
    )
}
```

## Webhooks

Payment gateways send async notifications about payment status changes. The `PaymentWebhookController` handles these notifications.

### Webhook Endpoints

- **Stripe**: `POST /api/v1/webhooks/payments/stripe`
- **Mada**: `POST /api/v1/webhooks/payments/mada`
- **STC Pay**: `POST /api/v1/webhooks/payments/stcpay`

### Webhook Setup

1. **Stripe**: Configure webhook URL in Stripe Dashboard
2. **Mada**: Configure webhook URL in HyperPay merchant panel
3. **STC Pay**: Configure webhook URL with STC Pay support

### Webhook Security

All webhooks verify signatures to ensure authenticity:

- **Stripe**: Uses HMAC-SHA256 with timestamp verification
- **Mada**: Uses HMAC-SHA256 signature
- **STC Pay**: Uses HMAC-SHA256 with base64 encoding

## Error Handling

The system includes comprehensive error handling:

### Exception Types

- `PaymentProcessingException`: Payment processing failures
- `RefundProcessingException`: Refund processing failures
- `GatewayAuthenticationException`: Authentication failures
- `WebhookVerificationException`: Webhook verification failures
- `RecurringPaymentException`: Recurring payment setup failures

### Retry Logic

All gateway operations include automatic retry with exponential backoff:
- Max attempts: 3
- Initial delay: 1 second
- Multiplier: 2.0
- Max delay: 5 seconds

## Testing

### Test Mode

Enable test mode in configuration:

```yaml
payment:
  testMode: true
```

In test mode:
- Gateways use sandbox/test endpoints
- No real charges are made
- Test API keys should be used

### Unit Tests

Run tests:

```bash
./gradlew test --tests "*Payment*"
```

### Integration Testing

For integration testing with real gateways:

1. Use test API keys
2. Use test card numbers (provided by each gateway)
3. Verify webhook signatures with test secrets

## Monitoring

### Health Check

Check payment gateway status:

```
GET /api/v1/webhooks/payments/health
```

Response:
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00Z",
  "gateways": {
    "totalGateways": 3,
    "enabledGateways": 3,
    "supportedMethods": ["card", "mada", "stcpay"]
  }
}
```

### Logging

All payment operations are comprehensively logged:
- Payment processing attempts
- Gateway API calls
- Webhook receptions
- Error details

## Security Best Practices

1. **API Keys**: Store in environment variables, never commit to code
2. **Webhook Secrets**: Verify all webhook signatures
3. **HTTPS**: Always use HTTPS for webhook endpoints
4. **PCI Compliance**: Never store card details in your database
5. **Logging**: Sanitize logs to avoid logging sensitive data

## Production Checklist

- [ ] Set `testMode: false`
- [ ] Configure production API keys
- [ ] Set up webhook endpoints with HTTPS
- [ ] Configure webhook secrets
- [ ] Enable only required payment gateways
- [ ] Set appropriate timeout values
- [ ] Configure monitoring and alerting
- [ ] Test webhook delivery
- [ ] Verify refund functionality
- [ ] Set up error tracking

## Troubleshooting

### Payment Fails Immediately

- Check gateway is enabled in configuration
- Verify API keys are correct
- Check network connectivity to gateway
- Review application logs for errors

### Webhook Not Received

- Verify webhook URL is publicly accessible
- Check webhook secret is configured correctly
- Review gateway dashboard for webhook delivery status
- Check firewall/security rules

### Signature Verification Fails

- Ensure webhook secret matches gateway configuration
- Check for payload modification (proxies, middleware)
- Verify timestamp tolerance (Stripe)

## Support

For issues or questions:
- Check the logs first
- Review gateway documentation
- Contact gateway support for gateway-specific issues
- Open an issue in the project repository

## License

This payment integration is part of the Liyaqa Gym Management System.
