package com.floratend.app

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File

/** An available synthetic service must still not be started by the deferred profile.
 * This proves the release guard and typed UI, never actual microphone recognition. */
class ProductionVoiceScopeTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val fixture get()=ComponentName(app.packageName,"com.floratend.app.FixtureRecognitionService")
    private var oldTag=""
    private var feedback=true
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"));check(!BuildConfig.SPEECH_INPUT_ENABLED)
        oldTag=AppLocales.selected(app)
        feedback=runBlocking { app.settings.spokenFeedback.first() }
        runBlocking { app.settings.set("onboarded",true);app.settings.set("spoken_feedback",false) }
        app.packageManager.setComponentEnabledSetting(fixture,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP)
        app.getSharedPreferences("qa_speech_fixture",0).edit().clear().commit()
        AppLocales.choose(app,"en-GB");rule.activityRule.scenario.recreate()
    }
    @After fun restore() {
        app.packageManager.setComponentEnabledSetting(fixture,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,PackageManager.DONT_KILL_APP)
        runBlocking { app.settings.set("spoken_feedback",feedback) };AppLocales.choose(app,oldTag)
    }
    @Test fun deferredInputNeverRequestsMicrophoneOrStartsServiceAndTypedCommandStillNavigates() {
        val node=rule.onNodeWithText(rule.activity.getString(R.string.voice),substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720)node.performScrollTo()
        node.performClick();rule.waitForIdle()
        assertTrue("The available fixture distinguishes the release guard from missing hardware",SpeechRecognizer.isRecognitionAvailable(rule.activity))
        rule.onNodeWithText(rule.activity.getString(R.string.push_to_talk)).assertDoesNotExist()
        rule.onNodeWithText(rule.activity.getString(R.string.voice_unavailable)).assertIsDisplayed()
        rule.runOnIdle { rule.activity.listen() }
        assertEquals(PackageManager.PERMISSION_DENIED,app.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
        assertFalse(rule.activity.listening)
        assertEquals(0,app.getSharedPreferences("qa_speech_fixture",0).getInt("starts",0))
        enterCommand("Open my calendar.")
        rule.onNodeWithText(rule.activity.getString(R.string.run_command)).performScrollTo().performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.voice_unknown)).assertIsDisplayed()
        enterCommand("Show my care calendar.")
        rule.onNodeWithText(rule.activity.getString(R.string.run_command)).performScrollTo().performClick()
        rule.waitUntil(5000) { runCatching {rule.onNodeWithText(rule.activity.getString(R.string.care_calendar)).assertIsDisplayed()}.isSuccess }
        val dir=File(app.getExternalFilesDir(null),"production-scope").apply {mkdirs()}
        File(dir,"voice.json").writeText(JSONObject().put("speechInputEnabled",false).put("microphonePermissionGranted",false)
            .put("availableSyntheticServiceStarted",false).put("pushToTalkControlPresent",false).put("typedCalendarNavigationPassed",true)
            .put("unsupportedPhraseRejected",true).put("actualMicrophoneRecognitionTested",false).put("publisherApprovedDeferral",true).toString(2))
    }
}
