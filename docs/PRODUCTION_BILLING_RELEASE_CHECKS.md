> Latest candidate status: [0.1.5 server verification and Test 2](SERVER_RECEIPT_RELEASE_CHECK_20261030.md). Historical results below apply to their stated build.

# Production billing preparation and release checks

> Later update: LAT is now active, the Amazon-delivered Test 1 build is installed and the £3.73 regional price is verified. Actual LAT purchase, restart persistence and owned-entitlement restoration after a clean Amazon reinstall passed. See [current live test evidence](LAT_LIVE_TEST_20261030.md). Earlier results below retain their original scope.

Verified 30 October 2026. Current candidate: **0.1.4 / code 5**. This report does not approve publication or claim a production purchase. The complete mandatory release matrix remains authoritative in `RELEASE_READINESS.md`.

## Amazon configuration prepared

The actual FloraTend console now contains an **ENTITLED** item, SKU `floratend.premium`, item ID `amzn1.dex.title.18e5f7f4adaf425a8a13d5ea6ca65b9b`. On publisher authorization, **Submit to Test** was selected. Amazon now shows **TEST / Approved**, US$4.99. Public promotion was not performed.

The publisher-approved base price is **US$4.99**, with Amazon-calculated regional prices: CAD 6.98, EUR 4.46, INR 474.81, JPY 787, AUD 7.10, MXN 94.15, GBP 3.73 and BRL 26.89. These are the saved test-item list prices; no public sale or completed production-service purchase is claimed. Runtime prices come from the SDK, never from these repository values.

All 12 available IAP metadata locales have saved titles, descriptions and matching original 512/114-pixel icons: en-US, en-GB, en-AU, en-IN, de-DE, es-ES, es-MX, fr-FR, it-IT, ja-JP, pt-BR and zh-CN. All fields were read back after reloading. These purchase-dialog locale choices differ from the app listing's 15 description and 11 screenshot channels. Human language review remains pending.

The upgrade describes the implemented features only: more than five plants, JSON record export and CSV plant-list export. It is a one-time unlock with no subscription. Exports omit photographs. Metadata source: `billing/product-metadata.json`. Saved configuration/readback and proof: `submission/production-iap-readback.json`, `submission/production-iap-draft.png`.

## Defects reproduced and fixed

1. **Interrupted entitlement writes could lose a grant or retain a revocation.** The receipt preferences and DataStore Premium flag were written separately, with no complete startup reconciliation. Two controlled startup regressions failed on the old client (`evidence/billing-startup-red.txt`). `EntitlementStore` now serializes owner changes and receipt writes, completes a synchronous disk commit on the IO dispatcher before projection/fulfillment, and reconstructs the projection on startup. A queued previous-owner write cannot replace the new owner's cache. The Settings flow also requires a valid cache; a stale Premium boolean alone cannot unlock the app.
2. **A genuine FAILED purchase could leave “Waiting for Amazon purchase confirmation.” displayed.** The SDK's failed response carried a user-data object with a blank user ID. The old handler required a matching response owner before handling any status, discarding this failure. `failure-message-red.json` records the real failed callback and stuck message. The fixed handler correlates the request and current owner, and requires a matching returned owner for successful receipts. Failed responses can report their error without granting anything. Terminal purchase messages survive a normal catalogue/history refresh and reset on another purchase, explicit restore or account change. `failed-purchase.json` verifies the blank ID, no grant and visible error after refresh.
3. **Blank signing configuration broke subsequent Gradle commands.** A signing helper invocation exposed an empty `FLORATEND_KEYSTORE` value; Gradle treated it as a file path and failed. The build now treats blank paths as unset. The failure and an explicit-empty-environment successful configuration run are retained in `blank-signing-environment-failure.log` and `blank-signing-environment-fixed.log`.

`BillingPolicy` holds the shared mode/owner/cache decisions. `EntitlementHistory` selects a valid receipt across pages, handles equal-date cancellation ordering, ignores deferred/unrelated receipts, and distinguishes empty full history from empty deltas. Unit tests exercise these decisions in both compiled debug and release variants. They are protocol fixtures, not proof of real paginated/deferred SDK delivery.

## Executed evidence

Files below are under `evidence/billing-production/` unless an alternative path is shown. App Tester purchases are sandbox transactions; no real charge or live product lookup is claimed.

| Check | Executed result and scope | Evidence |
|---|---|---|
| Final real purchase and cancellation | PASS on code 5; Cancel does not grant, Get Item/Close grants and stores a genuine receipt | `purchase-cancel-final.txt`, `.json` |
| Real purchase FAILED response | PASS on code 5; blank returned user ID handled, no grant, error retained after refresh | `failed-purchase.txt`, `.json` |
| Restore from actual SDK history | PASS on code 4; local flag removed, genuine receipt restores Premium | `restore.txt`, `.json` |
| Force-stop and offline relaunch | PASS on code 4; different process, no active network, genuine cached entitlement retained | `offline-prepare.txt`, `offline-restart.txt`, `.json`, `offline-network-state.txt` |
| Real account switch and free limits | PASS on code 4; previous grant not shared; sixth plant rejected; exports disabled | `account-free-gates.txt`, `account-switch.json`, `free-gates.json` |
| Real Premium gates | PASS on code 4; sixth plant saved, export controls enabled, generated payload contains the record | `premium-gates.txt`, `.json` |
| Final actual cancellation | PASS on code 5; latest real tester receipt canceled and Premium revoked | `final-revocation.txt`, `.json` |
| Native startup/cache/deletion checks | PASS: six code-5 tests, including actual delete-all confirmation and imported-photo removal | `final-privacy-startup-store.txt`, `privacy-deletion.json` |
| Debug and release JVM policy checks | PASS: 17 tests per variant; real compiled release flags reject sandbox grants | `final-validation-build.log`, Gradle JUnit reports |
| Debug/release lint and build | PASS for code 5 | `final-validation-build.log`, `../signed-release-build.log` |
| Packaged signed release | PASS: correct package/version/certificate, v2 signature, non-debuggable, expected public auth key, no recognized private-key/API-secret patterns or QA/private-tester markers | `release-signature.txt`, `release-badging.txt`, `release-artifact-audit.json` |
| Final Fire core matrix | PASS on code 5: 20 tests across physical HD10, 7/8-inch layout overrides, large font and offline suites; overrides do not establish physical smaller-device support | `core-matrix.log`, `../device/recorded-run.json` |
| Supporting Python regressions | PASS: 39 tests, including all 120 screenshot PNG hashes and native provenance | `python-tests.log` |

The code-4 account/offline/gate tests exercise the persistence and access rules also shipped in code 5. Code 5 additionally fixes failed-purchase handling; its real purchase, failure and revocation paths were rerun. A system document-picker destination was not exercised by the Premium-gate test. The deletion check covers this app's records and imported photographs, not copies a user previously exported elsewhere.

The cache tests deliberately use controlled fixture strings and simulated projection interruptions, isolated from the production receipt preferences. They do not represent purchases, real production receipts or an observed power-loss event. Real purchase tests are named separately and never inject SDK success callbacks.

The original shared App Tester catalogue was restored byte-for-byte, response defaults and User 1 were restored, sandbox mode disabled and the final disposable entitlement revoked. Other projects' transactions were untouched. Cleanup proof is `cleanup.json`. Raw tester UI containing account/receipt references remains excluded from Git. The LAT tester email is held privately outside the public repository. Invitation was authorized; dispatch depends on Amazon making the submitted test live.

Failed setup attempts are retained: an initial JUnit harness return-type error, an initial coroutine-listener compile error, and a tester selection that returned ALREADY_PURCHASED instead of the intended FAILED response. None is counted as passing purchase evidence.

## Remaining production steps and release limits

- Completed: publisher authorized US$4.99 and test-only submission; the IAP is Approved with a TEST label. Public promotion remains a separate, unauthorized action.
- Submitted: FloraTend 0.1.4 billing verification, code 5, DRM, 12 selected Fire devices. Publisher confirmed export compliance and tester consent/age declarations. The dedicated FloraTend Premium LAT group was selected at submission. Its association is not yet verified: the submitted test's tester screen showed no testers, and saving enrollment returned server HTTP 500 with “An error occurred in adding requested tester(s)”. Wait for Amazon test availability, then install the Amazon-delivered build. The supplied tester identifier stays private, and invitations require explicit instruction.
- Verify actual production-service catalogue, regional price, purchase, restoration, cancellation and ownership behavior. DevTest requires a live IAP item; a saved draft is insufficient.
- Server RVS is not deployed. Production server verification needs a private backend and shared secret; neither belongs in the APK. The current client relies on authenticated SDK callbacks and local bookkeeping. This report does not equate that with server verification.
- Real SDK pagination, duplicate/deferred delivery, licensing-error behavior and a signed-release device transition from sandbox still require purchase-environment verification. The pure policy tests do not close these integration cases.
- Human translation/asset review, publisher privacy review, broader OS/device/accessibility/performance checks, identification and other unimplemented specification features remain open in the 204-criterion matrix. This is not an Appstore-ready declaration.

Primary references: [Amazon single-IAP configuration](https://developer.amazon.com/docs/in-app-purchasing/iap-create-and-submit-iap-items.html), [LAT for IAP](https://developer.amazon.com/docs/in-app-purchasing/iap-lat.html), [DevTest prerequisites](https://developer.amazon.com/docs/app-testing/get-started-appstore-devtest-iap.html), [RVS](https://developer.amazon.com/docs/in-app-purchasing/iap-rvs-for-android-apps.html). Amazon documents LAT purchases as free while using its production services.

## Test-only submission update — 30 October 2026

Console proof: `submission/iap-test-approved.png` and `submission/lat-test-submitted.png`. LAT shows **Test submitted**. Test-only approval is not public release approval, invitation delivery or successful production purchase evidence. The overall release matrix remains blocked.

## Direct signed tablet installation and test

The exact submitted code-5 APK was directly installed on the physical Fire tablet and passed 15 signed-release device tests. SDK mode was PRODUCTION, but the real catalogue returned unavailable, price was null and purchasing remained disabled. No purchase was attempted. The app remains installed; the separate test harness was removed. [Full signed-device result and reproduction](SIGNED_TABLET_TEST_20261030.md). This strengthens signed-client evidence without closing LAT/invitation or production purchase gates.

## LAT readiness and actual callback check

LAT still shows Test submitted. The saved submitted form confirms the FloraTend group selection. Independent real SDK account, product and full-history queries all returned FAILED in production mode; the opt-in catalogue test fails on the account prerequisite. Price/purchase/owned-entitlement restore remain blocked. [Current diagnosis and reproduction](LAT_BILLING_CHECK_20261030.md). No speculative production client change or public promotion was performed.
