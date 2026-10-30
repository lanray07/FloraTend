package com.floratend.app

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.Assert.*

/** Persistence/failure fixtures only; no fixture represents an Amazon-verified purchase. */
class EntitlementStoreTest {
    private val cache = InstrumentationRegistry.getInstrumentation().targetContext
        .getSharedPreferences("qa_store_entitlement", Context.MODE_PRIVATE)
    private fun receipt(canceled: Boolean = false) = EntitledReceipt("controlled-fixture", BillingPolicy.PREMIUM_SKU, true, canceled, false, 50)
    @After fun clean() { cache.edit().clear().commit() }

    @Test fun interruptedGrantAndRevocationRecoverFromDurableCache(): Unit = runBlocking {
        for (canceled in listOf(false, true)) {
            val store = EntitlementStore(cache, false) { }
            store.changeOwner("owner", "PRODUCTION")
            val interrupted = EntitlementStore(cache, false) { throw IllegalStateException("simulated projection interruption") }
            try { interrupted.save("owner", "PRODUCTION", receipt(canceled)); fail("Projection interruption required") }
            catch (expected: IllegalStateException) { assertEquals("simulated projection interruption", expected.message) }
            var recovered: Boolean? = null
            EntitlementStore(cache, false) { recovered = it }.recover()
            assertEquals(!canceled, recovered)
        }
    }
    @Test fun replacingOwnerRejectsPreviouslyQueuedPurchase(): Unit = runBlocking {
        var premium = false
        val store = EntitlementStore(cache, false) { premium = it }
        store.changeOwner("first", "PRODUCTION")
        assertTrue(store.save("first", "PRODUCTION", receipt()))
        assertTrue(premium)
        store.changeOwner("second", "PRODUCTION")
        assertFalse(premium)
        assertFalse(store.save("first", "PRODUCTION", receipt()))
        assertEquals("second", cache.getString("owner", null))
        assertFalse(premium)
    }
    @Test fun releaseStoreRejectsSandboxAndInvalidCachedGrants(): Unit = runBlocking {
        var premium = true
        val releaseStore = EntitlementStore(cache, false) { premium = it }
        releaseStore.changeOwner("owner", "SANDBOX")
        assertFalse(releaseStore.save("owner", "SANDBOX", receipt()))
        cache.edit().putBoolean("active", true).putString("receipt", "controlled-fixture").commit()
        releaseStore.recover()
        assertFalse(premium)
        cache.edit().putString("mode", "PRODUCTION").remove("receipt").commit()
        releaseStore.recover()
        assertFalse(premium)
    }
}
