package com.floratend.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File

/** Run seed, then force-stop QA and disable networking, then verify. No user-package data. */
class OfflineProcessTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val app get() = rule.activity.application as FloraApp
    private val id = "qa-offline-process"
    @Test fun phase() = runBlocking {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val phase = InstrumentationRegistry.getArguments().getString("phase")
        Assume.assumeTrue("Explicit phase required", phase in listOf("seed", "verify", "cleanup"))
        val dao = app.database.dao()
        if (phase=="cleanup") {
            dao.journal().first().filter { it.plantId==id }.forEach { it.photo?.let { path -> File(path).delete() } }
            dao.plants().first().filter { it.id==id }.forEach { dao.delete(it) }
            return@runBlocking
        }
        if (phase=="seed") {
            check(dao.plant(id)==null) { "Run cleanup before reseeding" }
            app.settings.set("onboarded",true)
            AppLocales.choose(app,"en-GB")
            dao.save(Plant(id=id,name="Offline preservation fern",location="QA room",category="Herb",notes="Preserve across process death"))
            dao.save(CareSchedule(id=id,plantId=id,type="water",intervalDays=12,nextDate="2026-12-01"))
            File(app.getExternalFilesDir(null),"offline-seed-process.json").writeText(JSONObject().put("pid",android.os.Process.myPid()).toString(2))
            repeat(2) { index ->
                val bitmap = Bitmap.createBitmap(1600,1200,Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(if(index==0) android.graphics.Color.GREEN else android.graphics.Color.BLUE)
                val path = Photos.save(app,bitmap); bitmap.recycle()
                dao.save(JournalEntry(id="$id-$index",plantId=id,date="2026-10-0${index+1}",note="Saved image $index",heightCm=20.0+index,photo=path))
            }
        } else {
            assertEquals("Preserve across process death",dao.plant(id)!!.notes)
            assertEquals(12,dao.schedules().first().single { it.id==id }.intervalDays)
            val entries=dao.journal().first().filter { it.plantId==id }
            assertEquals(listOf("Saved image 1","Saved image 0"),entries.map { it.note })
            entries.forEach { entry ->
                val file=File(entry.photo!!)
                assertTrue(file.canonicalPath.startsWith(File(app.filesDir,"photos").canonicalPath+File.separator))
                val bitmap=BitmapFactory.decodeFile(file.path)!!
                assertEquals(1600,bitmap.width);assertEquals(1200,bitmap.height)
                val pixel=bitmap.getPixel(800,600)
                if(entry.note.endsWith("1")) assertTrue(android.graphics.Color.blue(pixel)>240)
                else assertTrue(android.graphics.Color.green(pixel)>240)
                bitmap.recycle()
            }
            rule.waitForIdle()
            val nav=rule.onNodeWithText("Journal",substring=false)
            if(rule.activity.resources.configuration.screenWidthDp>=720) nav.performScrollTo()
            nav.performClick()
            rule.onNodeWithText("Saved image 1").assertExists()
            rule.onNodeWithText("Saved image 0").assertExists()
            rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("First and latest saved photographs · Offline preservation fern"))
            rule.onNodeWithText("First and latest saved photographs · Offline preservation fern").assertIsDisplayed()
            val folder=File(app.getExternalFilesDir(null),"offline-process").apply { mkdirs() }
            val capture=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()!!
            File(folder,"offline-saved-images.png").outputStream().use { capture.compress(Bitmap.CompressFormat.PNG,100,it) };capture.recycle()
            File(folder,"verified.json").writeText(JSONObject().put("pid",android.os.Process.myPid()).put("phase","verify").put("plantPreserved",true).put("careSchedulePreserved",true).put("savedImageFilesDecoded",2).put("pixelsMatchSeed",true).put("journalComparisonDisplayed",true).toString(2))
        }
    }
}
