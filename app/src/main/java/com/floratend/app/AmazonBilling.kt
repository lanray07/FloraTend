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
    companion object { const val SKU = BillingPolicy.PREMIUM_SKU }
    private val app = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val cache = app.getSharedPreferences("amazon_entitlement", Context.MODE_PRIVATE)
    private val entitlements = EntitlementStore(cache, BuildConfig.DEBUG) { settings.set("premium", it) }
    private val mutablePrice = MutableStateFlow<String?>(null)
    private val mutableMessage = MutableStateFlow(AppLocales.wrap(app).getString(R.string.billing_unconfigured))
    private val mutableAvailable = MutableStateFlow(false)
    val price: StateFlow<String?> = mutablePrice
    val message: StateFlow<String> = mutableMessage
    val available: StateFlow<Boolean> = mutableAvailable
    private var registered = false
    private var registrationStarted = false
    private var user: String? = null
    private var userRequest: RequestId? = null
    private var productRequest: RequestId? = null
    private val purchases = mutableMapOf<RequestId, String>()
    private var updatesRequest: RequestId? = null
    private var updatesUser: String? = null
    private var fullHistory = false
    private var history = EntitlementHistory()
    private var restoreRequested = false
    private var keepPurchaseMessage = false

    fun register() {
        if (registrationStarted) return
        registrationStarted = true
        scope.launch {
            try {
                entitlements.recover()
                if (!runCatching { app.assets.open("AppstoreAuthenticationKey.pem").use { it.read() >= 0 } }.getOrDefault(false)) return@launch
                PurchasingService.registerListener(app, this@AmazonBilling)
                registered = true
                LicensingService.verifyLicense(app) {
                    scope.launch {
                        if (!allowedMode()) {
                            mutableAvailable.value = false
                            mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_environment)
                        } else refresh()
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                unavailable()
            }
        }
    }

    private fun allowedMode(): Boolean {
        val mode = LicensingService.getAppstoreSDKMode()
        return BillingPolicy.allowedMode(mode, BuildConfig.DEBUG)
    }

    /** Called from Activity.onResume; retains a verified cached entitlement while offline. */
    fun refresh() {
        if (!registered) { mutableMessage.value=AppLocales.wrap(app).getString(R.string.billing_unconfigured);return };if (userRequest != null) return
        mutableAvailable.value = false
        mutablePrice.value = null
        productRequest = null // A prior account refresh must not re-enable purchases with a late price response.
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
        keepPurchaseMessage = false
        mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_waiting)
        runCatching { purchases[PurchasingService.purchase(SKU)] = owner }.onFailure { unavailable() }
    }

    fun restore() {
        if (!registered) { unavailable(); return }
        keepPurchaseMessage = false
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
        val switched = cache.getString("owner", null) != owner || cache.getString("mode", null) != LicensingService.getAppstoreSDKMode()
        if (switched) {
            keepPurchaseMessage = false
            try { entitlements.changeOwner(owner, LicensingService.getAppstoreSDKMode()) }
            catch (error: Exception) {
                if (error is CancellationException) throw error
                unavailable(); return@launch
            }
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
        history = EntitlementHistory()
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
            if (!keepPurchaseMessage) mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_price_available)
        } else unavailable()
    } }

    override fun onPurchaseResponse(response: PurchaseResponse) { scope.launch {
        val expectedUser = purchases.remove(response.requestId) ?: return@launch
        if (!BillingPolicy.purchaseOwnerMatches(expectedUser, user, response.userData?.userId,
                response.requestStatus == PurchaseResponse.RequestStatus.SUCCESSFUL) || !allowedMode()) return@launch
        keepPurchaseMessage = true
        when (response.requestStatus) {
            PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                val receipt = response.receipt
                if (!matches(receipt) || receipt.isDeferred) {
                    mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_pending)
                    return@launch
                }
                if (!persist(receipt)) return@launch
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
            history.add(receipt.snapshot())
        }
        if (response.hasMore()) {
            runCatching { updatesRequest = PurchasingService.getPurchaseUpdates(false) }.onFailure { unavailable() }
        } else {
            if (history.shouldReplace(fullHistory, cache.getLong("purchaseEpoch", 0L))) {
                if (!persistSnapshot(history.latest)) return@launch
            }
            if (restoreRequested) startUpdates(true)
            if (!keepPurchaseMessage) mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_history_checked)
        }
    } }

    private fun matches(receipt: Receipt?): Boolean = receipt != null && receipt.sku == SKU &&
        receipt.productType == ProductType.ENTITLED && receipt.receiptId.isNotBlank()

    private fun Receipt.snapshot() = EntitledReceipt(receiptId, sku, productType == ProductType.ENTITLED,
        isCanceled, isDeferred, purchaseDate?.time ?: 0L)

    private suspend fun persist(receipt: Receipt) = persistSnapshot(receipt.snapshot())

    private suspend fun persistSnapshot(receipt: EntitledReceipt?): Boolean {
        val owner = user ?: return false
        try {
            if (!entitlements.save(owner, LicensingService.getAppstoreSDKMode(), receipt)) { unavailable(); return false }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            unavailable(); return false
        }
        if (receipt != null && !receipt.canceled && !receipt.deferred) runCatching {
            PurchasingService.notifyFulfillment(receipt.id, FulfillmentResult.FULFILLED)
        }
        return true
    }

    private fun unavailable() {
        mutableAvailable.value = false
        mutablePrice.value = null
        mutableMessage.value = AppLocales.wrap(app).getString(R.string.billing_unavailable)
    }
}

