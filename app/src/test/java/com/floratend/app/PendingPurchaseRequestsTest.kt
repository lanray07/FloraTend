package com.floratend.app

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PendingPurchaseRequestsTest {
    @Test fun resumeHistoryCannotOvertakeSuspendedReceiptVerification() = runBlocking {
        val requests = PendingPurchaseRequests<String>()
        requests["purchase"] = "owner"
        val releaseVerifier = CompletableDeferred<Unit>()
        val callback = launch(start = CoroutineStart.UNDISPATCHED) {
            requests.process("purchase") { releaseVerifier.await() }
        }
        try {
            assertFalse("An empty resume history must not supersede verification", requests.mayRefreshHistory(false))
            assertTrue("A second purchase must stay blocked during verification", requests.isNotEmpty())
        } finally {
            releaseVerifier.complete(Unit)
            callback.join()
        }
        assertTrue(requests.mayRefreshHistory(false))
        assertFalse(requests.mayRefreshHistory(true))
    }

    @Test fun failedVerificationReleasesThePendingRequest() = runBlocking {
        val requests = PendingPurchaseRequests<String>()
        requests["purchase"] = "owner"
        try {
            requests.process("purchase") { throw IllegalStateException("Verifier failed") }
            fail("Expected the handler failure")
        } catch (_: IllegalStateException) { /* The next refresh must still be possible. */ }
        assertTrue(requests.mayRefreshHistory(false))
    }

    @Test fun unrelatedCallbacksCannotConsumeAnotherOwnersRequest() = runBlocking {
        val requests = PendingPurchaseRequests<String>()
        requests["purchase"] = "owner"
        requests.process("unrelated") { fail("Unknown callback must not be processed") }
        assertFalse(requests.mayRefreshHistory(false))
        requests.clear()
        requests.process("purchase") { fail("Old account callback must not be processed") }
        assertTrue(requests.mayRefreshHistory(false))
    }
}
