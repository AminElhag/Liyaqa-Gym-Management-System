package com.liyaqa.gym.domain.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {

    @Test
    fun `valid email addresses should pass validation`() {
        assertTrue(EmailValidator.isValid("user@example.com"))
        assertTrue(EmailValidator.isValid("john.doe@company.co.uk"))
        assertTrue(EmailValidator.isValid("user+tag@example.com"))
        assertTrue(EmailValidator.isValid("123@test.com"))
    }

    @Test
    fun `invalid email addresses should fail validation`() {
        assertFalse(EmailValidator.isValid(""))
        assertFalse(EmailValidator.isValid("   "))
        assertFalse(EmailValidator.isValid("invalid"))
        assertFalse(EmailValidator.isValid("invalid@"))
        assertFalse(EmailValidator.isValid("@example.com"))
        assertFalse(EmailValidator.isValid("user@"))
        assertFalse(EmailValidator.isValid("user @example.com"))
    }

    @Test
    fun `validate method returns correct ValidationResult`() {
        val validResult = EmailValidator.validate("user@example.com")
        assertTrue(validResult is ValidationResult.Valid)

        val invalidResult = EmailValidator.validate("invalid")
        assertTrue(invalidResult is ValidationResult.Invalid)
    }
}
