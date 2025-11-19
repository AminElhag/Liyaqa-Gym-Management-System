# Financial Use Cases

This package contains comprehensive financial use cases for the Liyaqa Gym Management System with ZATCA (Saudi Arabia tax authority) compliance.

## Use Cases

### 1. ProcessPaymentUseCase
Processes payments with automatic VAT calculation and payment gateway integration.

**Features:**
- Automatic VAT calculation (15% for Saudi Arabia)
- Payment gateway integration (Stripe, MADA, STC Pay, etc.)
- Payment record creation with audit trail
- Transaction handling
- Domain event publishing

**Input:** `ProcessPaymentCommand`
- memberId, organizationId, branchId
- amount, currency, method
- subscriptionId, ptSessionId (optional)
- description, metadata

**Output:** `Result<UUID>` (Payment ID)

**Events Published:** `PaymentProcessedEvent`

---

### 2. GenerateInvoiceUseCase
Generates ZATCA-compliant invoices with QR codes.

**Features:**
- Subtotal calculation from line items
- Automatic VAT calculation (15%)
- Sequential invoice number generation
- ZATCA-compliant invoice creation with:
  - Seller information (gym name, VAT registration number)
  - Buyer information (member name, national ID)
  - Line items with descriptions (English & Arabic)
  - Subtotal, VAT breakdown, total
  - Timestamp
- QR code generation (ZATCA TLV format)
- XML and PDF generation (placeholder)

**Input:** `GenerateInvoiceCommand`
- memberId, organizationId, branchId
- lineItems (list of InvoiceLineItemCommand)
- dueDate (optional)
- notes (optional)

**Output:** `Result<UUID>` (Invoice ID)

**Events Published:** `InvoiceGeneratedEvent`

---

### 3. SubmitInvoiceToZATCAUseCase
Submits invoices to ZATCA Fatoora platform for clearance.

**Features:**
- Invoice to ZATCA XML format conversion (UBL 2.1)
- Digital certificate signing (placeholder)
- ZATCA API submission
- Clearance response handling
- Retry logic for transient failures (3 attempts with exponential backoff)
- Clearance UUID storage
- Invoice status updates

**Input:** Invoice ID (UUID)

**Output:** `Result<Boolean>` (true if cleared)

**Events Published:**
- `InvoiceSubmittedToZATCAEvent`
- `InvoiceClearedByZATCAEvent`

**Configuration:**
- `zatca.api-url` - ZATCA API endpoint
- `zatca.certificate-path` - Digital certificate path
- `zatca.enabled` - Enable/disable ZATCA integration

---

### 4. GenerateVATReportUseCase
Generates VAT reports for ZATCA portal submission.

**Features:**
- Aggregates all invoices in a specified period
- Calculates total output VAT (from sales)
- Calculates total input VAT (from expenses) - placeholder
- Generates VAT return data
- Exports in format suitable for ZATCA portal
- Branch-specific or organization-wide reporting

**Input:**
- startDate, endDate (LocalDate)
- branchId (optional, UUID)

**Output:** `Result<VATReportDTO>`

**Report Contains:**
- Total sales, output VAT
- Standard rated, zero rated, exempt sales
- Total purchases, input VAT
- Net VAT (output VAT - input VAT)
- Invoice statistics by ZATCA status

---

### 5. ProcessRefundUseCase
Processes payment refunds with policy validation.

**Features:**
- Refund policy validation (time limits)
- Refund amount calculation (prorated if configured)
- Payment gateway refund processing
- Credit note generation
- Invoice status updates
- Transaction handling
- Audit trail creation

**Input:** `ProcessRefundCommand`
- paymentId (UUID)
- amount (BigDecimal)
- reason (String)
- validatePolicy (Boolean, default: true)

**Output:** `Result<UUID>` (Refund ID)

**Events Published:** `RefundProcessedEvent`

**Configuration:**
- `refund.policy.max-days` - Maximum days for refund (default: 30)
- `refund.policy.prorate` - Enable proration (default: true)

**Proration Logic:**
- Same-day refunds: 100%
- After 1 week: 90%
- After 2 weeks: 80%
- Minimum: 50%

---

## Domain Entities

### Invoice
ZATCA-compliant tax invoice with:
- Seller and buyer information
- Line items
- VAT breakdown
- QR code
- ZATCA clearance status
- XML and PDF file paths

### Refund
Payment refund tracking with:
- Original payment reference
- Refund amount and reason
- Payment gateway refund ID
- Credit note information
- Processing status

### Payment
Financial transaction with:
- Amount, VAT, total
- Payment method
- Payment gateway details
- Invoice reference
- Refund tracking

---

## Value Objects

### InvoiceLineItem
Line item on an invoice with:
- Description (English & Arabic)
- Quantity
- Unit price
- Total amount

### Money
Monetary amount with currency:
- Amount (BigDecimal)
- Currency (Currency)
- Mathematical operations

### VAT
Value Added Tax with:
- Rate (15% for Saudi Arabia)
- Amount (Money)
- Percentage calculation

---

## Events

### Financial Events
- `PaymentProcessedEvent` - Payment completed successfully
- `InvoiceGeneratedEvent` - Invoice created
- `InvoiceSubmittedToZATCAEvent` - Invoice submitted to ZATCA
- `InvoiceClearedByZATCAEvent` - Invoice cleared by ZATCA
- `RefundProcessedEvent` - Refund completed

---

## ZATCA Compliance

### QR Code Format
Uses TLV (Tag-Length-Value) encoding as per ZATCA requirements:
1. Seller name
2. VAT registration number
3. Timestamp
4. Total amount (including VAT)
5. VAT amount

### Invoice Format
- UBL 2.1 XML format
- Digital signature (placeholder)
- All required ZATCA fields
- Arabic and English descriptions

### Clearance Process
1. Generate invoice with QR code
2. Convert to ZATCA XML format
3. Sign with digital certificate
4. Submit to ZATCA API
5. Wait for clearance response
6. Store clearance UUID
7. Update invoice status

---

## Configuration

Add these properties to `application.yml`:

```yaml
zatca:
  api-url: https://gw-fatoora.zatca.gov.sa
  certificate-path: /path/to/certificate.p12
  vat-registration-number: 300000000000003
  seller-name: Liyaqa Gym
  seller-name-arabic: ليّاقة للياقة البدنية
  enabled: true

refund:
  policy:
    max-days: 30
    prorate: true
```

---

## Transaction Handling

All financial operations are:
- **Transactional** - Using Spring `@Transactional`
- **Audited** - Creation and update timestamps
- **Event-driven** - Domain events published
- **Idempotent** - Safe to retry

---

## Error Handling

Uses `Result<T>` pattern for:
- Clean error handling
- No exceptions for business errors
- Explicit success/failure states

Common exceptions:
- `ResourceNotFoundException` - Entity not found
- `ValidationException` - Business rule violation
- `ZATCATransientException` - Temporary ZATCA failure (retryable)

---

## Future Enhancements

1. **ZATCA Integration**
   - Actual ZATCA SDK integration
   - Real digital certificate signing
   - Complete error code handling

2. **File Generation**
   - Actual XML generation using UBL library
   - PDF generation with invoice template
   - Credit note PDF generation

3. **Input VAT Tracking**
   - Expense invoice tracking
   - Input VAT calculation
   - Complete VAT report

4. **Advanced Refund Policies**
   - Configurable proration rules
   - Different policies per membership plan
   - Partial refund workflows

5. **Reporting**
   - Detailed financial reports
   - Payment reconciliation
   - ZATCA submission tracking
