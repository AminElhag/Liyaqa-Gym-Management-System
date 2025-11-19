package com.liyaqa.gym.application.financial

import com.liyaqa.gym.common.exception.ResourceNotFoundException
import com.liyaqa.gym.common.exception.ValidationException
import com.liyaqa.gym.domain.entities.Invoice
import com.liyaqa.gym.domain.events.EventPublisher
import com.liyaqa.gym.domain.events.InvoiceClearedByZATCAEvent
import com.liyaqa.gym.domain.events.InvoiceSubmittedToZATCAEvent
import com.liyaqa.gym.domain.repositories.InvoiceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Use case for submitting invoices to ZATCA Fatoora platform for clearance.
 *
 * This use case handles:
 * - Converting invoice to ZATCA XML format (UBL 2.1)
 * - Signing invoice with digital certificate
 * - Submitting to ZATCA API
 * - Handling clearance response
 * - Retry logic for transient failures
 * - Storing clearance UUID
 * - Updating invoice status
 * - Domain event publishing
 *
 * Note: This is a simplified implementation. In production, you would:
 * - Use actual ZATCA SDK or API client
 * - Implement proper XML signing with digital certificates
 * - Handle all ZATCA error codes and validations
 * - Implement proper retry strategies for different failure types
 *
 * @property invoiceRepository Repository for invoice persistence
 * @property eventPublisher Publisher for domain events
 */
@Service
@Transactional
class SubmitInvoiceToZATCAUseCase(
    private val invoiceRepository: InvoiceRepository,
    private val eventPublisher: EventPublisher,
    @Value("\${zatca.api-url:https://gw-fatoora.zatca.gov.sa}") private val zatcaApiUrl: String,
    @Value("\${zatca.certificate-path:}") private val certificatePath: String,
    @Value("\${zatca.enabled:false}") private val zatcaEnabled: Boolean
) {

    private val logger = LoggerFactory.getLogger(SubmitInvoiceToZATCAUseCase::class.java)

    /**
     * Executes the ZATCA submission use case with retry logic.
     *
     * @param invoiceId The invoice to submit
     * @return Result containing true if submitted successfully, or error
     */
    @Retryable(
        value = [ZATCATransientException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0)
    )
    fun execute(invoiceId: UUID): Result<Boolean> {
        return runCatching {
            logger.info("Submitting invoice to ZATCA: $invoiceId")

            // 1. Retrieve invoice
            val invoice = getInvoice(invoiceId)

            // 2. Validate invoice is ready for submission
            validateInvoiceForSubmission(invoice)

            // 3. Mark as submitted
            val submittedInvoice = invoice.submitToZATCA()
            val savedSubmittedInvoice = invoiceRepository.save(submittedInvoice)
                .getOrElse { error ->
                    logger.error("Failed to update invoice status to submitted: ${error.message}", error)
                    throw error
                }

            // 4. Publish submission event
            publishSubmittedEvent(savedSubmittedInvoice)

            // 5. Convert invoice to ZATCA XML format (UBL 2.1)
            val zatcaXml = convertToZATCAXML(savedSubmittedInvoice)

            // 6. Sign invoice with digital certificate
            val signedXml = signInvoice(zatcaXml)

            // 7. Submit to ZATCA API
            val clearanceResult = submitToZATCA(savedSubmittedInvoice, signedXml)

            // 8. Handle clearance response
            val clearedInvoice = if (clearanceResult.approved) {
                handleApproval(savedSubmittedInvoice, clearanceResult)
            } else {
                handleRejection(savedSubmittedInvoice, clearanceResult)
            }

            // 9. Save final invoice state
            invoiceRepository.save(clearedInvoice)
                .getOrElse { error ->
                    logger.error("Failed to save invoice clearance status: ${error.message}", error)
                    throw error
                }

            logger.info("Invoice ${invoiceId} ZATCA submission completed with status: ${clearanceResult.approved}")
            clearanceResult.approved

        }.onFailure { error ->
            logger.error("Failed to submit invoice to ZATCA: ${error.message}", error)
        }
    }

    /**
     * Retrieves the invoice from the repository.
     *
     * @param invoiceId The invoice identifier
     * @return The invoice entity
     * @throws ResourceNotFoundException if invoice not found
     */
    private fun getInvoice(invoiceId: UUID): Invoice {
        val invoiceOptional = invoiceRepository.findById(invoiceId)
            .getOrElse { error ->
                logger.error("Failed to query invoice repository: ${error.message}", error)
                throw error
            }

        if (!invoiceOptional.isPresent) {
            logger.warn("Invoice not found: $invoiceId")
            throw ResourceNotFoundException("Invoice with ID $invoiceId not found")
        }

        return invoiceOptional.get()
    }

    /**
     * Validates that the invoice is ready for ZATCA submission.
     *
     * @param invoice The invoice to validate
     * @throws ValidationException if invoice is not ready
     */
    private fun validateInvoiceForSubmission(invoice: Invoice) {
        if (!invoice.isZATCAPending() && !invoice.isZATCAFailed()) {
            throw ValidationException(
                "Invoice ${invoice.id} cannot be submitted to ZATCA. Current status: ${invoice.zatcaStatus}"
            )
        }

        if (invoice.sellerVatRegistrationNumber.isBlank()) {
            throw ValidationException("Seller VAT registration number is required for ZATCA submission")
        }

        if (invoice.qrCode == null) {
            throw ValidationException("Invoice QR code is required for ZATCA submission")
        }

        logger.debug("Invoice validation passed for ZATCA submission: ${invoice.id}")
    }

    /**
     * Converts invoice to ZATCA-compliant XML format (UBL 2.1).
     *
     * In production, this would generate a complete UBL 2.1 XML document
     * with all required ZATCA fields and validations.
     *
     * @param invoice The invoice to convert
     * @return XML string in ZATCA format
     */
    private fun convertToZATCAXML(invoice: Invoice): String {
        logger.info("Converting invoice ${invoice.id} to ZATCA XML format")

        // Simplified XML generation for demonstration
        // In production, use a proper UBL 2.1 library or template
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2">
                <ID>${invoice.invoiceNumber}</ID>
                <IssueDate>${invoice.issueDate}</IssueDate>
                <InvoiceTypeCode>388</InvoiceTypeCode>
                <DocumentCurrencyCode>${invoice.totalAmount.currency.currencyCode}</DocumentCurrencyCode>
                <AccountingSupplierParty>
                    <Party>
                        <PartyIdentification>
                            <ID schemeID="CRN">${invoice.sellerVatRegistrationNumber}</ID>
                        </PartyIdentification>
                        <PartyName>
                            <Name>${invoice.sellerName}</Name>
                        </PartyName>
                    </Party>
                </AccountingSupplierParty>
                <AccountingCustomerParty>
                    <Party>
                        <PartyName>
                            <Name>${invoice.buyerName}</Name>
                        </PartyName>
                    </Party>
                </AccountingCustomerParty>
                <TaxTotal>
                    <TaxAmount currencyID="${invoice.vat.amount.currency.currencyCode}">${invoice.vat.amount.amount}</TaxAmount>
                </TaxTotal>
                <LegalMonetaryTotal>
                    <LineExtensionAmount currencyID="${invoice.subtotal.currency.currencyCode}">${invoice.subtotal.amount}</LineExtensionAmount>
                    <TaxExclusiveAmount currencyID="${invoice.subtotal.currency.currencyCode}">${invoice.subtotal.amount}</TaxExclusiveAmount>
                    <TaxInclusiveAmount currencyID="${invoice.totalAmount.currency.currencyCode}">${invoice.totalAmount.amount}</TaxInclusiveAmount>
                    <PayableAmount currencyID="${invoice.totalAmount.currency.currencyCode}">${invoice.totalAmount.amount}</PayableAmount>
                </LegalMonetaryTotal>
            </Invoice>
        """.trimIndent()
    }

    /**
     * Signs the invoice XML with digital certificate.
     *
     * In production, this would use actual cryptographic signing
     * with the organization's ZATCA-registered certificate.
     *
     * @param xml The XML to sign
     * @return Signed XML string
     */
    private fun signInvoice(xml: String): String {
        logger.info("Signing invoice XML with digital certificate")

        // In production, implement actual XML signing using:
        // - XMLSignature library
        // - Organization's digital certificate from ZATCA
        // - Proper canonicalization and signature algorithms

        // For now, return the XML as-is
        // This is just a placeholder for the actual signing logic
        return xml
    }

    /**
     * Submits the signed invoice to ZATCA API.
     *
     * In production, this would make actual HTTP requests to ZATCA API endpoints.
     *
     * @param invoice The invoice being submitted
     * @param signedXml The signed XML
     * @return Clearance result from ZATCA
     */
    private fun submitToZATCA(invoice: Invoice, signedXml: String): ZATCAClearanceResult {
        logger.info("Submitting signed invoice to ZATCA API: ${invoice.invoiceNumber}")

        if (!zatcaEnabled) {
            logger.warn("ZATCA integration is disabled. Simulating approval.")
            return ZATCAClearanceResult(
                approved = true,
                clearanceUUID = "SIM-${UUID.randomUUID()}",
                errorMessage = null
            )
        }

        // In production, implement actual ZATCA API call:
        // - POST to $zatcaApiUrl/invoices/clearance/single
        // - Include authentication headers
        // - Handle rate limiting
        // - Parse response and extract clearance UUID

        try {
            // Simulate ZATCA API call
            // In production, use RestTemplate or WebClient to call actual ZATCA API
            logger.info("Would submit to ZATCA API: $zatcaApiUrl")

            // For now, simulate successful clearance
            return ZATCAClearanceResult(
                approved = true,
                clearanceUUID = "ZATCA-${UUID.randomUUID()}",
                errorMessage = null
            )

        } catch (e: Exception) {
            logger.error("ZATCA API call failed: ${e.message}", e)

            // Determine if this is a transient failure that should be retried
            if (isTransientFailure(e)) {
                throw ZATCATransientException("Transient ZATCA API failure: ${e.message}", e)
            }

            // Permanent failure
            return ZATCAClearanceResult(
                approved = false,
                clearanceUUID = null,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Handles successful ZATCA clearance.
     *
     * @param invoice The invoice
     * @param result The clearance result
     * @return Updated invoice
     */
    private fun handleApproval(invoice: Invoice, result: ZATCAClearanceResult): Invoice {
        logger.info("Invoice ${invoice.id} cleared by ZATCA with UUID: ${result.clearanceUUID}")

        val clearedInvoice = invoice.markZATCACleared(result.clearanceUUID!!)

        // Publish cleared event
        publishClearedEvent(clearedInvoice)

        return clearedInvoice
    }

    /**
     * Handles ZATCA rejection.
     *
     * @param invoice The invoice
     * @param result The clearance result
     * @return Updated invoice
     */
    private fun handleRejection(invoice: Invoice, result: ZATCAClearanceResult): Invoice {
        logger.warn("Invoice ${invoice.id} rejected by ZATCA: ${result.errorMessage}")

        return invoice.markZATCAFailed(result.errorMessage ?: "Unknown rejection reason")
    }

    /**
     * Determines if an exception represents a transient failure that should be retried.
     *
     * @param exception The exception
     * @return True if transient, false if permanent
     */
    private fun isTransientFailure(exception: Exception): Boolean {
        // In production, check for specific error types:
        // - Network timeouts
        // - HTTP 5xx errors
        // - Temporary ZATCA service unavailability
        return exception.message?.contains("timeout", ignoreCase = true) == true ||
               exception.message?.contains("unavailable", ignoreCase = true) == true
    }

    /**
     * Publishes the InvoiceSubmittedToZATCAEvent.
     *
     * @param invoice The submitted invoice
     */
    private fun publishSubmittedEvent(invoice: Invoice) {
        try {
            val event = InvoiceSubmittedToZATCAEvent(
                invoiceId = invoice.id,
                invoiceNumber = invoice.invoiceNumber,
                timestamp = Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published InvoiceSubmittedToZATCAEvent for invoice: ${invoice.id}")

        } catch (e: Exception) {
            logger.error("Failed to publish InvoiceSubmittedToZATCAEvent for invoice ${invoice.id}: ${e.message}", e)
        }
    }

    /**
     * Publishes the InvoiceClearedByZATCAEvent.
     *
     * @param invoice The cleared invoice
     */
    private fun publishClearedEvent(invoice: Invoice) {
        try {
            val event = InvoiceClearedByZATCAEvent(
                invoiceId = invoice.id,
                invoiceNumber = invoice.invoiceNumber,
                zatcaClearanceUUID = invoice.zatcaClearanceUUID!!,
                timestamp = Instant.now()
            )

            eventPublisher.publish(event)
            logger.info("Published InvoiceClearedByZATCAEvent for invoice: ${invoice.id}")

        } catch (e: Exception) {
            logger.error("Failed to publish InvoiceClearedByZATCAEvent for invoice ${invoice.id}: ${e.message}", e)
        }
    }
}

/**
 * Result from ZATCA clearance attempt.
 */
data class ZATCAClearanceResult(
    val approved: Boolean,
    val clearanceUUID: String?,
    val errorMessage: String?
)

/**
 * Exception for transient ZATCA failures that should be retried.
 */
class ZATCATransientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
