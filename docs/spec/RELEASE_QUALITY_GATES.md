# 25. MANDATORY ACCEPTANCE CRITERIA & RELEASE QUALITY GATES

## PRIMARY INSTRUCTION

FloraTend must not be declared production-ready simply because the application compiles or its screens look complete.

Every major feature must satisfy explicit, measurable acceptance criteria.

For every criterion, record:

- Requirement ID
- Expected behaviour
- Test procedure
- Actual result
- Evidence
- Status: PASS, FAIL, BLOCKED, or NOT TESTED
- Remaining corrective action

A requirement is considered complete only when the relevant test has been executed and its result verified.

Never mark untested functionality as passed.

## AC-01 — Android Build and Architecture

**Acceptance criteria**

- [ ] A complete Android project exists with valid Gradle configuration.
- [ ] The project compiles without errors.
- [ ] A debug APK can be generated.
- [ ] A release build can be generated using the configured signing process.
- [ ] Core modules are separated appropriately.
- [ ] No mandatory Google Play Services dependency exists.
- [ ] No API keys, passwords, or signing secrets are embedded in source code.
- [ ] Database migrations preserve existing user data.
- [ ] Application startup and navigation do not crash during the test scenarios.

**Verification**

Execute the available Gradle build, unit-test, lint, and release tasks.

Record command output and artifact paths.

**Pass condition:** All mandatory build checks succeed.

## AC-02 — Premium UI and Fire Tablet Compatibility

**Acceptance criteria**

- [ ] App supports portrait and landscape layouts.
- [ ] Layouts adapt to supported 7-inch, 8-inch, and 10-inch Fire tablet screen configurations.
- [ ] Primary touch targets are at least 48dp.
- [ ] Text remains readable at increased system font sizes.
- [ ] Light and dark themes work.
- [ ] Navigation is consistent.
- [ ] Empty, loading, error, and offline states are implemented.
- [ ] No production screens contain placeholder buttons or unfinished mockups.
- [ ] Accessibility labels are meaningful.
- [ ] Reduced-motion preferences are respected.

**Verification**

Run UI tests and inspect representative device screenshots.

Perform physical Fire tablet testing where hardware is available.

**Pass condition:** Required layouts and interactions work without critical clipping, inaccessible controls, or navigation failures.

## AC-03 — Plant Library

**Acceptance criteria**

- [ ] Users can create plant records.
- [ ] Users can edit existing records.
- [ ] Users can delete records after confirmation.
- [ ] Users can add photographs.
- [ ] Users can assign locations and categories.
- [ ] Users can search by plant name and category.
- [ ] Users can filter and sort plants.
- [ ] Records persist after app restart.
- [ ] Deleting one plant does not corrupt unrelated records.
- [ ] The free-tier plant limit is enforced correctly.
- [ ] Premium users can exceed the free-tier limit.

**Verification**

Execute CRUD tests, persistence tests, and entitlement tests.

**Pass condition:** All plant-management operations behave correctly and preserve data integrity.

## AC-04 — Watering and Care Scheduling

**Acceptance criteria**

- [ ] Users can create watering schedules.
- [ ] Users can record completed watering.
- [ ] Users can reschedule reminders.
- [ ] Users can pause and resume schedules.
- [ ] Users can configure fertilising, pruning, and repotting reminders.
- [ ] Completed tasks appear in history.
- [ ] Next-check dates update correctly.
- [ ] Timezone and daylight-saving changes are handled.
- [ ] Duplicate reminders are prevented.
- [ ] The app does not automatically claim a plant needs water solely because a fixed interval has elapsed.

**Verification**

Run recurrence, date-calculation, notification, and persistence tests.

**Pass condition:** Care scheduling produces correct records and appropriately scheduled reminders under tested conditions.

## AC-05 — Growth Journal

**Acceptance criteria**

- [ ] Users can add dated journal entries.
- [ ] Users can attach photographs.
- [ ] Users can record measurements.
- [ ] Users can edit and delete entries.
- [ ] Journal entries appear in chronological order.
- [ ] Before-and-after comparisons use actual saved photographs.
- [ ] Missing photographs are handled gracefully.
- [ ] Measurements use appropriate regional units.
- [ ] Journal data persists offline.

**Pass condition:** Growth journal functions work without data loss or fabricated measurements.

## AC-06 — Voice Input and Output

**Voice functionality is mandatory, subject to device capability.**

**Acceptance criteria**

- [ ] Microphone access is requested only when needed.
- [ ] Push-to-talk works on devices with supported speech recognition.
- [ ] A visible listening indicator appears.
- [ ] Recognised speech is displayed for review where appropriate.
- [ ] Supported commands trigger real application actions.
- [ ] Spoken responses use an available TextToSpeech engine.
- [ ] Destructive actions require confirmation.
- [ ] Recognition errors are handled.
- [ ] Permission denial does not crash the app.
- [ ] Missing recognition services trigger a clear fallback.
- [ ] Users can perform all essential actions without voice.
- [ ] No continuous background listening occurs.
- [ ] Raw microphone recordings are not stored by default.

### Mandatory voice test scenarios

Test commands including:

1. "Add a new plant."
2. "Which plants need watering today?"
3. "Mark my Monstera as watered."
4. "Show my care calendar."
5. "Read my reminders."

For each supported voice locale, validate the corresponding translated commands.

**Pass condition:** Supported commands execute correctly on tested speech-capable devices, and unsupported devices retain fully functional touch controls.

Do not mark voice recognition as universally compatible based on Android emulator tests alone.

## AC-07 — Worldwide Application Localisation

**This is a critical release gate.**

For every approved application locale:

- [ ] All required interface strings are translated.
- [ ] No untranslated English strings remain, except protected brand names or approved terminology.
- [ ] All placeholders remain valid.
- [ ] Plural rules work.
- [ ] Text expansion does not break layouts.
- [ ] Right-to-left interfaces render correctly where applicable.
- [ ] Dates use locale-appropriate formats.
- [ ] Numbers and measurements are formatted correctly.
- [ ] Notifications are localised.
- [ ] Premium paywall copy is localised.
- [ ] Accessibility labels are localised.
- [ ] Voice prompts are localised.
- [ ] Locale-specific command dictionaries exist where voice recognition is supported.
- [ ] Human review is recorded for release-approved translations.

### Localisation coverage

Use the approved target-locale matrix from Section 18.

Do not assume that all Amazon distribution countries support identical listing languages.

**Pass condition:** Every locale marked release-approved has passed automated validation, UI testing, and required human review.

A locale with missing translations must remain pending.

## AC-08 — Localised Amazon Appstore Screenshots

**This is a critical release gate.**

For every approved store-listing locale:

- [ ] Eight screenshot compositions are generated.
- [ ] Each screenshot uses actual implemented application screens.
- [ ] Visible interface text matches the target locale.
- [ ] Marketing headlines are translated naturally.
- [ ] Supporting captions are translated.
- [ ] Keywords reflect local search terminology.
- [ ] Text remains readable.
- [ ] No important content is cropped.
- [ ] Screenshot dimensions match current Amazon requirements.
- [ ] Phone and tablet variants exist where required.
- [ ] Images contain no fabricated reviews or ratings.
- [ ] Screenshots do not advertise unavailable features.
- [ ] Exported files exist and pass image validation.
- [ ] Screenshot manifest records the locale, device, feature, and filename.

**Verification**

Run screenshot automation and inspect exported images.

Use a combination of automated image validation and manual visual review.

**Pass condition:** Every required screenshot exists, uses the correct language, represents real functionality, and meets the applicable store specifications.

Translated headlines over English-language app screens are an automatic failure.

## AC-09 — Automatic Translation Pipeline

**Acceptance criteria**

- [ ] Source strings can be extracted automatically.
- [ ] New strings can be identified.
- [ ] Configured translation-provider integration works when credentials are available.
- [ ] Translation caching prevents unnecessary repeat requests.
- [ ] Placeholders are preserved.
- [ ] Protected terminology remains unchanged.
- [ ] Missing translations are reported.
- [ ] Invalid resources cause validation failures.
- [ ] Translation review status is recorded.
- [ ] API credentials remain outside the APK.
- [ ] Localisation reports can be regenerated.

**Pass condition:** A new source string can be processed through the translation pipeline, validated, and prepared for human review without manually editing every locale.

If translation credentials are unavailable, mark provider-dependent tests BLOCKED.

## AC-10 — Offline Functionality

**Acceptance criteria**

- [ ] Users can view saved plants offline.
- [ ] Users can add and edit plant records offline.
- [ ] Users can view saved care schedules offline.
- [ ] Users can complete care tasks offline.
- [ ] Users can add journal entries offline.
- [ ] Saved photographs remain accessible offline.
- [ ] Local data persists after restart.
- [ ] No account or internet connection is required for core features.
- [ ] Network-dependent features display appropriate messages.

**Verification**

Enable airplane mode, restart the application, and execute core workflows.

**Pass condition:** All core plant-management workflows remain usable without network access.

## AC-11 — Plant Identification

**Acceptance criteria**

- [ ] Users can select or capture a plant photograph.
- [ ] Image permission handling works.
- [ ] A real identification engine is used when configured.
- [ ] Results are presented as possible matches, not guaranteed facts.
- [ ] Users can confirm or correct results.
- [ ] Identification failures do not cause data loss.
- [ ] External image transmission requires appropriate consent.
- [ ] Manual plant entry remains available.

**Pass condition:** Identification works with a tested engine or is clearly marked unavailable without falsely advertising the feature.

An unconfigured identification adapter is not a completed identification feature.

## AC-12 — Amazon In-App Purchasing

**Acceptance criteria**

- [ ] Amazon-compatible billing is integrated.
- [ ] Premium product information can be retrieved.
- [ ] The paywall displays store-provided pricing.
- [ ] Purchase success grants the correct entitlement.
- [ ] Purchase cancellation does not grant Premium.
- [ ] Failed purchases are handled.
- [ ] Purchase restoration works.
- [ ] Entitlements persist appropriately.
- [ ] Free-tier limits are enforced.
- [ ] Premium features unlock correctly.
- [ ] Development-only test billing cannot activate real entitlements in release builds.
- [ ] No Google Play Billing dependency exists in the Amazon build.

**Pass condition:** Required purchase scenarios pass using an appropriate Amazon test environment.

If store configuration or credentials are missing, mark live billing verification BLOCKED.

## AC-13 — Premium Assets

**Acceptance criteria**

- [ ] Original app icon exists.
- [ ] Adaptive icon layers exist.
- [ ] Splash artwork exists.
- [ ] Plant category illustrations exist.
- [ ] Empty-state graphics exist.
- [ ] Premium upgrade artwork exists.
- [ ] Marketing screenshot templates exist.
- [ ] Asset licensing is documented.
- [ ] Images are optimised for target devices.
- [ ] No placeholder or copied competitor assets remain in the release build.

**Pass condition:** Required production assets exist, are visually consistent, and have documented usage rights.

## AC-14 — Amazon SEO / ASO

For each approved listing locale:

- [ ] App title is prepared.
- [ ] Product summary is prepared.
- [ ] Full description is prepared.
- [ ] Relevant search terminology is researched.
- [ ] Screenshot captions are localised.
- [ ] Feature claims match implemented functionality.
- [ ] Metadata complies with verified Amazon requirements.
- [ ] No unsupported keyword fields are invented.
- [ ] No keyword stuffing is present.
- [ ] No fabricated reviews, ratings, or awards are included.

**Pass condition:** Every release-approved locale has a complete, accurate, validated store-listing package.

## AC-15 — AI Search Optimisation

**Acceptance criteria**

- [ ] Marketing website builds successfully.
- [ ] Pages contain crawlable HTML.
- [ ] Page titles and descriptions are meaningful.
- [ ] Canonical URLs are configured.
- [ ] XML sitemap exists.
- [ ] Robots.txt exists.
- [ ] Appropriate structured data is implemented.
- [ ] Structured data matches visible content.
- [ ] Localised pages use appropriate hreflang annotations.
- [ ] Product facts are accurate.
- [ ] Privacy and support pages exist.
- [ ] Broken internal links are identified.
- [ ] Official Appstore links are added when available.

**Pass condition:** The website passes available technical SEO checks and contains accurate, accessible product information.

Do not claim guaranteed AI search visibility or rankings.

## AC-16 — Privacy and Security

**Acceptance criteria**

- [ ] No account is required for core features.
- [ ] User records remain local by default.
- [ ] Microphone permission is requested contextually.
- [ ] Camera permission is requested contextually.
- [ ] Plant photographs are not uploaded without consent.
- [ ] No API secrets are embedded in release artifacts.
- [ ] Exported data is handled securely.
- [ ] Data deletion works.
- [ ] Privacy disclosures match implemented behaviour.
- [ ] Optional backup is clearly separated from local storage.
- [ ] Third-party data processing is documented.

**Pass condition:** Privacy and security tests pass, with no unresolved critical findings.

## AC-17 — Accessibility

**Acceptance criteria**

- [ ] Essential controls are screen-reader accessible.
- [ ] Primary touch targets meet the defined minimum.
- [ ] Text scales appropriately.
- [ ] Colour is not the sole indicator of status.
- [ ] High-contrast layouts remain usable.
- [ ] Reduced-motion preferences are respected.
- [ ] Voice interaction is optional.
- [ ] Error messages are understandable.
- [ ] Navigation works without precise gestures.

**Pass condition:** Required accessibility checks pass on representative device configurations.

## AC-18 — Performance and Reliability

**Acceptance criteria**

- [ ] Core screens remain responsive during ordinary use.
- [ ] Plant lists scroll smoothly on tested devices.
- [ ] Large photo collections do not cause avoidable crashes.
- [ ] Image loading is memory-conscious.
- [ ] Background work does not unnecessarily drain battery.
- [ ] Database operations do not block the main UI thread.
- [ ] No reproducible critical crashes remain.
- [ ] App restart preserves user data.

Measure startup time, memory usage, and responsiveness on available devices.

Set final numerical performance budgets after establishing a baseline on representative Fire hardware.

Do not invent performance measurements.

## AC-19 — Release Package

**Acceptance criteria**

- [ ] Release build completes.
- [ ] Package identifier is confirmed.
- [ ] Version code and version name are configured.
- [ ] Release signing is documented.
- [ ] Required permissions are documented.
- [ ] Store icon is available.
- [ ] Localised screenshots are available.
- [ ] Localised metadata is available.
- [ ] Privacy policy is prepared.
- [ ] Billing integration is verified.
- [ ] Supported-device matrix is prepared.
- [ ] Submission checklist is complete.
- [ ] Outstanding blockers are explicitly documented.

**Pass condition:** All mandatory release requirements pass before the application is declared ready for submission.

Do not publish without explicit authorisation.

---

# 26. AUTOMATED ACCEPTANCE TEST REPORTING

Create:

- `ACCEPTANCE_CRITERIA.md`
- `ACCEPTANCE_TEST_RESULTS.csv`
- `LOCALIZATION_TEST_REPORT.md`
- `VOICE_TEST_REPORT.md`
- `SCREENSHOT_VALIDATION_REPORT.md`
- `AMAZON_BILLING_TEST_REPORT.md`
- `FIRE_DEVICE_COMPATIBILITY.md`
- `SECURITY_AND_PRIVACY_REPORT.md`
- `RELEASE_READINESS.md`

Every test result must include:

- Requirement ID
- Test name
- Execution date
- Environment or device
- Expected result
- Actual result
- PASS / FAIL / BLOCKED / NOT TESTED
- Evidence reference
- Corrective action where needed

Include actual build logs, test outputs, screenshots, or other reproducible evidence.

Never fabricate test evidence.

# 27. FINAL RELEASE DECISION RULES

Use the following release statuses.

**READY FOR SUBMISSION**

All mandatory acceptance criteria pass, required translations and screenshots are approved, billing is verified, and there are no unresolved critical defects.

**CONDITIONALLY READY**

Core application functionality passes, but non-critical work remains. Clearly identify outstanding requirements.

Do not describe this state as fully production-ready.

**BLOCKED**

One or more critical requirements are incomplete, failed, or unverified.

Examples:

- App cannot compile.
- Core data persistence fails.
- Required localisation is incomplete.
- Required screenshots are missing.
- Amazon billing is unverified.
- Critical privacy or security issues remain.
- Required device compatibility is untested.

A BLOCKED application must not be declared ready for submission.

# 28. FINAL CODEX EXECUTION INSTRUCTION

After implementing each development phase:

1. Run the relevant acceptance tests.
2. Record actual results.
3. Fix failures.
4. Rerun affected tests.
5. Update the acceptance matrix.
6. Continue to the next phase.

At final delivery, calculate completion using:

**Verified acceptance rate = Passed mandatory criteria ÷ Total mandatory criteria × 100**

Treat BLOCKED and NOT TESTED criteria as incomplete.

Report the percentage alongside the individual test results, not as a substitute for them.

Do not claim 100% completion unless every mandatory criterion has passed.

**Final objective: Deliver FloraTend as a genuinely functional, premium, multilingual, voice-enabled, offline-first plant-care application with verified Amazon Appstore assets, documented compatibility, and evidence-backed release readiness.**