package com.floratend.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Opt-in, actual Amazon+remote RVS flow. Never injects receipts or grants. */
class RealServerReceiptTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val app get() = rule.activity.application as FloraApp
    private fun report(name: String, value: JSONObject) {
        val folder = File(app.getExternalFilesDir(null), "server-receipt-test").apply { mkdirs() }
        File(folder, name).writeText(value.put("versionCode", BuildConfig.VERSION_CODE)
            .put("identifiersRecorded", false).toString(2))
    }

    @Test fun genuineOwnedReceiptPassesRemoteVerification(): Unit = runBlocking {
        assertFalse("Use the signed non-debuggable candidate", BuildConfig.DEBUG)
        assertEquals("PRODUCTION", LicensingService.getAppstoreSDKMode())
        assertTrue(ServerReceiptVerifier.configured())
        val history = LinkedBlockingQueue<PurchaseUpdatesResponse>()
        instrumentation.runOnMainSync {
            PurchasingService.registerListener(app, object : PurchasingListener {
                override fun onUserDataResponse(r: UserDataResponse) = app.billing.onUserDataResponse(r)
                override fun onProductDataResponse(r: ProductDataResponse) = app.billing.onProductDataResponse(r)
                override fun onPurchaseResponse(r: PurchaseResponse) = app.billing.onPurchaseResponse(r)
                override fun onPurchaseUpdatesResponse(r: PurchaseUpdatesResponse) {
                    app.billing.onPurchaseUpdatesResponse(r); history.offer(r)
                }
            })
            app.billing.restore()
        }
        val result = JSONObject().put("sdkMode", "PRODUCTION").put("receiptInjected", false)
            .put("newPurchaseAttempted", false).put("remoteEndpoint", BuildConfig.RECEIPT_VERIFIER_URL)
        try {
            var candidate: Receipt? = null
            var owner: String? = null
            do {
                val response = history.poll(40, TimeUnit.SECONDS) ?: error("No genuine purchase-history callback")
                assertEquals(PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL, response.requestStatus)
                owner = response.userData?.userId
                candidate = response.receipts.orEmpty().filter { it.sku == AmazonBilling.SKU &&
                    it.productType == ProductType.ENTITLED && !it.isCanceled && !it.isDeferred }
                    .maxByOrNull { it.purchaseDate?.time ?: 0 } ?: candidate
                if (!response.hasMore()) break
            } while (true)
            result.put("genuineOwnedReceiptReturned", candidate != null).put("nonblankOwner", !owner.isNullOrBlank())
            assertNotNull("Amazon history must return a genuine owned entitlement", candidate)
            assertFalse(owner.isNullOrBlank())
            val verification = ServerReceiptVerifier().verify(owner!!, candidate!!.receiptId)
            result.put("remoteVerification", verification.name)
            assertEquals(Verification.VERIFIED, verification)
            InstrumentationRegistry.getArguments().getString("productionVerifierUrl")?.let { productionUrl ->
                val production = ServerReceiptVerifier(productionUrl).verify(owner!!, candidate!!.receiptId)
                result.put("productionEndpointRejectsLatReceipt", production == Verification.INVALID)
                assertEquals(Verification.INVALID, production)
            }
            withTimeout(25000) { app.settings.premium.first { it } }
            val cache = app.getSharedPreferences("amazon_entitlement", 0)
            result.put("premiumActive", true).put("serverVerifiedCache", cache.getBoolean("serverVerified", false))
            assertTrue(cache.getBoolean("serverVerified", false))
            cache.edit().putInt("qa_before_restart_pid", android.os.Process.myPid()).commit()
        } finally {
            report("real-owned-rvs.json", result)
            instrumentation.runOnMainSync { PurchasingService.registerListener(app, app.billing) }
        }
    }

    @Test fun verifiedCacheSurvivesOfflineProcessRestart(): Unit = runBlocking {
        val connectivity = android.os.ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand("dumpsys connectivity")).bufferedReader().use { it.readText() }
        assertTrue("Disable all networks before this opt-in test", connectivity.contains("Active default network: none"))
        val cache = app.getSharedPreferences("amazon_entitlement", 0)
        val previous = cache.getInt("qa_before_restart_pid", -1)
        assertTrue(previous > 0)
        assertNotEquals(previous, android.os.Process.myPid())
        withTimeout(15000) { app.settings.premium.first { it } }
        assertTrue(cache.getBoolean("serverVerified", false))
        report("real-rvs-offline-restart.json", JSONObject().put("differentProcess", true)
            .put("noActiveNetwork", true).put("serverVerifiedCache", true).put("premiumActive", true))
        cache.edit().remove("qa_before_restart_pid").commit()
    }
}
