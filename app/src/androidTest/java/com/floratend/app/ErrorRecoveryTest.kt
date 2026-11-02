package com.floratend.app

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.*
import java.io.IOException

/** Controlled storage failure in isolated QA; user-facing rendering and retry use the real UI. */
class ErrorRecoveryTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val app get() = rule.activity.application as FloraApp
    private var model: PlantViewModel? = null
    private var savedId: String? = null
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        AppLocales.choose(app, "en-GB")
        runBlocking { app.settings.set("onboarded", true) }
        rule.activityRule.scenario.recreate()
    }
    @After fun cleanup() {
        model?.viewModelScope?.cancel()
        runBlocking { savedId?.let { app.database.dao().plant(it) }?.let {
            check(it.name == "QA error recovery fern")
            app.database.dao().delete(it)
        } }
    }
    @Test fun failedSaveShowsPlainRecoveryMessageAndPreservesDraftForRetry() {
        var attempts = 0
        val delegate = app.database.dao()
        val dao = object : FloraDao by delegate {
            override suspend fun savePlantLimited(plant: Plant, premium: Boolean): Boolean {
                if (attempts++ == 0) throw IOException("PRIVATE_IMPLEMENTATION_DETAIL_NOT_FOR_UI")
                savedId = plant.id
                return delegate.savePlantLimited(plant, premium)
            }
        }
        model = PlantViewModel(app, dao)
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FloraRoot(activity, model!!) } }
        rule.waitUntil(10000) { rule.onAllNodesWithText("Add plant").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Add plant").performClick()
        rule.onNodeWithText("Plant nickname").performTextReplacement("QA error recovery fern")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Your record could not be saved. Please try again.").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Your record could not be saved. Please try again.").assertIsDisplayed()
        rule.onAllNodesWithText("PRIVATE_IMPLEMENTATION_DETAIL_NOT_FOR_UI", substring = true).assertCountEquals(0)
        rule.onNodeWithText("Close", substring = false).assertIsEnabled().performClick()
        rule.onNodeWithText("QA error recovery fern").assertExists()
        rule.onNodeWithText("Save", substring = false).assertIsEnabled().performClick()
        rule.waitUntil(10000) { runBlocking { savedId?.let { delegate.plant(it) }?.name == "QA error recovery fern" } }
        assertEquals(2, attempts)
    }
    @Test fun invalidPlantExplainsDisabledSaveAndAllowsCorrectionWithoutLosingInput() {
        var saved: Plant? = null
        rule.activityRule.scenario.onActivity { activity -> activity.setContent {
            MaterialTheme { PlantEditor(Plant(name = "QA validation fern", potCm = -1.0), {}, { saved = it }) }
        } }
        rule.onNodeWithText("Save", substring = false).assertIsNotEnabled()
        rule.onNodeWithText("Enter a name, a valid date, and a positive pot size if supplied.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Pot diameter (cm, optional)").performScrollTo().performTextReplacement("12")
        rule.onNodeWithText("Save", substring = false).assertIsEnabled().performClick()
        rule.runOnIdle { assertEquals("QA validation fern", saved?.name); assertEquals(12.0, saved!!.potCm!!, 0.0) }
    }
}
