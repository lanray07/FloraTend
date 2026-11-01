package com.floratend.app

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class RecordLoadingTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private var fixtureVm:PlantViewModel?=null
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        runBlocking { app.settings.set("onboarded",true) }
        AppLocales.choose(app,"en-GB")
        rule.activityRule.scenario.recreate()
    }
    @After fun cleanup() { fixtureVm?.viewModelScope?.cancel() }
    private fun show(plantFlow:Flow<List<Plant>>) {
        val dao=object:FloraDao by app.database.dao() { override fun plants()=plantFlow }
        fixtureVm=PlantViewModel(app,dao)
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FloraRoot(activity,fixtureVm!!) } }
    }
    @Test fun loadingDoesNotPretendTheCollectionIsEmpty() {
        val rows=MutableSharedFlow<List<Plant>>(replay=1)
        show(rows)
        rule.onNodeWithText("Loading your records…").assertIsDisplayed()
        rule.onNodeWithText("Make room for something green").assertDoesNotExist()
        runBlocking { rows.emit(listOf(Plant(name="Delayed fern"))) }
        rule.waitUntil(10000) { rule.onAllNodesWithText("Delayed fern").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Delayed fern").assertExists()
        rule.onNodeWithText("Loading your records…").assertDoesNotExist()
    }
    @Test fun readFailureHasAWorkingRetryAndDoesNotWriteData() {
        val countBefore=runBlocking { app.database.dao().plantCount() }
        val attempts=AtomicInteger()
        show(flow {
            if(attempts.getAndIncrement()==0) throw IOException("controlled read failure")
            emit(listOf(Plant(name="Recovered fern")))
        })
        rule.onNodeWithText("Your records could not be loaded. Please try again.").assertIsDisplayed()
        rule.onNodeWithText("Try again").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Recovered fern").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Recovered fern").assertExists()
        Assert.assertEquals(countBefore,runBlocking { app.database.dao().plantCount() })
    }
}
