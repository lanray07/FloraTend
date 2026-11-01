package com.floratend.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.json.JSONObject
import java.io.File

/** Opens the official camera only after its button; cancels without taking or recording a photo. */
class CameraDelegationTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun externalCameraLaunchIsContextualAndCancellationPreservesDraft() {
        val app=rule.activity.application as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val old=AppLocales.selected(app)
        val before=File(app.filesDir,"photos").listFiles().orEmpty().map { it.path }.toSet()
        try {
            AppLocales.choose(app,"en-GB");runBlocking { app.settings.set("onboarded",true) }
            rule.activityRule.scenario.recreate()
            val garden=rule.onNodeWithText("Garden",substring=false)
            if(rule.activity.resources.configuration.screenWidthDp>=720) garden.performScrollTo()
            garden.performClick()
            assertEquals(PackageManager.PERMISSION_DENIED,app.checkSelfPermission(Manifest.permission.CAMERA))
            rule.onNodeWithText("Add plant",substring=false).performClick()
            rule.onNodeWithText("Plant nickname",substring=false).performTextInput("Camera cancellation draft")
            assertEquals(BuildConfig.APPLICATION_ID,device.currentPackageName)
            rule.onNodeWithText("Take photo",substring=false).performScrollTo().performClick()
            assertTrue("Official camera handler",device.wait(Until.hasObject(By.pkg("com.android.camera2")),10000))
            assertEquals("com.android.camera2",device.currentPackageName)
            assertEquals(PackageManager.PERMISSION_DENIED,app.checkSelfPermission(Manifest.permission.CAMERA))
            device.pressBack()
            assertTrue(device.wait(Until.hasObject(By.pkg(BuildConfig.APPLICATION_ID)),10000))
            rule.onNodeWithText("Camera cancellation draft",substring=false).assertExists()
            rule.onNodeWithText("Cancel",substring=false).performClick()
            assertEquals(before,File(app.filesDir,"photos").listFiles().orEmpty().map { it.path }.toSet())
            val folder=File(app.getExternalFilesDir(null),"camera-delegation").apply { mkdirs() }
            File(folder,"results.json").writeText(JSONObject().put("externalCameraPackage","com.android.camera2")
                .put("launchedOnlyAfterTakePhoto",true).put("appCameraPermissionDeniedBeforeAndAfter",true)
                .put("cancelPreservedDraft",true).put("photoCaptured",false).put("privatePhotoFilesUnchanged",true)
                .put("scope","Physical Fire HD10/API28 official camera launch/cancel; no photograph, camera preview screenshot, upload or hardware image quality claim.").toString(2))
        } finally {
            if(device.currentPackageName=="com.android.camera2") device.pressBack()
            AppLocales.choose(app,old)
        }
    }
}
