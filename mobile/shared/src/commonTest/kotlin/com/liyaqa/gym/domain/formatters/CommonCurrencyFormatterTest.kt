package com.liyaqa.gym.domain.formatters

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonCurrencyFormatterTest {

    @Test
    fun `formatDecimal should format to 2 decimal places`() {
        assertEquals("100.00", CommonCurrencyFormatter.formatDecimal(100.0))
        assertEquals("100.50", CommonCurrencyFormatter.formatDecimal(100.5))
        assertEquals("100.12", CommonCurrencyFormatter.formatDecimal(100.123))
    }

    @Test
    fun `formatSARWithSymbol should include Arabic symbol`() {
        assertEquals("ر.س 100.00", CommonCurrencyFormatter.formatSARWithSymbol(100.0))
        assertEquals("ر.س 250.50", CommonCurrencyFormatter.formatSARWithSymbol(250.5))
    }

    @Test
    fun `formatSARWithCode should include currency code`() {
        assertEquals("SAR 100.00", CommonCurrencyFormatter.formatSARWithCode(100.0))
        assertEquals("SAR 250.50", CommonCurrencyFormatter.formatSARWithCode(250.5))
    }

    @Test
    fun `formatWithThousandsSeparator should add commas`() {
        assertEquals("1,000.00", CommonCurrencyFormatter.formatWithThousandsSeparator(1000.0))
        assertEquals("10,000.50", CommonCurrencyFormatter.formatWithThousandsSeparator(10000.5))
        assertEquals("100,000.00", CommonCurrencyFormatter.formatWithThousandsSeparator(100000.0))
    }

    @Test
    fun `parseSAR should parse different formats`() {
        assertEquals(100.0, CommonCurrencyFormatter.parseSAR("100.00"))
        assertEquals(100.0, CommonCurrencyFormatter.parseSAR("ر.س 100.00"))
        assertEquals(100.0, CommonCurrencyFormatter.parseSAR("SAR 100.00"))
        assertEquals(1000.0, CommonCurrencyFormatter.parseSAR("1,000.00"))
    }
}
