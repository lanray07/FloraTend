package com.floratend.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.File
import android.graphics.Bitmap

class UiSmokeTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private fun shot(name:String) {
        rule.waitForIdle()
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val bitmap=instrumentation.uiAutomation.takeScreenshot() ?: error("Screenshot unavailable")
        val folder=File(instrumentation.targetContext.getExternalFilesDir(null),"acceptance").apply { mkdirs() }
        File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
    }
    @Test fun createPlantAndNavigateAllCoreScreens() {
        val app=rule.activity.application as FloraApp
        runBlocking { app.settings.set("onboarded",true) }
        rule.waitForIdle()
        rule.onNodeWithText("Add plant").performClick()
        rule.onNodeWithText("Plant nickname").performTextInput("Acceptance fern")
        rule.onNodeWithText("Species (optional)").performTextInput("Nephrolepis")
        rule.onNodeWithText("Location",substring=false).performTextInput("Study")
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Acceptance fern").fetchSemanticsNodes().isNotEmpty() }
        shot("01-garden")
        rule.onNodeWithText("Calendar",substring=false).performClick()
        rule.onNodeWithText("Add care schedule").performClick()
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitForIdle();shot("02-care")
        rule.onNodeWithText("Journal",substring=false).performClick();shot("03-journal")
        rule.onNodeWithText("Voice",substring=false).performClick();shot("04-voice")
        rule.onNodeWithText("Review or type a command").performTextInput("Read my reminders.")
        rule.onNodeWithText("Run command").performClick();rule.waitForIdle()
        rule.onNodeWithText("Insights",substring=false).performClick();shot("05-insights")
        rule.onNodeWithText("Settings",substring=false).performClick();shot("06-settings")
        rule.onNodeWithText("Dark theme").assertExists()
    }
}
