package com.floratend.app

import android.speech.tts.TextToSpeech
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File

class SpokenFeedbackTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private var original=true
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        original=runBlocking { app.settings.spokenFeedback.first() }
        runBlocking { app.settings.set("onboarded",true);app.settings.set("spoken_feedback",true) }
        AppLocales.choose(app,"en-GB")
        rule.activityRule.scenario.recreate()
        rule.waitUntil(15000) { rule.activity.speechOutputSupported!=null }
    }
    @After fun cleanup() { runBlocking { app.settings.set("spoken_feedback",original) } }
    @Test fun disablingFeedbackStopsRealTtsAndPreventsNewUtterances() {
        Assume.assumeTrue("Available installed English TTS engine required",rule.activity.speechOutputSupported==true)
        val field=MainActivity::class.java.getDeclaredField("tts").apply { isAccessible=true }
        val engine=field.get(rule.activity) as TextToSpeech
        val settings=rule.onNodeWithText("Settings",substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) settings.performScrollTo()
        settings.performClick()
        val words="This is a test of optional spoken feedback. ".repeat(30)
        rule.activityRule.scenario.onActivity { it.speak(words) }
        rule.waitUntil(10000) { engine.isSpeaking }
        rule.onNodeWithContentDescription("Spoken feedback").performScrollTo().performClick()
        rule.waitUntil(5000) { runBlocking { !app.settings.spokenFeedback.first() } && !engine.isSpeaking }
        rule.activityRule.scenario.onActivity { it.speak(words) }
        Thread.sleep(600)
        assertFalse(engine.isSpeaking)
        rule.activityRule.scenario.recreate()
        assertFalse(runBlocking { app.settings.spokenFeedback.first() })
        File(app.getExternalFilesDir(null),"spoken-feedback.json").writeText(JSONObject().put("realEngineStarted",true).put("disableStoppedPlayback",true).put("disabledSpeechDidNotStart",true).put("preferenceSurvivedRecreation",true).toString(2))
    }
}
