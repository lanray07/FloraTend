package com.floratend.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.Assume.assumeTrue
import java.io.File
import java.time.LocalDate

/** Controlled workload only. Never grants Premium or runs against the Amazon package. */
class PerformanceCollectionSeedTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun populateCareJournalAndHistoryForExistingControlledPlants()=runBlocking {
        assumeTrue("Disposable performance package only",BuildConfig.APPLICATION_ID.endsWith(".perf"))
        val app=rule.activity.application as FloraApp
        val plants=app.database.dao().plants().first()
        check(plants.size==120 && plants.all { it.id.startsWith("qa-large-photo-") })
        for(plant in plants) {
            app.database.dao().save(CareSchedule(id="care-${plant.id}",plantId=plant.id,nextDate=LocalDate.now().toString()))
            app.database.dao().save(JournalEntry(id="journal-${plant.id}",plantId=plant.id,note="Controlled performance observation",heightCm=25.4,leaves=7,photo=plant.photo))
            app.database.dao().record(CareRecord(id="history-${plant.id}",plantId=plant.id,type="soil",date=LocalDate.now().toString(),observation="Controlled performance check"))
        }
        val result=JSONObject().put("plants",plants.size).put("schedules",app.database.dao().schedules().first().size)
            .put("journal",app.database.dao().journal().first().size).put("history",app.database.dao().history().first().size)
            .put("controlledFixture",true).put("genuineAmazonPurchase",false)
        File(app.getExternalFilesDir(null),"performance-workload.json").writeText(result.toString(2))
    }
}
