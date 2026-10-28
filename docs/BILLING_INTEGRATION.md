# Amazon billing integration and remaining verification

Native dependency: `com.amazon.device:amazon-appstore-sdk:3.0.9`. The Android manifest registers Amazon's IAP response receiver with the notification permission. The intended one-time entitled product SKU is `floratend.premium`. No configured catalogue, product price, successful test purchase or release purchase is asserted by this document.

## Configuration

The app-specific public `AppstoreAuthenticationKey.pem` must be obtained from the actual Amazon Developer Console app record and placed under `app/src/main/assets/`. `AmazonBilling.register` leaves billing unavailable when that file is absent. The public key is distinct from private signing keys, developer passwords and any RVS shared secret. None of those private secrets belongs in source or the APK. The actual FloraTend console public key is now bundled. Gitignore permits only this exact public PEM path; private signing material remains excluded. The app record is recorded in `submission/console-draft.json`.

Configure the entitled product and marketplace distribution in the actual Amazon account. Set up Amazon App Tester or Live App Testing. These external steps and tests have not been replaced with a mock success path.

## Implemented client behaviour

- Registers the PurchasingListener, invokes license verification and checks SDK mode. Production builds accept PRODUCTION mode; sandbox is allowed only for debug builds.
- Retrieves user data on refresh/resume, requests product details and shows the returned product price when the expected entitled product is available.
- Correlates callbacks with request IDs and expected account IDs. Account changes clear the previous grant before synchronising the new account.
- Checks entitlement SKU, product type and receipt ID; deferred receipts do not grant Premium. Cancellations can revoke the grant.
- Restores full purchase history on request, handles pagination, and uses candidate receipt dates/cancellation status when applying updates.
- Persists account/mode/receipt metadata in private preferences and the Premium flag in DataStore. Verified cached state is retained when a refresh cannot complete offline.
- Notifies fulfillment after recording the entitlement. Failure/not-supported paths expose unavailable state rather than substituting a hardcoded price or granting Premium.

These are source-level descriptions. Request correlation, pagination, duplicate handling, deferred/cancelled receipts, process death, account switching, offline cache and release/sandbox transitions all need executed purchase-environment tests. The client integration is not a server Receipt Verification Service deployment. A backend RVS secret must never be added to the APK; choose and verify any server-side verification design separately.

## Required executed scenarios

Record product lookup and store-provided regional price; successful purchase; cancellation/failure; already-purchased restoration; full paginated restore; duplicate callbacks; deferred purchase; cancelled/revoked entitlement; account switch; offline relaunch; and a release build's rejection of sandbox state. Verify the free plant limit and each Premium gate in UI and persistence scenarios. Until these results exist, AC-12 remains incomplete and live billing is BLOCKED.

Official integration/API requirements and references are recorded in [AMAZON_REQUIREMENTS.md](AMAZON_REQUIREMENTS.md). Criterion-level status is generated in [AMAZON_BILLING_TEST_REPORT.md](../AMAZON_BILLING_TEST_REPORT.md); do not treat SDK compilation as a purchase test.

## Preserved build-coordination note

Before report regeneration, the billing report contained this note: a standalone `:app:compileDebugKotlin` attempt was stopped to avoid contention with the primary acceptance build. Its partial log is `evidence/billing-compile.log`; it is not passing build evidence. Use the main build/test/lint logs for compilation status. Billing messages are Android string resources (`billing_*`), ready for the translation pipeline; non-English billing resources are included as assistant-authored drafts pending human review.
