package com.floratend.app

import android.app.Application
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String, val species: String = "", val location: String = "",
    val category: String = "Indoor", val acquired: String = LocalDate.now().toString(),
    val potCm: Double? = null, val soil: String = "", val light: String = "",
    val notes: String = "", val photo: String? = null
)
@Entity(tableName = "schedules", foreignKeys = [ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE)], indices = [Index("plantId")])
data class CareSchedule(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), val plantId: String,
    val type: String = "soil", val intervalDays: Int = 7,
    val nextDate: String = LocalDate.now().toString(), val paused: Boolean = false,
    val lastCompleted: String? = null
)
@Entity(tableName = "history", foreignKeys = [ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE)], indices = [Index("plantId")])
data class CareRecord(@PrimaryKey val id: String = UUID.randomUUID().toString(), val plantId: String, val type: String, val date: String, val observation: String = "")
@Entity(tableName = "journal", foreignKeys = [ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE)], indices = [Index("plantId")])
data class JournalEntry(@PrimaryKey val id: String = UUID.randomUUID().toString(), val plantId: String, val date: String = LocalDate.now().toString(), val note: String, val heightCm: Double? = null, val leaves: Int? = null, val photo: String? = null)

@Dao interface FloraDao {
    @Query("SELECT * FROM plants ORDER BY name COLLATE NOCASE") fun plants(): Flow<List<Plant>>
    @Query("SELECT * FROM schedules ORDER BY nextDate") fun schedules(): Flow<List<CareSchedule>>
    @Query("SELECT * FROM journal ORDER BY date DESC, rowid DESC") fun journal(): Flow<List<JournalEntry>>
    @Query("SELECT * FROM history ORDER BY date DESC") fun history(): Flow<List<CareRecord>>
    @Upsert suspend fun save(plant: Plant)
    @Upsert suspend fun save(schedule: CareSchedule)
    @Upsert suspend fun save(entry: JournalEntry)
    @Insert suspend fun record(record: CareRecord)
    @Delete suspend fun delete(plant: Plant)
    @Delete suspend fun delete(entry: JournalEntry)
    @Query("DELETE FROM plants") suspend fun deletePlants()
    @Query("SELECT * FROM schedules WHERE paused = 0 AND nextDate <= :today") suspend fun due(today: String): List<CareSchedule>
    @Query("SELECT * FROM plants WHERE id = :id") suspend fun plant(id: String): Plant?
    @Query("SELECT COUNT(*) FROM plants") suspend fun plantCount(): Int
    @Transaction suspend fun savePlantLimited(plant: Plant, premium: Boolean): Boolean {
        if (plant(plant.id) == null && plantCount() >= 5 && !premium) return false
        save(plant)
        return true
    }
    @Transaction suspend fun complete(schedule: CareSchedule, today: LocalDate, observation: String) {
        record(CareRecord(plantId = schedule.plantId, type = schedule.type, date = today.toString(), observation = observation))
        save(schedule.copy(lastCompleted = today.toString(), nextDate = Recurrence.next(today, schedule.intervalDays).toString()))
    }
    @Transaction suspend fun wipe() { deletePlants() }
}
@Database(entities = [Plant::class, CareSchedule::class, JournalEntry::class, CareRecord::class], version = 1, exportSchema = true)
abstract class FloraDatabase : RoomDatabase() { abstract fun dao(): FloraDao }
object Recurrence {
    fun next(completed: LocalDate, interval: Int): LocalDate { require(interval in 1..3650); return completed.plusDays(interval.toLong()) }
    fun reminder(date: LocalDate, zone: ZoneId): ZonedDateTime = date.atTime(9, 0).atZone(zone)
}
class FloraApp : Application() {
    val database by lazy { Room.databaseBuilder(this, FloraDatabase::class.java, "floratend.db").build() }
    val settings by lazy { FloraSettings(this) }
    val billing by lazy { AmazonBilling(this, settings) }
    override fun onCreate() { super.onCreate(); Reminders.schedule(this); billing.register() }
}

