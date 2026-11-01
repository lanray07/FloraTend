package com.floratend.app

import android.os.Debug
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Diagnostic overhead is excluded from release frame verdicts; controlled perf package only. */
class CalendarEntryProfileTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun profileColdCalendarEntry()=runBlocking {
        check(BuildConfig.APPLICATION_ID.endsWith(".perf"))
        val app=rule.activity.application as FloraApp
        val plants=app.database.dao().plants().first()
        check(plants.size==120 && plants.all { it.id.startsWith("qa-large-photo-") })
        rule.waitUntil(15000) { rule.onAllNodesWithText("Calendar",substring=false).fetchSemanticsNodes().isNotEmpty() }
        val folder=File(app.getExternalFilesDir(null),"calendar-method").apply { mkdirs() }
        Debug.startMethodTracingSampling(File(folder,"calendar.trace").absolutePath,32*1024*1024,1000)
        try {
            rule.onNodeWithText("Calendar",substring=false).performScrollTo().performClick()
            rule.onNodeWithText(rule.activity.getString(R.string.care_calendar),substring=false).assertExists()
            rule.waitForIdle()
        } finally { Debug.stopMethodTracing() }
    }
}
