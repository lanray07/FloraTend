package com.floratend.app

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LocalizedFormattingTest {
    @Test fun monthFormattingMatchesAndroidCalendarOrder() {
        val app=InstrumentationRegistry.getInstrumentation().targetContext
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val old=Locale.getDefault();val rows=JSONArray();val mismatches=mutableListOf<String>()
        val date=Date.from(LocalDate.of(2011,3,9).atStartOfDay(ZoneId.systemDefault()).toInstant())
        try {
            for(tag in AppLocales.supported) {
                val locale=Locale.forLanguageTag(tag);Locale.setDefault(locale)
                val actual=formatMonth("2011-03")
                val pattern=android.icu.text.DateTimePatternGenerator.getInstance(locale).getBestPattern("yMMMM")
                val expected=android.icu.text.SimpleDateFormat(pattern,locale).format(date)
                rows.put(JSONObject().put("locale",tag).put("actual",actual).put("androidReference",expected))
                if(actual!=expected) mismatches+="$tag expected <$expected> actual <$actual>"
            }
            val folder=File(app.getExternalFilesDir(null),"localized-formatting").apply { mkdirs() }
            File(folder,"months.json").writeText(rows.toString(2))
            assertTrue(mismatches.joinToString("; "),mismatches.isEmpty())
        } finally { Locale.setDefault(old) }
    }
    @Test fun dateFormattingMatchesAndroidLocaleConvention() {
        val app=InstrumentationRegistry.getInstrumentation().targetContext
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val old=Locale.getDefault();val rows=JSONArray();val mismatches=mutableListOf<String>()
        val date=Date.from(LocalDate.of(2011,3,9).atStartOfDay(ZoneId.systemDefault()).toInstant())
        try {
            for(tag in AppLocales.supported) {
                val locale=Locale.forLanguageTag(tag);Locale.setDefault(locale)
                val actual=formatDate("2011-03-09")
                val expected=android.icu.text.DateFormat.getDateInstance(android.icu.text.DateFormat.MEDIUM,locale).format(date)
                rows.put(JSONObject().put("locale",tag).put("actual",actual).put("androidReference",expected))
                if(actual!=expected) mismatches+="$tag expected <$expected> actual <$actual>"
            }
            val folder=File(app.getExternalFilesDir(null),"localized-formatting").apply { mkdirs() }
            File(folder,"dates.json").writeText(rows.toString(2))
            assertTrue(mismatches.joinToString("; "),mismatches.isEmpty())
        } finally { Locale.setDefault(old) }
    }
}
