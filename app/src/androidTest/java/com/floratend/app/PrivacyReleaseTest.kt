package com.floratend.app

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Test
import org.junit.Rule
import org.junit.Assert.*
import java.io.File

/** Runs only on a dedicated empty collection. Never deletes a customer collection. */
class PrivacyReleaseTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun confirmedDeleteAllRemovesRecordsAndImportedPhotos() {
        val app=rule.activity.application as FloraApp
        val dao=app.database.dao()
        runBlocking { check(dao.plants().first().isEmpty()) { "Empty test collection required; refusing to delete user records" } }
        check(app.filesDir.resolve("photos").walkTopDown().none { it.isFile }) {
            "Empty photo storage required; refusing to delete pre-existing photographs"
        }
        val source=File(app.cacheDir,"qa-privacy-source.png")
        val bitmap=Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.GREEN)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
        val imported=Photos.import(app,Uri.fromFile(source))
        val plant=Plant(id="qa-privacy-release",name="FloraTend QA privacy",photo=imported)
        try {
            runBlocking {
                app.settings.set("onboarded",true)
                dao.save(plant)
                dao.save(CareSchedule(id="qa-privacy-care",plantId=plant.id))
                dao.save(JournalEntry(id="qa-privacy-journal",plantId=plant.id,note="Private test note",photo=imported))
                dao.record(CareRecord(id="qa-privacy-history",plantId=plant.id,type="soil",date="2026-10-30"))
            }
            assertTrue(File(imported).isFile)
            rule.waitForIdle()
            rule.onNodeWithText("Settings",substring=false).performScrollTo().performClick()
            rule.onNodeWithText(app.getString(R.string.delete_all),substring=false).performScrollTo().performClick()
            // Merely opening the confirmation must retain records and photographs.
            assertNotNull(runBlocking { dao.plant(plant.id) });assertTrue(File(imported).isFile)
            rule.onNodeWithText(app.getString(R.string.delete),substring=false).performClick()
            rule.waitUntil(10000) { runBlocking { dao.plants().first().isEmpty() && dao.schedules().first().isEmpty() &&
                dao.journal().first().isEmpty() && dao.history().first().isEmpty() } && !File(imported).exists() }
            val evidence=File(app.getExternalFilesDir(null),"release-checks").apply { mkdirs() }
            File(evidence,"privacy-deletion.json").writeText(JSONObject().put("versionCode",BuildConfig.VERSION_CODE)
                .put("confirmationRequired",true).put("allFourRecordTablesEmpty",true).put("importedPhotoRemoved",true)
                .put("scope","Actual native delete-all UI and private test data; provider/export copies are outside this scope").toString(2))
        } finally {
            runBlocking { dao.plants().first().filter { it.id==plant.id }.forEach { dao.delete(it) } }
            File(imported).delete();source.delete()
        }
    }
}
