package com.floratend.app

import android.content.Context
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Native Amazon callbacks are authenticated by the SDK/public key. This is not server RVS. */
class AmazonBilling(context: Context, private val settings: FloraSettings) : PurchasingListener {
    companion object { const val SKU = "floratend.premium" }
    private val app = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val cache = app.getSharedPreferences("amazon_entitlement", Context.MODE_PRIVATE)
    private val mutablePrice = MutableStateFlow<String?>(null)
    private val mutableMessage = MutableStateFlow(AppLocales.wrap(app).getString(R.string.billing_unconfigured))
    private val mutableAvailable = MutableStateFlow(false)
    val price: StateFlow<String?> = mutablePrice
    val message: StateFlow<String> = mutableMessage
    val available: StateFlow<Boolean> = mutableAvailable
    private var registered = false
    private var user: String? = null
    private var userRequest: RequestId? = null
    private var productRequest: RequestId? = null
    private val purchases = mutableMapOf<RequestId, String>()
    private var updatesRequest: RequestId? = null
    private var updatesUser: String? = null
    private var fullHistory = false
    private var updateCandidate: Receipt? = null
    private var restoreRequested = false

    fun register() {
        if (registered) return
        // Sandbox entitlements must never survive installation of a production build.
        if (!BuildConfig.DEBUG && cache.getString("mode", "") != "PRODUCTION") scope.launch {
            settings.set("premium", false)
        }
        if (!runCatching { app.assets.open("AppstoreAuthenticationKey.pem").use { it.read() >= 0 } }.getOrDefault(false)) return
        runCatching {
            PurchasingService.registerListener(app, this)
            registered = true
            LicensingService.verifyLicense(app) {
                scope.launch {
                    if (!allowedMode()) {
                        mutableAvailable.value = false
                        mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_environment)
                    } else refresh()
                }
            }
        }.onFailure { unavailable() }
    }

    private fun allowedMode(): Boolean {
        val mode = LicensingService.getAppstoreSDKMode()
        return mode == "PRODUCTION" || (BuildConfig.DEBUG && mode == "SANDBOX")
    }

    /** Called from Activity.onResume; retains a verified cached entitlement while offline. */
    fun refresh() {
        if (!registered) { mutableMessage.value=AppLocales.wrap(app).getString(R.string.billing_unconfigured);return };if (userRequest != null) return
        mutableAvailable.value = false
        mutablePrice.value = null
        runCatching { userRequest = PurchasingService.getUserData() }.onFailure { unavailable() }
    }

    fun purchase() {
        val owner = user
        if (!registered || !mutableAvailable.value || owner == null || !allowedMode()) {
            unavailable(); return
        }
        if (purchases.isNotEmpty() || updatesRequest != null) {
            mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_history_loading)
            return
        }
        mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_waiting)
        runCatching { purchases[PurchasingService.purchase(SKU)] = owner }.onFailure { unavailable() }
    }

    fun restore() {
        if (!registered) { unavailable(); return }
        restoreRequested = true
        refresh()
    }

    override fun onUserDataResponse(response: UserDataResponse) { scope.launch {
        if (response.requestId != userRequest) return@launch
        userRequest = null
        if (response.requestStatus != UserDataResponse.RequestStatus.SUCCESSFUL || !allowedMode()) {
            unavailable(); return@launch
        }
        val owner = response.userData?.userId?.takeIf { it.isNotBlank() } ?: run { unavailable(); return@launch }
        val switched = cache.getString("owner", null) != owner
        if (switched) {
            settings.set("premium", false)
            cache.edit().putString("owner", owner).putBoolean("active", false).apply()
            purchases.clear()
            updatesRequest = null
        }
        user = owner
        runCatching {
            productRequest = PurchasingService.getProductData(setOf(SKU))
            if (updatesRequest == null) startUpdates(restoreRequested || switched)
        }.onFailure { unavailable() }
    } }

    private fun startUpdates(reset: Boolean) {
        fullHistory = reset
        updateCandidate = null
        updatesUser = user
        updatesRequest = PurchasingService.getPurchaseUpdates(reset)
        restoreRequested = false
    }

    override fun onProductDataResponse(response: ProductDataResponse) { scope.launch {
        if (response.requestId != productRequest) return@launch
        productRequest = null
        val product = response.productData?.get(SKU)
        if (response.requestStatus == ProductDataResponse.RequestStatus.SUCCESSFUL &&
            product?.productType == ProductType.ENTITLED && product.price.isNotBlank() && allowedMode()) {
            mutablePrice.value = product.price
            mutableAvailable.value = true
            mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_price_available)
        } else unavailable()
    } }

    override fun onPurchaseResponse(response: PurchaseResponse) { scope.launch {
        val expectedUser = purchases.remove(response.requestId) ?: return@launch
        if (expectedUser != user || response.userData?.userId != expectedUser || !allowedMode()) return@launch
        when (response.requestStatus) {
            PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                val receipt = response.receipt
                if (!matches(receipt) || receipt.isDeferred) {
                    mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_pending)
                    return@launch
                }
                persist(!receipt.isCanceled, receipt)
                mutableMessage.value = if (receipt.isCanceled) AppLocales.wrap(app).getString(R.string.billing_canceled) else AppLocales.wrap(app).getString(R.string.billing_confirmed)
            }
            PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> {
                mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_restoring)
                restore()
            }
            else -> mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_incomplete)
        }
    } }

    override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) { scope.launch {
        if (response.requestId != updatesRequest) return@launch
        updatesRequest = null
        if (response.requestStatus != PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL ||
            response.userData?.userId != updatesUser || updatesUser != user || !allowedMode()) {
            mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_restore_failed)
            return@launch
        }
        for (receipt in response.receipts.orEmpty()) {
            if (!matches(receipt) || receipt.isDeferred) continue
            val previous = updateCandidate
            val time = receipt.purchaseDate?.time ?: 0L
            val previousTime = previous?.purchaseDate?.time ?: -1L
            if (previous == null || time > previousTime ||
                (time == previousTime && receipt.isCanceled)) updateCandidate = receipt
        }
        if (response.hasMore()) {
            runCatching { updatesRequest = PurchasingService.getPurchaseUpdates(false) }.onFailure { unavailable() }
        } else {
            val receipt = updateCandidate
            if (receipt != null && (fullHistory ||
                (receipt.purchaseDate?.time ?: 0L) >= cache.getLong("purchaseEpoch", 0L))) {
                persist(!receipt.isCanceled, receipt)
            } else if (fullHistory && receipt == null) persist(false, null)
            if (restoreRequested) startUpdates(true)
            mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_history_checked)
        }
    } }

    private fun matches(receipt: Receipt?): Boolean = receipt != null && receipt.sku == SKU &&
        receipt.productType == ProductType.ENTITLED && receipt.receiptId.isNotBlank()

    private suspend fun persist(active: Boolean, receipt: Receipt?) {
        // Persist before acknowledgment so a crash can safely replay the idempotent grant.
        cache.edit().putString("owner", user).putString("mode", LicensingService.getAppstoreSDKMode())
            .putBoolean("active", active).putString("receipt", receipt?.receiptId)
            .putLong("purchaseEpoch", receipt?.purchaseDate?.time ?: 0L).apply()
        settings.set("premium", active)
        if (active && receipt != null) runCatching {
            PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
        }
    }

    private fun unavailable() {
        mutableAvailable.value = false
        mutablePrice.value = null
        mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_unavailable)
    }
}

