package com.floratend.app

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class LocaleSelectionTest {
    @Test fun chineseScriptsAndRegionsSelectTheMatchingDictionary() {
        for(tag in listOf("zh-TW","zh-HK","zh-MO","zh-Hant"))
            assertEquals(tag,"zh-TW",AppLocales.dictionaryTag(Locale.forLanguageTag(tag)))
        for(tag in listOf("zh-CN","zh-SG","zh-Hans","zh"))
            assertEquals(tag,"zh-CN",AppLocales.dictionaryTag(Locale.forLanguageTag(tag)))
    }
    @Test fun explicitChineseScriptOverridesRegionFallback() {
        for(region in listOf("TW","HK","MO"))
            assertEquals("zh-Hans-$region","zh-CN",AppLocales.dictionaryTag(Locale.forLanguageTag("zh-Hans-$region")))
        assertEquals("zh-TW",AppLocales.dictionaryTag(Locale.forLanguageTag("zh-Hant-CN")))
    }
    @Test fun standingPriorityLanguagesHaveBundledChoices() {
        assertTrue(AppLocales.supported.containsAll(listOf("ko-KR","zh-CN","zh-TW")))
        assertEquals("ko-KR",AppLocales.dictionaryTag(Locale.KOREAN))
        assertNull(AppLocales.dictionaryTag(Locale.forLanguageTag("ga-IE")))
    }
}
