package com.floratend.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File

class SchemaMigrationTest {
    @get:Rule val helper=MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),FloraDatabase::class.java)
    @Test fun versionOneRecordsPhotosAndForeignKeysSurviveIndexMigration() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val name="migration-preservation-${System.nanoTime()}.db"
        val bitmap=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.GREEN) }
        val photo=Photos.save(context,bitmap);bitmap.recycle()
        try {
            helper.createDatabase(name,1).apply {
                execSQL("INSERT INTO plants VALUES(?,?,?,?,?,?,?,?,?,?,?)",arrayOf<Any?>("plant","移行 fern","Monstera deliciosa","Study","Herb","2026-01-01",25.4,"Coir","Bright","Preserve ≥ 25,4 cm – برگ",photo))
                execSQL("INSERT INTO plants VALUES(?,?,?,?,?,?,?,?,?,?,?)",arrayOf<Any?>("other","Other plant","","","Indoor","2026-01-01",null,"","","",null))
                execSQL("INSERT INTO schedules VALUES(?,?,?,?,?,?,?)",arrayOf<Any?>("schedule","plant","water",7,"2026-10-02",1,"2026-09-25"))
                execSQL("INSERT INTO journal VALUES(?,?,?,?,?,?,?)",arrayOf<Any?>("entry","plant","2026-10-01","Saved observation",25.4,3,photo))
                execSQL("INSERT INTO history VALUES(?,?,?,?,?)",arrayOf<Any?>("record","plant","soil","2026-09-25","Soil moist — no watering"))
                close()
            }
            helper.runMigrationsAndValidate(name,2,true,FloraDatabase.MIGRATION_1_2).close()
            val db=Room.databaseBuilder(context,FloraDatabase::class.java,name).addMigrations(FloraDatabase.MIGRATION_1_2).build()
            try {
                val plant=db.dao().plant("plant")!!
                assertEquals("移行 fern",plant.name);assertEquals("Preserve ≥ 25,4 cm – برگ",plant.notes)
                assertEquals(25.4,plant.potCm!!,0.00001);assertEquals(photo,plant.photo)
                assertTrue(db.dao().schedules().first().single().paused)
                assertEquals("2026-09-25",db.dao().schedules().first().single().lastCompleted)
                assertEquals(25.4,db.dao().journal().first().single().heightCm!!,0.00001)
                assertEquals(3,db.dao().journal().first().single().leaves)
                assertEquals("Soil moist — no watering",db.dao().history().first().single().observation)
                val decoded=BitmapFactory.decodeFile(photo)!!
                assertTrue(android.graphics.Color.green(decoded.getPixel(16,16))>240);decoded.recycle()
                db.dao().delete(plant)
                assertEquals("other",db.dao().plants().first().single().id)
                assertTrue(db.dao().journal().first().isEmpty());assertTrue(db.dao().history().first().isEmpty());assertTrue(db.dao().schedules().first().isEmpty())
                File(context.getExternalFilesDir(null),"migration-preservation.json").writeText(JSONObject().put("from",1).put("to",2).put("roomSchemaValidation",true).put("unicodeAndMeasurementsPreserved",true).put("photosPreserved",true).put("childRecordsPreserved",true).put("cascadesStillWork",true).put("unrelatedPlantPreserved",true).toString(2))
            } finally { db.close() }
        } finally { context.deleteDatabase(name);File(photo).delete() }
    }
    @Test fun applicationDatabaseRejectsMainThreadQueries() {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        var rejected=false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            try { app.database.query("SELECT 1",emptyArray()).close() }
            catch(expected:IllegalStateException) { rejected=true }
        }
        assertTrue("Production database must not allow main-thread queries",rejected)
    }
}
