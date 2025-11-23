package com.liyaqa.gym.domain.valueobjects

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Value object representing VAT (Value Added Tax).
 * Handles VAT calculation and validation.
 */
data class VAT(
    val rate: BigDecimal,
    val amount: Money
) {
    init {
        require(rate >= BigDecimal.ZERO && rate <= BigDecimal.ONE) {
            "VAT rate must be between 0 and 1 (0% to 100%). Got: $rate"
        }
        require(!amount.isNegative()) {
            "VAT amount cannot be negative"
        }
    }

    val ratePercentage: BigDecimal
        get() = rate.multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)

    /**
     * Multiply VAT amount by an integer multiplier
     */
    operator fun times(multiplier: Int): VAT {
        return VAT(rate, amount.times(multiplier))
    }

    /**
     * Multiply VAT amount by a BigDecimal multiplier
     */
    operator fun times(multiplier: BigDecimal): VAT {
        return VAT(rate, amount.times(multiplier))
    }

    companion object {
        /**
         * Saudi Arabia standard VAT rate (15%)
         */
        val SAUDI_VAT_RATE = BigDecimal("0.15")

        fun calculateFromPrice(price: Money, vatRate: BigDecimal): VAT {
            val vatAmount = price * vatRate
            return VAT(vatRate, vatAmount)
        }

        fun calculateSaudiVAT(price: Money): VAT {
            return calculateFromPrice(price, SAUDI_VAT_RATE)
        }

        fun fromPriceIncludingVAT(priceIncludingVAT: Money, vatRate: BigDecimal): VAT {
            // Price including VAT = Price * (1 + VAT rate)
            // VAT Amount = Price including VAT - (Price including VAT / (1 + VAT rate))
            val divisor = BigDecimal.ONE.add(vatRate)
            val priceWithoutVAT = priceIncludingVAT / divisor
            val vatAmount = priceIncludingVAT - priceWithoutVAT
            return VAT(vatRate, vatAmount)
        }

        fun fromPriceIncludingSaudiVAT(priceIncludingVAT: Money): VAT {
            return fromPriceIncludingVAT(priceIncludingVAT, SAUDI_VAT_RATE)
        }

        fun zero(currency: String): VAT {
            return VAT(BigDecimal.ZERO, Money.zero(currency))
        }

        fun zeroSAR(): VAT {
            return zero("SAR")
        }
    }
}
