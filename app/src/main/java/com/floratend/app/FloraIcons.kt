package com.floratend.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Original compact line icons; avoids packaging an entire extended icon catalogue. */
object FloraIcons {
    private fun icon(name:String,data:String) = ImageVector.Builder(name,24.dp,24.dp,24f,24f).addPath(
        pathData=PathParser().parsePathString(data).toNodes(),
        stroke=SolidColor(Color.Black),strokeLineWidth=1.7f,
        strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round
    ).build()
    val Leaf by lazy { icon("Leaf","M6,20 Q7,12 17,6 M7,17 C1,6 11,3 21,3 C21,15 16,20 7,17") }
    val Mic by lazy { icon("Microphone","M9,5 A3,3 0,0 1,15 5 L15,11 A3,3 0,0 1,9 11 Z M6,10 L6,12 A6,6 0,0 0,18 12 L18,10 M12,18 L12,22 M9,22 L15,22") }
    val Book by lazy { icon("Journal","M12,5 Q7,2 2,4 L2,20 Q7,18 12,21 Q17,18 22,20 L22,4 Q17,2 12,5 L12,21") }
    val Chart by lazy { icon("Insights","M4,3 L4,21 L22,21 M8,17 L8,12 M13,17 L13,8 M18,17 L18,5") }
}
