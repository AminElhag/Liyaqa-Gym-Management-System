package com.liyaqa.gym.domain.valueobjects

/**
 * Value object representing contact information.
 * Validates email and phone number formats.
 */
data class ContactInfo(
    val email: String,
    val phone: String
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.matches(EMAIL_REGEX)) { "Invalid email format: $email" }
        require(phone.isNotBlank()) { "Phone cannot be blank" }
        require(phone.matches(PHONE_REGEX)) { "Invalid phone format: $phone" }
    }

    fun maskedEmail(): String {
        val parts = email.split("@")
        if (parts.size != 2) return email
        val localPart = parts[0]
        val domain = parts[1]
        val masked = if (localPart.length <= 2) {
            "*".repeat(localPart.length)
        } else {
            localPart.take(2) + "*".repeat(localPart.length - 2)
        }
        return "$masked@$domain"
    }

    fun maskedPhone(): String {
        return if (phone.length <= 4) {
            "*".repeat(phone.length)
        } else {
            "*".repeat(phone.length - 4) + phone.takeLast(4)
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        private val PHONE_REGEX = Regex("^\\+?[0-9]{10,15}\$")

        fun of(email: String, phone: String): ContactInfo {
            return ContactInfo(email.trim().lowercase(), phone.trim())
        }
    }
}
