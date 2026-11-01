package com.floratend.app

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.util.Locale

/** Large-font layout contracts; does not approve translated wording or other hardware. */
class LocaleTextLayoutTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val id="qa-locale-layout-plant"
    private var tag=""
    private var onboarded=false
    private var dark=false
    private val variant get()=InstrumentationRegistry.getArguments().getString("variant") ?: "default"
    private val rows=JSONArray()
    private val folder get()=File(app.getExternalFilesDir(null),"locale-layouts/$variant").apply { mkdirs() }
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        tag=AppLocales.selected(app)
        runBlocking {
            onboarded=app.settings.onboarded.first();dark=app.settings.dark.first()
            app.settings.set("dark",false)
            app.database.dao().save(Plant(id=id,name="Layout fixture",location="Controlled room"))
            app.database.dao().save(CareSchedule(id=id,plantId=id))
            app.database.dao().save(JournalEntry(id=id,plantId=id,note="Controlled layout note",heightCm=25.4,leaves=7))
        }
        assertTrue("Run at the intended enlarged font",rule.activity.resources.configuration.fontScale>=1.75f)
    }
    @After fun restore():Unit=runBlocking {
        app.database.dao().plants().first().filter { it.id==id }.forEach { app.database.dao().delete(it) }
        app.settings.set("onboarded",onboarded);app.settings.set("dark",dark);AppLocales.choose(app,tag)
    }
    private fun waitText(id:Int) {
        val text=rule.activity.getString(id)
        rule.waitUntil(15000) { rule.onAllNodesWithText(text,substring=false).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun nav(id:Int) {
        val node=rule.onNodeWithText(rule.activity.getString(id),substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) node.performScrollTo()
        node.performClick()
    }
    private fun inspect(locale:String,surface:String) {
        rule.waitForIdle()
        val nodes=rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),useUnmergedTree=true).fetchSemanticsNodes()
        val failures=mutableListOf<String>();var measured=0;var unboundedWidths=0
        rule.runOnIdle {
            for(node in nodes) {
                val layouts=mutableListOf<TextLayoutResult>()
                node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
                for(layout in layouts) {
                    measured++
                    // Horizontal scroll containers use effectively unbounded paragraphs.
                    // RTL coordinates there lose float precision; record this limitation.
                    val boundedWidth=layout.multiParagraph.width<1_000_000f
                    if(!boundedWidth) unboundedWidths++
                    // Paragraph width can retain the maximum constraint after Text shrinks to
                    // intrinsic width. Compare actual line ink bounds instead of that box.
                    val clipped=(0 until layout.lineCount).any { line ->
                        layout.isLineEllipsized(line) ||
                            (boundedWidth && layout.getLineRight(line)-layout.getLineLeft(line)>layout.size.width+1f) ||
                            layout.getLineBottom(line)>layout.size.height+1f
                    }
                    if(clipped || layout.multiParagraph.didExceedMaxLines) failures+=layout.layoutInput.text.text
                }
            }
        }
        val config=rule.activity.resources.configuration
        rows.put(JSONObject().put("locale",locale).put("surface",surface).put("fontScale",config.fontScale)
            .put("widthDp",config.screenWidthDp).put("heightDp",config.screenHeightDp)
            .put("measuredTextLayouts",measured).put("unboundedWidthsNotAssessed",unboundedWidths).put("overflowTexts",JSONArray(failures)))
        File(folder,"results.json").writeText(JSONObject().put("variant",variant).put("surfaces",rows)
            .put("humanApproval",false).put("scope","Internal text-layout overflow and reachable actions; scrollable viewport clipping is separately inspected").toString(2))
        if(failures.isNotEmpty() || (locale in listOf("en-GB","de-DE","ar") && surface in listOf("garden","settings"))) {
            val image=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()!!
            File(folder,"$locale-$surface.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) };image.recycle()
        }
        assertTrue("No measured text layouts on $locale/$surface",measured>0)
        assertTrue("Overflow on $locale/$surface: $failures",failures.isEmpty())
    }
    @Test fun allLocalesKeepOnboardingCoreAndEditorTextWithinLayout() {
        for(locale in AppLocales.supported) {
            runBlocking { app.settings.set("onboarded",false) }
            AppLocales.choose(app,locale);rule.activityRule.scenario.recreate()
            rule.waitUntil(15000) { runCatching { rule.activity.resources.configuration.locales[0].toLanguageTag()==Locale.forLanguageTag(locale).toLanguageTag() }.getOrDefault(false) }
            waitText(R.string.welcome);inspect(locale,"onboarding-language")
            rule.onNodeWithText(rule.activity.getString(R.string.continue_label)).performScrollTo().performClick()
            waitText(R.string.experience);inspect(locale,"onboarding-experience")
            rule.onNodeWithText(rule.activity.getString(R.string.continue_label)).performScrollTo().performClick()
            waitText(R.string.onboarding_optional);inspect(locale,"onboarding-privacy")
            rule.onNodeWithText(rule.activity.getString(R.string.start_garden)).performScrollTo().performClick()
            waitText(R.string.garden)
            nav(R.string.garden)
            waitText(R.string.your_garden)
            for((name,nav,heading) in listOf(
                Triple("garden",R.string.garden,R.string.your_garden),Triple("calendar",R.string.calendar,R.string.care_calendar),
                Triple("journal",R.string.journal,R.string.growth_journal),Triple("voice",R.string.voice,R.string.voice_assistant),
                Triple("insights",R.string.insights,R.string.insights),Triple("settings",R.string.settings,R.string.settings))) {
                nav(nav);waitText(heading);inspect(locale,name)
            }
            nav(R.string.garden)
            rule.onNodeWithText(rule.activity.getString(R.string.add_plant)).performClick();waitText(R.string.plant_details)
            inspect(locale,"plant-editor")
            rule.onNode(hasText(rule.activity.getString(R.string.cancel)) and hasAnyAncestor(isDialog())).performClick()
            nav(R.string.journal)
            rule.onNodeWithText(rule.activity.getString(R.string.add_entry)).performClick()
            inspect(locale,"journal-editor")
            rule.onNode(hasText(rule.activity.getString(R.string.cancel)) and hasAnyAncestor(isDialog())).performClick()
        }
    }
}
