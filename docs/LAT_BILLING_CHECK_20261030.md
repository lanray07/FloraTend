> Latest candidate status: [0.1.5 server verification and Test 2](SERVER_RECEIPT_RELEASE_CHECK_20261030.md). Historical results below apply to their stated build.

# LAT billing readiness check — 30 October 2026

> Later update: LAT is now active, the Amazon-delivered Test 1 build is installed and the £3.73 regional price is verified. Actual LAT purchase, restart persistence and owned-entitlement restoration after a clean Amazon reinstall passed. See [current live test evidence](LAT_LIVE_TEST_20261030.md). Earlier results below retain their original scope.

**Blocked by the current Amazon test environment. Missing product/price is not resolved, and purchase plus owned-entitlement restore are not complete.** This check adds actual SDK callback evidence; it does not convert a sideload into an Amazon-delivered LAT installation.

## Verified console state

- LAT: **Test submitted**, not available for testing yet. Manage testers and Add upcoming version remain disabled.
- The read-only submitted form contains signed code **5**, DRM enabled, export certification checked and **FloraTend Premium LAT checked and disabled**. This confirms that the tester-group selection was persisted at submission. It does not prove invitation delivery or accepted tester ownership.
- Actual item: **TEST / Approved**, entitled SKU `floratend.premium`, base price **USD 4.99**. No public promotion or price change was performed.

Proof: `evidence/lat-billing/console-readback.json`, `lat-status.png`, `submitted-configuration.png` and `iap-status.png`. The older enrollment-save HTTP 500 remains historical evidence; repeated forced enrollment while Amazon disables tester management is not a verified remedy.

## Actual tablet and SDK results

The original signed non-debuggable 0.1.4/code-5 APK remains installed on the physical Fire HD10. Sandbox mode is disabled. The tablet has an active network; Appstore browsing worked, In-App Purchasing is enabled and the device is registered. The account screen displays a Send-to-Kindle alias, which does not verify the Amazon login email. Private account screenshots remain outside Git.

The normal signed-client probe again showed no price or Premium. A separate real PurchasingListener observer then delegated all callbacks to the client and recorded only status/availability fields. It used the actual public SDK for independent product and full-history queries after account lookup failed. No callback or receipt was fabricated.

| Operation | Actual callback |
|---|---|
| Account lookup | FAILED; no nonblank owner returned |
| Explicit query for floratend.premium | FAILED; no item or price returned; not reported as a successful response containing an unavailable SKU |
| Explicit full-history query | FAILED |
| SDK mode | PRODUCTION |
| Client availability / price | false / null |
| Purchase | Not attempted; no usable account/product/price |
| Owned-entitlement restoration | Blocked; no successful history response or prior production purchase |

The opt-in catalogue gate ran and **failed one test** on the actual account callback. This is intentionally retained as failed integration evidence, separate from the earlier passing client/device suites. Build success or the release unavailable-state probe does not mean the catalogue gate passed.

## Diagnosis and limits

Ranked hypotheses were pending LAT availability/ownership, incomplete tester access, and a client callback/rendering bug. The evidence currently rules out a successful SDK price being discarded by the client in this run: independent SDK queries themselves failed. It does **not** identify the precise Amazon backend reason for FAILED, or prove that LAT activation alone resolves every account issue. Pending LAT availability is the first observed external blocker.

No production source behavior was changed without a reproduced client defect. Changing the approved SKU, inventing a price, enabling Premium locally, publishing publicly or swapping back to sandbox would not complete the requested production-service test.

Amazon documents that test-only IAP items are available in the LAT app, and that testers acquire the LAT app through their invitation using the testing account. Sources: [LAT IAP](https://developer.amazon.com/docs/in-app-purchasing/iap-lat.html), [LAT FAQ](https://developer.amazon.com/docs/app-testing/live-app-testing-faq.html), [SDK response handling](https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html). These sources explain prerequisites; they do not prove this particular backend failure's cause.

## Reproduce and resume

Build only the separately signed harness, leaving the original candidate installed:

```powershell
.\tools\build_signed_release.ps1 -BuildDeviceTests
.\tools\test_lat_catalogue.ps1
```

This is explicitly opt-in and returns nonzero when live account/catalogue prerequisites fail. Ordinary CI compiles the probe but does not run external store tests. The runner removes the harness afterward and reopens FloraTend. An Amazon-resigned LAT APK may require UI-only automation rather than a developer-signed instrumentation harness; inspect certificates before replacing it.

Once Amazon makes Test 1 available: verify group enrollment/invitation, accept using the device's testing account, acquire/install the Amazon-delivered Test 1 build, then rerun real catalogue/price and complete the free LAT purchase. Verify Premium, remove only its recoverable local projection, restore from genuine SDK purchase history, and verify the same entitlement remains active after relaunch. Existing authorization covers this testing and invitation; no renewed approval is needed for those same actions. Keep raw account/receipt identifiers outside the public repository.

Current missing product, price, purchase and owned-entitlement restore remain release blockers. The prior App Tester sandbox successes and 15 signed-client device passes retain their original narrower scope.
