# Amazon code-7 verification - 1 November 2026

**Requested genuine LAT billing checks PASS on actual Amazon 0.1.6/code 7. Overall release remains BLOCKED.** Automatic purchase activation, explicit owned Restore and offline process restart passed. Earlier checkout delays remain an unresolved release limitation; no claim of full production readiness is made.

## Provenance and fresh preconditions

The actual Amazon-delivered nondebug code7 APK was installed in place, installer `com.amazon.venezia`, preserving records. SHA256 `9d6ebeceb52c85d3f827392b002799c5b43e0fdbbd79ca7daa81fd16a9aa3351`; certificate SHA256 `8e35b165d5a1dc14c10820cee25ed2c24e9c543ceb2f059b9f5bc93baa672297` matches the previous Amazon installation. Installed dex includes the correct LAT verifier and pending-purchase guard. [Installation](../evidence/lat-code7/installation.json).

Amazon acknowledged reset of the disposable entitlement for exactly one tester in FloraTend Premium LAT. The app then showed Premium inactive, GBP3.73 price and history checked. No app data was cleared. [Reset acknowledgement](../evidence/lat-code7/entitlement-reset-confirmed.png), [fresh precondition](../evidence/lat-code7/before-purchase.png).

## Genuine code-7 results

| Check | Result |
| --- | --- |
| Landscape checkout after reboot | PASS: dialog eventually appeared after initial waiting; cancelled without purchase. |
| Portrait checkout | PASS: genuine dialog rendered GBP3.73 and authorized free LAT purchase completed. |
| Automatic Premium activation | PASS: Premium active and purchase-confirmed message before pressing Restore. |
| Explicit owned Restore | PASS under exclusive tablet access: Premium active, history checked. |
| Offline process restart | PASS: Wi-Fi disabled, no default network, process PID changed; Premium and exports stayed available, unavailable refresh correctly reported. |
| Final online Restore | PASS: Premium active and history checked. |

[Landscape dialog](../evidence/lat-code7/post-reboot-landscape-delayed.png), [portrait dialog](../evidence/lat-code7/post-reboot-portrait-checkout.png), [automatic purchase grant](../evidence/lat-code7/after-free-purchase.png), [isolated Restore](../evidence/lat-code7/exclusive-owned-restore.png), [offline UI](../evidence/lat-code7/offline-process-restart.png), [offline process/network checks](../evidence/lat-code7/offline-process-state.json), [final online Restore](../evidence/lat-code7/final-online-restore.png), [scoped results](../evidence/lat-code7/genuine-results.json).

Genuine server verification evidence combines the fresh Amazon purchase, initially inactive entitlement, automatic confirmed Premium grant and the strict installed verifier/cache binding. No raw RVS response or receipt/account IDs were captured publicly. No fixture grant or developer instrumentation attached to the Amazon-signed app substitutes for these tests. UIAutomator was not attached during checkout; a later post-purchase read failed with null root and produced no assertion. Final passing UI observations were reviewed from fresh screenshots.

## Retained failures and limitations

Before reboot, two landscape attempts and one portrait attempt stayed on Settings waiting with no checkout/payment. Store detail reopening and client restart did not recover presentation. After reboot, the first landscape screenshot also showed waiting before checkout eventually appeared. The original presentation cause remains unisolated; this is not evidence of a black window. [Initial checks](../evidence/lat-code7/pre-reboot-checks.json), [initial landscape](../evidence/lat-code7/landscape-initial-waiting.png), [retry](../evidence/lat-code7/landscape-retry.png), [initial portrait](../evidence/lat-code7/portrait-attempt.png), [post-reboot waiting](../evidence/lat-code7/post-reboot-landscape.png).

During an earlier Restore observation Premium appeared inactive, then TrailNest opened on the same tablet. Concurrent device activity made that observation inconclusive. Device input stopped until the user paused the other chat. Under exclusive access, Wi-Fi was enabled and Restore, offline restart and final online Restore passed. No speculative source change was made from the contaminated observations. Their screenshots remain labeled as observations, not passing tests.

The source7 request-lifecycle correction has failing-before/passing-after regression evidence and24 JVM tests in each build variant plus lint. This genuine purchase passes its automatic-activation regression. Historical code6 automatic-activation failure and earlier failed developer-signed SDK tests retain their scopes. Production receipt edge cases and publisher privacy review remain open;125 overall criteria remain unmet.

## Cleanup

Wi-Fi restored on, default network available, original rotation accelerometer1/user0 restored. No user records removed, no test harness installed, Appstore library sort unchanged (By Title). Protected Appstore was not force-stopped or bypassed. [Final state](../evidence/lat-code7/final-device-state.json). Hosting remains Free, purchase was LAT only, and no public app/IAP promotion occurred.
