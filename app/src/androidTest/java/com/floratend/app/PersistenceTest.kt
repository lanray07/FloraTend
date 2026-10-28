package com.floratend.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    @Test fun freeLimitAndPremiumPolicyAreTransactional() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,FloraDatabase::class.java).build()
        try {
            repeat(5) { assertTrue(db.dao().savePlantLimited(Plant(name="Plant $it"),false)) }
            assertFalse(db.dao().savePlantLimited(Plant(name="Sixth"),false))
            assertEquals(5,db.dao().plantCount())
            assertTrue(db.dao().savePlantLimited(Plant(name="Premium sixth"),true))
            val first=db.dao().plants().first().first()
            assertTrue(db.dao().savePlantLimited(first.copy(notes="edited"),false))
            assertEquals(6,db.dao().plantCount())
        } finally { db.close() }
    }
    @Test fun offlineCrudRecurrenceAndCascadePreserveOtherPlants() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="acceptance-${System.nanoTime()}.db"
        var db=Room.databaseBuilder(context,FloraDatabase::class.java,name).build()
        val plant=Plant(name="Monstera",location="Living room")
        val other=Plant(name="Basil")
        try {
            db.dao().save(plant);db.dao().save(other)
            val schedule=CareSchedule(plantId=plant.id,intervalDays=7,nextDate="2026-10-28")
            db.dao().save(schedule);db.dao().complete(schedule,LocalDate.of(2026,10,28),"Soil checked: moist")
            db.dao().save(JournalEntry(plantId=plant.id,note="New leaf",heightCm=23.5,leaves=7))
            db.close()
            db=Room.databaseBuilder(context,FloraDatabase::class.java,name).build()
            assertEquals(2,db.dao().plants().first().size)
            assertEquals("2026-11-04",db.dao().schedules().first().single().nextDate)
            assertEquals(1,db.dao().history().first().size)
            assertEquals(23.5,db.dao().journal().first().single().heightCm!!,0.001)
            db.dao().save(plant.copy(name="Monstera edited"))
            assertEquals(1,db.dao().schedules().first().size)
            db.dao().delete(plant)
            assertEquals(other.id,db.dao().plants().first().single().id)
            assertTrue(db.dao().journal().first().isEmpty())
            assertTrue(db.dao().schedules().first().isEmpty())
            assertTrue(db.dao().history().first().isEmpty())
        } finally { db.close();context.deleteDatabase(name) }
    }
}
