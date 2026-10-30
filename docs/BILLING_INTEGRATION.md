# Amazon billing integration and remaining verification

Native dependency: `com.amazon.device:amazon-appstore-sdk:3.0.9`. The Android manifest registers Amazon's IAP and DRM response receivers with their notification permissions and declares package visibility for Appstore and App Tester. The intended one-time entitled product SKU is `floratend.premium`. Real sandbox purchase evidence is now recorded in [30 October verification](RELEASE_VERIFICATION_20261030.md); no live product or release purchase is asserted.

## Configuration

The app-specific public `AppstoreAuthenticationKey.pem` must be obtained from the actual Amazon Developer Console app record and placed under `app/src/main/assets/`. `AmazonBilling.register` leaves billing unavailable when that file is absent. The public key is distinct from private signing keys, developer passwords and any RVS shared secret. None of those private secrets belongs in source or the APK. The actual FloraTend console public key is now bundled. Gitignore permits only this exact public PEM path; private signing material remains excluded. The app record is recorded in `submission/console-draft.json`.

The actual entitled item is now **Ready to Submit**, with 12 localized metadata/icon drafts and proposed US$4.99 base pricing plus Amazon currency conversions. Prices are not publisher-approved or active. See `billing/product-metadata.json` and `submission/production-iap-readback.json`. An unsubmitted LAT draft copies signed version 0.1.4/code 5. No item submission or tester invitation has occurred. Amazon App Tester tests use the isolated Flora fixture in `billing/amazon.sdktester.json`; its US$4.99 response is sandbox data. The shared tablet catalogue was restored afterwards.

## Implemented client behaviour

- Registers the PurchasingListener, invokes license verification and checks SDK mode. Production builds accept PRODUCTION mode; sandbox is allowed only for debug builds.
- Retrieves user data on refresh/resume, requests product details and shows the returned product price when the expected entitled product is available.
- Correlates callbacks with request IDs and expected account IDs. Account changes clear the previous grant before synchronising the new account.
- Checks entitlement SKU, product type and receipt ID; deferred receipts do not grant Premium. Cancellations can revoke the grant.
- Restores full purchase history on request, handles pagination, and uses candidate receipt dates/cancellation status when applying updates.
- Commits account/mode/receipt metadata before projecting the Premium flag to DataStore or acknowledging fulfillment. A serialized EntitlementStore recovers the projection on startup; stale account writes and release sandbox grants are rejected. Cached state is retained when a refresh cannot complete offline.
- Correlates unsuccessful purchase callbacks by request and current owner even when Amazon returns a blank user ID. Successful grants require matching returned ownership. Terminal purchase errors remain visible through background refresh. Fulfillment follows successful persistence.

The [current verification report](PRODUCTION_BILLING_RELEASE_CHECKS.md) distinguishes real sandbox callbacks, controlled cache fixtures and JVM policy tests. Physical offline force-stop/relaunch, failed purchase, access gates and cache recovery passed. Real SDK pagination, duplicate/deferred delivery and signed-release device transitions still need integration tests. The client integration is not a server Receipt Verification Service deployment. A backend RVS secret must never be added to the APK; choose and verify any server-side verification design separately.

## Required executed scenarios

Record product lookup and store-provided regional price; successful purchase; cancellation/failure; already-purchased restoration; full paginated restore; duplicate callbacks; deferred purchase; cancelled/revoked entitlement; account switch; offline relaunch; and a release build's rejection of sandbox state. Verify the free plant limit and each Premium gate in UI and persistence scenarios. Until these results exist, AC-12 remains incomplete and live billing is BLOCKED.

Official integration/API requirements and references are recorded in [AMAZON_REQUIREMENTS.md](AMAZON_REQUIREMENTS.md). Criterion-level status is generated in [AMAZON_BILLING_TEST_REPORT.md](../AMAZON_BILLING_TEST_REPORT.md); do not treat SDK compilation as a purchase test.

## Preserved build-coordination note

Before report regeneration, the billing report contained this note: a standalone `:app:compileDebugKotlin` attempt was stopped to avoid contention with the primary acceptance build. Its partial log is `evidence/billing-compile.log`; it is not passing build evidence. Use the main build/test/lint logs for compilation status. Billing messages are Android string resources (`billing_*`), ready for the translation pipeline; non-English billing resources are included as assistant-authored drafts pending human review.
