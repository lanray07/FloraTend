# Cold-start checkout diagnosis - 1 November 2026

## Reproduction and minimisation

Actual Amazon code 7, exclusively reserved Fire HD10/API28, same inactive dedicated LAT entitlement and GBP3.73 catalogue. `python tools/measure_lat_checkout.py --confirm-priced-inactive-lat-screen --tap 320 698 --timeout 60 --limit 5 --cancel-visible --name <unique-run>` observes real presentation without UiAutomation and never presses Pay. The 5-second limit is an investigation threshold, not an Amazon SLA. Screenshot sampling gives an upper bound on presentation time.

Warm landscape attempts passed at 4.234s and 3.468s. After force-stopping only FloraTend and relaunching its Activity, price/history returned but checkout stayed absent for the full 60s observation. Backgrounding and foregrounding the same process released the pending dialog within the next screenshot. No store reset/reboot/receipt injection was used for that recovery. [Warm1](../evidence/checkout-timing/baseline-landscape-1.json), [warm2](../evidence/checkout-timing/baseline-landscape-2.json), [cold failure](../evidence/checkout-timing/cold-app-landscape.json), [same-process resume](../evidence/checkout-timing/same-process-resume-releases-dialog.png).

This isolates a lifecycle-dependent queue, rather than a fixed network delay. Ranked probes considered SDK Activity visibility, retained store state and asynchronous startup. The first probe recovered the dialog without changing the store/process, supporting the SDK lifecycle visibility explanation. Source registered from Application.onCreate only after suspending entitlement-cache recovery. Amazon requires listener registration on the main thread in the main Activity.onCreate: https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html (checked 1 November 2026).

## Candidate correction

Code8/0.1.7 removes registration from Application startup and calls it directly from MainActivity.onCreate. SDK registration now occurs synchronously before cache recovery can suspend beyond the first start/resume. Cache recovery and licensing refresh continue asynchronously, and the strict server receipt gate and pending-purchase guard remain intact. Missing-public-key fixture recovery still runs without SDK contact.

There is no unit seam that proves the proprietary SDK's Activity tracking. The correct regression is the same real Amazon-delivered cold-start probe, repeated in both orientations; QA or developer-signed SDK behaviour cannot substitute. The actual Amazon-delivered code 8 regression passed on 1 November 2026, as detailed below. Raw logs/captures remain private; only sanitized results and reviewed app screenshots are committed. The detector itself is checked against retained real waiting and checkout screenshots.


## Amazon-delivered regression

Amazon Test2 accepted code 8, then exposed the same ASIN Update. The in-place installed APK is nondebug, installer `com.amazon.venezia`, SHA256 `64e3c82611a9e6c1dde48abd55207c3908d33cd6ba357314eea4566ad3d4de46`, with the matching Amazon certificate and LAT endpoint. [Installation](../evidence/checkout-timing/code8-installation.json), [submission](../evidence/checkout-timing/code8-submitted.png).

| Actual checkout attempt | Upper-bound presentation time | Outcome |
|---|---:|---|
| First launch after update, landscape |3.391s| Dialog shown, cancelled |
| Force-stop/restart, landscape |3.156s| Dialog shown, cancelled |
| Force-stop/restart, portrait |3.297s| Dialog shown, cancelled |
| Second force-stop/restart, portrait |3.125s| Dialog shown, subsequent free LAT purchase |

Each timing has its own JSON and reviewed screenshot under `evidence/checkout-timing/code8-*`. No background/foreground workaround, reboot, store reset or UiAutomation attachment was needed. This is the green result against the retained code 7 cold-start 60-second failure, scoped to this Fire hardware and current store session. It does not establish an Amazon-wide latency SLA.

The fresh free LAT purchase automatically activated Premium before Restore. Explicit owned Restore, offline process restart (Wi-Fi disabled/no default network/changed PID), and final online Restore all passed. [Automatic grant](../evidence/checkout-timing/code8-automatic-grant.png), [Restore](../evidence/checkout-timing/code8-owned-restore.png), [offline UI](../evidence/checkout-timing/code8-offline-restart.png), [offline state](../evidence/checkout-timing/code8-offline-state.json), [final Restore](../evidence/checkout-timing/code8-final-online-restore.png), [scoped results](../evidence/checkout-timing/code8-genuine-results.json). These are genuine installed-client strict server-gate/UI observations, not captured raw RVS responses. Historical code 6 automatic-grant failure remains retained.

The signed developer candidate passed 24 JVM tests per debug/release variant and both lint checks. Twenty-one isolated QA instrumentation tests passed on the Fire across the base 19-test suite and two additional workflows; these controlled fixtures are separate from genuine Amazon billing. Source defaults to production verification; only this test artifact selects LAT. Wi-Fi and original rotation1/0 were restored; no actual app data was cleared or replaced with a developer APK.
