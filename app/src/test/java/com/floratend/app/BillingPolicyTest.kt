package com.floratend.app

import org.junit.Assert.*
import org.junit.Test

class BillingPolicyTest {
    private fun receipt(id: String, date: Long, canceled: Boolean = false, deferred: Boolean = false,
                        sku: String = BillingPolicy.PREMIUM_SKU, entitled: Boolean = true) =
        EntitledReceipt(id, sku, entitled, canceled, deferred, date)

    @Test fun compiledBuildFlagsRejectSandboxInRelease() {
        assertEquals(BuildConfig.DEBUG, BillingPolicy.allowedMode("SANDBOX", BuildConfig.DEBUG))
        assertEquals(BuildConfig.DEBUG, BillingPolicy.cachedGrant(BuildConfig.DEBUG, "SANDBOX", "owner", "receipt", true))
        assertTrue(BillingPolicy.allowedMode("PRODUCTION", BuildConfig.DEBUG))
        for (mode in listOf(null, "", "UNKNOWN", "sandbox", "LOCAL"))
            assertFalse(BillingPolicy.allowedMode(mode, BuildConfig.DEBUG))
    }
    @Test fun cacheRequiresCompleteOwnerReceiptModeAndActiveFlag() {
        assertTrue(BillingPolicy.cachedGrant(false, "PRODUCTION", "owner", "receipt", true))
        assertFalse(BillingPolicy.cachedGrant(false, "PRODUCTION", "owner", "receipt", false))
        assertFalse(BillingPolicy.cachedGrant(false, "SANDBOX", "owner", "receipt", true))
        for (missing in listOf(null, "", "  ")) {
            assertFalse(BillingPolicy.cachedGrant(true, "PRODUCTION", missing, "receipt", true))
            assertFalse(BillingPolicy.cachedGrant(true, "PRODUCTION", "owner", missing, true))
        }
    }
    @Test fun historyKeepsNewestAcrossPagesAndIgnoresUnrelatedAndDeferredReceipts() {
        val history = EntitlementHistory()
        history.add(receipt("old", 10))
        history.add(receipt("unrelated", 900, sku = "other.product"))
        history.add(receipt("consumable", 800, entitled = false))
        history.add(receipt("", 700))
        history.add(receipt("pending", 600, deferred = true))
        history.add(receipt("new", 30))
        history.add(receipt("older-page", 20, canceled = true))
        assertEquals("new", history.latest?.id)
        assertTrue(history.shouldReplace(true, 100))
        assertFalse(history.shouldReplace(false, 100))
        assertTrue(history.shouldReplace(false, 30))
    }
    @Test fun cancellationWinsAnEqualTimestampInEitherDeliveryOrderAndDuplicatesAreIdempotent() {
        for (reverse in listOf(false, true)) {
            val history = EntitlementHistory()
            val receipts = listOf(receipt("same", 50), receipt("same", 50, canceled = true))
            for (r in if (reverse) receipts.reversed() else receipts) { history.add(r); history.add(r) }
            assertTrue(history.latest!!.canceled)
        }
    }
    @Test fun emptyFullHistoryRevokesButEmptyDeltaRetainsCachedState() {
        val history = EntitlementHistory()
        assertNull(history.latest)
        assertTrue(history.shouldReplace(true, 30))
        assertFalse(history.shouldReplace(false, 30))
    }
    @Test fun failedResponseMayOmitUserDataButSuccessMustProveThePendingOwner() {
        assertTrue(BillingPolicy.purchaseOwnerMatches("owner", "owner", null, false))
        assertFalse(BillingPolicy.purchaseOwnerMatches("owner", "owner", null, true))
        assertFalse(BillingPolicy.purchaseOwnerMatches("owner", "owner", "other", true))
        assertTrue(BillingPolicy.purchaseOwnerMatches("owner", "owner", "owner", true))
        assertFalse(BillingPolicy.purchaseOwnerMatches("first", "second", null, false))
        assertFalse(BillingPolicy.purchaseOwnerMatches(null, null, null, false))
    }
}
