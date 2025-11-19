package com.liyaqa.gym.domain.valueobjects

import java.math.BigDecimal

/**
 * Value object representing a line item on an invoice.
 *
 * @property description Description of the item (e.g., "Monthly Gym Membership")
 * @property descriptionArabic Arabic description for ZATCA compliance
 * @property quantity Quantity of items
 * @property unitPrice Price per unit (excluding VAT)
 * @property totalAmount Total amount for this line item (quantity * unitPrice, excluding VAT)
 */
data class InvoiceLineItem(
    val description: String,
    val descriptionArabic: String?,
    val quantity: BigDecimal,
    val unitPrice: Money,
    val totalAmount: Money
) {
    init {
        require(description.isNotBlank()) { "Line item description cannot be blank" }
        require(quantity > BigDecimal.ZERO) { "Quantity must be positive" }
        require(!unitPrice.isNegative()) { "Unit price cannot be negative" }
        require(!totalAmount.isNegative()) { "Total amount cannot be negative" }

        // Verify total amount calculation
        val calculatedTotal = unitPrice * quantity
        require(totalAmount.amount == calculatedTotal.amount) {
            "Total amount must equal quantity * unit price"
        }
    }

    companion object {
        fun create(
            description: String,
            descriptionArabic: String?,
            quantity: BigDecimal,
            unitPrice: Money
        ): InvoiceLineItem {
            val totalAmount = unitPrice * quantity
            return InvoiceLineItem(
                description = description,
                descriptionArabic = descriptionArabic,
                quantity = quantity,
                unitPrice = unitPrice,
                totalAmount = totalAmount
            )
        }

        fun createSingle(
            description: String,
            descriptionArabic: String?,
            price: Money
        ): InvoiceLineItem {
            return create(
                description = description,
                descriptionArabic = descriptionArabic,
                quantity = BigDecimal.ONE,
                unitPrice = price
            )
        }
    }
}
