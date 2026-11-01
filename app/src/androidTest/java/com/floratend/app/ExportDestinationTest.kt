package com.floratend.app

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*

/** Actual Android DocumentsUI destination and Activity recreation; QA-only entitlement fixture. */
class ExportDestinationTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val device get()=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val fixture="qa-export-rotation"
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        AppLocales.choose(app,"en-GB")
        runBlocking { app.settings.set("onboarded",true); app.database.dao().save(Plant(id=fixture,name="Export fern – 東京",notes="Private fixture, quoted: \"green\"")) }
        rule.activityRule.scenario.recreate()
        // Debug SANDBOX bookkeeping only, never evidence of an Amazon receipt.
        app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).edit()
            .putString("mode","SANDBOX").putString("owner","qa-export-owner")
            .putString("receipt","qa-export-receipt").putBoolean("active",true).commit()
        runBlocking { app.settings.set("premium",true) }
    }
    @After fun cleanup() {
        device.unfreezeRotation()
        runBlocking { app.database.dao().plant(fixture)?.let { app.database.dao().delete(it) };app.settings.set("premium",false) }
        app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).edit().clear().commit()
    }
    @Test fun destinationReturnAfterRecreationWritesCompleteUtf8Snapshot() {
        val settings=rule.onNodeWithText("Settings",substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) settings.performScrollTo()
        settings.performClick()
        rule.onNodeWithText("Export JSON",substring=false).performScrollTo().assertIsEnabled().performClick()
        assertTrue("Android document picker",device.wait(Until.hasObject(By.pkg("com.android.documentsui")),10000))
        val name=device.findObject(By.clazz("android.widget.EditText"))
        assertNotNull(name)
        val filename="floratend-qa-rotation-${System.currentTimeMillis()}.json"
        device.setOrientationNatural()
        device.waitForIdle()
        // DocumentsUI may recreate its own filename field; choose the final destination after rotation.
        val finalName=device.wait(Until.findObject(By.clazz("android.widget.EditText")),5000)
        assertNotNull(finalName)
        finalName.text=filename
        java.io.File(app.getExternalFilesDir(null),"export-filename.txt").writeText(filename)
        val save=device.wait(Until.findObject(By.res("com.android.documentsui","button1")),5000)
            ?: device.findObject(By.text("Save")) ?: device.findObject(By.text("SAVE"))
        assertNotNull("DocumentsUI Save",save)
        save.click()
        rule.waitUntil(15000) { rule.onAllNodesWithText("Export saved.").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Export saved.").assertExists()
    }
}
