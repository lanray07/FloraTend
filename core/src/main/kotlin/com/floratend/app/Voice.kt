package com.floratend.app

import java.util.Locale
import java.text.Normalizer
import java.io.InputStream
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

sealed interface VoiceCommand {
    data object Add : VoiceCommand
    data object Due : VoiceCommand
    data object Calendar : VoiceCommand
    data object Read : VoiceCommand
    data class Water(val name: String) : VoiceCommand
    data class Location(val name: String) : VoiceCommand
    data object Unknown : VoiceCommand
}
data class VoiceDictionary(val commands: Map<String,List<String>>) {
    companion object {
        fun read(input: InputStream): VoiceDictionary {
            val properties=Properties().apply { input.reader(Charsets.UTF_8).use { load(it) } }
            val grouped=properties.stringPropertyNames().groupBy { it.substringBefore('.') }
            return VoiceDictionary(grouped.mapValues { (_,keys) -> keys.sortedBy { it.substringAfter('.').toIntOrNull() ?: 0 }.map { properties.getProperty(it) } })
        }
    }
}
object VoiceParser {
    private val dictionaries=ConcurrentHashMap<String,VoiceDictionary>()
    fun install(tag: String, dictionary: VoiceDictionary) { dictionaries[tag]=dictionary }
    private val english=VoiceDictionary(mapOf(
        "add" to listOf("add a new plant","add plant"),"due" to listOf("which plants need watering today","which plants need a check today"),
        "calendar" to listOf("show my care calendar","show my plant-care calendar"),"read" to listOf("read my reminders","read my care reminders"),
        "water_prefix" to listOf("mark my "),"water_suffix" to listOf(" as watered"),"location_prefix" to listOf("show plants in ")
    ))
    private fun fold(value:String, locale:Locale, edges:Boolean=true):String {
        val normalized=Normalizer.normalize(value,Normalizer.Form.NFKC).replace('’','\'').replace('‘','\'').replace(Regex("\\s+")," ").lowercase(locale)
        return if(edges) normalized.trim().trimEnd('.', '?', '!', '؟', '。', '！', '？').trim() else normalized
    }
    fun parse(text: String, locale: Locale = Locale.getDefault()): VoiceCommand {
        val dictionary=dictionaries[locale.toLanguageTag()] ?: dictionaries.entries.firstOrNull { Locale.forLanguageTag(it.key).language==locale.language }?.value
            ?: if(locale.language=="en") english else return VoiceCommand.Unknown
        val value=fold(text,locale)
        fun matches(key:String)=dictionary.commands[key].orEmpty().any { fold(it,locale)==value }
        when {
            matches("add") -> return VoiceCommand.Add
            matches("due") -> return VoiceCommand.Due
            matches("calendar") -> return VoiceCommand.Calendar
            matches("read") -> return VoiceCommand.Read
        }
        fun capture(prefixKey:String,suffixKey:String):String? {
            val prefixes=dictionary.commands[prefixKey] ?: listOf("")
            val suffixes=dictionary.commands[suffixKey] ?: listOf("")
            for(rawPrefix in prefixes) for(rawSuffix in suffixes) {
                val prefix=fold(rawPrefix,locale,false).trimStart()
                val suffix=fold(rawSuffix,locale,false).trimEnd()
                if(prefix.isEmpty() && suffix.isEmpty()) continue
                if(value.startsWith(prefix) && value.endsWith(suffix) && value.length>prefix.length+suffix.length) {
                    val name=value.substring(prefix.length,value.length-suffix.length).trim()
                    if(name.isNotEmpty()) return name
                }
            }
            return null
        }
        capture("water_prefix","water_suffix")?.let { return VoiceCommand.Water(it) }
        capture("location_prefix","location_suffix")?.let { return VoiceCommand.Location(it) }
        return VoiceCommand.Unknown
    }
}
