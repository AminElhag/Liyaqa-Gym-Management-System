package com.liyaqa.gym.domain.formatters

import platform.Foundation.*

/**
 * iOS implementation of CurrencyFormatter using Foundation NumberFormatter
 */
actual class CurrencyFormatter {
    actual fun formatSAR(amount: Double, showCurrencyCode: Boolean): String {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        formatter.currencyCode = "SAR"
        formatter.locale = NSLocale("ar_SA")

        val formatted = formatter.stringFromNumber(NSNumber(amount)) ?: ""

        return if (showCurrencyCode) {
            formatted.replace("ر.س", "SAR")
        } else {
            formatted
        }
    }

    actual fun format(amount: Double, currencyCode: String): String {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        formatter.currencyCode = currencyCode

        return formatter.stringFromNumber(NSNumber(amount)) ?: ""
    }
}
