package com.floratend.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GardenSummaryTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun previewUpdatesForPauseCompletionAndResume()=runBlocking {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val app=rule.activity.application as FloraApp
        check(app.database.dao().plants().first().isEmpty())
        val oldLocale=AppLocales.selected(app)
        val onboarded=app.settings.onboarded.first()
        val plant=Plant(id="qa-summary-plant",name="Summary fixture")
        val today=LocalDate.now()
        val paused=CareSchedule(id="qa-summary-paused",plantId=plant.id,nextDate=today.toString(),paused=true)
        val early=CareSchedule(id="qa-summary-early",plantId=plant.id,nextDate=today.plusDays(1).toString())
        val later=CareSchedule(id="qa-summary-later",plantId=plant.id,nextDate=today.plusDays(3).toString())
        fun await(date:LocalDate) {
            val text=rule.activity.getString(R.string.next_check,formatDate(date.toString()))
            rule.waitUntil(10000) { rule.onAllNodesWithText(text,substring=false).fetchSemanticsNodes().size==1 }
        }
        try {
            app.settings.set("onboarded",true)
            AppLocales.choose(app,"en-GB")
            app.database.dao().save(plant)
            listOf(paused,later,early).forEach { app.database.dao().save(it) }
            rule.activityRule.scenario.recreate()
            await(today.plusDays(1))
            app.database.dao().save(early.copy(paused=true))
            await(today.plusDays(3))
            app.database.dao().complete(later,today,"Controlled summary check")
            await(today.plusDays(7))
            app.database.dao().save(paused.copy(paused=false))
            await(today)
        } finally {
            app.database.dao().delete(plant)
            app.settings.set("onboarded",onboarded)
            AppLocales.choose(app,oldLocale)
        }
    }
}
