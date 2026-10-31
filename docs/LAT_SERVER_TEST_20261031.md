# Amazon Test 2 server-verification check - 31 October 2026

**In progress / BLOCKED:** Amazon Test 2 is delivered and the actual Amazon-signed code-6 app is installed. Genuine server receipt, purchase and restore tests have not passed. After reboot, ADB requires the user to unlock the tablet and approve USB debugging.

## Actual acquisition and installation

The publisher supplied Test 2's invitation privately. Its retail ASIN is B0HPTL2TBX; the invitation token and tester account identifiers remain outside Git. The invitation was accepted and delivery targeted only the connected 7th Fire. Amazon's library lists both independent tracks; its generic Open action still launched code 5. Opening Test 2's own product detail page exposed Update, which installed **0.1.5 / code 6** without uninstalling or clearing local data.

Installer: `com.amazon.venezia`. APK SHA256: `2e647a22019aefb67be951a58086a13deb535b40934b2665a3009ce86e8a332d`. Amazon signing certificate SHA256: `8e35b165d5a1dc14c10820cee25ed2c24e9c543ceb2f059b9f5bc93baa672297`, matching the prior Amazon build; the package is not debuggable. The downloaded Amazon APK remains private. [Installation readback](../evidence/lat-server-live/installation.json).

## Observed checkout failure

The actual new app showed Amazon's GBP3.73 price and successful history check, with Premium inactive. Two landscape purchase attempts, separated by a client force-stop/restart, remained on Settings waiting for Amazon confirmation. Screenshots were captured without attaching UIAutomator after either tap. There is no evidence of a black window: no checkout dialog appeared at all. No payment button was pressed and no entitlement was granted.

Amazon Appstore logs report successful item lookup/history and acceptance of the purchase command, followed by the metric for initiating the detail dialog. System activity logs show no Amazon checkout activity starting during the observed attempts. [Sanitized SDK stages](../evidence/lat-server-live/checkout-sdk-stages.json). These metrics do not establish a completed purchase or successful remote RVS request. Full diagnostic logs are held privately because they can contain account/request identifiers.

The client-only restart did not resolve the failure. Fire OS denied force-stopping its protected Appstore package; that protection was not bypassed. A normal tablet reboot was then performed to refresh the store service. The device returned as ADB unauthorized, and the user was asked to unlock it and accept USB debugging. Diagnosis must resume there before inferring a cause or implementing a speculative billing change.

## Remaining work

After USB authorization, retry the same landscape path after reboot, then test portrait separately. Validate the actual Amazon receipt through the configured LAT server, confirm Premium, explicit owned restore, and offline process restart. Preserve data, restore the Appstore library sort to By Title (temporarily changed to By Recency during acquisition), and retain original rotation/Wi-Fi settings. The server tests, source gating and prior baseline results remain scoped to their existing evidence. No public app/IAP promotion or paid hosting was enabled. Overall release readiness remains blocked.
