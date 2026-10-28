package com.floratend.app

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class LocalizedVoiceTest {
    private val tags=listOf("en-GB","en-US","en-AU","es-ES","es-419","fr-FR","fr-CA","de-DE","it-IT","pt-BR","pt-PT","nl-NL","pl-PL","sv-SE","ja-JP","hi-IN","ar","tr-TR","id-ID")
    private fun load(tag:String):VoiceDictionary {
        val input=javaClass.classLoader!!.getResourceAsStream("voice/$tag.properties") ?: error("Missing bundled dictionary $tag")
        return input.use { VoiceDictionary.read(it) }.also { VoiceParser.install(tag,it) }
    }
    @Test fun everyBundledLocaleSupportsCoreCommandsAndNames() {
        for(tag in tags) {
            val locale=Locale.forLanguageTag(tag);val dictionary=load(tag)
            for((key,expected) in listOf("add" to VoiceCommand.Add,"due" to VoiceCommand.Due,"calendar" to VoiceCommand.Calendar,"read" to VoiceCommand.Read)) {
                assertTrue("$tag needs $key commands",dictionary.commands[key].orEmpty().isNotEmpty())
                for(example in dictionary.commands.getValue(key)) assertEquals("$tag $example",expected,VoiceParser.parse(example+"!",locale))
            }
            val prefix=dictionary.commands.getValue("water_prefix").first()
            val suffix=dictionary.commands.getValue("water_suffix").first()
            assertEquals("$tag watering",VoiceCommand.Water("monstera"),VoiceParser.parse(prefix+"Monstera"+suffix+".",locale))
            val locationPrefix=dictionary.commands["location_prefix"]?.first().orEmpty()
            val locationSuffix=dictionary.commands["location_suffix"]?.first().orEmpty()
            assertEquals("$tag location",VoiceCommand.Location("room"),VoiceParser.parse(locationPrefix+"Room"+locationSuffix,locale))
            assertEquals("$tag never supports destructive commands",VoiceCommand.Unknown,VoiceParser.parse("DELETE ALL RECORDS",locale))
        }
    }
    @Test fun canonicalRealWorldTranscriptions() {
        tags.forEach(::load)
        val examples=listOf(
            Triple("es-ES","Añade una planta nueva.",VoiceCommand.Add),
            Triple("fr-FR","Lis mes rappels.",VoiceCommand.Read),
            Triple("de-DE","Zeige meinen Pflegekalender.",VoiceCommand.Calendar),
            Triple("ja-JP","新しい植物を追加",VoiceCommand.Add),
            Triple("ar","أضف نباتا جديدا",VoiceCommand.Add),
            Triple("tr-TR","Yeni bir bitki ekle.",VoiceCommand.Add),
            Triple("id-ID","Tambahkan tanaman baru.",VoiceCommand.Add)
        )
        for((tag,text,expected) in examples) assertEquals("$tag $text",expected,VoiceParser.parse(text,Locale.forLanguageTag(tag)))
    }
    @Test fun unicodePunctuationAndUnambiguousAffixes() {
        load("fr-FR")
        assertEquals(VoiceCommand.Read,VoiceParser.parse("  LIS  MES RAPPELS！ ",Locale.FRANCE))
        assertEquals(VoiceCommand.Due,VoiceParser.parse("Quelles plantes ont besoin d'arrosage aujourd'hui ?",Locale.FRANCE))
        assertEquals(VoiceCommand.Unknown,VoiceParser.parse("marque mon comme arrosé",Locale.FRANCE))
    }
}
