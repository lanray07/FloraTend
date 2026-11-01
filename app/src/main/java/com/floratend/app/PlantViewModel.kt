package com.floratend.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface RecordsState {
    data object Loading : RecordsState
    data class Ready(val plants: List<Plant>, val schedules: List<CareSchedule>, val journal: List<JournalEntry>, val history: List<CareRecord>) : RecordsState
    data object Unavailable : RecordsState
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlantViewModel @JvmOverloads constructor(application: Application, dataDao: FloraDao? = null) : AndroidViewModel(application) {
    private val app = application as FloraApp
    val dao = dataDao ?: app.database.dao()
    private val readAttempt = MutableStateFlow(0)
    val records = readAttempt.flatMapLatest {
        combine(dao.plants(), dao.schedules(), dao.journal(), dao.history()) { plants, schedules, journal, history ->
            RecordsState.Ready(plants, schedules, journal, history) as RecordsState
        }.onStart { emit(RecordsState.Loading) }
            .catch { cause ->
                if (cause is kotlinx.coroutines.CancellationException) throw cause
                emit(RecordsState.Unavailable)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecordsState.Loading)
    suspend fun exportPayload(kind: String): String {
        check(app.settings.premium.first()) { "Premium is required" }
        val snapshot = dao.snapshot()
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            when (kind) {
                "json" -> Export.json(snapshot.plants, snapshot.schedules, snapshot.journal, snapshot.history)
                "csv" -> Export.csv(snapshot.plants)
                else -> error("Unknown export format")
            }
        }
    }
    fun retryLoading() { readAttempt.value += 1 }
    val premium = app.settings.premium.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val error = MutableStateFlow<String?>(null)
    fun save(plant: Plant, done: () -> Unit) = viewModelScope.launch {
        runCatching {
            if (dao.savePlantLimited(plant, premium.value)) done()
            else error.value = AppLocales.wrap(app).getString(R.string.free_limit)
        }.onFailure { error.value = AppLocales.wrap(app).getString(R.string.save_error) }
    }
    fun delete(plant: Plant) = viewModelScope.launch { dao.delete(plant); Reminders.refresh(app) }
    fun schedule(schedule: CareSchedule) = viewModelScope.launch { dao.save(schedule); Reminders.refresh(app) }
    fun complete(schedule: CareSchedule, observation: String = "") = viewModelScope.launch { dao.complete(schedule, LocalDate.now(), observation); Reminders.refresh(app) }
    fun journal(entry: JournalEntry) = viewModelScope.launch { dao.save(entry) }
    fun delete(entry: JournalEntry) = viewModelScope.launch { dao.delete(entry) }
    fun wipe() = viewModelScope.launch { dao.wipe(); app.filesDir.resolve("photos").deleteRecursively(); Reminders.refresh(app) }
}
