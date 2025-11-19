package com.liyaqa.gym.domain.valueobjects

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

/**
 * Value object representing a monetary amount with currency.
 * Immutable and includes validation for monetary operations.
 */
data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    init {
        require(amount.scale() <= currency.defaultFractionDigits) {
            "Amount scale cannot exceed ${currency.defaultFractionDigits} for currency $currency"
        }
    }

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return Money(amount.add(other.amount), currency)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return Money(amount.subtract(other.amount), currency)
    }

    operator fun times(multiplier: BigDecimal): Money {
        return Money(
            amount.multiply(multiplier).setScale(currency.defaultFractionDigits, RoundingMode.HALF_UP),
            currency
        )
    }

    operator fun times(multiplier: Int): Money {
        return this * BigDecimal(multiplier)
    }

    operator fun div(divisor: BigDecimal): Money {
        require(divisor != BigDecimal.ZERO) { "Cannot divide by zero" }
        return Money(
            amount.divide(divisor, currency.defaultFractionDigits, RoundingMode.HALF_UP),
            currency
        )
    }

    fun isPositive(): Boolean = amount > BigDecimal.ZERO

    fun isNegative(): Boolean = amount < BigDecimal.ZERO

    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "Cannot perform operation with different currencies: $currency and ${other.currency}"
        }
    }

    companion object {
        fun of(amount: Double, currencyCode: String): Money {
            val currency = Currency.getInstance(currencyCode)
            return Money(
                BigDecimal.valueOf(amount).setScale(currency.defaultFractionDigits, RoundingMode.HALF_UP),
                currency
            )
        }

        fun of(amount: BigDecimal, currencyCode: String): Money {
            val currency = Currency.getInstance(currencyCode)
            return Money(
                amount.setScale(currency.defaultFractionDigits, RoundingMode.HALF_UP),
                currency
            )
        }

        fun zero(currencyCode: String): Money {
            val currency = Currency.getInstance(currencyCode)
            return Money(BigDecimal.ZERO.setScale(currency.defaultFractionDigits), currency)
        }

        fun sar(amount: Double): Money = of(amount, "SAR")

        fun sar(amount: BigDecimal): Money = of(amount, "SAR")
    }
}
