package com.floratend.app

import android.database.sqlite.SQLiteDatabase
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import java.io.File
import java.security.MessageDigest

/** Back up the exact known QA identification records, then require host verification. */
class IdentificationCleanupTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val interrupted get()=InstrumentationRegistry.getArguments().getString("floratend.interruptedIdentification")=="true"
    private val ids get()=if(interrupted)setOf("qa-identification-live") else setOf("qa-identification-live","qa-identification-offline")
    private val app get()=rule.activity.application as FloraApp
    private val folder get()=File(app.getExternalFilesDir(null),if(interrupted)"identification-interrupted-backup" else "identification-backup")
    private fun hash(file:File)=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") {"%02x".format(it)}
    private suspend fun controlled():List<Plant> {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val records=app.database.dao().snapshot()
        check(records.plants.map {it.id}.toSet()==ids && records.plants.size==ids.size)
        check(records.schedules.isEmpty() && records.journal.isEmpty() && records.history.isEmpty())
        return records.plants
    }
    @Test fun backupControlledRecords()=runBlocking {
        val plants=controlled();check(!folder.exists());check(folder.mkdirs())
        val db=app.database.openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL)").use {check(it.moveToFirst() && it.getInt(0)==0)}
        val copy=File(folder,"floratend.db");app.getDatabasePath("floratend.db").copyTo(copy)
        SQLiteDatabase.openDatabase(copy.path,null,SQLiteDatabase.OPEN_READONLY).use { saved ->
            saved.rawQuery("PRAGMA integrity_check",null).use {assertTrue(it.moveToFirst());assertEquals("ok",it.getString(0))}
            saved.rawQuery("SELECT count(*) FROM plants",null).use {assertTrue(it.moveToFirst());assertEquals(ids.size,it.getInt(0))}
        }
        val photos=File(folder,"photos").apply {mkdirs()};val hashes=JSONObject()
        for(plant in plants) {
            val photo=File(requireNotNull(plant.photo));check(photo.canonicalFile.parentFile==File(app.filesDir,"photos").canonicalFile)
            val copied=photo.copyTo(File(photos,photo.name));assertEquals(hash(photo),hash(copied));hashes.put(photo.name,hash(photo))
        }
        File(folder,"manifest.json").writeText(JSONObject().put("controlledIds",JSONArray(plants.map {it.id}))
            .put("plantCount",ids.size).put("otherDomainTablesEmpty",true).put("databaseSha256",hash(copy)).put("photoHashes",hashes).toString(2))
    }
    @Test fun clearAfterHostVerifiedBackup()=runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("floratend.identificationBackupVerified")=="true")
        val plants=controlled();val manifest=JSONObject(File(folder,"manifest.json").readText())
        assertEquals(manifest.getString("databaseSha256"),hash(File(folder,"floratend.db")))
        val hashes=manifest.getJSONObject("photoHashes")
        for(plant in plants) {val photo=File(requireNotNull(plant.photo));assertEquals(hashes.getString(photo.name),hash(photo));assertEquals(hash(photo),hash(File(folder,"photos/${photo.name}")))}
        for(plant in plants)app.database.dao().delete(plant)
        for(plant in plants)assertTrue(File(requireNotNull(plant.photo)).delete())
        val remaining=app.database.dao().snapshot();assertTrue(remaining.plants.isEmpty() && remaining.schedules.isEmpty() && remaining.journal.isEmpty() && remaining.history.isEmpty())
        val results=File(app.getExternalFilesDir(null),"identification-live").apply {mkdirs()}
        File(results,if(interrupted)"interrupted-cleanup.json" else "cleanup.json").writeText(JSONObject().put("hostBackupVerified",true)
            .put("knownRecordsRemoved",ids.size).put("remainingPlants",0).put("remainingSchedules",0).put("remainingJournal",0).put("remainingHistory",0)
            .put("mainAmazonPackageTouched",false).toString(2))
    }
}
