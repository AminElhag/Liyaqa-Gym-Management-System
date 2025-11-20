package com.liyaqa.gym.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneValidatorTest {

    @Test
    fun `valid Saudi phone numbers should pass validation`() {
        assertTrue(PhoneValidator.isValidSaudi("+966512345678"))
        assertTrue(PhoneValidator.isValidSaudi("0512345678"))
        assertTrue(PhoneValidator.isValidSaudi("512345678"))
    }

    @Test
    fun `invalid Saudi phone numbers should fail validation`() {
        assertFalse(PhoneValidator.isValidSaudi(""))
        assertFalse(PhoneValidator.isValidSaudi("123"))
        assertFalse(PhoneValidator.isValidSaudi("+966612345678")) // Invalid prefix
        assertFalse(PhoneValidator.isValidSaudi("0612345678")) // Invalid local prefix
    }

    @Test
    fun `normalizeSaudi should convert to international format`() {
        assertEquals("+966512345678", PhoneValidator.normalizeSaudi("+966512345678"))
        assertEquals("+966512345678", PhoneValidator.normalizeSaudi("0512345678"))
        assertEquals("+966512345678", PhoneValidator.normalizeSaudi("512345678"))
    }

    @Test
    fun `general phone validation should work for international numbers`() {
        assertTrue(PhoneValidator.isValid("+1234567890"))
        assertTrue(PhoneValidator.isValid("+966512345678"))
        assertTrue(PhoneValidator.isValid("1234567890"))
    }
}
