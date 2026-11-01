package com.floratend.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.assertTrue
import java.time.LocalDate

class CalendarScaleTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val prefix="qa-calendar-scale-"
    @After fun cleanup():Unit=runBlocking {
        app.database.dao().plants().first().filter { it.id.startsWith(prefix) }.forEach { app.database.dao().delete(it) }
    }
    @Test fun largePlantFilterComposesVisibleItemsAndCanSelectLastPlant() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        AppLocales.choose(app,"en-GB")
        runBlocking {
            app.settings.set("onboarded",true)
            repeat(120) { i -> app.database.dao().save(Plant(id=prefix+i,name="Scale fern ${i.toString().padStart(3,'0')}")) }
            for(i in listOf(0,119)) app.database.dao().save(CareSchedule(id=prefix+i,plantId=prefix+i,nextDate=LocalDate.now().toString()))
        }
        rule.activityRule.scenario.recreate()
        val calendar=rule.onNodeWithText("Calendar",substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) calendar.performScrollTo()
        calendar.performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Scale fern 000",substring=false).fetchSemanticsNodes().isNotEmpty() }
        val composed=rule.onAllNodesWithText("Scale fern ",substring=true).fetchSemanticsNodes().size
        assertTrue("Only visible plant filters and schedule cards should compose, not all120; actual=$composed",composed<20)
        rule.onNode(hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
            .performScrollToNode(hasText("Scale fern 119"))
        rule.onNode(hasText("Scale fern 119") and hasClickAction()).performClick()
        rule.onAllNodesWithText("Scale fern 119",substring=false).assertCountEquals(2)
        rule.onAllNodesWithText("Scale fern 000",substring=false).assertCountEquals(0)
    }
}
