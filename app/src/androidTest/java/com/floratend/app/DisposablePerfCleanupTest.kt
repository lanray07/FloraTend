package com.floratend.app

import android.database.sqlite.SQLiteDatabase
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import java.io.File
import java.security.MessageDigest

/** Back up and validate only the known isolated performance fixture before removal. */
class DisposablePerfCleanupTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun verifiedBackupPrecedesControlledFixtureCleanup()=runBlocking {
        assumeTrue("Disposable performance package only",BuildConfig.APPLICATION_ID.endsWith(".perf"))
        val app=rule.activity.application as FloraApp
        val plants=app.database.dao().plants().first()
        check(plants.size==120 && plants.all { it.id.startsWith("qa-large-photo-") })
        val tables=listOf("plants","schedules","journal","history")
        val db=app.database.openHelper.writableDatabase
        fun counts()=tables.associateWith { table -> db.query("SELECT COUNT(*) FROM $table").use { it.moveToFirst();it.getInt(0) } }
        val before=counts()
        val folder=File(app.getExternalFilesDir(null),"disposable-backup").apply { check(!exists());mkdirs() }
        db.query("PRAGMA wal_checkpoint(FULL)").use { check(it.moveToFirst() && it.getInt(0)==0) }
        File(app.applicationInfo.dataDir,"databases").copyRecursively(File(folder,"databases"))
        SQLiteDatabase.openDatabase(File(folder,"databases/floratend.db").absolutePath,null,SQLiteDatabase.OPEN_READONLY).use { copy ->
            for(table in tables) copy.rawQuery("SELECT COUNT(*) FROM $table",null).use { cursor ->
                check(cursor.moveToFirst());assertEquals(before.getValue(table),cursor.getInt(0))
            }
        }
        val photos=plants.map { File(requireNotNull(it.photo)) }
        fun hash(file:File)=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).toList()
        val photoFolder=File(folder,"photos").apply { mkdirs() }
        for(photo in photos) {
            check(photo.canonicalFile.toPath().startsWith(app.filesDir.canonicalFile.toPath()))
            val copy=photo.copyTo(File(photoFolder,photo.name))
            assertEquals(hash(photo),hash(copy))
        }
        // Every record is the known controlled fixture, with a verified DB/photo backup.
        for(plant in plants) app.database.dao().delete(plant)
        for(photo in photos) assertTrue(photo.delete())
        val after=counts();assertTrue(after.values.all { it==0 })
        File(folder,"result.json").writeText(JSONObject().put("backupValidated",true)
            .put("beforeCounts",JSONObject(before)).put("afterCounts",JSONObject(after))
            .put("linkedPhotosBackedUpAndHashVerified",photos.size)
            .put("onlyControlledFixtureRemoved",true).put("mainAmazonPackageTouched",false).toString(2))
    }
}
