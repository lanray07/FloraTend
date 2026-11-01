package com.floratend.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.util.Locale

/** Compiled-resource contracts, not human linguistic approval or real speech recognition. */
class LocalizedSurfaceContractTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val app get() = rule.activity.application as FloraApp
    private var oldTag = ""
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        oldTag=AppLocales.selected(app)
        runBlocking { app.settings.set("onboarded",true) }
    }
    @After fun restore() { AppLocales.choose(app,oldTag) }
    private fun choose(tag:String) {
        AppLocales.choose(app,tag)
        rule.activityRule.scenario.recreate()
        rule.waitUntil(20000) { runCatching {
            rule.activity.resources.configuration.locales[0].toLanguageTag()==Locale.forLanguageTag(tag).toLanguageTag()
        }.getOrDefault(false) }
        rule.waitForIdle()
    }
    private fun nav(id:Int) {
        val node=rule.onNodeWithText(rule.activity.getString(id),substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) node.performScrollTo()
        node.performClick()
    }
    @Test fun allLocalePaywallsVoicePromptsAndToggleLabelsUseSelectedResources() {
        val rows=JSONArray()
        for(tag in AppLocales.supported) {
            choose(tag)
            nav(R.string.settings)
            for(id in listOf(R.string.premium,R.string.premium_intro,R.string.restore)) {
                rule.onNodeWithText(rule.activity.getString(id),substring=false).performScrollTo().assertIsDisplayed()
            }
            for(id in listOf(R.string.dark_theme,R.string.spoken_feedback,R.string.imperial,R.string.care_reminders)) {
                rule.onNodeWithContentDescription(rule.activity.getString(id),substring=false).performScrollTo().assertIsDisplayed()
            }
            rule.onNodeWithText(rule.activity.getString(R.string.purchase_verification_privacy),substring=false).performScrollTo().assertIsDisplayed()
            nav(R.string.voice)
            rule.onNodeWithText(rule.activity.getString(R.string.voice_intro),substring=false).assertIsDisplayed()
            for(id in listOf(R.string.voice_privacy,R.string.command,R.string.voice_examples)) {
                rule.onNodeWithText(rule.activity.getString(id),substring=false).performScrollTo().assertIsDisplayed()
            }
            rows.put(JSONObject().put("locale",tag).put("paywallTexts",3).put("toggleAccessibilityLabels",4).put("voicePrompts",4))
        }
        val dir=File(app.getExternalFilesDir(null),"surface-contracts").apply { mkdirs() }
        File(dir,"surfaces.json").writeText(JSONObject().put("locales",rows).put("humanApproval",false)
            .put("realSpeechRecognition",false).put("amazonPurchase",false).toString(2))
    }
    @Test fun compiledPluralsMatchIcuCategoryAndSourceForms() {
        val expected=JSONObject(InstrumentationRegistry.getInstrumentation().context.assets.open("plural-source-forms.json").bufferedReader().use { it.readText() })
        val rows=JSONArray()
        for(tag in AppLocales.supported) {
            val locale=Locale.forLanguageTag(tag)
            AppLocales.choose(app,tag)
            val ctx=AppLocales.wrap(app)
            val rules=android.icu.text.PluralRules.forLocale(locale)
            for((name,id) in listOf("collection_count" to R.plurals.collection_count,"reminder_count" to R.plurals.reminder_count)) {
                val forms=expected.getJSONObject(tag).getJSONObject(name)
                for(count in listOf(0,1,2,3,4,5,11,21,22,25,100,101,102)) {
                    val category=rules.select(count.toDouble())
                    val form=forms.optString(category,forms.getString("other"))
                    val wanted=String.format(locale,form,count)
                    val actual=ctx.resources.getQuantityString(id,count,count)
                    assertEquals("$tag/$name/$count/$category",wanted,actual)
                    rows.put(JSONObject().put("locale",tag).put("plural",name).put("count",count).put("category",category).put("matched",true))
                }
            }
        }
        val dir=File(app.getExternalFilesDir(null),"surface-contracts").apply { mkdirs() }
        File(dir,"plurals.json").writeText(JSONObject().put("comparisons",rows).put("humanApproval",false).toString(2))
    }
}
