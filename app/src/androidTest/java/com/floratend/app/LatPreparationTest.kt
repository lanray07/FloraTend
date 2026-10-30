package com.floratend.app

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Opt-in preservation check before replacing a developer-signed sideload with LAT. */
class LatPreparationTest {
    @Test fun backUpAndRequireEmptyCollectionBeforeReplacement() = runBlocking {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
        val dao = app.database.dao()
        val counts = JSONObject().put("plants", dao.plants().first().size)
            .put("schedules", dao.schedules().first().size)
            .put("journal", dao.journal().first().size)
            .put("history", dao.history().first().size)
        app.database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use {
            while (it.moveToNext()) { /* Flush committed DB pages before the private archive. */ }
        }
        val destination = File(app.getExternalFilesDir(null), "lat-private").apply { mkdirs() }
        val data = File(app.applicationInfo.dataDir).canonicalFile
        ZipOutputStream(File(destination, "before-replacement.zip").outputStream()).use { zip ->
            for (name in listOf("databases", "files", "shared_prefs", "no_backup")) {
                val source = File(data, name)
                if (!source.exists()) continue
                source.walkTopDown().filter { it.isFile }.forEach { file ->
                    check(file.canonicalPath.startsWith(data.path + File.separator))
                    zip.putNextEntry(ZipEntry(file.relativeTo(data).invariantSeparatorsPath))
                    file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
                }
            }
        }
        val photos = File(app.filesDir, "photos").walkTopDown().count { it.isFile }
        File(destination, "record-counts.json").writeText(JSONObject().put("recordCounts", counts)
            .put("photoFiles", photos).put("privateArchiveCreated", true).toString(2))
        assertEquals("Existing user records must not be removed", 0,
            listOf("plants", "schedules", "journal", "history").sumOf { counts.getInt(it) })
        assertEquals("Existing photographs must not be removed", 0, photos)
    }
}
