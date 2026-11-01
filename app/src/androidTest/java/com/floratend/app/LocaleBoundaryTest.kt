package com.floratend.app

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.util.Locale

/** Controlled native locale boundaries; not human wording approval or speech recognition. */
class LocaleBoundaryTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val id="qa-locale-boundary-plant"
    private var oldTag=""
    private var oldLocale=Locale.getDefault()
    private var oldImperial=false
    private var oldOnboarded=false
    private var oldDark=false
    private var photo:File?=null
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        oldTag=AppLocales.selected(app);oldLocale=Locale.getDefault()
        runBlocking {
            oldImperial=app.settings.imperial.first();oldOnboarded=app.settings.onboarded.first()
            oldDark=app.settings.dark.first()
            app.settings.set("onboarded",true)
        }
    }
    @After fun restore():Unit=runBlocking {
        app.database.dao().plants().first().filter { it.id==id }.forEach { app.database.dao().delete(it) }
        app.settings.set("imperial",oldImperial);app.settings.set("onboarded",oldOnboarded)
        app.settings.set("dark",oldDark)
        photo?.delete();AppLocales.choose(app,oldTag);Locale.setDefault(oldLocale)
    }
    private fun choose(tag:String) {
        AppLocales.choose(app,tag);rule.activityRule.scenario.recreate()
        rule.waitUntil(15000) { runCatching { rule.activity.resources.configuration.locales[0].toLanguageTag()==Locale.forLanguageTag(tag).toLanguageTag() }.getOrDefault(false) }
        rule.waitForIdle()
    }
    private fun nav(resource:Int) {
        val node=rule.onNodeWithText(rule.activity.getString(resource),substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) node.performScrollTo()
        node.performClick()
    }
    private fun waitText(text:String) {
        rule.waitUntil(10000) { rule.onAllNodesWithText(text,substring=false).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(text,substring=false).assertExists()
    }
    private fun report(name:String,rows:JSONArray) {
        val folder=File(app.getExternalFilesDir(null),"locale-boundaries").apply { mkdirs() }
        File(folder,name).writeText(JSONObject().put("locales",rows).put("humanApproval",false).toString(2))
    }
    @Test fun actualPhotoAndCardLabelsFollowEverySelectedLocale() {
        val image=File(app.cacheDir,"qa-locale-boundary.jpg");photo=image
        val bitmap=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.GREEN) }
        image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG,90,it) };bitmap.recycle()
        val rows=JSONArray()
        for(tag in AppLocales.supported) {
            choose(tag)
            val photoLabel=rule.activity.getString(R.string.plant_photo)
            val category=rule.activity.getString(R.string.herbs)
            val cardLabel="Controlled herb. Scientific identifier. $category. Controlled room"
            rule.activityRule.scenario.onActivity { activity -> activity.setContent {
                MaterialTheme { Column {
                    Photo(image.absolutePath)
                    PlantCard(Plant(id=id,name="Controlled herb",species="Scientific identifier",category="Herb",location="Controlled room"),null) {}
                } }
            } }
            rule.waitForIdle()
            rule.onNodeWithContentDescription(photoLabel,substring=false).assertIsDisplayed()
            rule.onNodeWithContentDescription(cardLabel,substring=false).assertIsDisplayed()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role,Role.Button))
            rows.put(JSONObject().put("locale",tag).put("photoLabel",photoLabel).put("cardLabel",cardLabel).put("cardButtonRole",true))
        }
        report("accessibility-labels.json",rows)
    }
    @Test fun realJournalAndPotMeasurementsMatchIndependentLocaleFormatting() {
        runBlocking {
            app.database.dao().save(Plant(id=id,name="Measurement fixture",potCm=12.3))
            app.database.dao().save(JournalEntry(id=id,plantId=id,date="2026-01-02",note="Controlled measurement",heightCm=25.4,leaves=1234))
        }
        val rows=JSONArray()
        for(tag in AppLocales.supported) {
            choose(tag);val locale=Locale.forLanguageTag(tag);nav(R.string.journal)
            val measurements=JSONArray()
            for(imperial in listOf(false,true)) {
                runBlocking { app.settings.set("imperial",imperial) }
                val number=android.icu.text.NumberFormat.getNumberInstance(locale).format(if(imperial) 10.0 else 25.4)
                val height=rule.activity.getString(if(imperial) R.string.height_in_value else R.string.height_value,number)
                val leafNumber=android.icu.text.NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed=false }.format(1234)
                val leaves=String.format(locale,rule.activity.getString(R.string.leaves_value).replace("%1${'$'}d","%1${'$'}s"),leafNumber)
                waitText(height);waitText(leaves)
                measurements.put(JSONObject().put("imperial",imperial).put("height",height).put("leaves",leaves))
            }
            nav(R.string.garden);waitText("Measurement fixture")
            rule.onNodeWithText("Measurement fixture",substring=false).performClick()
            val potNumber=android.icu.text.NumberFormat.getNumberInstance(locale).apply { minimumFractionDigits=1;maximumFractionDigits=1;isGroupingUsed=false }.format(12.3)
            val pot=String.format(locale,rule.activity.getString(R.string.pot_value).replace("%1${'$'}.1f","%1${'$'}s"),potNumber)
            waitText(pot);rule.onNodeWithText(rule.activity.getString(R.string.close),substring=false).performClick()
            rows.put(JSONObject().put("locale",tag).put("growthMeasurements",measurements).put("pot",pot))
        }
        report("measurements.json",rows)
    }
    @Test fun majorStatusesExposeTextAndSelectableState() {
        runBlocking {
            app.settings.set("dark",false)
            app.database.dao().save(Plant(id=id,name="Controlled status plant"))
            app.database.dao().save(CareSchedule(id=id,plantId=id))
        }
        choose("en-GB");nav(R.string.calendar)
        rule.onNodeWithText("Pause",substring=false).performScrollTo().performClick()
        waitText("Paused");waitText("Resume")
        rule.onNodeWithText("Record care",substring=false).assertIsNotEnabled()
        rule.onNodeWithText("Resume",substring=false).performScrollTo().performClick()
        waitText("Pause")
        rule.onNodeWithText("Record care",substring=false).assertIsEnabled()
        nav(R.string.garden)
        rule.onNodeWithText("Indoor plants",substring=false).performClick().assertIsSelected()
        nav(R.string.settings)
        val toggle=rule.onNodeWithContentDescription("Dark theme",substring=false)
        toggle.performScrollTo().assertIsOff().performClick()
        rule.waitUntil(10000) { runCatching { toggle.assertIsOn();true }.getOrDefault(false) }
        report("status-semantics.json",JSONArray().put(JSONObject().put("locale","en-GB")
            .put("pausedAndResumeText",true).put("disabledActionSemantics",true)
            .put("categorySelectedSemantics",true).put("switchToggleState",true)))
    }
}
