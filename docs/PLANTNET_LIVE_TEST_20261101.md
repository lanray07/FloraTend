# Pl@ntNet live and native verification — 1 November 2026

Development0.1.12/code13 was tested in the isolated `com.floratend.app.qa` package on the physical FireHD10/API28. The actual Amazon-installed app remains0.1.10/code11, nondebug, installer `com.amazon.venezia`. No Amazon submission, main-package replacement, uninstall or data clear occurred.

## Reproduced defect and fix

Credentialed metadata requests returned an identify allowance of500 and valid language tags including `sat`, `zh-hant`, `zh-tw`, `pt-br` and `en-au`. The original language-list validator rejected these, causing the handler to return503 before forwarding an image. A local regression based on that observed metadata went red (`503 !== 200`). This was not a failed live photo request: the contract defect was found and fixed before the first photo upload.

The validator now accepts bounded language/script/region tags while retaining fixed upstream origins, strict request-language validation, response limits, no redirects, quota checks and secret redaction. All21 pure identification tests pass, plus the real local SQLite budget test and six unchanged receipt tests:28server tests total. The fixed Worker was deployed using the existing Cloudflare setup with Free-compatible SQLite configuration, disabled request observability, cap480/day and5/IP/day. No paid plan or upgrade was selected. The account billing-subscription API returned403; this run does not independently certify account billing status. Provider quota500 and remaining499 were directly observed.

## Genuine request and actual UI

One real photo request passed through the deployed proxy and the production `ServerPlantIdentifier`, with the endpoint supplied through BuildConfig. The QA decorator counted and delegated the real transport; it supplied no identification response. The application's normal default endpoint remains empty pending publisher/privacy and release configuration review.

The selected test photograph is Martin Vorel's [English Daisy](https://libreshot.com/english-daisy/), published underCC0. Its source label is *Bellis perennis*. The original download is private; source URL, license, dimensions and SHA256 are retained in `photo-provenance.json`. Production `Photos.import` normalized it into app-private JPEG storage before the request.

- The disclosure and selected photo were visible. Cancel produced zero client calls.
- Sendphoto produced exactly one actual request. An orientation change did not produce a second request or lose the editor/result.
- The real result's top suggestion was *Bellis perennis*, score0.59052. The UI showed59% as a **match score**, alternatives and the warning that suggestions can be wrong.
- The database species remained unchanged before explicit selection/review/save. The selected species was manually corrected, then saved. The existing note and photograph were preserved.
- The provider identify quota changed500 to499. Metadata queries and the later offline cases did not consume another identification.

This establishes one genuine integration sample matching the publisher's label. It is not an independent botanical accuracy benchmark, a calibrated probability claim, a test of every provider language, or Amazon-signed code13 instrumentation. Raw provider responses, account partition headers and credentials were not published.

## Offline and process boundaries

After the genuine test, QA was force-stopped and Wi-Fi disabled. Android reported **no active default network**. A separate instrumentation invocation confirmed that the corrected species, note and photograph persisted and were visible in the actual detail UI.

A second controlled record exercised the real transport with no network. The UI displayed the localized unavailable state. Edited manual species and note remained intact and could be saved; the photo remained attached. Another force-stop and separate invocation verified all three persisted. No synthetic response supplied the offline failure.

Seven functional native tests passed: three initial contracts (synthetic consent/correction, actual private-file boundaries and Android-platform JSON), one genuine request/review/rotation case, and three offline/failure/restart cases. Each live/offline method ran separately, so the process-restart evidence is not inferred from ordinary recomposition. Two additional backup/cleanup tests passed.

## Preservation and cleanup

The two controlled QA rows and both linked photos were backed up before removal. On-device SQLite checks and independent host read-only SQLite integrity/count/ID checks passed; every transferred photo hash matched. The backup remains privately outsideGit. Only after that verification did the scoped cleanup remove the two known QA records and verify all four domain tables empty. The QA and QA-test packages were then uninstalled.

Wi-Fi is on, rotation is accelerometer1/user0, no instrumentation or QA package remains, and the original Amazon app was reopened. Its APK SHA256 remains `fb765d78dfef3f7ea67482d456e44e90a8f1d086bd640c34a25d16b359dfbf86`, exactly matching the prior Amazon11 artifact. Main records/preferences were not modified. Original library sorting was not changed.

The locally supplied text key was never staged. It was encrypted into the CurrentUserDPAPI configuration, and its original bytes were separately encrypted and round-trip verified before removing the plaintext workspace file. Ignore rules now exclude that input filename and DPAPI blobs. Keys stay outsideGit/APKs; only server secrets were configured.

## Reproduction and remaining scope

Use only an isolated QA installation; preserve any existing data before cleanup. Quote complete Gradle properties in PowerShell:

```powershell
.\gradlew.bat --no-daemon '-Pfloratend.testBuildType=qa' '-Pfloratend.plantIdentifierUrl=https://floratend-plant-identification.lanraybanks.workers.dev/identify' :app:assembleQa :app:assembleQaAndroidTest
```

Push the licensed fixture to the QA external-files `identification-fixtures/daisy.jpg` path. Native methods in `LiveIdentificationTest` require `-e floratend.liveIdentification true`; invoke the genuine method once with Wi-Fi on, then the saved-choice restart, offline-failure and offline-manual-restart methods in separate invocations with an actual process stop and verified no-network state. Genuine requests use the real daily allowance. Cleanup requires the separate verified-backup flag and exact known-record guards; never run a stateful whole-package test blindly.

The acceptance ledger records178/204 scoped passes,26 incomplete. Human language approval, broader layout/device/provider coverage, publisher/privacy review, signed-candidate configuration, production billing edges and the retained natural-install rendering failure remain separate gates. No public release readiness is claimed.
