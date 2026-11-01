package com.floratend.app

import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/** Context-only fixture checks preserve the first UI launch after an in-place update. */
class PerformanceUpgradeBackupTest {
    private val app get()=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
    private val folder get()=File(app.getExternalFilesDir(null),"upgrade-backup")
    private val tables=listOf("plants","schedules","journal","history")
    private fun hash(file:File)=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
    private fun counts()=tables.associateWith { name -> app.database.openHelper.readableDatabase.query("SELECT count(*) FROM $name").use { it.moveToFirst();it.getInt(0) } }
    private fun guard() {
        check(app.packageName=="com.floratend.app.perf")
        check(app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE==0)
        check(counts().values.all { it==120 })
    }
    @Test fun backupControlledFixtureWithoutDeleting()=runBlocking {
        guard()
        val snapshot=app.database.dao().snapshot()
        check(snapshot.plants.size==120 && snapshot.plants.all { it.id.startsWith("qa-large-photo-") })
        check(!folder.exists());check(folder.mkdirs())
        val db=app.database.openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL)").use { check(it.moveToFirst() && it.getInt(0)==0) }
        File(app.applicationInfo.dataDir,"databases").copyRecursively(File(folder,"databases"))
        SQLiteDatabase.openDatabase(File(folder,"databases/floratend.db").path,null,SQLiteDatabase.OPEN_READONLY).use { copy ->
            copy.rawQuery("PRAGMA integrity_check",null).use { assertTrue(it.moveToFirst());assertEquals("ok",it.getString(0)) }
            for(table in tables) copy.rawQuery("SELECT count(*) FROM $table",null).use { assertTrue(it.moveToFirst());assertEquals(120,it.getInt(0)) }
        }
        val photos=File(folder,"photos").apply { mkdirs() };val hashes=JSONObject()
        for(plant in snapshot.plants) {
            val photo=File(requireNotNull(plant.photo))
            check(photo.canonicalFile.toPath().startsWith(app.filesDir.canonicalFile.toPath()))
            val saved=photo.copyTo(File(photos,photo.name));assertEquals(hash(photo),hash(saved))
            hashes.put(photo.name,hash(saved))
        }
        guard()
        File(folder,"result.json").writeText(JSONObject().put("versionCode",app.packageManager.getPackageInfo(app.packageName,0).versionCode)
            .put("domainCounts",JSONObject(counts())).put("photoHashes",hashes).put("dataRetained",true).toString(2))
    }
    @Test fun inPlaceUpdatePreservesControlledFixture()=runBlocking {
        guard()
        val before=JSONObject(File(folder,"result.json").readText())
        val hashes=before.getJSONObject("photoHashes")
        val plants=app.database.dao().snapshot().plants
        check(plants.size==120 && plants.all { it.id.startsWith("qa-large-photo-") })
        for(plant in plants) { val photo=File(requireNotNull(plant.photo));assertEquals(hashes.getString(photo.name),hash(photo)) }
        File(app.getExternalFilesDir(null),"upgrade-check.json").writeText(JSONObject().put("beforeVersionCode",before.getInt("versionCode"))
            .put("afterVersionCode",app.packageManager.getPackageInfo(app.packageName,0).versionCode)
            .put("domainCounts",JSONObject(counts())).put("photosPreservedAndHashVerified",plants.size)
            .put("instrumentationHasNoActivityRule",true).put("mainPackageAccessed",false).toString(2))
    }
}
