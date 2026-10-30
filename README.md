# FloraTend

Native Android plant collection, care-check scheduler and growth journal, primarily intended for Amazon Fire tablets. Kotlin, Compose Material 3, Room, DataStore and WorkManager; no required Google Play Services. Product name and package identifier remain provisional pending publisher clearance.

**Development build — release status BLOCKED.** The authoritative executed verification is [RELEASE_READINESS.md](RELEASE_READINESS.md), with individual results in [ACCEPTANCE_TEST_RESULTS.csv](ACCEPTANCE_TEST_RESULTS.csv). Compilation, source implementation and a small set of device tests do not establish production readiness.

## Build and run

Use Java 17 and Android SDK Platform 36. Gradle 8.13 is pinned by the wrapper. Configure `ANDROID_HOME` or an ignored `local.properties` containing `sdk.dir=<your SDK path>`; install appropriate SDK build tools and platform tools. Dependencies require network access on a fresh machine.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:installDebug
adb shell am start -n com.floratend.app/.MainActivity
```

On macOS/Linux use `bash gradlew` in place of `.\gradlew.bat`. The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Instrumented tests require a connected, authorised compatible device or emulator:

```powershell
.\tools\test_localized_fire.ps1
```

Tests can modify the app's local records and preferences; use a dedicated test installation/device profile. No production account or plant data should be used. The app requires Android API 26 or higher, targets API 36, and currently has version `0.1.2` / code `3`. A minimum SDK declaration is not a verified device-compatibility promise. Stateful App Tester and store capture tests require explicit opt-in; do not run the entire instrumentation package blindly.

## Supporting checks

Python 3.10+ is sufficient for supporting checks. Screenshot recomposition additionally requires Pillow and suitable installed fonts.

```powershell
python -m unittest discover -s tools -p "test_*.py" -v
python tools/localize.py extract
python tools/localize.py validate
python website/build.py
python website/validate.py
python tools/report_acceptance.py
```

Localisation validation is intentionally nonzero while required translations or review approvals are missing. Pipeline unit tests using a stub provider do not verify the live translation service. See [localization/README.md](localization/README.md). Reporting reads dated observations from `evidence/acceptance-observations.json`; see [docs/TEST_REPORTING.md](docs/TEST_REPORTING.md). Static site configuration is described in [website/README.md](website/README.md).

## Release signing and Amazon configuration

Keep release signing keys and passwords outside the repository. Set these process environment variables through your private secret manager: `FLORATEND_KEYSTORE` (absolute keystore path), `FLORATEND_STORE_PASSWORD`, `FLORATEND_KEY_ALIAS`, and `FLORATEND_KEY_PASSWORD`. Then run:

```powershell
.\gradlew.bat :app:assembleRelease
```

Release signing is configured from those variables. A missing keystore is a blocked signed-release check; do not relabel an unsigned or debug-signed APK as a submission build. Independently inspect the generated APK's signing certificate with Android `apksigner verify --print-certs` and retain evidence. `tools/build_signed_release.ps1` can create/reuse an app-specific local RSA3072 key, storing its configuration outside the repository with Windows DPAPI CurrentUser protection. It never publishes signing secrets. Back up the signing identity securely; DPAPI configuration is not portable.

Native Amazon Appstore SDK 3.0.9 is integrated. Billing is unavailable until the app-specific public `AppstoreAuthenticationKey.pem` is supplied under `app/src/main/assets/` and the `floratend.premium` entitled product is configured. This public authentication key is distinct from the private signing keystore and any server-side receipt-verification secret. The actual app-specific public key is now bundled; `.gitignore` permits this exact public PEM and excludes private signing material. See [docs/BILLING_INTEGRATION.md](docs/BILLING_INTEGRATION.md) and [docs/AMAZON_REQUIREMENTS.md](docs/AMAZON_REQUIREMENTS.md). No test purchase is simulated as a real entitlement, and this project does not publish automatically.

## Current scope and remaining gates

Implemented source includes local plant CRUD/search/categories, configurable recurring care checks/history, journal notes/measurements/photos, touch/text command entry, push-to-talk and TTS capability handling, a tablet navigation rail, settings, basic summaries, and export code. Refer to test evidence before treating any workflow as verified.

- All 19 requested app locale bundles and voice dictionaries are packaged as assistant-authored drafts. Current resource validation has zero errors. Spanish, German, Japanese and Arabic native UI/actions have executed Fire tests. Real captures cover 19 locales; 120 composed screenshot drafts cover 15 metadata locales, with 88 uploaded across all 11 available Amazon image channels. Human language review and website translations remain pending. All 15 available Amazon description locales are saved as drafts.
- Locale command parsing has JVM and physical-device coverage. Recognition/TTS depend on installed engines and device/language capability; offline recognition is not claimed. This Fire has no speech recognition service and its default TTS supports the three English tags, not the other 16 requested tags.
- Photo import copies downsampled images to private app storage. Camera capture currently uses an external camera preview thumbnail, not full-resolution CameraX capture.
- Optional identification has no configured engine. Cloud sync and encrypted backup are unavailable. JSON export excludes image binaries; CSV exports plant records only. There is no backup-restore import workflow.
- Real Amazon App Tester catalogue/price, purchase, cancellation, restoration, account switching, revocation and failed-refresh cache tests passed. Production/LAT, live product configuration, broader response cases and several planned Premium divisions remain unverified. See [dated verification and limitations](docs/RELEASE_VERIFICATION_20261030.md).
- Reminder delivery uses periodic WorkManager checks, not an exact-time alarm; the recurrence helper's 09:00 calculation does not make delivery exact.
- Room schema version 1 is exported; there is no historical schema upgrade yet. Future schema changes require explicit non-destructive migrations and migration tests.
- Human approval of localized assets, complete tablet/phone and accessibility matrices, privacy/legal review, publisher details and name clearance remain release gates. All 39 privacy-label answers are saved for publisher review; this is not legal approval.

No app submission, website deployment or external publication is authorised by the supporting CI workflow. Original specifications are preserved in [docs/spec/](docs/spec/MASTER_BUILD_PROMPT.md).
