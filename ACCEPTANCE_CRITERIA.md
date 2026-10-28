# FloraTend acceptance criteria

Generated from the user's preserved [quality gates](docs/spec/RELEASE_QUALITY_GATES.md). Each checkbox is one mandatory criterion; locale-wide criteria require all approved locales, not one sample. The [master build specification](docs/spec/MASTER_BUILD_PROMPT.md) also remains authoritative. Statuses measure executed verification, not source-code completeness.

## AC-01 — Android Build and Architecture

Procedure: Run Gradle debug/release builds, unit tests and lint; inspect dependencies and migration tests.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-01.01 | Android Build and Architecture | A complete Android project exists with valid Gradle configuration. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.02 | Android Build and Architecture | The project compiles without errors. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.03 | Android Build and Architecture | A debug APK can be generated. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.04 | Android Build and Architecture | A release build can be generated using the configured signing process. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.05 | Android Build and Architecture | Core modules are separated appropriately. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.06 | Android Build and Architecture | No mandatory Google Play Services dependency exists. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.07 | Android Build and Architecture | No API keys, passwords, or signing secrets are embedded in source code. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.08 | Android Build and Architecture | Database migrations preserve existing user data. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-01.09 | Android Build and Architecture | Application startup and navigation do not crash during the test scenarios. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-02 — Premium UI and Fire Tablet Compatibility

Procedure: Exercise UI on 7/8/10-inch tablet configurations, both orientations, themes and enlarged fonts; retain screenshots.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-02.01 | Premium UI and Fire Tablet Compatibility | App supports portrait and landscape layouts. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.02 | Premium UI and Fire Tablet Compatibility | Layouts adapt to supported 7-inch, 8-inch, and 10-inch Fire tablet screen configurations. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.03 | Premium UI and Fire Tablet Compatibility | Primary touch targets are at least 48dp. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.04 | Premium UI and Fire Tablet Compatibility | Text remains readable at increased system font sizes. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.05 | Premium UI and Fire Tablet Compatibility | Light and dark themes work. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.06 | Premium UI and Fire Tablet Compatibility | Navigation is consistent. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.07 | Premium UI and Fire Tablet Compatibility | Empty, loading, error, and offline states are implemented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.08 | Premium UI and Fire Tablet Compatibility | No production screens contain placeholder buttons or unfinished mockups. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.09 | Premium UI and Fire Tablet Compatibility | Accessibility labels are meaningful. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-02.10 | Premium UI and Fire Tablet Compatibility | Reduced-motion preferences are respected. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-03 — Plant Library

Procedure: Execute create/edit/duplicate/delete/search/filter and restart workflows; verify related records and free/Premium limits.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-03.01 | Plant Library | Users can create plant records. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.02 | Plant Library | Users can edit existing records. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.03 | Plant Library | Users can delete records after confirmation. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.04 | Plant Library | Users can add photographs. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.05 | Plant Library | Users can assign locations and categories. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.06 | Plant Library | Users can search by plant name and category. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.07 | Plant Library | Users can filter and sort plants. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.08 | Plant Library | Records persist after app restart. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.09 | Plant Library | Deleting one plant does not corrupt unrelated records. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.10 | Plant Library | The free-tier plant limit is enforced correctly. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-03.11 | Plant Library | Premium users can exceed the free-tier limit. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-04 — Watering and Care Scheduling

Procedure: Test recurrence, task completion, snooze/pause, persistence, timezone and DST boundaries and notification deduplication.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-04.01 | Watering and Care Scheduling | Users can create watering schedules. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.02 | Watering and Care Scheduling | Users can record completed watering. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.03 | Watering and Care Scheduling | Users can reschedule reminders. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.04 | Watering and Care Scheduling | Users can pause and resume schedules. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.05 | Watering and Care Scheduling | Users can configure fertilising, pruning, and repotting reminders. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.06 | Watering and Care Scheduling | Completed tasks appear in history. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.07 | Watering and Care Scheduling | Next-check dates update correctly. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.08 | Watering and Care Scheduling | Timezone and daylight-saving changes are handled. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.09 | Watering and Care Scheduling | Duplicate reminders are prevented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-04.10 | Watering and Care Scheduling | The app does not automatically claim a plant needs water solely because a fixed interval has elapsed. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-05 — Growth Journal

Procedure: Create/edit/delete dated entries and real photos, compare saved photos, restart offline and verify measurements.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-05.01 | Growth Journal | Users can add dated journal entries. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.02 | Growth Journal | Users can attach photographs. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.03 | Growth Journal | Users can record measurements. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.04 | Growth Journal | Users can edit and delete entries. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.05 | Growth Journal | Journal entries appear in chronological order. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.06 | Growth Journal | Before-and-after comparisons use actual saved photographs. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.07 | Growth Journal | Missing photographs are handled gracefully. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.08 | Growth Journal | Measurements use appropriate regional units. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-05.09 | Growth Journal | Journal data persists offline. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-06 — Voice Input and Output

Procedure: Run mandatory translated commands on recognition-capable hardware and unavailable-service/permission-denied fallbacks; inspect TTS.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-06.01 | Voice Input and Output | Microphone access is requested only when needed. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.02 | Voice Input and Output | Push-to-talk works on devices with supported speech recognition. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.03 | Voice Input and Output | A visible listening indicator appears. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.04 | Voice Input and Output | Recognised speech is displayed for review where appropriate. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.05 | Voice Input and Output | Supported commands trigger real application actions. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.06 | Voice Input and Output | Spoken responses use an available TextToSpeech engine. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.07 | Voice Input and Output | Destructive actions require confirmation. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.08 | Voice Input and Output | Recognition errors are handled. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.09 | Voice Input and Output | Permission denial does not crash the app. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.10 | Voice Input and Output | Missing recognition services trigger a clear fallback. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.11 | Voice Input and Output | Users can perform all essential actions without voice. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.12 | Voice Input and Output | No continuous background listening occurs. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.13 | Voice Input and Output | Raw microphone recordings are not stored by default. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-07 — Worldwide Application Localisation

Procedure: Validate each approved locale's resources, placeholders and plurals; inspect RTL/text expansion and record human review.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-07.01 | Requested interface locale audit | All required interface strings are translated. | 2026-10-28 / Windows / Python 3.10 / Android resource source audit | Validation reports 18 missing target resource files; source-English has no human release approval. No worldwide language completion is claimed. | BLOCKED | evidence/localization-validation.txt; localization/validation-report.json | Translate, validate, render and review every release-approved interface locale. |
| AC-07.02 | Worldwide Application Localisation | No untranslated English strings remain, except protected brand names or approved terminology. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.03 | Worldwide Application Localisation | All placeholders remain valid. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.04 | Worldwide Application Localisation | Plural rules work. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.05 | Worldwide Application Localisation | Text expansion does not break layouts. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.06 | Worldwide Application Localisation | Right-to-left interfaces render correctly where applicable. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.07 | Worldwide Application Localisation | Dates use locale-appropriate formats. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.08 | Worldwide Application Localisation | Numbers and measurements are formatted correctly. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.09 | Worldwide Application Localisation | Notifications are localised. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.10 | Worldwide Application Localisation | Premium paywall copy is localised. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.11 | Worldwide Application Localisation | Accessibility labels are localised. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.12 | Worldwide Application Localisation | Voice prompts are localised. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.13 | Worldwide Application Localisation | Locale-specific command dictionaries exist where voice recognition is supported. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-07.14 | Human linguistic approval audit | Human review is recorded for release-approved translations. | / Not executed | Review registry is empty and all locale review statuses remain pending. | BLOCKED | localization/review.json; localization/validation-report.json | Obtain qualified human review tied to the exact current source hash. |

## AC-08 — Localised Amazon Appstore Screenshots

Procedure: Capture eight implemented screens per approved listing locale/device variant; validate exported dimensions, language, cropping and manifest.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-08.01 | Localised store screenshot prerequisite audit | Eight screenshot compositions are generated. | / Not executed | No locale has completed translation and human approval; eight approved store-locale compositions are not verified. | BLOCKED | LOCALIZATION_TEST_REPORT.md | Complete locale approval, then capture implemented screens and verify eight compositions per approved listing locale. |
| AC-08.02 | Localised Amazon Appstore Screenshots | Each screenshot uses actual implemented application screens. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.03 | Localised Amazon Appstore Screenshots | Visible interface text matches the target locale. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.04 | Localised Amazon Appstore Screenshots | Marketing headlines are translated naturally. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.05 | Localised Amazon Appstore Screenshots | Supporting captions are translated. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.06 | Localised Amazon Appstore Screenshots | Keywords reflect local search terminology. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.07 | Localised Amazon Appstore Screenshots | Text remains readable. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.08 | Localised Amazon Appstore Screenshots | No important content is cropped. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.09 | Localised Amazon Appstore Screenshots | Screenshot dimensions match current Amazon requirements. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.10 | Localised Amazon Appstore Screenshots | Phone and tablet variants exist where required. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.11 | Localised Amazon Appstore Screenshots | Images contain no fabricated reviews or ratings. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.12 | Localised Amazon Appstore Screenshots | Screenshots do not advertise unavailable features. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.13 | Localised Amazon Appstore Screenshots | Exported files exist and pass image validation. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-08.14 | Localised Amazon Appstore Screenshots | Screenshot manifest records the locale, device, feature, and filename. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-09 — Automatic Translation Pipeline

Procedure: Run pipeline tests, process a new source string, validate generated resources, and test configured provider using private credentials.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-09.01 | Android resource extraction and XML round trip | Source strings can be extracted automatically. | 2026-10-28 / Windows / Python 3.10 / development resource tooling | Actual app extraction exited 0. Executed tests verify XML resource round trip and exclusion of nontranslatable text. This verifies tooling extraction, not complete application localisation. | PASS | evidence/localization-extraction.txt; evidence/localization-tests.txt | Rerun extraction after source changes; audit UI literals separately. |
| AC-09.02 | Automatic Translation Pipeline | New strings can be identified. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-09.03 | Live DeepL provider verification | Configured translation-provider integration works when credentials are available. | / Not executed | Live provider credentials are unavailable. Stub-provider tests do not establish live endpoint or language compatibility. | BLOCKED | LOCALIZATION_TEST_REPORT.md | Configure developer-only translation credentials privately, execute a live translation, validate output and obtain review. |
| AC-09.04 | Cached translation request suppression | Translation caching prevents unnecessary repeat requests. | 2026-10-28 / Windows / Python 3.10 / deterministic stub provider | test_translation_cache passed: repeated unchanged input reused one provider call; changed source requested another call. No live provider claim. | PASS | evidence/localization-tests.txt; tools/test_localize.py | Verify live-provider requests separately when credentials are available. |
| AC-09.05 | Placeholder protection and corruption rejection | Placeholders are preserved. | 2026-10-28 / Windows / Python 3.10 / deterministic translation fixtures | Tests passed for repeated positional/named placeholder round trips, missing/duplicated token rejection and mismatched argument detection. | PASS | evidence/localization-tests.txt; tools/test_localize.py | Run the same validator on every real translation before human review. |
| AC-09.06 | Protected brand terminology validation | Protected terminology remains unchanged. | 2026-10-28 / Windows / Python 3.10 / deterministic translation fixtures | Protected FloraTend tokens survive a round trip and a changed brand term is rejected by validation. | PASS | evidence/localization-tests.txt; tools/test_localize.py | Review real translated horticultural terminology as well as protected tokens. |
| AC-09.07 | Missing-resource detection | Missing translations are reported. | 2026-10-28 / Windows / Python 3.10 / current resource tree and fixtures | Missing-string fixture test passed; actual validation reported 18 absent locale resource files and exited nonzero. Detection passed while translation completeness remains blocked. | PASS | evidence/localization-tests.txt; evidence/localization-validation.txt; localization/validation-report.json | Resolve the reported missing resources before release. |
| AC-09.08 | Invalid XML resource and plural rejection | Invalid resources cause validation failures. | 2026-10-28 / Windows / Python 3.10 / invalid-resource fixtures | Executed tests reject duplicate resource names, missing required plural forms, missing other forms and mismatched placeholders. | PASS | evidence/localization-tests.txt; tools/test_localize.py | Keep validation mandatory for generated locale files. |
| AC-09.09 | Pending translation review status reporting | Translation review status is recorded. | 2026-10-28 / Windows / Python 3.10 / current locale report inspection | Regenerated validation report records pending review status for each requested locale. No human approval is inferred from this status-tracking check. | PASS | evidence/localization-validation.txt; localization/validation-report.json; localization/review.json | Obtain and record actual source-hash-bound human approval separately. |
| AC-09.10 | Automatic Translation Pipeline | API credentials remain outside the APK. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-09.11 | Localisation report regeneration | Localisation reports can be regenerated. | 2026-10-28 / Windows / Python 3.10 / development localisation commands | Actual extract and validate commands regenerated source and per-locale validation outputs, including explicit missing-resource and pending-review results. | PASS | evidence/localization-extraction.txt; evidence/localization-validation.txt; localization/validation-report.json | Regenerate after each source or translation update. |

## AC-10 — Offline Functionality

Procedure: Enable airplane mode, force-stop/restart and execute saved plant, care, journal and photo workflows.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-10.01 | Offline Functionality | Users can view saved plants offline. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.02 | Offline Functionality | Users can add and edit plant records offline. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.03 | Offline Functionality | Users can view saved care schedules offline. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.04 | Offline Functionality | Users can complete care tasks offline. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.05 | Offline Functionality | Users can add journal entries offline. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.06 | Offline Functionality | Saved photographs remain accessible offline. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.07 | Offline Functionality | Local data persists after restart. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.08 | Offline Functionality | No account or internet connection is required for core features. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-10.09 | Offline Functionality | Network-dependent features display appropriate messages. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-11 — Plant Identification

Procedure: Select/capture a photo; test consent, actual configured engine, uncertainty, correction, failure and manual entry.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-11.01 | Plant Identification | Users can select or capture a plant photograph. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-11.02 | Plant Identification | Image permission handling works. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-11.03 | Plant Identification | A real identification engine is used when configured. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-11.04 | Plant Identification | Results are presented as possible matches, not guaranteed facts. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-11.05 | Plant Identification | Users can confirm or correct results. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-11.06 | Plant Identification | Identification failures do not cause data loss. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-11.07 | Plant Identification | External image transmission requires appropriate consent. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-11.08 | Plant Identification | Manual plant entry remains available. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-12 — Amazon In-App Purchasing

Procedure: Use Amazon App Tester or Live App Testing for product lookup, purchase, cancel, failure, restore and offline entitlement scenarios.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-12.01 | Amazon In-App Purchasing | Amazon-compatible billing is integrated. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.02 | Amazon configured product lookup prerequisite | Premium product information can be retrieved. | / Not executed | The app-specific public key, configured catalogue and Amazon test environment are not verified. No live product retrieval or store price is recorded. | BLOCKED | docs/BILLING_INTEGRATION.md | Configure the actual Amazon app and entitled product, then execute product lookup and all purchase/restore scenarios. |
| AC-12.03 | Amazon In-App Purchasing | The paywall displays store-provided pricing. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.04 | Amazon In-App Purchasing | Purchase success grants the correct entitlement. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.05 | Amazon In-App Purchasing | Purchase cancellation does not grant Premium. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.06 | Amazon In-App Purchasing | Failed purchases are handled. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.07 | Amazon In-App Purchasing | Purchase restoration works. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.08 | Amazon In-App Purchasing | Entitlements persist appropriately. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.09 | Amazon In-App Purchasing | Free-tier limits are enforced. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.10 | Amazon In-App Purchasing | Premium features unlock correctly. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.11 | Amazon In-App Purchasing | Development-only test billing cannot activate real entitlements in release builds. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.12 | Amazon In-App Purchasing | No Google Play Billing dependency exists in the Amazon build. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-13 — Premium Assets

Procedure: Inspect actual asset files, variants, optimisation and documented original creation/licensing.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-13.01 | Premium Assets | Original app icon exists. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.02 | Premium Assets | Adaptive icon layers exist. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.03 | Premium Assets | Splash artwork exists. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.04 | Premium Assets | Plant category illustrations exist. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.05 | Premium Assets | Empty-state graphics exist. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.06 | Premium Assets | Premium upgrade artwork exists. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.07 | Premium Assets | Marketing screenshot templates exist. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.08 | Premium Assets | Asset licensing is documented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.09 | Premium Assets | Images are optimised for target devices. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-13.10 | Premium Assets | No placeholder or copied competitor assets remain in the release build. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-14 — Amazon SEO / ASO

Procedure: Review each approved listing locale against current official Amazon metadata limits and implemented product behaviour.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-14.01 | Amazon SEO / ASO | App title is prepared. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.02 | Amazon SEO / ASO | Product summary is prepared. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.03 | Amazon SEO / ASO | Full description is prepared. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.04 | Amazon SEO / ASO | Relevant search terminology is researched. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.05 | Amazon SEO / ASO | Screenshot captions are localised. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.06 | Amazon SEO / ASO | Feature claims match implemented functionality. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.07 | Amazon SEO / ASO | Metadata complies with verified Amazon requirements. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.08 | Amazon SEO / ASO | No unsupported keyword fields are invented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.09 | Amazon SEO / ASO | No keyword stuffing is present. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-14.10 | Amazon SEO / ASO | No fabricated reviews, ratings, or awards are included. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-15 — AI Search Optimisation

Procedure: Build website, parse crawlable HTML, validate metadata, sitemap, robots, structured data, locale links and internal links.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-15.01 | Static website generation | Marketing website builds successfully. | 2026-10-28 / Windows / Python 3.10 / local generated website | The actual website build generated all 12 requested English pages and static CSS without external dependencies. This is a local build, not deployment approval. | PASS | evidence/supporting/website-build.log; website/dist/build-report.json | Complete production URL, privacy/publisher details, translations and visual review before publication. |
| AC-15.02 | Static crawlable HTML structure validation | Pages contain crawlable HTML. | 2026-10-28 / Windows / Python 3.10 / HTMLParser structural checks | All 12 pages contain server-readable static HTML headings and content; no JavaScript rendering is required. Local noindex remains intentional until a real domain is configured. | PASS | evidence/supporting/website-validation.log; website/validate.py; website/dist/index.html | Configure and test the real deployment before allowing indexing. |
| AC-15.03 | Page title and description inspection | Page titles and descriptions are meaningful. | 2026-10-28 / Windows / Python 3.10 / generated-page and source content review | All 12 pages have one title, one h1, a language declaration and a page-specific description; reviewed copy describes each page's subject and development status. | PASS | evidence/supporting/website-validation.log; website/build.py; website/dist/features.html | Translate and review metadata for additional approved languages. |
| AC-15.04 | Production canonical URL configuration | Canonical URLs are configured. | / Not executed | No verified deployment SITE_URL is configured. Generator tests prove configured-domain output using a reserved fixture, but no production canonical is invented. | BLOCKED | evidence/supporting/website-build.log; evidence/supporting/tool-tests.log | Set the verified deployed HTTPS origin and run production build and validation. |
| AC-15.05 | Production sitemap completeness | XML sitemap exists. | / Not executed | Valid sitemap.xml exists locally but intentionally contains no production URLs without a verified SITE_URL. A configured-origin fixture passes twelve-URL validation. | BLOCKED | website/dist/sitemap.xml; evidence/supporting/tool-tests.log | Generate a complete sitemap using the actual deployment URL, then verify published URLs. |
| AC-15.06 | Robots file existence and safe local mode | Robots.txt exists. | 2026-10-28 / Windows / Python 3.10 / local output and configured-origin fixture | robots.txt exists. Local mode disallows crawling; configured-origin fixture produces Allow and a sitemap directive. No deployed crawl policy is claimed. | PASS | website/dist/robots.txt; evidence/supporting/tool-tests.log; evidence/supporting/website-validation.log | Review the published robots policy at deployment. |
| AC-15.07 | SoftwareApplication JSON-LD parsing | Appropriate structured data is implemented. | 2026-10-28 / Windows / Python 3.10 / generated static HTML | Validator parsed valid SoftwareApplication JSON-LD on all 12 generated pages; no fabricated reviews, offers or download URLs are included. | PASS | evidence/supporting/website-validation.log; website/dist/index.html | Reconcile structured data with the approved product release and deployed page facts. |
| AC-15.08 | Structured-data visible-fact review | Structured data matches visible content. | 2026-10-28 / Windows / source and generated English HTML inspection | Schema name, Android platform and in-development/not-downloadable description match visible headings and development notices. Schema contains no price, rating, availability or compatibility claim beyond visible content. | PASS | website/build.py; website/dist/index.html; website/dist/fire-tablet-compatibility.html | Repeat review after product facts or release availability change. |
| AC-15.09 | Localised alternate-page prerequisite | Localised pages use appropriate hreflang annotations. | / Not executed | Only source-English pages exist. No hreflang links are fabricated for untranslated pages; source-English alternate URLs require a real domain. | BLOCKED | website/dist/build-report.json; website/README.md | Build reviewed locale pages and configure reciprocal real URLs before approving locale alternate annotations. |
| AC-15.10 | AI Search Optimisation | Product facts are accurate. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-15.11 | Privacy and support page generation | Privacy and support pages exist. | 2026-10-28 / Windows / Python 3.10 / local generated website | Actual privacy.html and support.html files were generated and validated as reachable internal pages. Both disclose development limitations; legal identity and monitored contact remain pending. | PASS | evidence/supporting/website-validation.log; website/dist/privacy.html; website/dist/support.html | Complete publisher, contact and legal review before publication; page existence is not privacy approval. |
| AC-15.12 | Internal link validation and negative fixture | Broken internal links are identified. | 2026-10-28 / Windows / Python 3.10 / local HTML output | Actual 12-page output has zero broken internal links. Negative test removes a linked page and verifies validation fails. | PASS | evidence/supporting/website-validation.log; evidence/supporting/tool-tests.log | Repeat validation against translated and deployed page paths. |
| AC-15.13 | Official Amazon listing link availability | Official Appstore links are added when available. | / Not executed | No verified public FloraTend Appstore listing URL is available; no substitute URL is generated. | BLOCKED | website/dist/build-report.json; website/README.md | After authorised publication provides an actual listing URL, configure APPSTORE_URL and verify the link. |

## AC-16 — Privacy and Security

Procedure: Inspect permissions and network behaviour; exercise deletion/export; scan source and release artifact for secrets and compare disclosures.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-16.01 | Privacy and Security | No account is required for core features. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.02 | Privacy and Security | User records remain local by default. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.03 | Privacy and Security | Microphone permission is requested contextually. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.04 | Privacy and Security | Camera permission is requested contextually. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.05 | Privacy and Security | Plant photographs are not uploaded without consent. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.06 | Privacy and Security | No API secrets are embedded in release artifacts. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.07 | Privacy and Security | Exported data is handled securely. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.08 | Privacy and Security | Data deletion works. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.09 | Privacy and Security | Privacy disclosures match implemented behaviour. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.10 | Privacy and Security | Optional backup is clearly separated from local storage. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-16.11 | Privacy and Security | Third-party data processing is documented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-17 — Accessibility

Procedure: Run screen-reader, enlarged-font, contrast and target-size checks on representative devices, including touch-only operation.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-17.01 | Accessibility | Essential controls are screen-reader accessible. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.02 | Accessibility | Primary touch targets meet the defined minimum. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.03 | Accessibility | Text scales appropriately. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.04 | Accessibility | Colour is not the sole indicator of status. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.05 | Accessibility | High-contrast layouts remain usable. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.06 | Accessibility | Reduced-motion preferences are respected. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.07 | Accessibility | Voice interaction is optional. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.08 | Accessibility | Error messages are understandable. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-17.09 | Accessibility | Navigation works without precise gestures. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-18 — Performance and Reliability

Procedure: Measure startup, memory and scrolling on representative Fire hardware; exercise large photo sets and restart persistence.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-18.01 | Performance and Reliability | Core screens remain responsive during ordinary use. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-18.02 | Performance and Reliability | Plant lists scroll smoothly on tested devices. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-18.03 | Performance and Reliability | Large photo collections do not cause avoidable crashes. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-18.04 | Performance and Reliability | Image loading is memory-conscious. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-18.05 | Performance and Reliability | Background work does not unnecessarily drain battery. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-18.06 | Performance and Reliability | Database operations do not block the main UI thread. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-18.07 | Performance and Reliability | No reproducible critical crashes remain. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-18.08 | Performance and Reliability | App restart preserves user data. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## AC-19 — Release Package

Procedure: Audit signed release artifacts, device support, permissions, store assets/metadata, billing evidence and submission checklist.

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-19.01 | Release Package | Release build completes. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.02 | Release Package | Package identifier is confirmed. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.03 | Release Package | Version code and version name are configured. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.04 | Release Package | Release signing is documented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.05 | Release Package | Required permissions are documented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.06 | Release Package | Store icon is available. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.07 | Release Package | Localised screenshots are available. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.08 | Release Package | Localised metadata is available. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.09 | Release Package | Privacy policy is prepared. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.10 | Release Package | Billing integration is verified. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.11 | Release Package | Supported-device matrix is prepared. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.12 | Release Package | Submission checklist is complete. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-19.13 | Release Package | Outstanding blockers are explicitly documented. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |

## Master specification sections

- 1. YOUR ROLE AND MISSION
- 2. CORE PRODUCT FEATURES
- 3. TECHNICAL ARCHITECTURE
- 4. PREMIUM VISUAL DESIGN
- 5. ORIGINAL PREMIUM ASSETS
- 6. ONBOARDING
- 7. PLANT LIBRARY
- 8. SMART WATERING SCHEDULER
- 9. FERTILISING, PRUNING AND REPOTTING
- 10. PLANT GROWTH JOURNAL
- 11. PLANT HEALTH OBSERVATIONS
- 12. OPTIONAL PHOTO-ASSISTED PLANT IDENTIFICATION
- 13. PREMIUM VOICE INTERACTION
- 14. PLANT CARE CALENDAR
- 15. PREMIUM ANALYTICS
- 16. OFFLINE-FIRST PRIVACY
- 17. PREMIUM MONETISATION
- 18. MANDATORY WORLDWIDE LOCALISATION
- 19. AUTOMATED TRANSLATION PIPELINE
- 20. PREMIUM LOCALISED AMAZON APPSTORE SCREENSHOTS
- 21. AMAZON APPSTORE SEO / ASO
- 22. AI SEARCH OPTIMISATION
- 23. PRIVACY AND SAFETY
- 24. CONCRETE CODEX EXECUTION WORKFLOW
