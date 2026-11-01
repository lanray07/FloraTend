package com.floratend.app

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File

/** Real OS picker rotation and URI grant; only the named disposable test photograph is selected. */
class PhotoPickerRotationTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val device get()=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val prefix="qa-picker-rotation"
    private var oldTag=""
    private var beforeFiles=emptySet<String>()
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        oldTag=AppLocales.selected(app);AppLocales.choose(app,"en-GB")
        runBlocking { app.settings.set("onboarded",true) }
        beforeFiles=File(app.filesDir,"photos").listFiles().orEmpty().map { it.path }.toSet()
        device.setOrientationLeft()
        rule.activityRule.scenario.recreate()
        val garden=rule.onNodeWithText("Garden",substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) garden.performScrollTo()
        garden.performClick()
    }
    @After fun cleanup() {
        if(device.currentPackageName==BuildConfig.APPLICATION_ID) {
            val image=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            if(image!=null) {
                val tag=InstrumentationRegistry.getArguments().getString("evidenceTag") ?: "latest"
                val folder=File(app.getExternalFilesDir(null),"photo-rotation").apply { mkdirs() }
                File(folder,"$tag-app.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) };image.recycle()
            }
        }
        runBlocking { app.database.dao().exportPlants().filter { it.id.startsWith(prefix) || it.name=="Photo rotation fern" }.forEach { app.database.dao().delete(it) } }
        File(app.filesDir,"photos").listFiles().orEmpty().filter { it.path !in beforeFiles }.forEach { it.delete() }
        AppLocales.choose(app,oldTag);device.unfreezeRotation()
    }
    private fun chooseAndRotate() {
        rule.onNodeWithText("Choose photo",substring=false).performScrollTo().performClick()
        if(!device.wait(Until.hasObject(By.pkg("com.android.documentsui")),1500)) {
            device.wait(Until.findObject(By.text("Docs")),5000)?.click()
            device.findObject(By.text("Just once"))?.click()
        }
        assertTrue("Real document picker",device.wait(Until.hasObject(By.pkg("com.android.documentsui")),10000))
        device.setOrientationNatural()
        val image=device.wait(Until.findObject(By.textContains("floratend-qa-picker")),10000)
        assertNotNull("Dedicated test image must be visible",image)
        image.click()
        assertTrue("Returned to the QA app",device.wait(Until.hasObject(By.pkg(BuildConfig.APPLICATION_ID)),10000))
    }
    private fun verify(path:String?) {
        val file=File(requireNotNull(path))
        assertTrue(file.canonicalPath.startsWith(File(app.filesDir,"photos").canonicalPath+File.separator))
        val image=BitmapFactory.decodeFile(file.path)
        assertEquals(1600,image.width);assertEquals(1200,image.height)
        assertTrue(android.graphics.Color.green(image.getPixel(0,0))>150);image.recycle()
        assertEquals(PackageManager.PERMISSION_DENIED,app.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE))
    }
    private fun report(mode:String) {
        val folder=File(app.getExternalFilesDir(null),"photo-rotation").apply { mkdirs() }
        File(folder,"$mode-result.json").writeText(JSONObject().put("mode",mode).put("actualOsPicker",true)
            .put("rotatedWhilePickerOpen",true).put("draftFieldsPreserved",true).put("privateDownsampledCopy",true)
            .put("broadStoragePermissionDenied",true).put("scope","Named controlled PNG through real DocumentsUI and URI grant; not camera or identification engine.").toString(2))
    }
    @Test fun plantDraftAndSelectedPhotoSurvivePickerRotation() {
        rule.onNodeWithText("Add plant",substring=false).performClick()
        rule.onNodeWithText("Plant nickname",substring=false).performTextInput("Photo rotation fern")
        rule.onNodeWithText("Location",substring=false).performTextInput("Window")
        rule.onNode(hasText("Herbs",substring=false) and hasAnyAncestor(isDialog())).performScrollTo().performClick()
        chooseAndRotate()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Photo rotation fern",substring=false).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Window",substring=false).assertExists()
        rule.waitUntil(10000) { rule.onAllNodesWithContentDescription(rule.activity.getString(R.string.plant_photo)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitUntil(10000) { runBlocking { app.database.dao().exportPlants().any { it.name=="Photo rotation fern" && it.photo!=null } } }
        val saved=runBlocking { app.database.dao().exportPlants().single { it.name=="Photo rotation fern" } }
        assertEquals("Window",saved.location);assertEquals("Herb",saved.category)
        verify(saved.photo)
        report("plant")
    }
    @Test fun journalDraftAndSelectedPhotoSurvivePickerRotation() {
        val plant=Plant(id=prefix+"-journal",name="Journal rotation fern")
        runBlocking { app.database.dao().save(plant) }
        val journal=rule.onNodeWithText("Journal",substring=false)
        if(rule.activity.resources.configuration.screenWidthDp>=720) journal.performScrollTo()
        journal.performClick()
        rule.onNodeWithText("Add journal entry",substring=false).performClick()
        rule.onNodeWithText("Notes and observations",substring=false).performTextInput("Retain this draft during selection.")
        rule.onNodeWithText("Height (cm, optional)",substring=false).performScrollTo().performTextInput("25.4")
        chooseAndRotate()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Retain this draft during selection.",substring=false).fetchSemanticsNodes().isNotEmpty() }
        rule.waitUntil(10000) { rule.onAllNodesWithContentDescription(rule.activity.getString(R.string.plant_photo)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitUntil(10000) { runBlocking { app.database.dao().journal().first().any { it.plantId==plant.id && it.photo!=null } } }
        val entry=runBlocking { app.database.dao().journal().first().single { it.plantId==plant.id } }
        assertEquals(25.4,entry.heightCm!!,0.00001);verify(entry.photo)
        report("journal")
    }
}
