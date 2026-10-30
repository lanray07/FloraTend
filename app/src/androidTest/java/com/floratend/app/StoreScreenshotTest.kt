package com.floratend.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.time.LocalDate
import java.util.Locale

/** Captures real rendered UI, with isolated demonstration records, never synthetic UI. */
class StoreScreenshotTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val context:Context get()=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private val app get()=context as FloraApp
    private var oldTag=""
    private var oldLocale=Locale.getDefault()
    private var oldOnboarded=false
    private var oldDark=false
    private var oldReminders=false
    private val id="qa-store-monstera"
    private val folder get()=File(context.getExternalFilesDir(null),"store-captures").apply{mkdirs()}
    private fun text(res:Int)=AppLocales.wrap(context).getString(res)
    private fun waitText(value:String)=rule.waitUntil(15000){runCatching{rule.onAllNodesWithText(value,substring=false).fetchSemanticsNodes().isNotEmpty()}.getOrDefault(false)}
    private fun nav(res:Int) {
        val node=rule.onNodeWithText(text(res),substring=false)
        if(AppLocales.wrap(context).resources.configuration.screenWidthDp>=720) node.performScrollTo()
        node.performClick()
    }
    private suspend fun clean() {
        app.database.dao().plants().first().filter{it.id.startsWith("qa-store-")}.forEach{app.database.dao().delete(it)}
    }
    @Before fun prepare() {
        oldTag=AppLocales.selected(context);oldLocale=Locale.getDefault()
        runBlocking {
            oldOnboarded=app.settings.onboarded.first();oldDark=app.settings.dark.first();oldReminders=app.settings.reminders.first()
            clean()
            check(app.database.dao().plants().first().isEmpty()) { "Store capture requires an empty test collection; refusing to expose real plant records" }
            app.settings.set("onboarded",true);app.settings.set("dark",false);app.settings.set("reminders",false)
        }
        val image=File(context.cacheDir,"qa-store-botanical.png")
        val bitmap=BitmapFactory.decodeResource(context.resources,R.drawable.botanical_hero)
        image.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        runBlocking {
            app.database.dao().save(Plant(id=id,name="Monstera deliciosa",species="Araceae",photo=image.absolutePath))
            app.database.dao().save(Plant(id="qa-store-fern",name="Nephrolepis exaltata",species="Nephrolepis exaltata"))
            app.database.dao().save(Plant(id="qa-store-sansevieria",name="Dracaena trifasciata",species="Dracaena trifasciata"))
            app.database.dao().save(CareSchedule(id="qa-store-soil",plantId=id,nextDate=LocalDate.now().toString(),intervalDays=7))
            app.database.dao().save(CareSchedule(id="qa-store-feed",plantId="qa-store-fern",type="fertilise",nextDate=LocalDate.now().plusDays(4).toString(),intervalDays=28))
            app.database.dao().record(CareRecord(id="qa-store-history",plantId=id,type="soil",date=LocalDate.now().minusDays(7).toString()))
        }
    }
    @After fun restore() {
        runBlocking{clean();app.settings.set("onboarded",oldOnboarded);app.settings.set("dark",oldDark);app.settings.set("reminders",oldReminders)}
        AppLocales.choose(context,oldTag);Locale.setDefault(oldLocale)
        File(context.cacheDir,"qa-store-botanical.png").delete()
    }
    private fun shot(tag:String,scene:String):JSONObject {
        rule.waitForIdle();InstrumentationRegistry.getInstrumentation().waitForIdleSync();Thread.sleep(400)
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot() ?: error("Screenshot unavailable")
        val dir=File(folder,tag).apply{mkdirs()};val file=File(dir,"$scene.png")
        file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        val result=JSONObject().put("scene",scene).put("file","$tag/$scene.png").put("width",bitmap.width).put("height",bitmap.height)
        bitmap.recycle();return result
    }
    @Test fun captureEightScenesInEveryAppLocale() {
        val all=JSONArray()
        for(tag in AppLocales.supported) {
            AppLocales.choose(context,tag)
            runBlocking { app.settings.set("dark",false) }
            rule.activityRule.scenario.onActivity{it.recreate()}
            waitText(text(R.string.your_garden));waitText("Monstera deliciosa")
            rule.activityRule.scenario.onActivity{assertEquals(Locale.forLanguageTag(tag).toLanguageTag(),it.resources.configuration.locales[0].toLanguageTag())}
            val scenes=JSONArray()
            scenes.put(shot(tag,"01-hero"))
            nav(R.string.calendar);waitText(text(R.string.care_calendar));scenes.put(shot(tag,"02-watering"))
            nav(R.string.garden);waitText("Monstera deliciosa")
            rule.onNodeWithText("Monstera deliciosa",substring=false).performScrollTo().performClick()
            waitText(text(R.string.close));scenes.put(shot(tag,"03-library"))
            rule.onNodeWithText(text(R.string.close),substring=false).performClick()
            nav(R.string.calendar);waitText(text(R.string.month));rule.onNodeWithText(text(R.string.month),substring=false).performClick()
            scenes.put(shot(tag,"04-calendar"))
            runBlocking {
                app.database.dao().save(JournalEntry(id="qa-store-journal",plantId=id,date=LocalDate.now().toString(),note=text(R.string.check_first),heightCm=45.0,leaves=7))
            }
            nav(R.string.journal);waitText(text(R.string.growth_journal));waitText(text(R.string.check_first));scenes.put(shot(tag,"05-growth"))
            nav(R.string.voice);waitText(text(R.string.voice_assistant));scenes.put(shot(tag,"06-voice"))
            nav(R.string.garden);waitText(text(R.string.your_garden))
            runBlocking{app.settings.set("dark",true)};rule.waitForIdle();scenes.put(shot(tag,"07-offline"))
            runBlocking{app.settings.set("dark",false)}
            nav(R.string.insights);waitText(text(R.string.total_plants));scenes.put(shot(tag,"08-insights"))
            all.put(JSONObject().put("locale",tag).put("scenes",scenes))
            File(folder,"capture-manifest.json").writeText(JSONObject().put("versionCode",BuildConfig.VERSION_CODE).put("versionName",BuildConfig.VERSION_NAME).put("createdAtEpochMs",System.currentTimeMillis()).put("demonstrationRecords",true).put("humanLanguageApproval",false).put("locales",all).toString(2))
        }
        assertEquals(19,all.length())
    }
}
