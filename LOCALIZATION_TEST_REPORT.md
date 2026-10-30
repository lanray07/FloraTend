# Localisation verification — 2026-10-30

All 19 requested Android locale bundles now contain the current 168 translatable resources. Resource validation reports zero missing entries, format-placeholder errors or plural-category errors. These are assistant-authored drafts; **zero locales have human release approval**. The validation command therefore intentionally exits 1 and AC-07 remains BLOCKED.

| Executed check | Result | Evidence |
|---|---|---|
| Current XML extraction and resource validation | 168 entries, 19 locale bundles, zero resource errors; human review pending | `evidence/localization-extraction.txt`, `localization/validation-report.json` |
| Python supporting tools | 36 tests passed | `evidence/localization-supporting-tests.txt` |
| Android debug build, lint and JVM tests | Build successful; 11 JVM tests passed | `evidence/api36-build.log`, `app/src/test/java/com/floratend/app/LocalizedVoiceTest.kt` |
| Packaged voice dictionaries | All 19 Properties dictionaries tested against add, due, water, calendar, read and location commands; Unicode and unknown-command checks passed | `LocalizedVoiceTest.kt`, `localization/voice/` |
| Physical Fire resource/dictionary loading | All 19 bundles loaded; Arabic RTL configuration and plural samples checked | `evidence/localized-device/screens/resource-and-dictionary-checks.json` |
| Representative translated screens/actions | PASS on the unlocked physical Fire: locale recreation, Garden/Voice screenshots and five typed command actions for es-ES, de-DE, ja-JP and ar. All 11 native cases passed after a fresh instrumentation retry | `evidence/localized-device/instrumentation.txt`, `evidence/api36-verification.json` |
| Actual microphone recognition | Not available on this physical Fire; no compatible SpeechRecognizer service | `evidence/device/physical-hd10/speech-engine-test.json` |
| Human language review and approved store screenshot sets | Not executed | `localization/review.json` |

The language picker saves an explicit choice across recreation and can follow the device language. UI, notification, billing and error strings use the selected resources. Voice commands use packaged locale dictionaries; touch/text fallback remains available, and unsupported TTS is disclosed. Japanese, Hindi and Turkish suffix syntax and Unicode punctuation have parser tests. These tests do not establish natural speech accuracy or linguistic quality.

The 15 supported Amazon listing locales have complete draft text, saved and read back from the console (`submission/listing-console-readback.json`). Store locale availability differs from the 19 requested app locales. No separate Arabic, Hindi, Turkish, Indonesian, Canadian French or es-419 listing field is claimed.

Remaining release work: qualified review tied to the exact source hash, all required locale/screen captures, expansion/accessibility inspection, and recognition/TTS tests on engines that support each approved language. A live translation provider was not called: `DEEPL_AUTH_KEY` remains unconfigured. Draft-generation provenance is retained under `localization/drafts/`.

The first API 36 run failed because the device UI Automation service was already registered. Its failure is preserved in `evidence/localized-device/api36-automation-failure.txt`; the fresh retry passed all 11 tests. Earlier 2026-10-28 lock-screen failures are retained in archived instrumentation logs. The runner now reads the live Fire OS keyguard state because its delegate can retain a stale showing flag after unlock.
