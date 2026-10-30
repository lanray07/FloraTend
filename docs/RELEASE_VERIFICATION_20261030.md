# FloraTend verification â€” 30 October 2026

This is a development verification record, not release approval. The Amazon app and products remain unpublished. Human language, privacy and remaining mandatory release gates are outstanding.

## Real localized screenshots

`StoreScreenshotTest` captured eight implemented scenes in each of the 19 packaged app locales on the physical Fire HD 10 (KFTRWI, API 28). The successful 194.848-second instrumentation run and 152 original PNGs are under `evidence/store-captures/run-20261030-094326/`. Connectivity reported **Active default network: none**. Demonstration plants, schedules, journal and care records were inserted through the real database and removed afterwards; no customer collection was used. The imported botanical illustration is existing original project artwork, not a claimed photograph or identification result.

The composer produced 120 landscape PNG drafts (eight for each of 15 metadata locales), at 2560 Ã— 1600. Each contains the full, uncropped native screenshot with a localized headline. `assets/screenshots/localized/localized-manifest.json` records native/export hashes, locale mapping and filenames. `en-IN` uses the packaged `en-GB` UI, and `es-MX` uses `es-419`. Contact sheets support visual review. The PNG regression tests check dimensions, CRCs, content hashes, the complete locale/scene matrix and native provenance without Pillow. Recomposition requires Pillow and suitable installed fonts; fonts are not redistributed.

Amazon exposes only 11 image-language channels in this draft. All 88 images were uploaded and read back after reloading, with separate asset IDs in `submission/screenshot-console-upload.json`. Dutch, Polish, Portuguese (Portugal) and Swedish packs exist in Git but have no dedicated image channel in this console; its stated default is English (U.S.). The 15 description locales are separate from these 11 image channels. Uploaded assets remain drafts pending human language and claims review.

The captures identify version 0.1.1/code 2. Version 0.1.2/code 3 changes Amazon manifest configuration only; native UI and translations are unchanged. The voice scene truthfully shows this Fire's unavailable-recognizer fallback. No microphone recognition is claimed.

## Real Amazon App Tester billing

The tests invoked Amazon Appstore SDK 3.0.9 against its installed App Tester, with genuine SDK callbacks and receipts. The Flora-only fixture in `billing/amazon.sdktester.json` defines an entitled `floratend.premium` sandbox product at US$4.99. This is **not a live price or a configured live IAP**. No real money was spent.

| Scenario | Executed result | Evidence under `evidence/billing/` |
|---|---|---|
| Catalogue and returned price displayed in Settings | PASS | `catalogue-probe.txt`, `.json` |
| Cancel real purchase dialog, then complete Get Item / Close | PASS; cancel does not grant, success persists a real receipt | `purchase-and-cancel.txt`, `.json` |
| Remove only local Premium flag, restore from actual SDK history | PASS | `restore-receipt.txt`, `.json` |
| Cancel actual FloraTend receipt in App Tester | PASS; SDK cancellation revokes Premium and cached active state | `canceled-receipt.txt`, `.json` |
| Change actual sandbox account | PASS; previous owner's grant is not shared | `account-switch.txt`, `.json` |
| Return to original account and restore | PASS | `original-account-restore.txt` |
| Configure GetUserData FAILED | PASS; previously verified cache retained, new purchases disabled | `failed-refresh-cache.txt`, `.json` |
| Final cancellation and cleanup | PASS; no test Premium remains | `cleanup-revocation.txt`, `.json`, `cleanup.json` |

Two setup/harness failures remain recorded: the first purchase test waited before closing App Tester's thank-you screen; the fixed test closes it before awaiting the SDK result. An initial cleanup attempt ran before the transaction cancellation had taken effect; the visible receipt still said FULFILLED. After cancellation visibly became CANCELED, the revocation test passed. These failed attempts are not counted as passes.

The original shared tester catalogue was restored byte-for-byte from a private backup outside Git. Other projects' transactions were not changed. User 1 and default response settings were restored; sandbox mode is disabled with Amazon's documented `none` value. Raw tester screenshots/XML and private backup paths are excluded from Git because they contain account/receipt references and unrelated project data. Focused reports contain no receipt or owner IDs.

Missing required Amazon package queries and the DRM response receiver were fixed in the main manifest. `billing-manifest-before-fix.json` and `billing-manifest-after-fix.json` preserve the failed/passing static check. Generic device runners now explicitly select core tests, so stateful billing tests and screenshot fixtures require deliberate opt-in.

Not established: live IAP configuration/pricing, regional prices outside this sandbox, production purchase/LAT, server RVS, paginated/duplicate/deferred responses, process-death recovery, release rejection of sandbox cache, or every Premium feature gate. These remain release work. A failed SDK refresh is not a physical network-disconnection test.

## Privacy draft

All 39 Amazon questionnaire data types were answered and saved; the console reported **Saved for review**. Exact selections and purposes are in `submission/privacy-label-draft.json`, with screenshot proof. Ten types are declared collected: app-scoped user IDs, marketplace/account-region information, purchase history, conservative device/connection identifiers, photographs, optional speech recordings processed by an installed provider, SDK crash/diagnostic information, user content and app-owned care-calendar records. The calendar category does not mean device calendar permission or importing another calendar. Local-only records and conditional provider processing are described explicitly in the public policy.

No advertising SDK, advertising identifier collection or separate FloraTend behavioral analytics is declared. Amazon SDK operational receipt/parsing/exception metrics informed the diagnostics answers. The static SDK inventory records its artifact hash and inspection scope; absence of direct identifier APIs is not proof of all runtime provider processing. Connection identifiers are a conservative inference about service connections, not a claim that FloraTend reads a hardware serial number. Provider retention was not independently verified.

The questionnaire's transfer answers apply Amazon's service-provider and expected user-initiated-action exceptions. They do not mean information never leaves the device: Amazon handles purchases, installed speech providers may use a network, and exports go to the user's chosen destination. Publisher review of these classifications remains mandatory. This work does not claim a legal compliance assessment.

## Display-test timing failure

The first broader code-3 run passed the physical and large-font suites, then timed out opening plant details from a typed command after rotation in the 8-inch override. The isolated method and full four-test class both passed unchanged on retry (`evidence/layout8-targeted.txt`, `evidence/layout8-full-repro.txt`), so a production defect was not established. The original failure is retained in `evidence/device/layout-8in-20261030-command-timeout.txt`. The harness now asserts the entered text, dismisses the software keyboard through Espresso before scrolling/tapping, and records a screenshot plus semantic-tree diagnostics on any repeat. IME/layout timing is a hypothesis, not a proven root cause. The final full runner passed all 20 tests: physical HD 10 (8), 7-inch/large-font override (4), 8-inch override (4), and physically disconnected-network HD 10 (4). The overrides are layout checks on the same physical HD 10, not additional verified hardware models. The earlier localized suite passed another 11 tests. Build/JVM/lint passed; 39 supporting Python tests passed. Artifact hashes are in `evidence/release-verification-20261030.json`.

Official policy: [Amazon Appstore privacy labels](https://developer.amazon.com/docs/app-submission/appstore-privacy-labels.html). Test configuration and callback behavior: [Amazon App Tester guide](https://developer.amazon.com/docs/in-app-purchasing/iap-app-tester-user-guide.html). Manifest setup: [Amazon IAP integration](https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html).

The keyboard-dismissal helper requires the explicit test-only `androidx.test.espresso:espresso-core:3.6.1` dependency, aligned with this project's AndroidX Test generation ([official release record](https://developer.android.com/jetpack/androidx/releases/test)). The initial missing-dependency compile failure is retained as `evidence/test-harness-missing-espresso-dependency.log`; the production APK does not include this test library. CI now compiles the instrumentation APK as well as running JVM/lint/Python checks.

## Reproduce

Use a reserved, unlocked test tablet, not customer data. Core native checks: `tools/test_localized_fire.ps1`; broader display/offline checks: `tools/test_fire.ps1`. Capture: `tools/capture_store.ps1`; then `python tools/compose_localized_screenshots.py --captures evidence/store-captures/<run>`. Run Python regression tests with `python -m unittest discover -s tools -p 'test_*.py' -v`.

Billing methods are individually selected with `adb shell am instrument -w -e class com.floratend.app.BillingSandboxTest#<method> com.floratend.app.test/androidx.test.runner.AndroidJUnitRunner`. They require the matching real App Tester configuration described above; do not invoke the whole stateful class blindly. Back up shared tester configuration, merge only the Flora SKU, verify SANDBOX mode, preserve other projects, and restore the environment after testing.
