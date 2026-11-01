package com.floratend.app

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File

class AccessibilityLayoutTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val app get() = rule.activity.application as FloraApp
    private val fixture = "qa-accessibility-plant"
    private var language = ""
    private val variant get() = InstrumentationRegistry.getArguments().getString("variant") ?: "default"
    private val folder get() = File(app.getExternalFilesDir(null),"accessibility/$variant").apply { mkdirs() }
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        language=AppLocales.selected(app)
        AppLocales.choose(app,"en-GB")
        runBlocking {
            app.settings.set("onboarded",true)
            app.database.dao().save(Plant(id=fixture,name="Accessible fern"))
            app.database.dao().save(CareSchedule(id=fixture,plantId=fixture))
        }
        rule.activityRule.scenario.recreate()
        rule.waitUntil(15000) { runCatching { rule.activity.resources.configuration.locales[0].toLanguageTag()=="en-GB" }.getOrDefault(false) }
        rule.waitForIdle()
        val config=rule.activity.resources.configuration
        File(folder,"configuration.json").writeText(JSONObject().put("widthDp",config.screenWidthDp).put("heightDp",config.screenHeightDp).put("fontScale",config.fontScale).put("densityDpi",config.densityDpi).toString(2))
    }
    @After fun cleanup() {
        runBlocking { app.database.dao().plants().first().filter { it.id==fixture }.forEach { app.database.dao().delete(it) } }
        AppLocales.choose(app,language)
    }
    private fun nav(label:String) {
        val node=rule.onNodeWithText(label,substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) node.performScrollTo()
        node.performClick()
    }
    private fun shot(name:String) {
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()!!
        File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
    }
    @Test fun primaryTouchBoundsAndLabels() {
        val density=rule.activity.resources.displayMetrics.density
        val results=JSONArray()
        for(label in listOf("Add plant","Garden","Calendar","Journal","Voice","Insights","Settings")) {
            val node=rule.onNodeWithText(label,substring=false)
            if(label!="Add plant" && rule.activity.resources.configuration.screenWidthDp>=720) node.performScrollTo()
            val bounds=node.fetchSemanticsNode().touchBoundsInRoot
            val width=bounds.width/density;val height=bounds.height/density
            results.put(JSONObject().put("label",label).put("widthDp",width).put("heightDp",height))
        }
        File(folder,"primary-touch-bounds.json").writeText(results.toString(2))
        for(i in 0 until results.length()) {
            val item=results.getJSONObject(i)
            assertTrue(item.toString(),item.getDouble("widthDp")>=47.5 && item.getDouble("heightDp")>=47.5)
        }
    }
    @Test fun primaryFlowsRemainReachableAtConfiguredFontAndScreenSize() {
        shot("garden")
        rule.onNodeWithText("Add plant").assertIsDisplayed()
        nav("Calendar");shot("calendar")
        rule.onNodeWithText("Record care",substring=false).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Add care schedule").assertIsDisplayed()
        nav("Journal");shot("journal")
        rule.onNodeWithText("Add journal entry").assertIsDisplayed()
        nav("Voice")
        rule.onNodeWithText("Run command").performScrollTo().assertIsDisplayed();shot("voice")
        nav("Settings")
        rule.onNodeWithText("Dark theme").performScrollTo().assertIsDisplayed();shot("settings")
    }
    @Test fun plantCardAccessibleLabelIdentifiesThePlant() {
        rule.onNodeWithText("Accessible fern").assert(hasContentDescription("Accessible fern",substring=true))
    }
}
