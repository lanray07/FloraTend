package com.floratend.app

import android.content.Context
import android.graphics.Bitmap
import android.speech.SpeechRecognizer
import android.view.View
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

/** Real resource and typed-command tests. This class never claims recognised speech. */
class LocalizedUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private val app: FloraApp get() = context as FloraApp
    private val representatives = listOf("es-ES", "de-DE", "ja-JP", "ar")
    private val fixtureId = "qa-localization-plant"
    private val fixtureName = "FloraTend QA Monstera"
    private var originalTag = ""
    private var originalDefault = Locale.getDefault()
    private var originalOnboarded = false
    private var originalDark = false
    private var originalReminders = false

    @Before fun prepare() {
        originalTag = AppLocales.selected(context)
        originalDefault = Locale.getDefault()
        runBlocking {
            originalOnboarded = app.settings.onboarded.first()
            originalDark = app.settings.dark.first()
            originalReminders = app.settings.reminders.first()
            removeFixtures()
            app.settings.set("onboarded", true)
            app.settings.set("dark", false)
            app.settings.set("reminders", false)
        }
        rule.waitForIdle()
    }

    @After fun restore() {
        // Application context remains valid even if ActivityScenario is between instances.
        runBlocking {
            removeFixtures()
            app.settings.set("onboarded", originalOnboarded)
            app.settings.set("dark", originalDark)
            app.settings.set("reminders", originalReminders)
        }
        AppLocales.choose(context, originalTag)
        Locale.setDefault(originalDefault)
    }

    private suspend fun removeFixtures() {
        app.database.dao().plants().first().filter { it.id.startsWith("qa-localization-") }
            .forEach { app.database.dao().delete(it) }
    }

    private fun seed() {
        runBlocking {
            removeFixtures()
            app.database.dao().save(Plant(id=fixtureId, name=fixtureName, location="FloraTend QA room"))
            app.database.dao().save(CareSchedule(id="qa-localization-soil", plantId=fixtureId,
                nextDate=LocalDate.now().toString(), intervalDays=7))
        }
    }

    private fun localizedContext(): Context = AppLocales.wrap(context)
    private fun text(id: Int): String = localizedContext().getString(id)

    private fun dictionary(tag: String): VoiceDictionary = context.assets.open("voice/$tag.properties").use {
        VoiceDictionary.read(it).also { loaded -> VoiceParser.install(tag, loaded) }
    }

    private fun waitText(value: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithText(value, substring=false).fetchSemanticsNodes().isNotEmpty() }
                .getOrDefault(false)
        }
    }

    private fun chooseAndRecreate(tag: String) {
        AppLocales.choose(context, tag)
        rule.activityRule.scenario.onActivity { it.recreate() }
        rule.waitUntil(20000) {
            runCatching {
                var ready = false
                rule.activityRule.scenario.onActivity { activity ->
                    ready = !activity.isFinishing && !activity.isDestroyed &&
                        activity.resources.configuration.locales[0].toLanguageTag() == Locale.forLanguageTag(tag).toLanguageTag() &&
                        AppLocales.selected(activity) == tag
                }
                ready
            }.getOrDefault(false)
        }
        dictionary(tag)
        waitText(text(R.string.your_garden))
        rule.waitForIdle()
    }

    private fun nav(id: Int) {
        val node = rule.onNodeWithText(text(id), substring=false)
        if (localizedContext().resources.configuration.screenWidthDp >= 720) node.performScrollTo()
        node.performClick()
    }

    private fun screenshot(tag: String, screen: String) {
        rule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        Thread.sleep(400) // Allow the window compositor to publish the semantically idle frame.
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: error("Screenshot unavailable")
        val folder = File(context.getExternalFilesDir(null), "localization").apply { mkdirs() }
        File(folder, "$tag-$screen.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }

    private fun report(name: String, value: JSONObject) {
        val folder = File(context.getExternalFilesDir(null), "localization").apply { mkdirs() }
        File(folder,name).writeText(value.toString(2))
    }

    @Test fun allNineteenResourceBundlesAndCommandDictionariesLoad() {
        assertEquals(19, AppLocales.supported.size)
        AppLocales.choose(context,"en-GB")
        val sourceHeading = localizedContext().getString(R.string.your_garden)
        val stringIds = R.string::class.java.fields.filter { it.type == Int::class.javaPrimitiveType }
        assertTrue("Expected the complete app string resource class", stringIds.size >= 160)
        val results = JSONArray()
        for (tag in AppLocales.supported) {
            AppLocales.choose(context,tag)
            val wrapped = localizedContext()
            val locale = Locale.forLanguageTag(tag)
            assertEquals(locale.toLanguageTag(), wrapped.resources.configuration.locales[0].toLanguageTag())
            for (field in stringIds) {
                assertTrue("$tag/${field.name} must load nonempty", wrapped.getString(field.getInt(null)).isNotBlank())
            }
            if (locale.language != "en") assertNotEquals("$tag heading must not fall back to source English",sourceHeading,wrapped.getString(R.string.your_garden))
            for (count in listOf(0,1,2,3,11,100)) {
                assertTrue(wrapped.resources.getQuantityString(R.plurals.reminder_count,count,count).isNotBlank())
                assertTrue(wrapped.resources.getQuantityString(R.plurals.collection_count,count,count).isNotBlank())
            }
            assertEquals(if(tag=="ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR,
                wrapped.resources.configuration.layoutDirection)
            val loaded = dictionary(tag)
            for (key in listOf("add","due","calendar","read","water_prefix","water_suffix","location_prefix")) {
                assertTrue("$tag/$key is required", loaded.commands[key]?.isNotEmpty() == true)
            }
            assertEquals(VoiceCommand.Add,VoiceParser.parse(loaded.commands.getValue("add").first(),locale))
            assertEquals(VoiceCommand.Due,VoiceParser.parse(loaded.commands.getValue("due").first(),locale))
            assertEquals(VoiceCommand.Calendar,VoiceParser.parse(loaded.commands.getValue("calendar").first(),locale))
            assertEquals(VoiceCommand.Read,VoiceParser.parse(loaded.commands.getValue("read").first(),locale))
            val water = loaded.commands.getValue("water_prefix").first()+fixtureName+loaded.commands.getValue("water_suffix").first()
            assertEquals(VoiceCommand.Water(fixtureName.lowercase(locale)),VoiceParser.parse(water,locale))
            results.put(JSONObject().put("locale",tag).put("loadedStringCount",stringIds.size)
                .put("pluralSampleCounts",JSONArray(listOf(0,1,2,3,11,100)))
                .put("layoutDirection",wrapped.resources.configuration.layoutDirection)
                .put("dictionaryLoaded",true).put("fiveTextCommandsParsed",true))
        }
        report("resource-and-dictionary-checks.json",JSONObject().put("locales",results)
            .put("speechRecognitionExercised",false).put("humanLinguisticReview",false))
    }

    @Test fun selectedLanguagePersistsAndRepresentativeScreensRender() {
        seed()
        val results = JSONArray()
        for (tag in representatives) {
            chooseAndRecreate(tag)
            assertEquals(tag,AppLocales.selected(context))
            waitText(fixtureName)
            rule.onNodeWithText(text(R.string.your_garden),substring=false).assertIsDisplayed()
            rule.onNodeWithText(text(R.string.add_plant),substring=false).assertIsDisplayed()
            screenshot(tag,"garden")
            var direction = -1
            rule.activityRule.scenario.onActivity { direction=it.resources.configuration.layoutDirection }
            assertEquals(if(tag=="ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR,direction)
            nav(R.string.voice)
            waitText(text(R.string.voice_assistant))
            rule.onNodeWithText(text(R.string.voice_assistant),substring=false).assertIsDisplayed()
            rule.onNodeWithText(text(R.string.command),substring=false).assertExists()
            rule.onNodeWithText(text(R.string.voice_examples),substring=false).assertExists()
            screenshot(tag,"voice")
            results.put(JSONObject().put("locale",tag).put("selectedTagPersisted",true)
                .put("activityRecreated",true).put("activityLayoutDirection",direction)
                .put("gardenScreenshot","$tag-garden.png").put("voiceScreenshot","$tag-voice.png"))
        }
        report("representative-ui-checks.json",JSONObject().put("locales",results)
            .put("humanTypographyOrTranslationApproval",false))
    }

    private fun submit(command: String) {
        rule.onNodeWithText(text(R.string.command),substring=false).performScrollTo().performTextReplacement(command)
        rule.onNodeWithText(text(R.string.run_command),substring=false).performScrollTo().performClick()
    }

    private fun assertReminderResponse() {
        val count = runBlocking { app.database.dao().due(LocalDate.now().toString()).size }
        val expected = localizedContext().resources.getQuantityString(R.plurals.reminder_count,count,count)
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithText(expected,substring=true).fetchSemanticsNodes().isNotEmpty() }
                .getOrDefault(false)
        }
        rule.onNodeWithText(expected,substring=true).assertExists()
    }

    @Test fun representativeTranslatedTypedCommandsTriggerRealUiActions() {
        val results = JSONArray()
        for (tag in representatives) {
            seed()
            chooseAndRecreate(tag)
            val loaded = dictionary(tag)
            waitText(fixtureName)
            nav(R.string.voice)
            waitText(text(R.string.voice_assistant))
            submit(loaded.commands.getValue("add").first())
            waitText(text(R.string.plant_details))
            rule.onNodeWithText(text(R.string.plant_details),substring=false).assertIsDisplayed()
            rule.onNodeWithText(text(R.string.cancel),substring=false).performClick()
            rule.waitUntil(15000) { rule.onAllNodesWithText(text(R.string.plant_details),substring=false).fetchSemanticsNodes().isEmpty() }
            submit(loaded.commands.getValue("due").first())
            assertReminderResponse()
            submit(loaded.commands.getValue("calendar").first())
            waitText(text(R.string.care_calendar))
            rule.onNodeWithText(text(R.string.care_calendar),substring=false).assertIsDisplayed()
            nav(R.string.voice)
            waitText(text(R.string.voice_assistant))
            submit(loaded.commands.getValue("read").first())
            assertReminderResponse()
            val water = loaded.commands.getValue("water_prefix").first()+fixtureName+loaded.commands.getValue("water_suffix").first()
            submit(water)
            waitText(text(R.string.confirm_water))
            assertTrue(runBlocking { app.database.dao().history().first().none { it.plantId==fixtureId } })
            rule.onNodeWithText(text(R.string.confirm),substring=false).performClick()
            rule.waitUntil(15000) { runBlocking { app.database.dao().history().first().count { it.plantId==fixtureId }==1 } }
            assertEquals(LocalDate.now().plusDays(7).toString(),runBlocking {
                app.database.dao().schedules().first().single { it.plantId==fixtureId }.nextDate
            })
            waitText(text(R.string.care_saved))
            results.put(JSONObject().put("locale",tag).put("typedAddOpenedEditor",true)
                .put("typedDueReadActualReminders",true).put("typedCalendarNavigated",true)
                .put("typedReadReadActualReminders",true).put("typedWaterRequiredConfirmationAndSaved",true))
        }
        report("translated-typed-command-checks.json",JSONObject().put("locales",results)
            .put("speechRecognitionExercised",false)
            .put("recognitionServiceAvailable",SpeechRecognizer.isRecognitionAvailable(context)))
    }
}
