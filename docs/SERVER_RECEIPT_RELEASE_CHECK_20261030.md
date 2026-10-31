> Superseded availability status: [31 October actual Amazon code-6 installation and checkout diagnosis](LAT_SERVER_TEST_20261031.md). Test 2 is now in progress and installed; genuine receipt tests remain blocked.

# Server receipt verification - 30 October 2026

**Release remains BLOCKED.** Source 0.1.5/code 6 is submitted as Amazon LAT Test 2. The console reads **Test submitted**; the Amazon-resigned build is not yet available for genuine receipt and checkout verification. Test 1 code 5 purchase and restore results remain historical evidence for its SDK-only implementation.

## Implemented and deployed

Two separate Cloudflare Workers services are deployed on the account's confirmed **Free ($0) plan**, without an upgrade: [LAT verifier](https://floratend-receipt-verifier.lanraybanks.workers.dev/verify) and [production verifier](https://floratend-receipt-verifier-production.lanraybanks.workers.dev/verify). POST requires an app-scoped Amazon user ID and receipt ID. The shared Amazon secret stays on the server. The production service rejects beta/test receipts; the LAT service requires beta receipts. SQLite Durable Objects enforce atomic per-owner and global minute limits using HMAC identifiers. Free quota exhaustion must fail requests, without paid fallback. Supabase created no project because the free-project quota was already full.

The client verifies genuine production-mode receipts before granting Premium or acknowledging fulfillment. It refreshes full history on owner refresh, rejects SDK-only legacy caches, binds a verified cache to its exact endpoint and owner, and guards against late suspended verification results overwriting newer decisions. A transient outage can retain an already verified cache but cannot grant a new entitlement. Android defaults to the production URL; the submitted LAT APK explicitly uses the LAT URL. See [server setup](../server/README.md) and the root environment template.

An actual deployed-runtime defect was reproduced and fixed: Workers rejects fetch redirect mode `error`. The verifier now uses `manual` and fails closed for all upstream redirects. This is a **server transport fix**, not a checkout-window fix.

## Executed verification

Evidence is under [receipt-verification](../evidence/receipt-verification/). Results:

- Six Node server protocol/security tests passed, including the Workers redirect regression.
- Twenty-one JVM tests passed for each of debug and release; Android debug/release lint and signed candidate builds passed.
- Seven signed, nondebug cache/startup fixture tests and eleven separate QA core/localization/voice-fallback device tests passed. These controlled tests do not prove a real receipt was verified.
- Thirty-nine Python checks, source audit, structural localization checks, twelve-page website validation and Worker bundle validation passed. Human translation approval remains pending.
- Each deployed endpoint passed actual negative-input and concurrent limiter checks: ten invalid pairs rejected by Amazon and two rate-limited requests. No fake receipt was accepted.
- The genuine receipt observer **failed** on the developer-signed direct code-6 installation: no owned-history callback arrived. An independent real SDK probe returned FAILED for account, product and full history. No genuine receipt reached RVS. These failures are retained, not recategorized as passing tests.

## Checkout and reset limitation

The earlier `evidence/lat-live/purchase-blank-window.png` was mislabeled: inspection shows the app Settings screen waiting, not a black checkout window. It does not prove the reported blank window. The original report has been corrected. The blank checkout issue remains **unreproduced and unisolated**, not a claimed code fix. Later fresh baseline checks rendered checkout successfully in both orientations (below).

Amazon acknowledged the authorized Test 1 entitlement reset, including one individual retry. Initial checks still recovered ownership, blocking observation. After another Amazon redelivery, full history correctly showed Premium inactive. Fresh portrait checkout rendered GBP3.73 and was cancelled. Fresh landscape checkout also rendered GBP3.73; the authorized free LAT purchase completed and Amazon confirmed Premium active. Explicit Restore purchases was invoked afterwards. See [sanitized readback](../evidence/receipt-verification/test1-checkout-readback.json) and the portrait/landscape screenshots beside it. No UIAutomator observer attached after either purchase tap. This clears the immediate presentation check on code 5 but establishes neither the original root cause nor code-6 server verification. The new observer requires a real store-installed build and fresh preconditions, captures checkout without attaching UIAutomator after the purchase tap, never presses Pay and restores rotation. Its full checkout path remains unverified.

## Test 2 and next checks

The console accepted the nondebug 0.1.5/code-6 APK with DRM and retained twelve Fire tablet targets. Its SHA256 is `a94efb019d38a3a82f8f62f4583ae4b88629bdf739a6d325fbb64f4563a72329`. Only the existing FloraTend Premium LAT group is selected. [Submission screenshot](../evidence/receipt-verification/lat2-submitted.png) and [readback](../submission/lat2-receipt-verification.json) identify the independent track. Amazon warns first submission on tracks 2â€“5 may take up to 24 hours. There was no public app or IAP promotion.

When ready, acquire **Test 2**, verify its Amazon installation/version, test a genuine server-verified purchase, explicit owned restore and offline process restart, and isolate checkout in portrait and landscape. Preserve local records and keep receipt/account/invitation identifiers out of Git. The separate Test 1 invitation does not install Test 2. Privacy disclosures now describe the candidate server processing, but publisher review remains pending. The overall acceptance matrix still has 125 unmet criteria; this backend work does not complete the overall release.
