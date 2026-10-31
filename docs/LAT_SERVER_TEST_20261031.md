# Amazon Test 2 server-verification check - 31 October 2026

**Release remains BLOCKED.** Actual Amazon code 6 completed a free LAT purchase, server-gated owned Restore and offline process restart. Automatic Premium activation immediately after purchase failed. Source 0.1.6/code 7 fixes a reproduced verification/history race; Amazon has accepted it as **Test submitted**. Its fresh genuine purchase regression remains pending.

## Actual Amazon installation

Test 2's invitation was accepted privately. ASIN B0HPTL2TBX exposed an in-place update, preserving local records. Installed 0.1.5/code 6 is nondebug, installer `com.amazon.venezia`, APK SHA256 `2e647a22019aefb67be951a58086a13deb535b40934b2665a3009ce86e8a332d`, Amazon certificate SHA256 `8e35b165d5a1dc14c10820cee25ed2c24e9c543ceb2f059b9f5bc93baa672297`. [Installation readback](../evidence/lat-server-live/installation.json). Account, receipt and invitation identifiers remain private.

## Actual code-6 results

| Check | Result and scope |
| --- | --- |
| Initial landscape checkout | FAIL: two attempts stayed on Settings waiting; no checkout activity appeared. Client restart did not recover it. |
| Landscape after normal tablet reboot | PASS: genuine checkout rendered GBP3.73; cancelled. |
| Portrait after reboot | PASS: checkout rendered GBP3.73; authorized free LAT purchase completed. |
| Automatic Premium activation | FAIL: Amazon reported purchase success but fresh app UI still showed Premium inactive and Waiting. Failure evidence retained. |
| Explicit owned Restore | PASS: Premium activated through the strict server verification gate; history checked. |
| Offline process restart | PASS: Wi-Fi disabled, no default network, process changed; Premium and exports remained available while unavailable refresh was reported. |
| Online owned Restore afterwards | PASS: Premium active and history checked. |

No UIAutomator attached after either checkout tap; screenshots observed checkout. The initial failure was absence of a dialog, not an observed black window. Fire denied force-stopping its protected Appstore, and that protection was not bypassed. Reboot recovered presentation, but the original presentation root cause remains unisolated. The source fix below addresses the separate activation failure.

Genuine verification evidence combines a fresh Amazon purchase, initially inactive entitlement, explicit Restore producing Premium, and inspection of the installed Amazon APK's LAT endpoint and server-verified cache binding. Strict source requires a positive verifier result for a new production-mode grant. This is scoped end-to-end client evidence, not a captured raw RVS response or instrumentation attached to an Amazon-signed app. Earlier failed developer-signed SDK probes remain failed; fixtures do not replace genuine results.

Evidence: [failed automatic activation](../evidence/lat-server-live/after-purchase-premium-check.json), [landscape checkout](../evidence/lat-server-live/post-reboot-landscape-checkout.png), [portrait checkout](../evidence/lat-server-live/post-reboot-portrait-checkout.png), [server-gated Restore](../evidence/lat-server-live/server-verified-restore.png), [APK inspection](../evidence/lat-server-live/installed-verifier-inspection.json), [offline restart](../evidence/lat-server-live/offline-process-restart.json), [offline UI](../evidence/lat-server-live/offline-process-restart.png), [final online UI](../evidence/lat-server-live/online-restored-final.png).

## Code-7 correction and validation

Purchase callbacks previously removed the pending request before awaiting receipt verification. Checkout Activity resume can then start a full-history refresh during that suspension, superseding the positive purchase response with another verification revision. Pending requests now remain owned until callback processing finishes, preventing resume history from overtaking verification. Queued explicit restore runs afterwards. Account/revision checks and the strict server gate remain enforced.

The suspended-verification regression failed against the old lifecycle and passed after correction. All **24 JVM tests pass in each of debug and release**, and both lint tasks pass. [Expected red regression](../evidence/lat-server-live/purchase-race-red.log), [validation log](../evidence/lat-server-live/code7-validation.log), [test totals](../evidence/lat-server-live/code7-jvm-results.json).

Signed LAT candidate **0.1.6/code 7**, SHA256 `dc39d4875f9350f743a0966ed0db62eccb5abdcfb45476406014893b760a174d`, explicitly uses the free LAT verifier. Source defaults to the separate production verifier. [Candidate provenance](../evidence/lat-server-live/code7-candidate.json), [signature](../evidence/lat-server-live/code7-signature.txt). The developer-signed candidate has not replaced the Amazon-delivered installation. Upcoming Test 2 release `amzn1.devportal.apprelease.0e6f92e850c74edd9216137c06b725a5` is **Test submitted**, DRM applied, 12 Fire tablets, only FloraTend Premium LAT group selected. [Submission screenshot](../evidence/lat-server-live/code7-submitted.png), [readback](../submission/lat2-receipt-verification.json). No public promotion occurred.

## Remaining checks

Acquire Amazon-delivered code 7 in place; verify version, installer and certificate. Retest fresh genuine purchase activation, explicit owned Restore and offline process restart, including both checkout orientations. Reset only the dedicated LAT test entitlement when authorized; never clear app records to manufacture success. Production receipt edge cases and publisher privacy review remain separate open gates. Free hosting only; no real charge. The overall matrix retains 125 unmet criteria.

Final code-6 device state: Wi-Fi on, original accelerometer rotation 1/user rotation 0, Appstore library By Title, no user data removed or harness installed. [Device state](../evidence/lat-server-live/final-device-state.json).
