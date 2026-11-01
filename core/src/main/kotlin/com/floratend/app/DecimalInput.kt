package com.floratend.app

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

/** Editor numbers use the selected app locale; grouping is deliberately rejected
 * so a decimal separator from a different locale cannot silently change magnitude. */
object DecimalInput {
    fun number(value: String, locale: Locale = Locale.getDefault()): Double? {
        val text = value.trim()
        if (text.isEmpty()) return null
        val formatter = NumberFormat.getNumberInstance(locale).apply { isGroupingUsed = false }
        val position = ParsePosition(0)
        val number = formatter.parse(text, position)?.toDouble() ?: return null
        return number.takeIf { position.index == text.length && it.isFinite() }
    }
    fun integer(value: String, locale: Locale = Locale.getDefault()): Int? =
        number(value, locale)?.takeIf { it >= Int.MIN_VALUE && it <= Int.MAX_VALUE && it % 1.0 == 0.0 }?.toInt()

    fun format(value: Double, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = false
            maximumFractionDigits = 340
            if (this is DecimalFormat) isDecimalSeparatorAlwaysShown = false
        }.format(value)
}
