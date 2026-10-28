package com.floratend.app

import org.json.JSONArray
import org.json.JSONObject

object Export {
    fun csv(plants: List<Plant>): String {
        fun cell(value: String) = "\"" + (if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'" else "") + value.replace("\"", "\"\"") + "\""
        return "id,name,species,location,category,acquired,pot_cm,soil,light,notes\r\n" + plants.joinToString("\r\n") { p -> listOf(p.id,p.name,p.species,p.location,p.category,p.acquired,p.potCm?.toString().orEmpty(),p.soil,p.light,p.notes).joinToString(",") { cell(it) } }
    }
    fun json(plants: List<Plant>, schedules: List<CareSchedule>, journal: List<JournalEntry>, history: List<CareRecord>): String {
        fun array(items: List<JSONObject>) = JSONArray().apply { items.forEach { put(it) } }
        return JSONObject().put("schemaVersion", 1)
            .put("plants", array(plants.map { p -> JSONObject().put("id",p.id).put("name",p.name).put("species",p.species).put("location",p.location).put("category",p.category).put("acquired",p.acquired).put("potCm",p.potCm).put("soil",p.soil).put("light",p.light).put("notes",p.notes).put("photoIncluded",false) }))
            .put("schedules", array(schedules.map { s -> JSONObject().put("id",s.id).put("plantId",s.plantId).put("type",s.type).put("intervalDays",s.intervalDays).put("nextDate",s.nextDate).put("paused",s.paused).put("lastCompleted",s.lastCompleted) }))
            .put("journal", array(journal.map { j -> JSONObject().put("id",j.id).put("plantId",j.plantId).put("date",j.date).put("note",j.note).put("heightCm",j.heightCm).put("leaves",j.leaves).put("photoIncluded",false) }))
            .put("history", array(history.map { h -> JSONObject().put("plantId",h.plantId).put("date",h.date).put("type",h.type).put("observation",h.observation) })).toString(2)
    }
}
