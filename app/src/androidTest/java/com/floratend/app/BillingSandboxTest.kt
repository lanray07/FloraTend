package com.floratend.app

import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context
import kotlinx.coroutines.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.model.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File

/** Talks to Amazon's installed App Tester; never injects a purchase callback or grant. */
class BillingSandboxTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
    private val app get()=instrumentation.targetContext.applicationContext as FloraApp
    private fun save(name:String,result:JSONObject){
        val dir=File(app.getExternalFilesDir(null),"billing-tests").apply{mkdirs()}
        File(dir,name).writeText(result.put("versionCode",BuildConfig.VERSION_CODE).put("sandboxOnly",true).toString(2))
    }
    private fun available(){
        instrumentation.runOnMainSync{app.billing.refresh()}
        runBlocking{withTimeout(35000){app.billing.available.first{it}}}
        assertEquals("Real Amazon App Tester must be selected","SANDBOX",LicensingService.getAppstoreSDKMode())
    }
    @Test fun catalogueReportsPriceAndSandboxMode(){
        val result=JSONObject()
        try{
            available()
            val price=app.billing.price.value
            assertFalse("A real App Tester product price is required",price.isNullOrBlank())
            result.put("mode",LicensingService.getAppstoreSDKMode()).put("catalogueAvailable",true).put("sdkPrice",price)
                .put("premiumAtProbe",runBlocking{app.settings.premium.first()})
            runBlocking{app.settings.set("onboarded",true)}
            rule.waitForIdle()
            rule.onNodeWithText("Settings",substring=false).performScrollTo().performClick()
            rule.waitUntil(10000){rule.onAllNodesWithText(price!!,substring=true).fetchSemanticsNodes().isNotEmpty()}
            result.put("storePriceRenderedInSettings",true)
        }catch(error:Throwable){result.put("error",error.toString()).put("sdkMessage",app.billing.message.value);throw error}
        finally{save("catalogue-probe.json",result)}
    }
    private fun clickNative(label:String) {
        val deadline=System.currentTimeMillis()+20000
        while(System.currentTimeMillis()<deadline) {
            fun find(node:AccessibilityNodeInfo?):AccessibilityNodeInfo? {
                if(node==null)return null
                if(node.text?.toString()?.trim()?.equals(label,true)==true || node.contentDescription?.toString()?.trim()?.equals(label,true)==true)return node
                for(i in 0 until node.childCount){find(node.getChild(i))?.let{return it}}
                return null
            }
            var found=find(instrumentation.uiAutomation.rootInActiveWindow)
            while(found!=null && !found.isClickable) found=found.parent
            if(found?.performAction(AccessibilityNodeInfo.ACTION_CLICK)==true)return
            Thread.sleep(150)
        }
        error("Expected real App Tester control not visible: $label")
    }
    @Test fun cancelThenPurchaseThroughRealTesterDialogs(){
        val result=JSONObject()
        try {
            available();Thread.sleep(800)
            assertFalse("Use a fresh FloraTend sandbox entitlement",runBlocking{app.settings.premium.first()})
            runBlocking {
                instrumentation.runOnMainSync{app.billing.purchase()}
                val canceled=async(start=CoroutineStart.UNDISPATCHED){withTimeout(30000){app.billing.message.first{it==app.getString(R.string.billing_incomplete)}}}
                clickNative("Cancel")
                canceled.await()
            }
            assertFalse(runBlocking{app.settings.premium.first()});result.put("cancelDidNotGrantPremium",true)
            available();Thread.sleep(800)
            instrumentation.runOnMainSync{app.billing.purchase()}
            clickNative("Get Item")
            // App Tester delivers the purchase result after this interstitial closes.
            clickNative("Close")
            runBlocking{withTimeout(30000){app.settings.premium.first{it}}}
            val cache=app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE)
            assertEquals("SANDBOX",cache.getString("mode",""));assertTrue(cache.getBoolean("active",false))
            assertFalse(cache.getString("receipt",null).isNullOrBlank())
            result.put("realTesterPurchaseGranted",true).put("receiptPersisted",true).put("mode",LicensingService.getAppstoreSDKMode())
        } catch(error:Throwable){result.put("error",error.toString()).put("sdkMessage",app.billing.message.value);throw error}
        finally{save("purchase-and-cancel.json",result)}
    }
    @Test fun restoreActualSandboxReceiptAfterLocalGrantIsRemoved(){
        val result=JSONObject()
        try {
            available();Thread.sleep(800)
            runBlocking{app.settings.set("premium",false)}
            instrumentation.runOnMainSync{app.billing.restore()}
            runBlocking{withTimeout(30000){app.settings.premium.first{it}}}
            app.getSharedPreferences("qa_billing_control",Context.MODE_PRIVATE).edit().putString("original_owner",app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).getString("owner",null)).apply()
            result.put("restoredFromRealTesterHistory",true).put("mode",LicensingService.getAppstoreSDKMode())
        }catch(error:Throwable){result.put("error",error.toString());throw error}
        finally{save("restore-receipt.json",result)}
    }

    private val observedHistory=LinkedBlockingQueue<PurchaseUpdatesResponse>()
    private val observedPurchases=LinkedBlockingQueue<PurchaseResponse>()
    private fun traceRealSdkCallbacks(){
        instrumentation.runOnMainSync {
            PurchasingService.registerListener(app,object:PurchasingListener {
                override fun onUserDataResponse(r:UserDataResponse){app.billing.onUserDataResponse(r)}
                override fun onProductDataResponse(r:ProductDataResponse){app.billing.onProductDataResponse(r)}
                override fun onPurchaseResponse(r:PurchaseResponse){app.billing.onPurchaseResponse(r);observedPurchases.offer(r)}
                override fun onPurchaseUpdatesResponse(r:PurchaseUpdatesResponse){app.billing.onPurchaseUpdatesResponse(r);observedHistory.offer(r)}
            })
        }
    }
    @Test fun actualCanceledReceiptRevokesPremium(){
        val result=JSONObject()
        try {
            available();Thread.sleep(800);traceRealSdkCallbacks()
            instrumentation.runOnMainSync{app.billing.restore()}
            val response=observedHistory.poll(30,TimeUnit.SECONDS) ?: error("No real App Tester history response")
            assertEquals(PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL,response.requestStatus)
            assertTrue("The SDK must return the canceled FloraTend receipt",response.receipts.any{it.sku==AmazonBilling.SKU && it.isCanceled})
            runBlocking{withTimeout(30000){app.settings.premium.first{!it}}}
            assertFalse(app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).getBoolean("active",true))
            result.put("realCanceledReceiptObserved",true).put("premiumRevoked",true)
        }catch(error:Throwable){result.put("error",error.toString());throw error}
        finally{save("canceled-receipt.json",result)}
    }
    @Test fun actualFailedPurchaseDoesNotGrantPremium() {
        val result=JSONObject()
        try {
            available();Thread.sleep(800)
            assertFalse(runBlocking { app.settings.premium.first() })
            traceRealSdkCallbacks()
            runBlocking {
                val failure=async(start=CoroutineStart.UNDISPATCHED) { withTimeout(10000) {
                    app.billing.message.first { it==app.getString(R.string.billing_incomplete) }
                } }
                instrumentation.runOnMainSync { app.billing.purchase() }
                val response=observedPurchases.poll(30,TimeUnit.SECONDS) ?: error("No real failed purchase response")
                assertEquals(PurchaseResponse.RequestStatus.FAILED,response.requestStatus)
                result.put("realSdkFailedResponse",true).put("responseHadUserData",response.userData!=null)
                    .put("responseUserIdIsBlank",response.userData?.userId.isNullOrBlank())
                    .put("responseUserMatchesCurrentOwner",response.userData?.userId==app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).getString("owner",null))
                failure.await()
            }
            assertFalse(runBlocking { app.settings.premium.first() })
            result.put("premiumNotGranted",true)
            // Model Activity.onResume: a successful catalogue refresh must not hide the failure.
            available();instrumentation.waitForIdleSync()
            assertEquals(app.getString(R.string.billing_incomplete),app.billing.message.value)
            result.put("errorRemainsVisibleAfterRefresh",true)
        } catch(error:Throwable) { result.put("error",error.toString());throw error }
        finally { save("failed-purchase.json",result.put("lastMessage",app.billing.message.value)) }
    }
    @Test fun differentSandboxAccountCannotUsePreviousGrant(){
        val result=JSONObject()
        try {
            available();Thread.sleep(800)
            val control=app.getSharedPreferences("qa_billing_control",Context.MODE_PRIVATE)
            val original=control.getString("original_owner",null)
            assertFalse("A genuine previous account must be recorded",original.isNullOrBlank())
            val owner=app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).getString("owner",null)
            assertFalse(owner.isNullOrBlank());assertNotEquals(original,owner)
            assertFalse(runBlocking{app.settings.premium.first()})
            result.put("sdkAccountChanged",true).put("previousPremiumNotShared",true)
        }catch(error:Throwable){result.put("error",error.toString());throw error}
        finally{save("account-switch.json",result)}
    }
    @Test fun failedSdkRefreshRetainsPreviouslyVerifiedCachedEntitlement(){
        val result=JSONObject()
        try {
            instrumentation.runOnMainSync{app.billing.refresh()}
            runBlocking{withTimeout(30000){app.billing.message.first{it==app.getString(R.string.billing_unavailable)}}}
            assertEquals("SANDBOX",LicensingService.getAppstoreSDKMode())
            val cache=app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE)
            assertTrue(cache.getBoolean("active",false));assertFalse(cache.getString("receipt",null).isNullOrBlank())
            assertTrue(runBlocking{app.settings.premium.first()})
            assertFalse(app.billing.available.value)
            result.put("failedSdkRefresh",true).put("verifiedCachedEntitlementRetained",true).put("purchaseDisabledDuringFailure",true)
        }catch(error:Throwable){result.put("error",error.toString());throw error}
        finally{save("failed-refresh-cache.json",result)}
    }

    @Test fun prepareRealReceiptForOfflineRelaunch() {
        available()
        runBlocking { withTimeout(10000) { app.settings.premium.first { it } } }
        assertTrue(app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).getBoolean("active",false))
        app.getSharedPreferences("qa_billing_control",Context.MODE_PRIVATE).edit()
            .putInt("before_restart_pid",android.os.Process.myPid()).commit()
        save("offline-restart-prepare.json",JSONObject().put("genuineSandboxGrantConfirmed",true))
    }

    @Test fun realCachedEntitlementSurvivesOfflineProcessRestart() {
        val connectivity=android.os.ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand("dumpsys connectivity")).bufferedReader().use { it.readText() }
        assertTrue("Disable every active network before running this case",connectivity.contains("Active default network: none"))
        val previous=app.getSharedPreferences("qa_billing_control",Context.MODE_PRIVATE).getInt("before_restart_pid",-1)
        assertTrue(previous>0);assertNotEquals(previous,android.os.Process.myPid())
        runBlocking { withTimeout(10000) { app.settings.premium.first { it } } }
        assertEquals("SANDBOX",app.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE).getString("mode",null))
        save("offline-restart.json",JSONObject().put("differentProcess",true).put("noActiveNetwork",true)
            .put("genuineCachedEntitlementRetained",true))
    }

    private fun gateViewModel(): PlantViewModel {
        lateinit var model: PlantViewModel
        instrumentation.runOnMainSync { model=PlantViewModel(app) }
        return model
    }
    private fun gatePlant(number:Int)=Plant(id="qa-premium-gate-$number",name="FloraTend QA gate $number")
    private fun settingsScreen() {
        runBlocking { app.settings.set("onboarded",true) }
        rule.waitForIdle()
        rule.onNodeWithText("Settings",substring=false).performScrollTo().performClick()
    }
    @Test fun realFreeAccountCannotAddSixthPlantOrExport() {
        available()
        assertFalse(runBlocking { app.settings.premium.first() })
        val model=gateViewModel()
        runBlocking {
            check(app.database.dao().plants().first().all { it.id.startsWith("qa-premium-gate-") }) { "Dedicated empty test collection required" }
            for(number in 1..5) app.database.dao().save(gatePlant(number))
            model.save(gatePlant(6)) { error("Free account must not add the sixth plant") }.join()
            assertNull(app.database.dao().plant(gatePlant(6).id))
            assertEquals(app.getString(R.string.free_limit),model.error.value)
        }
        settingsScreen()
        rule.onNodeWithText(app.getString(R.string.export_json)).assertIsNotEnabled()
        rule.onNodeWithText(app.getString(R.string.export_csv)).assertIsNotEnabled()
        save("free-gates.json",JSONObject().put("sixthPlantRejected",true).put("exportButtonsDisabled",true))
    }
    @Test fun realPremiumAccountUnlocksSixthPlantAndExports() {
        try {
            available()
            runBlocking { withTimeout(10000) { app.settings.premium.first { it } } }
            val model=gateViewModel()
            runBlocking {
                withTimeout(10000) { model.premium.first { it } }
                check(app.database.dao().plants().first().all { it.id.startsWith("qa-premium-gate-") })
                for(number in 1..5) app.database.dao().save(gatePlant(number))
                model.save(gatePlant(6)) { }.join()
                assertNotNull(app.database.dao().plant(gatePlant(6).id))
                val plants=app.database.dao().plants().first()
                val json=Export.json(plants,app.database.dao().schedules().first(),app.database.dao().journal().first(),app.database.dao().history().first())
                val csv=Export.csv(plants)
                assertTrue(json.contains(gatePlant(6).name));assertTrue(csv.contains(gatePlant(6).name))
            }
            settingsScreen()
            rule.onNodeWithText(app.getString(R.string.export_json)).assertIsEnabled()
            rule.onNodeWithText(app.getString(R.string.export_csv)).assertIsEnabled()
            save("premium-gates.json",JSONObject().put("realSdkPremium",true).put("sixthPlantSaved",true)
                .put("exportButtonsEnabled",true).put("exportPayloadContainsSavedRecord",true)
                .put("systemDocumentDestinationNotExercised",true))
        } finally {
            runBlocking { app.database.dao().plants().first().filter { it.id.startsWith("qa-premium-gate-") }.forEach { app.database.dao().delete(it) } }
        }
    }

}
