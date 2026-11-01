package com.floratend.app

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runners.MethodSorters
import java.io.File

/** Real Android permission + SpeechRecognizer binder callbacks, synthetic provider
 * output only. This is deliberately not evidence of real audio recognition. */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SpeechContractTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val fixture get()=ComponentName(app.packageName,"com.floratend.app.FixtureRecognitionService")
    private val prefs get()=app.getSharedPreferences("qa_speech_fixture",0)
    private var feedback=true
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        feedback=runBlocking { app.settings.spokenFeedback.first() }
        runBlocking { app.settings.set("onboarded",true);app.settings.set("spoken_feedback",false) }
        AppLocales.choose(app,"en-GB")
        app.packageManager.setComponentEnabledSetting(fixture,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP)
        prefs.edit().clear().putString("scenario","result").commit()
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        val nav=rule.onNodeWithText("Voice",substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) nav.performScrollTo()
        nav.performClick()
    }
    @After fun restore() {
        app.packageManager.setComponentEnabledSetting(fixture,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,PackageManager.DONT_KILL_APP)
        runBlocking { app.settings.set("spoken_feedback",feedback) }
    }
    private fun grant() = InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(app.packageName,Manifest.permission.RECORD_AUDIO)
    private fun report(name:String,values:JSONObject) {
        val folder=File(app.getExternalFilesDir(null),"speech-contract").apply { mkdirs() }
        values.put("syntheticProviderOutput",true).put("realAudioRecognitionTested",false).put("microphoneAudioCaptured",false)
        File(folder,"$name.json").writeText(values.toString(2))
    }
    @Test fun aPermissionDenialKeepsTypedAlternative() {
        assertEquals(PackageManager.PERMISSION_DENIED,app.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
        assertEquals(0,prefs.getInt("starts",0))
        rule.onNodeWithText("Push to talk").performScrollTo().performClick()
        val device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val deny=device.wait(Until.findObject(By.text(java.util.regex.Pattern.compile("(?i)(deny|don.?t allow)"))),10000)
        assertNotNull("System microphone permission dialog must appear only after tap",deny)
        val folder=File(app.getExternalFilesDir(null),"speech-contract").apply { mkdirs() }
        val image=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()!!
        File(folder,"contextual-microphone-dialog.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) };image.recycle()
        deny!!.click()
        rule.onNodeWithText(rule.activity.getString(R.string.mic_denied)).assertExists()
        assertEquals(PackageManager.PERMISSION_DENIED,app.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
        assertEquals(0,prefs.getInt("starts",0))
        rule.onNodeWithText("Review or type a command").performTextInput("Read my reminders.")
        rule.onNodeWithText("Run command").performScrollTo().performClick()
        rule.onNodeWithText(rule.activity.resources.getQuantityString(R.plurals.reminder_count,0,0),substring=true).assertExists()
        report("permission-denial",JSONObject().put("permissionRequestedAfterTap",true).put("denialHandled",true).put("typedCommandStillWorks",true))
    }
    @Test fun bResultsAreVisibleBeforeCommandExecution() {
        grant()
        rule.onNodeWithText("Push to talk").performScrollTo().performClick()
        rule.onNodeWithText("Listening…").assertExists()
        rule.waitUntil(10000) { rule.activity.transcript=="Add a new plant." }
        rule.onNodeWithText("Review or type a command").assertTextContains("Add a new plant.")
        rule.onNodeWithText("Plant details",substring=false).assertDoesNotExist()
        rule.onNodeWithText("Run command").performScrollTo().performClick()
        rule.onNodeWithText("Plant details",substring=false).assertExists()
        rule.onNodeWithText("Cancel",substring=false).performClick()
        report("reviewed-results",JSONObject().put("listeningIndicatorDisplayed",true).put("transcriptDisplayed",true).put("commandRequiredExplicitRun",true))
    }
    @Test fun cErrorsAndBackgroundingEndListening() {
        grant();prefs.edit().putString("scenario","error").commit()
        rule.onNodeWithText("Push to talk").performScrollTo().performClick()
        rule.waitUntil(10000) { rule.activity.transcript==rule.activity.getString(R.string.voice_error) }
        assertFalse(rule.activity.listening)
        rule.onNodeWithText("Push to talk").assertIsEnabled()
        prefs.edit().putString("scenario","pending").commit()
        rule.onNodeWithText("Push to talk").performScrollTo().performClick()
        rule.waitUntil(5000) { rule.activity.listening && prefs.getInt("starts",0)>=2 }
        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.waitUntil(5000) { prefs.getInt("cancellations",0)>0 }
        assertFalse(rule.activity.listening)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        report("errors-and-stop",JSONObject().put("errorHandled",true).put("errorAllowsRetry",true).put("onStopCanceledService",true).put("listeningStateCleared",true))
    }
}
