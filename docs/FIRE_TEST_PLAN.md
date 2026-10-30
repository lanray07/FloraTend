# Fire device verification and evidence boundaries

The release remains **BLOCKED**. Run `powershell -ExecutionPolicy Bypass -File tools/test_fire.ps1` from the project workspace on a dedicated test device/profile. The runner builds debug and instrumentation APKs, runs JVM tests and lint, installs those APKs, and executes the four suites below. It does not submit, publish or push anything.

The runner creates and removes labelled QA records and changes test preferences, orientation, font scale, display size/density and Wi-Fi state. It restores display overrides, original font scale and Wi-Fi state in `finally`. Existing display overrides cause it to stop rather than overwrite their configuration. Use a dedicated installation without irreplaceable user records; fixture assumptions include a one-plant collection during creation tests.

## Actual hardware versus layout configurations

| Suite log | Environment | Expected tests | Coverage boundary |
|---|---|---:|---|
| `evidence/device/physical-hd10.txt` | Physical Amazon KFTRWI, Fire HD 10 (2021), API 28, en-GB | 8 | Two persistence/policy cases, one real WorkManager notification case, one speech-engine capability case, four Compose UI cases |
| `evidence/device/layout-7in-large-font.txt` | Same physical HD10 with `wm size 600x1024`, density 160, font scale 1.3 | 4 | Display-configuration smoke coverage approximating a smaller layout; not physical 7-inch device verification |
| `evidence/device/layout-8in.txt` | Same physical HD10 with `wm size 800x1280`, density 160, font scale 1.0 | 4 | Display-configuration smoke coverage; not physical 8-inch device verification |
| `evidence/device/offline-hd10.txt` | Physical HD10 at reset display settings with Wi-Fi disabled and no active default network verified | 4 | Core Compose workflows without an active default network; not a complete airplane-mode, process-kill/restart or saved-photo matrix |

Each run must have an exact successful test count, no failure/skip/abort evidence, and logs newer than the completed build. A successful command exit alone is insufficient: Android instrumentation can report failed tests with shell exit zero. Final suite logs must be read. `evidence/device/offline-network-state.txt` records the actual connectivity observation used for the offline run.

## Test-to-requirement mapping

`PersistenceTest.offlineCrudRecurrenceAndCascadePreserveOtherPlants` creates two plants plus schedule/history/journal rows, closes and reopens a file-backed database, verifies saved values, edits a plant and deletes one plant without removing the unrelated plant. This supports persistence, recurrence and cascade integrity for the tested storage operations; it does not prove a historical migration or every process-death scenario.

`PersistenceTest.freeLimitAndPremiumPolicyAreTransactional` verifies the storage policy accepts five free records, rejects the sixth, accepts a Premium-policy insert and permits editing an existing record. The Premium input is a test parameter; this is not a real store purchase or verified entitlement-unlock flow.

`UiSmokeTest.createPlantAndNavigateAllCoreScreens` creates and edits a plant, exercises a search match/no-match, creates a default soil-check schedule and a journal note, submits a typed English reminder command, visits the main surfaces and verifies an unavailable Amazon purchase button is disabled. It does not exercise all plant filters/categories, all schedule types or every Premium division.

`UiSmokeTest.careActionsJournalEditsAndVoiceConfirmation` pauses/resumes a schedule, snoozes it and submits rescheduling, confirms a typed English watering-record command, awaits the actual history and next-date changes, then edits/deletes a journal note and confirms plant deletion. A typed command does not verify speech recognition. The current test directly awaits snooze and completion state; reschedule submission alone is not mapped to a complete rescheduling acceptance pass.

`UiSmokeTest.rotationDarkThemeAndRecognitionFallback` rotates portrait/landscape, switches the dark preference, captures screenshots, probes speech availability and verifies the unavailable-recognizer text/touch path when applicable. It asserts microphone permission remains denied and a typed Add command opens the actual editor. Capturing a dark screenshot is not a contrast audit or a visual accessibility pass. It does not test an explicit denied microphone permission dialog on a recognition-capable device.

`UiSmokeTest.privatePhotoImportAndMissingImageFallback` generates a deterministic image fixture, imports it through `Photos.import`, checks private storage and bounded dimensions, deletes the saved file and verifies the missing-photo UI. This tests the import function and fallback, not the external Android picker, camera provider, permission handling, persistent photo timeline or before/after comparison.

`ReminderWorkerTest.workerUsesOneNotificationAndUpdatesAfterCompletion` runs actual WorkManager jobs, checks one aggregate notification, waits for the published count and verifies completion updates/cancels it. It assumes notification permission/settings permit notifications; skipped assumptions must not be counted as a passed notification acceptance test. Immediate refresh after care edits and serialized notification reads/posts are the current implementation under verification. Exact-time background delivery, battery impact, reboot and Android 13 permission handling remain separate tests.

## Speech capability evidence already observed

The retained `evidence/device/first-extended/speech-engine-test.json` and `voice-capabilities.json` are observations from an earlier extended attempt, whose overall suite failed. They show no installed SpeechRecognizer service (`recognitionAvailable=false`), microphone permission denied (`-1`), and en-GB device locale. They are not evidence that the complete final run passed.

The real default TTS engine `com.ivona.tts.oem` initialized successfully and synthesized the fixed English test phrase to a 94,390-byte temporary audio file. The test reported successful synthesis and then deleted that temporary file. No recorded user speech was stored, and no claim of audible human listening review is made.

The engine was queried for all 19 requested locale tags: en-GB returned availability code 1; en-US and en-AU returned code 0 (language-level availability); the other 16 returned -2 (unsupported). These are installed-engine capability responses, not approved translations, tested accents, guaranteed regional voices or voice-command-language support. The final passing physical run, if obtained, must contain its own current capability reports under `evidence/device/physical-hd10/`.

Recognition on a speech-capable device, visible live listening/transcription, explicit permission denial/recovery, recognition error paths, all five spoken commands and translated command dictionaries remain unverified. Touch/text fallback and real TTS capability do not justify a universal voice compatibility claim. Offline recognition is not advertised.

## Failure retention and guarded result recording

The first extended attempt's two timing failures are preserved in `evidence/device/physical-hd10-first-extended-failures.txt`; screenshots/capabilities from that attempt are retained under `first-extended/`. A later attempt also reported ActivityScenario lifecycle failures; preserve its log separately before replacing the canonical final log. A passing rerun may supersede those defects only after the relevant fixes and affected tests are actually verified.

`python tools/record_fire_results.py` is read-only and validates evidence freshness, counts, expected classes, build success and offline state. `--apply` merges only the scoped mapped observations and regenerates the acceptance reports. Run it only after reviewing all four current successful suite logs. It refuses incomplete/failed/stale evidence. It does not approve full accessibility, localised screenshots, real purchasing, photo-picker workflows, universal voice support, complete Fire device coverage or release readiness.

Screenshot capture remains QA evidence. Eight marketing compositions in each approved store locale, phone variants, typography/cropping review and an approved screenshot manifest are still required. Physical 7/8-inch models, enlarged-font readability review, screen readers, contrast, target-size measurements, RTL/translated UI, device restart, sustained performance and full privacy/security checks remain incomplete unless separately recorded.

## Localized suite

Run `tools/test_localized_fire.ps1` with the physical tablet unlocked and reserved. It builds, checks lint/JVM tests and executes 11 native cases, including 19-locale resource/dictionary loading and representative es-ES/de-DE/ja-JP/ar UI actions/screenshots. `-SkipBuild` reuses already verified APKs. The runner keeps the plugged-in device awake temporarily and restores the original setting in `finally`. It stops when the live keyguard monitor/input restriction reports a lock (the Fire OS delegate showing flag can be stale after unlock); unlock manually rather than treating a missing Compose hierarchy as a product failure or bypassing the PIN. Previous instrumentation output is retained before a rerun. Human language review remains a separate release gate.
