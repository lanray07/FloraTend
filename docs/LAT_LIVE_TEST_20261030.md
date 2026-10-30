> Latest candidate status: [0.1.5 server verification and Test 2](SERVER_RECEIPT_RELEASE_CHECK_20261030.md). Historical results below apply to their stated build.

# Amazon-delivered LAT Test 1 — 30 October 2026

**PASS: actual LAT catalogue, purchase, restart persistence and owned-entitlement restore on the Amazon-delivered build.** This update supersedes the environment state in [the earlier blocked check](LAT_BILLING_CHECK_20261030.md), while retaining that run's actual failed callbacks as historical evidence.

## Acquisition and installation

Amazon now shows **Test in-progress** with one tester in FloraTend Premium LAT. The tester invitation was **Delivered**. The invitation opened the actual **TEST 1** listing, ASIN `B0HPTB96DG`. Only the free test app was acquired; no public app or IAP promotion was performed.

The Appstore initially displayed Open while the developer sideload still had no installer. That was insufficient installation proof. Before replacing it, the opt-in `LatPreparationTest` passed against that installation: all four record tables and the photograph directory were empty. A private archive was created, pulled outside the repository and validated. Account preferences and the invitation URL are not public evidence.

After removing the verified-empty sideload, Amazon delivered code **5 / 0.1.4** to the connected Fire HD10. The installed package is non-debuggable, has installer `com.amazon.venezia`, and has Amazon's different signing certificate. Its APK SHA-256 is `35b1481191acda7b6ab068469ff207d7ce5f4bca99fd3fbe3dadd3c05f405899`. See `evidence/lat-live/amazon-installed-artifact.json` and `amazon-certificate.txt`. A developer-signed instrumentation harness cannot attach to this Amazon-resigned build; subsequent verification uses the real UI.

## Executed results

| Check | Actual result | Evidence under `evidence/lat-live/` |
|---|---|---|
| LAT availability | Test in-progress | `lat-active.png` |
| Test app acquisition | TEST 1 listing and Free Download | `test1-listing.png`, `free-test-download.png` |
| Preservation prerequisite | PASS: one native test; zero plants, schedules, journal entries, history entries and photos | `preparation-test.txt`, `pre-replacement-counts.json` |
| Preparation build | PASS: release test harness compiled | `preparation-build.log` |
| Amazon installation | Actual Amazon installer, code 5, non-debuggable, distinct certificate | `amazon-installed-artifact.json`, `amazon-certificate.txt` |
| Fresh launch | Native onboarding renders | `amazon-first-launch.png` |
| Product/price | Unlock for **£3.73**; Premium inactive; Amazon purchase history checked | `catalogue-price-gbp.png` |
| Purchase checkout | Amazon modal displays FloraTend Premium, £3.73, VERIFY AND PAY | `lat-purchase-checkout.png` |
| Completed purchase | PASS: Premium purchase confirmed by Amazon; Premium active and exports enabled | `purchase-confirmed.png` |
| Process restart | PASS: Premium remains active after force-stop and relaunch | `premium-after-restart.png` |
| Owned-entitlement restore | PASS: uninstall, Amazon reinstall, fresh onboarding, genuine history restores Premium | `restore-uninstall.txt`, `restore-fresh-onboarding.png`, `owned-restored-after-reinstall.png` |
| Explicit Restore purchases | PASS: history checked, Premium stays active | `explicit-restore-confirmed.png` |
| Final installation and cleanup | PASS: same Amazon APK hash/installer; original orientation restored; no test harness or sandbox override | `reinstalled-package.txt`, `restored-apk-sha256.txt`, `final-device-state.json`, `final-premium-landscape.png` |

The previously missing price resolved after LAT became available and the Amazon-delivered build was installed. No hard-coded price, local Premium override, fabricated callback or production code change was used.

The first purchase attempt in landscape showed a blank window. Relaunch recovered Settings with Premium still inactive. The next attempt in portrait, without an immediate UIAutomator dump after the tap, displayed checkout normally. Both orientation and observation timing changed, so this does not isolate a root cause or establish a landscape fix. Correction after image inspection: `purchase-blank-window.png` depicts Settings waiting, not a black checkout window; it does not substantiate the reported blank screen. The issue remains unisolated.

## Purchase, restoration and remaining limits

The publisher confirmed the checkout terms at action time. VERIFY AND PAY then returned to Settings with **Premium purchase confirmed by Amazon**, **Premium is active for this Amazon account**, and enabled JSON/CSV export controls. [Amazon documents that LAT IAP transactions are free](https://developer.amazon.com/docs/in-app-purchasing/iap-lat.html). The £3.73 value is the SDK-provided UK list price; this was the TEST-only item in Test 1, with no public promotion. No payment-method change or paid production purchase was performed. Account billing statements were not audited.

After force-stop and relaunch, Premium remained active. The app's collection still showed zero plants and this session created no journal, schedule, history or photograph records. Only this empty test installation was uninstalled. Amazon re-delivered Test 1; its installer, version, non-debuggable flags and APK hash match the first downloaded Amazon artifact. Fresh onboarding reappeared, establishing that app-local preferences were removed; automatic Android backup is disabled in this app. Without any new checkout or injected entitlement, Settings showed Premium active and Amazon purchase history checked. Pressing **Restore purchases** completed with the same valid owned state. This verifies actual owned-entitlement recovery from Amazon services after removing the local app cache.

Original automatic rotation was restored, and the final landscape Settings UI retains Premium. The temporary developer test harness is absent and sandbox mode is `none`. The purchased test entitlement is deliberately retained for the tester. Raw account/receipt identifiers, invitation token, private backup and downloaded APKs remain outside Git. The supporting Python suite passed **39 tests**; the preservation harness passed one native test and compiled successfully. CI results on the pushed commit are recorded separately.

No production billing source change was needed to resolve catalogue availability. The initial blank-window attempt remains a diagnostic limitation, not a claimed fix: the successful transaction ran in portrait, and landscape checkout was not isolated or reverified after the item became owned.

The overall release remains blocked. Server RVS, wider billing delivery edge cases, human language/privacy review and the other open acceptance criteria are not closed by a working catalogue or an open checkout.
