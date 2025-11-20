package com.liyaqa.gym.domain.formatters

import java.text.NumberFormat
import java.util.*

/**
 * Android implementation of CurrencyFormatter using Java NumberFormat
 */
actual class CurrencyFormatter {
    actual fun formatSAR(amount: Double, showCurrencyCode: Boolean): String {
        val format = NumberFormat.getCurrencyInstance(Locale("ar", "SA"))
        format.currency = Currency.getInstance("SAR")

        val formatted = format.format(amount)

        return if (showCurrencyCode) {
            // Replace the symbol with SAR code
            formatted.replace("ر.س", "SAR")
        } else {
            formatted
        }
    }

    actual fun format(amount: Double, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(currencyCode)
        return format.format(amount)
    }
}
