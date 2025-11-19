package com.liyaqa.gym.common.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility extension functions for common operations.
 */

fun LocalDateTime.toIsoString(): String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

fun String.isValidEmail(): Boolean {
    return this.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
}

fun String.isValidPhoneNumber(): Boolean {
    return this.matches(Regex("^\\+?[1-9]\\d{1,14}$"))
}

fun <T> T?.orThrow(message: String): T {
    return this ?: throw IllegalStateException(message)
}
