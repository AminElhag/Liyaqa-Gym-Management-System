package com.liyaqa.gym.domain.valueobjects

/**
 * Value object representing a physical address.
 */
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val country: String,
    val postalCode: String
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(state.isNotBlank()) { "State cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
    }

    fun formattedAddress(): String {
        return "$street, $city, $state $postalCode, $country"
    }

    fun singleLine(): String {
        return "$street, $city, $state, $country $postalCode"
    }

    fun multiLine(): String {
        return buildString {
            appendLine(street)
            appendLine("$city, $state $postalCode")
            append(country)
        }
    }

    companion object {
        fun of(street: String, city: String, state: String, country: String, postalCode: String): Address {
            return Address(
                street.trim(),
                city.trim(),
                state.trim(),
                country.trim(),
                postalCode.trim().uppercase()
            )
        }

        fun saudiAddress(street: String, city: String, state: String, postalCode: String): Address {
            return of(street, city, state, "Saudi Arabia", postalCode)
        }
    }
}
