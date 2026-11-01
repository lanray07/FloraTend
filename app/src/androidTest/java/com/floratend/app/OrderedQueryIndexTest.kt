package com.floratend.app

import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class OrderedQueryIndexTest {
    @Test fun chronologicalQueriesAvoidTemporarySorts() {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val plans=JSONObject()
        for((name,sql) in listOf("journal" to "SELECT * FROM journal ORDER BY date DESC, rowid DESC",
            "history" to "SELECT * FROM history ORDER BY date DESC",
            "schedules" to "SELECT * FROM schedules ORDER BY nextDate")) {
            val details=JSONArray()
            app.database.openHelper.readableDatabase.query("EXPLAIN QUERY PLAN $sql").use { cursor ->
                while(cursor.moveToNext()) details.put(cursor.getString(3))
            }
            plans.put(name,details)
        }
        File(app.getExternalFilesDir(null),"ordered-query-plans.json").writeText(plans.toString(2))
        for(name in listOf("journal","history","schedules"))
            assertFalse("$name: ${plans.getJSONArray(name)}",plans.getJSONArray(name).toString().contains("TEMP B-TREE"))
    }
}
