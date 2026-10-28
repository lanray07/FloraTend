package com.floratend.app

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.SpeechRecognizer
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.time.LocalDate

class UiSmokeTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val fixture="FloraTend QA fern"
    @Before fun prepare() {
        runBlocking {
            app.settings.set("onboarded",true)
            app.settings.set("dark",false)
            app.database.dao().plants().first().filter { it.name.startsWith("FloraTend QA ") || it.name=="Acceptance fern" }.forEach { app.database.dao().delete(it) }
        }
        rule.waitForIdle()
    }
    @After fun cleanup() {
        runBlocking {
            app.settings.set("dark",false)
            app.database.dao().plants().first().filter { it.name.startsWith("FloraTend QA ") }.forEach { app.database.dao().delete(it) }
        }
    }
    private fun seed():Plant {
        val plant=Plant(id="acceptance-fern",name=fixture,species="Nephrolepis",location="Study")
        runBlocking { app.database.dao().save(plant) }
        rule.waitForIdle()
        return plant
    }
    private fun nav(label:String) {
        val node=rule.onNodeWithText(label,substring=false)
        if(InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration.screenWidthDp>=720) node.performScrollTo()
        node.performClick()
    }
    private fun waitText(text:String) { rule.waitUntil(10000) { rule.onAllNodesWithText(text,substring=false).fetchSemanticsNodes().isNotEmpty() } }
    private fun shot(name:String) {
        rule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        // Wait for the window compositor after Compose's semantic tree becomes idle.
        Thread.sleep(400)
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val bitmap=instrumentation.uiAutomation.takeScreenshot() ?: error("Screenshot unavailable")
        val folder=File(instrumentation.targetContext.getExternalFilesDir(null),"acceptance").apply { mkdirs() }
        File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
    }
    @Test fun createPlantAndNavigateAllCoreScreens() {
        rule.onNodeWithText("Add plant").performClick()
        rule.onNodeWithText("Plant nickname").performTextInput(fixture)
        rule.onNodeWithText("Species (optional)").performTextInput("Nephrolepis")
        rule.onNodeWithText("Location",substring=false).performTextInput("Study")
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Plant details",substring=false).fetchSemanticsNodes().isEmpty() && rule.onAllNodesWithText(fixture).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Your collection · 1 plant").assertExists()
        shot("01-garden")
        rule.onNodeWithText(fixture).performScrollTo().performClick();shot("07-plant")
        rule.onNodeWithText("Edit",substring=false).performClick()
        rule.onNodeWithText("Species (optional)").performTextReplacement("Boston fern")
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Plant details",substring=false).fetchSemanticsNodes().isEmpty() };waitText("Boston fern");rule.onNodeWithText("Boston fern").assertExists()
        rule.onNodeWithText("Search plants, species or locations").performTextInput("Study")
        rule.onNodeWithText(fixture).assertExists()
        rule.onNodeWithText("Search plants, species or locations").performTextReplacement("absent species")
        rule.onNodeWithText(fixture).assertDoesNotExist()
        rule.onNodeWithText("Search plants, species or locations").performTextClearance()
        nav("Calendar")
        rule.onNodeWithText("Add care schedule").performClick()
        rule.onNodeWithText("Save",substring=false).performClick()
        waitText("Record care");rule.onNodeWithText("Record care",substring=false).assertExists();shot("02-care")
        nav("Journal")
        rule.onNodeWithText("Add journal entry").performClick()
        rule.onNodeWithText("Notes and observations").performTextInput("Demonstration journal note; no growth measurement.")
        rule.onNodeWithText("Save",substring=false).performClick()
        waitText("Demonstration journal note; no growth measurement.");rule.onNodeWithText("Demonstration journal note; no growth measurement.").assertExists();shot("03-journal")
        nav("Voice")
        rule.onNodeWithText("A helping voice").assertIsDisplayed();shot("04-voice")
        rule.onNodeWithText("Review or type a command").performTextInput("Read my reminders.")
        rule.onNodeWithText("Run command").performScrollTo().performClick()
        rule.onNodeWithText("1 plant-care check to review. $fixture").performScrollTo().assertIsDisplayed()
        nav("Insights")
        rule.onNodeWithText("Plants in your collection").assertIsDisplayed();shot("05-insights")
        nav("Settings")
        rule.onNodeWithText("Dark theme").assertIsDisplayed();shot("06-settings")
        rule.onNodeWithText("Amazon product unavailable").assertIsNotEnabled()
    }
    @Test fun careActionsJournalEditsAndVoiceConfirmation() {
        val plant=seed()
        val schedule=CareSchedule(id="acceptance-schedule",plantId=plant.id)
        runBlocking { app.database.dao().save(schedule) }
        nav("Calendar")
        waitText("Pause");rule.onNodeWithText("Pause",substring=false).performScrollTo().performClick();waitText("Resume")
        rule.onNodeWithText("Record care",substring=false).assertIsNotEnabled()
        rule.onNodeWithText("Resume",substring=false).performScrollTo().performClick();waitText("Pause")
        rule.onNodeWithText("Snooze until tomorrow").performScrollTo().performClick()
        rule.waitUntil(10000) { runBlocking { app.database.dao().schedules().first().single { it.plantId==plant.id }.nextDate==LocalDate.now().plusDays(1).toString() } }
        rule.onNodeWithText("Next 7 days").performClick()
        rule.onNodeWithText("Reschedule",substring=false).performScrollTo().performClick()
        rule.onNodeWithText("Date (YYYY-MM-DD)").performTextReplacement(LocalDate.now().toString())
        rule.onNodeWithText("Save",substring=false).performClick()
        nav("Voice")
        rule.onNodeWithText("Review or type a command").performTextInput("Mark my $fixture as watered.")
        rule.onNodeWithText("Run command").performScrollTo().performClick()
        shot("14-water-confirmation")
        val state=org.json.JSONObject().put("transcript",rule.activity.transcript).put("parsedCommand",VoiceParser.parse(rule.activity.transcript).toString()).put("plantMatches",runBlocking { app.database.dao().plants().first().count { it.name.equals(fixture,true) } }).put("schedules",runBlocking { app.database.dao().schedules().first().filter { it.plantId==plant.id }.toString() })
        File(app.getExternalFilesDir(null),"acceptance/water-command-state.json").writeText(state.toString(2))
        rule.onNodeWithText("Confirm",substring=false).assertExists()
        assertTrue(runBlocking { app.database.dao().history().first().none { it.plantId==plant.id } })
        rule.onNodeWithText("Confirm",substring=false).performClick()
        rule.waitUntil(10000) { runBlocking { app.database.dao().history().first().count { it.plantId==plant.id }==1 } }
        assertEquals(LocalDate.now().plusDays(7).toString(),runBlocking { app.database.dao().schedules().first().single { it.plantId==plant.id }.nextDate })
        val entry=JournalEntry(id="acceptance-entry",plantId=plant.id,note="FloraTend QA original note")
        runBlocking { app.database.dao().save(entry) }
        nav("Journal")
        waitText(entry.note);rule.onNodeWithText(entry.note).performScrollTo().performClick()
        rule.onNodeWithText("Notes and observations").performTextReplacement("FloraTend QA edited note")
        rule.onNodeWithText("Save",substring=false).performClick()
        waitText("FloraTend QA edited note");rule.onNodeWithText("FloraTend QA edited note").performClick()
        rule.onNodeWithText("Delete",substring=false).performScrollTo().performClick()
        rule.onAllNodesWithText("Delete",substring=false).let { it[it.fetchSemanticsNodes().lastIndex].performClick() }
        rule.waitUntil(10000) { runBlocking { app.database.dao().journal().first().none { it.plantId==plant.id } } }
        nav("Garden")
        rule.onNodeWithText(fixture).performScrollTo().performClick()
        rule.onNodeWithText("Delete",substring=false).performScrollTo().performClick()
        rule.onAllNodesWithText("Delete",substring=false).let { it[it.fetchSemanticsNodes().lastIndex].performClick() }
        rule.waitUntil(10000) { runBlocking { app.database.dao().plant(plant.id)==null } }
    }
    @Test fun rotationDarkThemeAndRecognitionFallback() {
        seed()
        rule.activityRule.scenario.onActivity { it.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        rule.waitUntil(10000) { rule.activity.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_PORTRAIT }
        rule.onNodeWithText("Add plant").assertIsDisplayed();shot("10-portrait")
        runBlocking { app.settings.set("dark",true) }
        rule.waitForIdle();shot("11-portrait-dark")
        rule.activityRule.scenario.onActivity { it.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        rule.waitUntil(10000) { rule.activity.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE }
        rule.onNodeWithText("Add plant").assertIsDisplayed();shot("12-landscape-dark")
        nav("Voice")
        val folder=File(app.getExternalFilesDir(null),"acceptance").apply { mkdirs() }
        File(folder,"voice-capabilities.json").writeText(org.json.JSONObject().put("recognitionAvailable",SpeechRecognizer.isRecognitionAvailable(rule.activity)).put("microphonePermission",rule.activity.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)).put("locale",java.util.Locale.getDefault().toLanguageTag()).put("ttsServiceCount",rule.activity.packageManager.queryIntentServices(android.content.Intent(android.speech.tts.TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE),0).size).toString(2))
        if(!SpeechRecognizer.isRecognitionAvailable(rule.activity)) {
            rule.onNodeWithText("Push to talk").assertIsNotEnabled()
            rule.onNodeWithText("Speech recognition is unavailable on this device.",substring=true).assertIsDisplayed()
        }
        assertEquals(PackageManager.PERMISSION_DENIED,rule.activity.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO))
        rule.onNodeWithText("Review or type a command").performTextInput("Add a new plant.")
        rule.onNodeWithText("Run command").performScrollTo().performClick()
        rule.waitUntil(10000) { runCatching { rule.onNodeWithText("Plant details",substring=false).assertIsDisplayed();true }.getOrDefault(false) }
        rule.onNodeWithText("Plant details",substring=false).assertIsDisplayed()
        rule.onNodeWithText("Cancel",substring=false).performClick();shot("13-voice-fallback")
    }
    @Test fun privatePhotoImportAndMissingImageFallback() {
        val plant=seed()
        val original=File(app.cacheDir,"acceptance-source.jpg")
        val bitmap=Bitmap.createBitmap(2000,1200,Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.GREEN)
        original.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG,85,it) };bitmap.recycle()
        val path=Photos.import(app,Uri.fromFile(original))
        try {
            assertTrue(File(path).canonicalPath.startsWith(File(app.filesDir,"photos").canonicalPath+File.separator))
            val options=BitmapFactory.Options().apply { inJustDecodeBounds=true }
            BitmapFactory.decodeFile(path,options);assertTrue(options.outWidth<=1600 && options.outHeight<=1600)
            runBlocking { app.database.dao().save(JournalEntry(id="acceptance-photo",plantId=plant.id,note="FloraTend QA image fixture",photo=path)) }
            File(path).delete()
            nav("Journal")
            waitText("This photograph is no longer available.");rule.onNodeWithText("This photograph is no longer available.").assertIsDisplayed()
        } finally { File(path).delete();original.delete() }
    }
}
