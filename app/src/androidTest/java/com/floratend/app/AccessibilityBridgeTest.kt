package com.floratend.app

import android.app.UiAutomation
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/** Native accessibility bridge, without Compose test clock and without suppressing VoiceView. */
class AccessibilityBridgeTest {
    @Test fun realVoiceViewServiceCanFocusAndInvokePrimaryControls() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as FloraApp
        val automation=instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        assertTrue(Settings.Secure.getString(app.contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty().contains("com.amazon.logan"))
        val id="qa-accessibility-bridge"
        AppLocales.choose(app,"en-GB")
        runBlocking { app.settings.set("onboarded",true);app.database.dao().plant(id)?.let { app.database.dao().delete(it) } }
        instrumentation.startActivitySync(Intent(app,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        fun nodes():List<AccessibilityNodeInfo> {
            val root=automation.rootInActiveWindow ?: return emptyList()
            if(root.packageName?.toString()!=app.packageName) return emptyList()
            val result=mutableListOf<AccessibilityNodeInfo>()
            fun walk(node:AccessibilityNodeInfo) { result+=node;repeat(node.childCount) { node.getChild(it)?.let(::walk) } }
            walk(root);return result
        }
        fun find(text:String):AccessibilityNodeInfo {
            val until=System.currentTimeMillis()+10000
            do {
                nodes().firstOrNull { it.text?.toString()?.trim()==text || it.contentDescription?.toString()==text || it.contentDescription?.toString()?.startsWith(text+".")==true }?.let { return it }
                Thread.sleep(80)
            } while(System.currentTimeMillis()<until)
            error("Native accessibility node missing: $text")
        }
        val results=JSONArray()
        fun activate(text:String) {
            var node=find(text)
            while(!node.isClickable && node.parent!=null) node=node.parent
            assertTrue("$text clickable",node.isClickable)
            val focus=node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
            assertTrue("$text accessibility focus",focus)
            assertTrue("$text accessibility click",node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            results.put(JSONObject().put("label",text).put("class",node.className).put("focus",focus).put("click",true))
            instrumentation.waitForIdleSync()
        }
        try {
            find(AppLocales.wrap(app).getString(R.string.your_garden))
            activate("Add plant")
            find("Plant details")
            val edit=nodes().first { it.isEditable }
            assertTrue(edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,"VoiceView bridge fern") }))
            instrumentation.waitForIdleSync()
            Thread.sleep(150)
            activate("Save")
            find("VoiceView bridge fern")
            val fixture=runBlocking { app.database.dao().exportPlants().single { it.name=="VoiceView bridge fern" } }
            runBlocking { app.database.dao().save(fixture.copy(id=id));app.database.dao().delete(fixture) }
            for((labelId,headingId) in listOf(R.string.calendar to R.string.care_calendar,R.string.journal to R.string.growth_journal,R.string.voice to R.string.voice_assistant,R.string.insights to R.string.insights_intro,R.string.settings to R.string.settings_intro)) {
                activate(AppLocales.wrap(app).getString(labelId));find(AppLocales.wrap(app).getString(headingId))
            }
            activate("Language: English (United Kingdom)")
            find("Language")
            activate("Close")
            val folder=File(app.getExternalFilesDir(null),"accessibility/voiceview-bridge").apply { mkdirs() }
            File(folder,"results.json").writeText(JSONObject().put("voiceViewServiceEnabled",true).put("dontSuppressAccessibilityServices",true)
                .put("nativeNodeActions",results).put("editableTextSetAndPersisted",true)
                .put("scope","Physical Fire HD10 API28; real VoiceView active, native accessibility focus/click/set-text; no audio recording or claim of multilingual pronunciation/manual touch-gesture review.").toString(2))
        } finally {
            runBlocking { app.database.dao().exportPlants().filter { it.id==id || it.name=="VoiceView bridge fern" }.forEach { app.database.dao().delete(it) } }
        }
    }
}
