package com.floratend.app

import java.util.Locale

sealed interface VoiceCommand {
    data object Add : VoiceCommand
    data object Due : VoiceCommand
    data object Calendar : VoiceCommand
    data object Read : VoiceCommand
    data class Water(val name: String) : VoiceCommand
    data class Location(val name: String) : VoiceCommand
    data object Unknown : VoiceCommand
}
object VoiceParser {
    // Additional dictionaries remain pending translation and device validation.
    fun parse(text: String, locale: Locale = Locale.getDefault()): VoiceCommand {
        if (locale.language != "en") return VoiceCommand.Unknown
        val value = text.trim().lowercase(locale).trimEnd('.', '?', '!')
        return when {
            value == "add a new plant" || value == "add plant" -> VoiceCommand.Add
            value.contains("calendar") -> VoiceCommand.Calendar
            value == "read my reminders" || value == "read my care reminders" -> VoiceCommand.Read
            value.contains("today") && (value.contains("watering") || value.contains("water")) -> VoiceCommand.Due
            value.startsWith("mark my ") && value.endsWith(" as watered") -> VoiceCommand.Water(value.removePrefix("mark my ").removeSuffix(" as watered"))
            value.startsWith("show plants in ") -> VoiceCommand.Location(value.removePrefix("show plants in "))
            else -> VoiceCommand.Unknown
        }
    }
}
