package com.floratend.app

/** Pure decisions shared by runtime billing and debug/release regression tests. */
object BillingPolicy {
    const val PREMIUM_SKU = "floratend.premium"

    fun allowedMode(mode: String?, debugBuild: Boolean): Boolean =
        mode == "PRODUCTION" || (debugBuild && mode == "SANDBOX")

    fun cachedGrant(debugBuild: Boolean, mode: String?, owner: String?, receipt: String?, active: Boolean): Boolean =
        active && allowedMode(mode, debugBuild) && !owner.isNullOrBlank() && !receipt.isNullOrBlank()

    fun purchaseOwnerMatches(expected: String?, current: String?, returned: String?, successful: Boolean): Boolean =
        !expected.isNullOrBlank() && expected == current && (!successful || returned == expected)
}

data class EntitledReceipt(
    val id: String, val sku: String, val entitled: Boolean,
    val canceled: Boolean, val deferred: Boolean, val purchaseEpoch: Long
)

/** Accumulates all history pages; deferred/unrelated receipts cannot supersede a grant. */
class EntitlementHistory {
    var latest: EntitledReceipt? = null
        private set

    fun add(receipt: EntitledReceipt) {
        if (receipt.sku != BillingPolicy.PREMIUM_SKU || !receipt.entitled || receipt.id.isBlank() || receipt.deferred) return
        val previous = latest
        if (previous == null || receipt.purchaseEpoch > previous.purchaseEpoch ||
            (receipt.purchaseEpoch == previous.purchaseEpoch && receipt.canceled)) latest = receipt
    }

    fun shouldReplace(fullHistory: Boolean, cachedPurchaseEpoch: Long): Boolean =
        fullHistory || (latest?.purchaseEpoch?.let { it >= cachedPurchaseEpoch } == true)
}
