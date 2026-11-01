package com.floratend.app

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Constraints

/** Remeasure horizontal-scroll paragraphs at the actual Text size, using the same
 * resolved font, density, style and direction. RTL coordinates at Int.MAX_VALUE
 * paragraph widths lose float precision and cannot be compared to small controls. */
fun boundedTextLayout(original:TextLayoutResult):TextLayoutResult {
    if(original.multiParagraph.width<1_000_000f) return original
    check(original.size.width in 0..100_000) { "Actual text size must be finite and measurable" }
    val input=original.layoutInput
    return TextMeasurer(input.fontFamilyResolver,input.density,input.layoutDirection).measure(
        text=input.text,style=input.style,overflow=input.overflow,softWrap=input.softWrap,
        maxLines=input.maxLines,placeholders=input.placeholders,
        constraints=Constraints(maxWidth=original.size.width+1)
    )
}

fun textExceedsActualBounds(original:TextLayoutResult):Boolean {
    val layout=boundedTextLayout(original)
    return original.multiParagraph.didExceedMaxLines || layout.multiParagraph.didExceedMaxLines ||
        (0 until layout.lineCount).any { line ->
            layout.isLineEllipsized(line) ||
                layout.getLineRight(line)-layout.getLineLeft(line)>original.size.width+1f ||
                layout.getLineBottom(line)>original.size.height+1f
        }
}
