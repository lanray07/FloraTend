package com.floratend.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.util.Locale

class MeasurementEditorTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val app get() = rule.activity.application as FloraApp
    private var tag = ""
    private val fixture = "qa-measurement-plant"
    private fun text(id: Int) = rule.activity.getString(id)
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        tag = AppLocales.selected(app)
        runBlocking {
            app.settings.set("onboarded", true)
            app.settings.set("imperial", false)
            app.database.dao().save(Plant(id=fixture, name="Decimal fern",category="Herb"))
        }
        AppLocales.choose(app, "de-DE")
        rule.activityRule.scenario.recreate()
        rule.waitUntil(15000) { runCatching { rule.activity.resources.configuration.locales[0].language == "de" }.getOrDefault(false) }
        rule.waitForIdle()
    }
    @After fun cleanup() {
        runBlocking { app.database.dao().plants().first().filter { it.id==fixture }.forEach { app.database.dao().delete(it) } }
        AppLocales.choose(app, tag)
        Locale.setDefault(AppLocales.current(app))
    }
    @Test fun germanDecimalCanBeSavedAndReopenedWithoutChangingMagnitude() {
        val journal = rule.onNodeWithText(text(R.string.journal), substring=false)
        if (rule.activity.resources.configuration.screenWidthDp >= 720) journal.performScrollTo()
        journal.performClick()
        rule.onNodeWithText(text(R.string.add_entry)).performClick()
        rule.onNodeWithText(text(R.string.height_cm)).performScrollTo().performTextInput("25,4")
        rule.onNodeWithText(text(R.string.save), substring=false).assertIsEnabled().performClick()
        rule.waitUntil(10000) { runBlocking { app.database.dao().journal().first().any { it.plantId==fixture } } }
        val entry = runBlocking { app.database.dao().journal().first().single { it.plantId==fixture } }
        assertEquals(25.4, entry.heightCm!!, 0.00001)
        rule.onNodeWithText(text(R.string.height_value).format("25,4")).performScrollTo().performClick()
        rule.onNodeWithText(text(R.string.height_cm)).assertTextContains("25,4")
    }
    @Test fun translatedCategoryNameCanBeSearched() {
        rule.onNodeWithText(text(R.string.search)).performTextInput(text(R.string.herbs))
        rule.onNodeWithText("Decimal fern").assertExists()
    }
}
