package com.floratend.app

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class DecimalInputTest {
    @Test fun decimalSeparatorsAndLocalizedDigitsPreserveMagnitude() {
        for ((tag, text) in listOf("en-GB" to "25.4", "de-DE" to "25,4", "fr-FR" to "25,4", "ar" to "٢٥٫٤")) {
            val locale = Locale.forLanguageTag(tag)
            assertEquals(tag, 25.4, DecimalInput.number(text, locale)!!, 0.000001)
            assertEquals(tag, 25.4, DecimalInput.number(DecimalInput.format(25.4, locale), locale)!!, 0.000001)
        }
    }
    @Test fun rejectsGroupingPartialNonfiniteAndOverflowValues() {
        for (text in listOf("25,4", "1,000", "25.4cm", "NaN", "∞", "", "1e999")) {
            assertNull(text, DecimalInput.number(text, Locale.UK))
        }
        assertNull(DecimalInput.number("25.4", Locale.GERMANY))
        assertNull(DecimalInput.integer("2.5", Locale.UK))
        assertNull(DecimalInput.integer("2147483648", Locale.UK))
        assertEquals(12, DecimalInput.integer("١٢", Locale.forLanguageTag("ar")))
    }
    @Test fun tinyMeasurementsDoNotBecomeZeroWhenReopened() {
        val value = 0.0000000000000254
        assertEquals(value, DecimalInput.number(DecimalInput.format(value, Locale.GERMANY), Locale.GERMANY)!!, 1e-30)
    }
}
