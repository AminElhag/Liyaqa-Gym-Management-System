package com.liyaqa.gym.domain.formatters

/**
 * Platform-specific currency formatter interface
 * Implementations should use platform-native currency formatting
 */
expect class CurrencyFormatter() {
    /**
     * Format amount in Saudi Riyal (SAR)
     * @param amount The amount to format
     * @param showCurrencyCode Whether to show "SAR" in the output
     * @return Formatted currency string (e.g., "ر.س 100.00" or "SAR 100.00")
     */
    fun formatSAR(amount: Double, showCurrencyCode: Boolean = false): String

    /**
     * Format amount with custom currency code
     * @param amount The amount to format
     * @param currencyCode ISO currency code (e.g., "USD", "SAR")
     * @return Formatted currency string
     */
    fun format(amount: Double, currencyCode: String): String
}

/**
 * Common currency formatting utilities
 */
object CommonCurrencyFormatter {
    /**
     * Format SAR with Arabic Riyal symbol
     */
    fun formatSARWithSymbol(amount: Double): String {
        return "ر.س ${formatDecimal(amount)}"
    }

    /**
     * Format SAR with currency code
     */
    fun formatSARWithCode(amount: Double): String {
        return "SAR ${formatDecimal(amount)}"
    }

    /**
     * Format decimal amount to 2 decimal places
     */
    fun formatDecimal(amount: Double): String {
        return "%.2f".format(amount)
    }

    /**
     * Format amount as integer (no decimal places)
     */
    fun formatInteger(amount: Double): String {
        return "%.0f".format(amount)
    }

    /**
     * Parse SAR string to double
     * Handles formats like "ر.س 100.00", "SAR 100.00", "100.00"
     */
    fun parseSAR(value: String): Double? {
        val cleaned = value
            .replace("ر.س", "")
            .replace("SAR", "")
            .replace(",", "")
            .trim()

        return cleaned.toDoubleOrNull()
    }

    /**
     * Format amount with thousands separator
     */
    fun formatWithThousandsSeparator(amount: Double): String {
        val parts = formatDecimal(amount).split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else "00"

        val formattedInteger = integerPart
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()

        return "$formattedInteger.$decimalPart"
    }

    /**
     * Format SAR with thousands separator and symbol
     */
    fun formatSARFull(amount: Double): String {
        return "ر.س ${formatWithThousandsSeparator(amount)}"
    }
}
