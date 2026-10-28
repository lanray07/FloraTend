package com.floratend.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class CareEngineTest {
    @Test fun recurrenceCrossesLeapDayAndYear() {
        assertEquals(LocalDate.of(2028,2,29),Recurrence.next(LocalDate.of(2028,2,28),1))
        assertEquals(LocalDate.of(2027,1,7),Recurrence.next(LocalDate.of(2026,12,31),7))
    }
    @Test fun reminderKeepsLocalMorningAcrossDaylightSaving() {
        val zone=ZoneId.of("Europe/London")
        val before=Recurrence.reminder(LocalDate.of(2026,3,28),zone)
        val after=Recurrence.reminder(LocalDate.of(2026,3,29),zone)
        assertEquals(9,before.hour);assertEquals(9,after.hour)
        assertEquals(23,java.time.Duration.between(before,after).toHours())
    }
    @Test(expected=IllegalArgumentException::class) fun rejectZeroInterval() { Recurrence.next(LocalDate.now(),0) }
    @Test(expected=IllegalArgumentException::class) fun rejectExcessiveInterval() { Recurrence.next(LocalDate.now(),3651) }
    @Test fun requiredVoiceCommands() {
        assertEquals(VoiceCommand.Add,VoiceParser.parse("Add a new plant.",Locale.UK))
        assertEquals(VoiceCommand.Due,VoiceParser.parse("Which plants need watering today?",Locale.UK))
        assertEquals(VoiceCommand.Water("monstera"),VoiceParser.parse("Mark my Monstera as watered.",Locale.UK))
        assertEquals(VoiceCommand.Calendar,VoiceParser.parse("Show my care calendar.",Locale.UK))
        assertEquals(VoiceCommand.Read,VoiceParser.parse("Read my reminders.",Locale.UK))
    }
    @Test fun unknownLanguageNeverExecutesEnglishCommand() { assertEquals(VoiceCommand.Unknown,VoiceParser.parse("Add plant",Locale.JAPAN)) }
    @Test fun voiceDoesNotAcceptDestructiveCommand() { assertEquals(VoiceCommand.Unknown,VoiceParser.parse("Delete all plants",Locale.UK)) }
    @Test fun csvProtectsFormulasAndQuotes() {
        val csv=Export.csv(listOf(Plant(name="=HYPERLINK(\"danger\")",notes="a,b\nsecond")))
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"danger\"\")\""))
        assertTrue(csv.contains("\"a,b\nsecond\""))
    }
}
