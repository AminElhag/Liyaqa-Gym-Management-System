package com.liyaqa.gym.domain.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NationalIdValidatorTest {

    @Test
    fun `valid national IDs should pass validation`() {
        assertTrue(NationalIdValidator.isValid("1234567890"))
        assertTrue(NationalIdValidator.isValid("2987654321"))
    }

    @Test
    fun `invalid national IDs should fail validation`() {
        assertFalse(NationalIdValidator.isValid(""))
        assertFalse(NationalIdValidator.isValid("123")) // Too short
        assertFalse(NationalIdValidator.isValid("12345678901")) // Too long
        assertFalse(NationalIdValidator.isValid("3234567890")) // Invalid first digit
        assertFalse(NationalIdValidator.isValid("0234567890")) // Invalid first digit
        assertFalse(NationalIdValidator.isValid("12345abc90")) // Contains letters
    }

    @Test
    fun `isSaudiNational should correctly identify Saudi nationals`() {
        assertTrue(NationalIdValidator.isSaudiNational("1234567890"))
        assertTrue(NationalIdValidator.isSaudiNational("2987654321"))
        assertFalse(NationalIdValidator.isSaudiNational("3234567890"))
    }
}
