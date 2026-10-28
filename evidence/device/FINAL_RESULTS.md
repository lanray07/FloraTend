# Fire tablet verification — 28 October 2026

**All 20 device test executions passed.** The tested app source is commit `3374d299627c29649e0a1ba477411c6b7aca7b41`.

| Environment | Tests | Result | Evidence |
|---|---:|---|---|
| Physical Amazon Fire HD 10 (2021), KFTRWI, API 28, en-GB | 8 | PASS | [Device suite](physical-hd10.txt) |
| Same tablet, 600×1024 / 160dpi layout override, font scale 1.3 | 4 | PASS | [Smaller layout and enlarged text](layout-7in-large-font.txt) |
| Same tablet, 800×1280 / 160dpi layout override | 4 | PASS | [8-inch layout configuration](layout-8in.txt) |
| Physical HD 10, Wi-Fi disabled and no active default network | 4 | PASS | [Offline suite](offline-hd10.txt), [network observation](offline-network-state.txt) |

The layout overrides do **not** establish physical Fire 7 or Fire HD 8 compatibility. Portrait, landscape, light/dark rendering, navigation, CRUD, care pause/resume/snooze, typed watering confirmation, journal edits/deletion and missing-photo recovery were exercised. Photo import used an explicitly generated test bitmap, not a fabricated plant-growth photograph.

The two Room tests verify file-backed database close/reopen, related-record integrity, recurrence, editing without cascading deletion, and the transactional free-tier limit. They do not establish a full app process-death/reboot or historical database migration matrix. The notification test ran actual WorkManager jobs, checked one aggregate notification and verified completion updates its count or cancels it.

The device has no Android speech-recognition service. The unavailable-service message, disabled microphone control and typed command fallback passed. The installed TTS engine successfully synthesized the fixed English test phrase; this is not human listening review or proof of recognition compatibility on other devices. Current [speech capability evidence](physical-hd10/speech-engine-test.json) includes all 19 requested TTS locale availability queries.

## Fixes verified

- Correct singular/plural collection text.
- Cancel recognition and speech when the activity stops; avoid asking for microphone access when no recognition service exists.
- Clear text-field focus when executing a command so its response/dialog is reachable.
- Refresh notifications after care changes and serialize notification reads/posts.
- Use a monochrome notification icon.
- Make the navigation rail scrollable and wrap care controls instead of nesting horizontal scrolling inside the care list.
- Await actual database/UI and notification publication in tests, scroll controls into view, and capture after window composition.
- Keep the tablet awake during the matrix and restore its original power setting afterward.

Earlier failed attempts remain alongside this report. They include test synchronization errors, keyboard-obscured controls, nested scrolling, shared-device automation interruptions and an observed screen timeout. They are superseded by the four current passing suite logs, not silently removed.

## Supporting checks and reproducibility

- Debug app and instrumentation builds, eight JVM tests and Android lint passed: [build log](../device-build.log), [JVM output](../jvm/TEST-com.floratend.app.CareEngineTest.xml), [lint: zero errors, 23 warnings](../lint-final.txt).
- All 32 Python tests passed: [output](../python-tests-final.txt).
- All 48 current QA PNG captures passed signature, CRC, image-data and dimension validation: [capture manifest](capture-validation.json). This is not approval of localized marketing screenshots.
- GitHub Actions passed for the tested app-source commit: [run](https://github.com/lanray07/FloraTend/actions/runs/40656346330), [record](../ci-validated-code.json).
- [APK/build/suite hashes](recorded-run.json) bind this evidence to the tested build.

Run `./tools/test_fire.ps1` from PowerShell with an exclusively available, unlocked Fire tablet. The runner builds, installs, executes all four profiles and restores settings in `finally`. Regenerate scoped acceptance observations with `python tools/record_fire_results.py --apply`; it rejects failed, missing or stale results.

Restoration was checked: native 1200×1920 resolution, density 240, font scale 1.0, Wi-Fi on, and plugged-in stay-awake setting 0. See [restored settings](restored-settings.txt). No data from another app was changed.

Overall release verification is **51/204 mandatory criteria (25.00%)**. Submission remains **BLOCKED**, including required translations/human review, approved localized store assets, real Amazon purchase verification and remaining device/accessibility/security coverage. Individual results are in [the acceptance CSV](../../ACCEPTANCE_TEST_RESULTS.csv) and [release readiness](../../RELEASE_READINESS.md).
