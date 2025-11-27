package com.liyaqa.gym.domain.valueobjects

/**
 * Value object representing calculated usage charges for a billing period.
 * Immutable and includes all usage-based charges broken down by type.
 */
data class UsageCharges(
    val smsCount: Int,
    val smsCharges: Money,
    val emailCount: Int,
    val emailCharges: Money,
    val apiCallsCount: Int,
    val apiCharges: Money,
    val storageGB: Int,
    val storageCharges: Money,
    val totalCharges: Money
) {
    init {
        require(smsCount >= 0) { "SMS count cannot be negative" }
        require(emailCount >= 0) { "Email count cannot be negative" }
        require(apiCallsCount >= 0) { "API calls count cannot be negative" }
        require(storageGB >= 0) { "Storage GB cannot be negative" }
        require(!smsCharges.isNegative()) { "SMS charges cannot be negative" }
        require(!emailCharges.isNegative()) { "Email charges cannot be negative" }
        require(!apiCharges.isNegative()) { "API charges cannot be negative" }
        require(!storageCharges.isNegative()) { "Storage charges cannot be negative" }
        require(!totalCharges.isNegative()) { "Total charges cannot be negative" }

        // Verify all charges use same currency
        val currency = totalCharges.currency
        require(smsCharges.currency == currency) { "SMS charges must use same currency" }
        require(emailCharges.currency == currency) { "Email charges must use same currency" }
        require(apiCharges.currency == currency) { "API charges must use same currency" }
        require(storageCharges.currency == currency) { "Storage charges must use same currency" }

        // Verify total equals sum of individual charges
        val calculatedTotal = smsCharges + emailCharges + apiCharges + storageCharges
        require(totalCharges.amount == calculatedTotal.amount) {
            "Total charges must equal sum of individual charges"
        }
    }

    /**
     * Check if there are any charges.
     */
    fun hasCharges(): Boolean = totalCharges.isPositive()

    /**
     * Check if there are no charges.
     */
    fun hasNoCharges(): Boolean = totalCharges.isZero()

    /**
     * Get a breakdown of charges by type.
     */
    fun getBreakdown(): Map<String, ChargeBreakdown> {
        return mapOf(
            "sms" to ChargeBreakdown("SMS Messages", smsCount, smsCharges),
            "email" to ChargeBreakdown("Email Messages", emailCount, emailCharges),
            "api" to ChargeBreakdown("API Calls", apiCallsCount, apiCharges),
            "storage" to ChargeBreakdown("Storage (GB)", storageGB, storageCharges)
        )
    }

    /**
     * Get only charges that have a non-zero amount.
     */
    fun getNonZeroCharges(): Map<String, ChargeBreakdown> {
        return getBreakdown().filter { it.value.charges.isPositive() }
    }

    companion object {
        /**
         * Create zero usage charges with specified currency.
         */
        fun zero(currencyCode: String): UsageCharges {
            val zeroMoney = Money.zero(currencyCode)
            return UsageCharges(
                smsCount = 0,
                smsCharges = zeroMoney,
                emailCount = 0,
                emailCharges = zeroMoney,
                apiCallsCount = 0,
                apiCharges = zeroMoney,
                storageGB = 0,
                storageCharges = zeroMoney,
                totalCharges = zeroMoney
            )
        }

        /**
         * Create usage charges from individual components.
         */
        fun create(
            smsCount: Int,
            smsCharges: Money,
            emailCount: Int,
            emailCharges: Money,
            apiCallsCount: Int,
            apiCharges: Money,
            storageGB: Int,
            storageCharges: Money
        ): UsageCharges {
            val totalCharges = smsCharges + emailCharges + apiCharges + storageCharges
            return UsageCharges(
                smsCount = smsCount,
                smsCharges = smsCharges,
                emailCount = emailCount,
                emailCharges = emailCharges,
                apiCallsCount = apiCallsCount,
                apiCharges = apiCharges,
                storageGB = storageGB,
                storageCharges = storageCharges,
                totalCharges = totalCharges
            )
        }
    }
}

/**
 * Breakdown of charges for a specific usage type.
 */
data class ChargeBreakdown(
    val description: String,
    val quantity: Int,
    val charges: Money
) {
    init {
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(quantity >= 0) { "Quantity cannot be negative" }
        require(!charges.isNegative()) { "Charges cannot be negative" }
    }

    /**
     * Get unit price if quantity is greater than 0.
     */
    fun getUnitPrice(): Money? {
        return if (quantity > 0) {
            charges / quantity.toBigDecimal()
        } else {
            null
        }
    }
}
