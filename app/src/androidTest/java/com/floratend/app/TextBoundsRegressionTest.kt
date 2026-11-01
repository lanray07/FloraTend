package com.floratend.app

import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TextBoundsRegressionTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun realRtlScrollParagraphFitsButActuallyEllipsizedTextFails() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        rule.activityRule.scenario.onActivity { activity -> activity.setContent {
            MaterialTheme { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        FilterChip(selected=false,onClick={},label={Text("نباتات المنزل",Modifier.testTag("fits"))})
                    }
                    Text("نص طويل لا يتسع داخل هذا العرض",Modifier.width(24.dp).testTag("clipped"),maxLines=1,overflow=TextOverflow.Ellipsis)
                }
            } }
        } }
        rule.waitForIdle()
        val nodes=listOf("fits","clipped").associateWith { rule.onNodeWithTag(it,useUnmergedTree=true).fetchSemanticsNode() }
        fun layout(tag:String):TextLayoutResult {
            val values=mutableListOf<TextLayoutResult>()
            nodes.getValue(tag).config[SemanticsActions.GetTextLayoutResult].action!!.invoke(values)
            return values.single()
        }
        rule.runOnIdle {
            val fitting=layout("fits")
            assertTrue("Exercise the actual unbounded horizontal paragraph",fitting.multiParagraph.width>=1_000_000f)
            assertTrue(boundedTextLayout(fitting).multiParagraph.width<1_000_000f)
            assertFalse(textExceedsActualBounds(fitting))
            assertTrue(textExceedsActualBounds(layout("clipped")))
        }
    }
}
