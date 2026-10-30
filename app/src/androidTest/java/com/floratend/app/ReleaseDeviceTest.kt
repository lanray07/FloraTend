package com.floratend.app

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.amazon.device.drm.LicensingService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Explicit opt-in; observes the real signed client without injecting SDK callbacks. */
class ReleaseDeviceTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun signedCandidateAndRealStoreAvailability() {
        assumeFalse(BuildConfig.DEBUG)
        val app = rule.activity.application as FloraApp
        assertEquals(0, app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
        assertFalse(BillingPolicy.allowedMode("SANDBOX", BuildConfig.DEBUG))
        runBlocking { app.settings.set("onboarded", true) }
        val settings = rule.onNodeWithText("Settings", substring = false)
        if (app.resources.configuration.screenWidthDp >= 720) settings.performScrollTo()
        settings.performClick()
        rule.onNodeWithText("Restore purchases", substring = false).performScrollTo().performClick()
        // Observe actual SDK availability after the async ownership/catalogue round trip.
        Thread.sleep(15000)
        rule.waitForIdle()
        val premium = runBlocking { app.settings.premium.first() }
        val available = app.billing.available.value
        val price = app.billing.price.value
        if (available) assertFalse(price.isNullOrBlank()) else {
            assertNull(price)
            rule.onNodeWithText(app.getString(R.string.store_unavailable)).assertIsNotEnabled()
        }
        val folder = File(app.getExternalFilesDir(null), "release-device").apply { mkdirs() }
        File(folder, "store-probe.json").writeText(JSONObject()
            .put("versionCode", BuildConfig.VERSION_CODE)
            .put("debuggable", false)
            .put("installation", "direct signed APK sideload")
            .put("sdkMode", LicensingService.getAppstoreSDKMode())
            .put("catalogueAvailable", available)
            .put("price", price ?: JSONObject.NULL)
            .put("premium", premium)
            .put("message", app.billing.message.value)
            .put("purchaseAttempted", false)
            .put("productionPurchaseVerified", false).toString(2))
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Release screenshot unavailable")
        File(folder, "signed-release-settings.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
