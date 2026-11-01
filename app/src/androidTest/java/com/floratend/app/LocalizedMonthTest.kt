package com.floratend.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Rule
import org.junit.Assert.*

class LocalizedMonthTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun frenchInsightsUsesReadableMonthInsteadOfIsoKey() {
        val app=rule.activity.application as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val old=AppLocales.selected(app)
        val plant=Plant(id="qa-localized-month",name="Month fixture")
        try {
            runBlocking {
                app.settings.set("onboarded",true);app.database.dao().save(plant)
                app.database.dao().record(CareRecord(id=plant.id,plantId=plant.id,type="soil",date="2011-03-09"))
            }
            AppLocales.choose(app,"fr-FR");rule.activityRule.scenario.recreate()
            val nav=rule.onNodeWithText(rule.activity.getString(R.string.insights),substring=false)
            if(rule.activity.resources.configuration.screenWidthDp>=720) nav.performScrollTo()
            nav.performClick()
            rule.waitUntil(10000) {
                rule.onAllNodes(hasText("mars 2011",substring=false) or hasText("2011-03",substring=false)).fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("2011-03",substring=false).assertDoesNotExist()
            rule.onNodeWithText("mars 2011",substring=false).performScrollTo().assertIsDisplayed()
        } finally { runBlocking { app.database.dao().delete(plant) };AppLocales.choose(app,old) }
    }
}
