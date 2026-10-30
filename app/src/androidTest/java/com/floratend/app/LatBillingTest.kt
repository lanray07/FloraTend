package com.floratend.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Opt-in production callback probe. Captures statuses only; never fakes a grant. */
class LatBillingTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun actualProductionCatalogueAndClientPrice() {
        assumeFalse(BuildConfig.DEBUG)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = rule.activity.application as FloraApp
        val users = LinkedBlockingQueue<UserDataResponse>()
        val products = LinkedBlockingQueue<ProductDataResponse>()
        val updates = LinkedBlockingQueue<PurchaseUpdatesResponse>()
        val report = JSONObject().put("versionCode", BuildConfig.VERSION_CODE)
            .put("purchaseAttempted", false).put("identifiersRecorded", false)
        try {
            instrumentation.runOnMainSync {
                PurchasingService.registerListener(app, object : PurchasingListener {
                    override fun onUserDataResponse(r: UserDataResponse) {
                        app.billing.onUserDataResponse(r); users.offer(r)
                    }
                    override fun onProductDataResponse(r: ProductDataResponse) {
                        app.billing.onProductDataResponse(r); products.offer(r)
                    }
                    override fun onPurchaseUpdatesResponse(r: PurchaseUpdatesResponse) {
                        app.billing.onPurchaseUpdatesResponse(r); updates.offer(r)
                    }
                    override fun onPurchaseResponse(r: PurchaseResponse) { app.billing.onPurchaseResponse(r) }
                })
                app.billing.restore()
            }
            val user = users.poll(30, TimeUnit.SECONDS)
            report.put("userDataStatus", user?.requestStatus?.name ?: "NO_CALLBACK")
                .put("nonblankOwnerReturned", !user?.userData?.userId.isNullOrBlank())
            // A failed owner lookup short-circuits normal catalogue lookup. Query the same
            // public SKU directly to distinguish that from a client-rendering failure.
            var product = products.poll(2, TimeUnit.SECONDS)
            if (product == null) {
                report.put("explicitDiagnosticProductQuery", true)
                instrumentation.runOnMainSync { PurchasingService.getProductData(setOf(AmazonBilling.SKU)) }
                product = products.poll(30, TimeUnit.SECONDS)
            }
            val item = product?.productData?.get(AmazonBilling.SKU)
            report.put("productDataStatus", product?.requestStatus?.name ?: "NO_CALLBACK")
                .put("skuReturned", item != null)
                .put("skuMarkedUnavailable", product?.unavailableSkus?.contains(AmazonBilling.SKU) ?: false)
                .put("sdkPrice", item?.price ?: JSONObject.NULL)
                .put("skuType", item?.productType?.name ?: JSONObject.NULL)
            var history = updates.poll(2, TimeUnit.SECONDS)
            if (history == null) {
                report.put("explicitDiagnosticHistoryQuery", true)
                instrumentation.runOnMainSync { PurchasingService.getPurchaseUpdates(true) }
                history = updates.poll(30, TimeUnit.SECONDS)
            }
            report.put("historyStatus", history?.requestStatus?.name ?: "NO_CALLBACK")
            rule.waitForIdle()
            report.put("sdkMode", LicensingService.getAppstoreSDKMode())
                .put("clientAvailable", app.billing.available.value)
                .put("clientPrice", app.billing.price.value ?: JSONObject.NULL)
                .put("clientMessage", app.billing.message.value)
            assertEquals("Production SDK required", "PRODUCTION", LicensingService.getAppstoreSDKMode())
            assertEquals("Amazon must supply a usable account before buying", "SUCCESSFUL", user?.requestStatus?.name)
            assertEquals("Amazon must return the product", "SUCCESSFUL", product?.requestStatus?.name)
            assertNotNull("Test SKU is absent from Amazon's real response", item)
            assertFalse("A store-provided price is required", item?.price.isNullOrBlank())
            assertTrue("The client must render the real store availability", app.billing.available.value)
            assertEquals(item?.price, app.billing.price.value)
            report.put("catalogueAndPriceVerified", true)
        } catch (error: Throwable) {
            report.put("catalogueAndPriceVerified", false).put("failure", error.message)
            throw error
        } finally {
            instrumentation.runOnMainSync { PurchasingService.registerListener(app, app.billing) }
            val folder = File(app.getExternalFilesDir(null), "lat-billing").apply { mkdirs() }
            File(folder, "production-callbacks.json").writeText(report.toString(2))
        }
    }
}
