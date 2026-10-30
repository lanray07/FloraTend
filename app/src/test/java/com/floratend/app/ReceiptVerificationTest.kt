package com.floratend.app

import org.junit.Assert.*
import org.junit.Test

class ReceiptVerificationTest {
    @Test fun latVerifiedCacheCannotBeReusedByAProductionEndpoint() {
        val lat = "https://lat.example.test/verify"
        val production = "https://production.example.test/verify"
        assertTrue(BillingPolicy.verifiedAuthority(true, lat, lat))
        assertFalse(BillingPolicy.verifiedAuthority(true, lat, production))
        assertFalse(BillingPolicy.verifiedAuthority(true, null, production))
        assertFalse(BillingPolicy.verifiedAuthority(false, production, production))
        assertFalse(BillingPolicy.verifiedAuthority(true, "", ""))
    }
    @Test fun missingOrUnsafeEndpointCannotEnableProductionCheckout() {
        for (url in listOf("", "http://example.test/verify", "https://user:pass@example.test/verify", "https://example.test/verify?key=secret", "https://example.test/verify#token")) {
            assertFalse(ServerReceiptVerifier.configured(url))
        }
        assertTrue(ServerReceiptVerifier.configured("https://example.test/functions/v1/verify"))
    }
    @Test fun productionNeverGrantsWithoutPositiveServerVerification() {
        for (result in Verification.entries) {
            assertEquals(result == Verification.VERIFIED, ReceiptVerificationPolicy.grant("PRODUCTION", false, result))
            assertEquals(result == Verification.VERIFIED, ReceiptVerificationPolicy.grant("PRODUCTION", true, result))
            assertFalse(ReceiptVerificationPolicy.grant("SANDBOX", false, result))
            assertTrue(ReceiptVerificationPolicy.grant("SANDBOX", true, result))
            assertFalse(ReceiptVerificationPolicy.grant("UNKNOWN", true, result))
        }
    }

    @Test fun oldSdkOnlyCacheCannotBecomeAServerVerifiedGrant() {
        assertFalse(BillingPolicy.cachedGrant(false, "PRODUCTION", "owner", "receipt", true))
        assertTrue(BillingPolicy.cachedGrant(false, "PRODUCTION", "owner", "receipt", true, true))
        assertFalse(BillingPolicy.cachedGrant(false, "PRODUCTION", "other", "", true, true))
        assertFalse(BillingPolicy.cachedGrant(false, "SANDBOX", "owner", "receipt", true, true))
    }
}
