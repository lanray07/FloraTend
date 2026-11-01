package com.floratend.app

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.net.ConnectivityManager
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/** Explicit opt-in, actual provider/transport/UI. No synthetic identification response. */
class LiveIdentificationTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val id="qa-identification-live"
    private val name="QA live daisy"
    private val corrected="Bellis perennis (checked manually)"
    private val folder get()=File(app.getExternalFilesDir(null),"identification-live").apply {mkdirs()}
    @Before fun requireOptIn() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("floratend.liveIdentification")=="true")
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        check(BuildConfig.PLANT_IDENTIFIER_URL=="https://floratend-plant-identification.lanraybanks.workers.dev/identify")
    }
    @After fun releaseFixture() {
        if(BuildConfig.APPLICATION_ID.endsWith(".qa"))app.javaClass.getMethod("setIdentificationFixture",PlantIdentifier::class.java).invoke(app,null)
    }
    private fun waitText(text:String) {rule.waitUntil(15000) {rule.onAllNodesWithText(text,substring=false).fetchSemanticsNodes().isNotEmpty()}}
    private fun click(resource:Int)=rule.onNodeWithText(rule.activity.getString(resource)).performClick()
    private fun shot(filename:String) {assertTrue(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(File(folder,filename)))}
    private fun openEditor() {waitText(name);rule.onNodeWithText(name).performClick();click(R.string.edit);rule.waitForIdle()}
    @Test fun genuineConsentedPhotoMatchesAndChoiceIsSavedOnlyAfterReview() {
        val source=File(app.getExternalFilesDir(null),"identification-fixtures/daisy.jpg")
        check(source.isFile)
        val photo=Photos.import(app,Uri.fromFile(source))
        val calls=AtomicInteger();val returned=AtomicReference<IdentificationResult?>()
        val actual=app.plantIdentifier;check(actual.available)
        val counting=object:PlantIdentifier {
            override val available get()=actual.available
            override suspend fun identify(photo:String,language:String):IdentificationResult {
                calls.incrementAndGet();return actual.identify(photo,language).also {returned.set(it)}
            }
        }
        app.javaClass.getMethod("setIdentificationFixture",PlantIdentifier::class.java).invoke(app,counting)
        runBlocking {
            check(app.database.dao().exportPlants().none {it.id==id})
            app.settings.set("onboarded",true)
            app.database.dao().save(Plant(id=id,name=name,species="Manual species before request",notes="QA note survives identification",photo=photo))
        }
        AppLocales.choose(app,"en-GB");rule.activityRule.scenario.recreate();openEditor()
        rule.onNodeWithText(rule.activity.getString(R.string.identify_photo)).performScrollTo();click(R.string.identify_photo)
        rule.onNodeWithText(rule.activity.getString(R.string.identification_consent)).assertExists();shot("01-consent.png")
        rule.onAllNodesWithText(rule.activity.getString(R.string.cancel)).onLast().performClick()
        rule.runOnIdle {assertEquals(0,calls.get())}
        rule.onNodeWithText(rule.activity.getString(R.string.identify_photo)).performScrollTo();click(R.string.identify_photo);click(R.string.send_photo)
        rule.waitUntil(5000) {calls.get()==1}
        val beforeOrientation=rule.activity.resources.configuration.orientation
        rule.runOnIdle {rule.activity.requestedOrientation=if(beforeOrientation==Configuration.ORIENTATION_LANDSCAPE)ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}
        rule.waitUntil(45000) {returned.get()!=null}
        val result=returned.get()
        val report=JSONObject().put("actualTransport",true).put("endpointFromBuildConfig",true)
            .put("photoSource","https://libreshot.com/english-daisy/").put("photoAuthor","Martin Vorel").put("photoLicense","CC0")
            .put("sourceLabel","Bellis perennis").put("requestCount",calls.get()).put("resultType",result!!::class.java.simpleName)
            .put("savedAutomatically",runBlocking {app.database.dao().exportPlants().single {it.id==id}.species!="Manual species before request"})
        if(result is IdentificationResult.Suggestions)report.put("suggestions",JSONArray().apply {result.items.forEach {put(JSONObject().put("scientificName",it.scientificName).put("score",it.score))}})
        File(folder,"genuine-result.json").writeText(report.toString(2))
        assertTrue("Actual service did not return suggestions; sanitized result retained",result is IdentificationResult.Suggestions)
        waitText("Bellis perennis");rule.onNodeWithText("Bellis perennis").performScrollTo().assertExists();shot("02-real-suggestions.png")
        assertEquals("Manual species before request",runBlocking {app.database.dao().exportPlants().single {it.id==id}.species})
        rule.onNodeWithText("Bellis perennis").performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.species)).performScrollTo().performTextReplacement(corrected)
        shot("03-editable-species-field.png");click(R.string.save)
        rule.waitUntil(10000) {runBlocking {app.database.dao().exportPlants().single {it.id==id}.species==corrected}}
        val saved=runBlocking {app.database.dao().exportPlants().single {it.id==id}}
        assertEquals("QA note survives identification",saved.notes);assertEquals(photo,saved.photo);assertEquals(1,calls.get())
        report.put("savedAutomatically",false).put("savedAfterExplicitReview",true).put("manualCorrectionPersisted",true)
            .put("notePreserved",true).put("photoPreserved",true).put("requestCountAfterRotation",calls.get())
            .put("orientationChanged",rule.activity.resources.configuration.orientation!=beforeOrientation)
            .put("normalizedPhotoSha256",MessageDigest.getInstance("SHA-256").digest(File(photo).readBytes()).joinToString("") {"%02x".format(it)})
        File(folder,"genuine-result.json").writeText(report.toString(2))
    }
    /** Run in a separate instrumentation invocation after an actual QA process stop. */
    @Test fun savedChoiceSurvivesSeparateProcessRestart() {
        check((app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork==null)
        val saved=runBlocking {app.database.dao().exportPlants().single {it.id==id}}
        assertEquals(corrected,saved.species);assertEquals("QA note survives identification",saved.notes)
        assertTrue(File(requireNotNull(saved.photo)).isFile)
        waitText(name);rule.onNodeWithText(name).performClick();waitText(corrected);shot("04-restarted-record.png")
        File(folder,"restart-result.json").writeText(JSONObject().put("speciesPersisted",true).put("notePersisted",true).put("photoExists",true).put("noDefaultNetwork",true).toString(2))
    }
    @Test fun realOfflineFailureKeepsEditableDraftAndAllowsManualSave() {
        check((app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork==null)
        val offlineId="qa-identification-offline"
        val offlineName="QA offline identification"
        val source=File(app.getExternalFilesDir(null),"identification-fixtures/daisy.jpg")
        val photo=Photos.import(app,Uri.fromFile(source))
        runBlocking {
            check(app.database.dao().exportPlants().none {it.id==offlineId})
            app.database.dao().save(Plant(id=offlineId,name=offlineName,species="Original offline species",notes="Original note",photo=photo))
        }
        waitText(offlineName);rule.onNodeWithText(offlineName).performClick();click(R.string.edit)
        rule.onNodeWithText(rule.activity.getString(R.string.species)).performScrollTo().performTextReplacement("Manual offline species")
        rule.onNodeWithText(rule.activity.getString(R.string.notes)).performScrollTo().performTextReplacement("QA offline edited note")
        rule.onNodeWithText(rule.activity.getString(R.string.identify_photo)).performScrollTo();click(R.string.identify_photo);click(R.string.send_photo)
        rule.waitUntil(30000) {rule.onAllNodesWithText(rule.activity.getString(R.string.identification_error)).fetchSemanticsNodes().isNotEmpty()}
        rule.onNodeWithText(rule.activity.getString(R.string.identification_error)).performScrollTo();shot("05-real-offline-error.png")
        assertEquals("Original offline species",runBlocking {app.database.dao().exportPlants().single {it.id==offlineId}.species})
        click(R.string.save)
        rule.waitUntil(10000) {runBlocking {app.database.dao().exportPlants().single {it.id==offlineId}.species=="Manual offline species"}}
        val saved=runBlocking {app.database.dao().exportPlants().single {it.id==offlineId}}
        assertEquals("QA offline edited note",saved.notes);assertEquals(photo,saved.photo)
        File(folder,"offline-error-result.json").writeText(JSONObject().put("noDefaultNetwork",true).put("actualTransportFailure",true)
            .put("manualSpeciesSaved",true).put("editedNoteSaved",true).put("photoPreserved",true).put("databaseUnchangedBeforeExplicitSave",true).toString(2))
    }
    @Test fun offlineManualSaveSurvivesAnotherProcessRestart() {
        check((app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork==null)
        val saved=runBlocking {app.database.dao().exportPlants().single {it.id=="qa-identification-offline"}}
        assertEquals("Manual offline species",saved.species);assertEquals("QA offline edited note",saved.notes);assertTrue(File(requireNotNull(saved.photo)).isFile)
        waitText("QA offline identification");rule.onNodeWithText("QA offline identification").performClick();waitText("Manual offline species");shot("06-offline-manual-restarted.png")
        File(folder,"offline-manual-restart.json").writeText(JSONObject().put("noDefaultNetwork",true).put("manualSpeciesPersisted",true).put("editedNotePersisted",true).put("photoExists",true).toString(2))
    }
}
