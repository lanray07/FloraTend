package com.floratend.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CommandReviewTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun labelledEditorSupportsLatinRtlAndJapaneseWithoutRunningOnInput() = runBlocking {
        val app=rule.activity.application as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".perf") || BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val feedback=app.settings.spokenFeedback.first()
        val originalLocale=AppLocales.selected(app)
        val rows=JSONArray()
        app.settings.set("spoken_feedback",false)
        app.settings.set("onboarded",true)
        try {
            for((locale,text) in listOf("en-GB" to "Review this unknown command", "ar" to "مراجعة أمر تجريبي", "ja-JP" to "入力した言葉を確認")) {
                AppLocales.choose(app,locale)
                rule.activityRule.scenario.recreate()
                rule.waitForIdle()
                val nav=rule.onNodeWithText(rule.activity.getString(R.string.voice),substring=false)
                if(rule.activity.resources.configuration.screenWidthDp>=720) nav.performScrollTo()
                nav.performClick()
                val label=rule.activity.getString(R.string.command)
                rule.onNodeWithText(rule.activity.getString(R.string.run_command)).performScrollTo()
                rule.waitForIdle()
                onView(withId(R.id.voice_command)).check(matches(withHint(label))).perform(replaceText(text),closeSoftKeyboard())
                fun verifyText() { onView(withId(R.id.voice_command)).check(matches(withText(text))) }
                rule.waitForIdle()
                verifyText()
                rule.runOnUiThread {
                    val editor=rule.activity.findViewById<android.widget.EditText>(R.id.voice_command)
                    val node=editor.createAccessibilityNodeInfo()
                    assertTrue(node.isEditable)
                    assertEquals(label,node.hintText.toString())
                    assertEquals(text,node.text.toString())
                    val layout=requireNotNull(editor.layout)
                    val contentWidth=editor.width-editor.compoundPaddingLeft-editor.compoundPaddingRight
                    for(line in 0 until layout.lineCount) {
                        assertTrue(layout.getLineLeft(line)>=-1f)
                        assertTrue(layout.getLineRight(line)<=contentWidth+1f)
                    }
                    assertTrue(layout.height<=editor.height-editor.compoundPaddingTop-editor.compoundPaddingBottom)
                    rows.put(JSONObject().put("locale",locale).put("fontScale",rule.activity.resources.configuration.fontScale)
                        .put("screenWidthDp",rule.activity.resources.configuration.screenWidthDp)
                        .put("screenHeightDp",rule.activity.resources.configuration.screenHeightDp)
                        .put("nativeEditableAccessibilityNode",true).put("hintMatchesLocale",true)
                        .put("textLayoutWithinContentBounds",true))
                    node.recycle()
                }
                assertEquals(text,rule.activity.transcript)
                rule.onNodeWithText(rule.activity.getString(R.string.voice_unknown)).assertDoesNotExist()
                rule.onNodeWithText(rule.activity.getString(R.string.run_command)).performScrollTo().performClick()
                rule.onNodeWithText(rule.activity.getString(R.string.voice_unknown)).assertExists()
                verifyText()
            }
            val folder=File(app.getExternalFilesDir(null),"command-review").apply { mkdirs() }
            File(folder,"result.json").writeText(JSONObject().put("versionCode",BuildConfig.VERSION_CODE).put("locales",rows)
                .put("reviewBeforeExplicitRun",true).put("unknownCommandPreservesInput",true)
                .put("realMicrophoneRecognition",false).toString(2))
        } finally {
            app.settings.set("spoken_feedback",feedback)
            AppLocales.choose(app,originalLocale)
        }
    }
}
