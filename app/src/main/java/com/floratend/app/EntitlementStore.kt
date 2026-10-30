package com.floratend.app

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The receipt cache is authoritative; the DataStore flag is a recoverable projection.
 * This is local SDK bookkeeping, not remote receipt verification.
 */
internal class EntitlementStore(
    private val cache: SharedPreferences,
    private val debugBuild: Boolean,
    private val projectPremium: suspend (Boolean) -> Unit
) {
    private val lock = Mutex()

    suspend fun recover() = lock.withLock {
        projectPremium(BillingPolicy.cachedGrant(debugBuild, cache.getString("mode", null),
            cache.getString("owner", null), cache.getString("receipt", null), cache.getBoolean("active", false),
            BillingPolicy.verifiedAuthority(cache.getBoolean("serverVerified", false),
                cache.getString("verificationEndpoint", null), BuildConfig.RECEIPT_VERIFIER_URL)))
    }

    suspend fun changeOwner(owner: String, mode: String) = lock.withLock {
        require(owner.isNotBlank())
        write(cache.edit().putString("owner", owner).putString("mode", mode)
            .putBoolean("active", false).remove("receipt").remove("purchaseEpoch").remove("serverVerified").remove("verificationEndpoint"))
        projectPremium(false)
    }

    suspend fun save(owner: String, mode: String, receipt: EntitledReceipt?, serverVerified: Boolean = false): Boolean = lock.withLock {
        // A callback queued before account replacement must not rewrite the new owner's cache.
        if (owner != cache.getString("owner", null) || !BillingPolicy.allowedMode(mode, debugBuild)) return@withLock false
        if (receipt != null && (receipt.sku != BillingPolicy.PREMIUM_SKU || !receipt.entitled ||
                receipt.id.isBlank() || receipt.deferred)) return@withLock false
        val active = receipt != null && !receipt.canceled
        if (active && mode == "PRODUCTION" && !serverVerified) return@withLock false
        write(cache.edit().putString("mode", mode).putBoolean("active", active)
            .putBoolean("serverVerified", active && serverVerified)
            .putString("verificationEndpoint", if (active && serverVerified) BuildConfig.RECEIPT_VERIFIER_URL else null)
            .putString("receipt", receipt?.id).putLong("purchaseEpoch", receipt?.purchaseEpoch ?: 0L))
        projectPremium(active)
        true
    }

    private suspend fun write(edit: SharedPreferences.Editor) {
        // Complete the durable write before projecting or acknowledging fulfillment.
        check(withContext(Dispatchers.IO) { edit.commit() }) { "Entitlement cache could not be persisted" }
    }
}
