package com.floratend.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class PlantViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FloraApp
    val dao = app.database.dao()
    val plants = dao.plants().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val schedules = dao.schedules().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val journal = dao.journal().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val history = dao.history().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
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
