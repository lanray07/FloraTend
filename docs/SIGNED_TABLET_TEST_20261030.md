# Direct signed release test — 30 October 2026

The connected physical Fire HD10 (KFTRWI, Android API 28) now has **0.1.4 / code 5** installed. This was a direct USB APK install, not Amazon LAT delivery. The installed APK was pulled back and its SHA-256 matched the submitted candidate exactly: `155fe9912c7060b4dd27dc46185c4f11522c05c79907d2774482914e3acfe35f`. The app is non-debuggable and uses the original release certificate.

## Executed result

**15 / 15 signed-release instrumentation tests passed in 57.375 seconds.** A separately signed release test harness targeted the original APK; no success callbacks or purchase receipts were injected into the actual SDK flow. Tests include:

- Actual plant entry/navigation, care actions, journal edits, photo import and missing-image fallback, rotation/dark theme, typed voice commands and unavailable-recognition fallback.
- Room CRUD/cascade/free-limit rules (in-memory database fixtures), actual WorkManager notification behavior and installed speech capability inspection.
- Actual delete-all confirmation, four record tables emptied and imported fixture photograph removed.
- Controlled startup/cache persistence, interruption and owner replacement fixtures. These are policy/recovery tests, not purchases.
- Real signed client restore and price availability with sandbox mode disabled.

Amazon SDK mode was **PRODUCTION**. Actual catalogue availability was **false**, price was **null** and Premium was **false**. The UI showed a disabled “Amazon product unavailable” purchase button and the store-configuration message. No purchase was attempted or charged. A sideload has not established LAT ownership or successful production billing. The Amazon test still shows **Test submitted**, with tester management disabled; the previously observed enrollment error and unverified invitation remain open.

The device has no speech recognizer. Its installed TTS engine successfully synthesized English audio (94,390 bytes); only the three English locale tags reported available. Other requested speech languages remain unavailable on this tablet.

## Installation and cleanup

Android rejected an in-place debug-to-release update because certificates differ. Before replacing the empty debug test installation, all four app record tables were verified empty and a validated private data archive was saved outside Git. No plant records were discarded. The exact signed candidate was then installed and tested. The test harness was removed afterward; the signed app remains installed and launched. Display overrides are absent, sandbox mode is `none` and keep-awake setting was restored to `3`.

Signing keys, original debug-data archive and tester email remain outside the public repository. No private receipt/account identifiers are in this evidence package.

## Reproduction

Use a dedicated disposable test profile: core UI tests write and clean QA fixtures, and the privacy test refuses a nonempty collection/photo directory. Do not run this against personal plant records. Preserve the submitted APK in a separate local path before building the instrumentation variant, since Gradle can repackage its generated release output under the test configuration.

```powershell
.\tools\build_signed_release.ps1 -BuildDeviceTests
.\tools\test_signed_fire.ps1 -SkipBuild -CandidateApk 'C:\private\submitted-code5.apk'
```

The runner refuses to automatically uninstall the app for a signature mismatch. Signing variables and the existing Windows DPAPI configuration are handled by the signing helper. `floratend.testBuildType=release` is opt-in; ordinary CI/debug tests retain their default configuration.

Evidence: `evidence/direct-release/native-tests.txt`, `verification.json`, `store-probe.json`, `signature.txt`, `privacy-deletion.json`, `screens/speech-engine-test.json` and the captured native screens. The initial unquoted PowerShell property-argument build failure is retained separately; it is not counted as test evidence. The successful signed harness build is `instrumentation-build.log`.
