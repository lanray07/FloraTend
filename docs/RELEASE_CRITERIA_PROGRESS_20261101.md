> Latest: [code11 follow-up](RELEASE_FOLLOWUP_20261101.md) and [current generated matrix](../ACCEPTANCE_TEST_RESULTS.csv). The code10 results/counts below are a preserved milestone.

# Release criteria progress — 1 November 2026

Development source is 0.1.9 / code 10, Room schema 2. The installed Amazon application remains verified code 8. This development pass closes 43 of the 104 previously unmet criteria: 143/204 now have scoped PASS evidence; 61 remain incomplete. Genuine code 8 billing results do not certify this new candidate. Full release readiness is not claimed.

## Fixed and verified

- Locale decimal editing: German `25,4` failed Save before the fix and now saves/reopens correctly. Parsing rejects grouping ambiguity, trailing junk, nonfinite values and fractional/overflowing leaf counts.
- Translated category search: German category queries originally hid matching canonical Herb records. Search now includes translated labels and locale-aware sorting/casing, covered by native and JVM regressions.
- Slow/error reads: delayed Room streams originally displayed an empty garden. Combined Loading/Ready/Unavailable states now distinguish these outcomes; Retry recovers without changing the persisted count. Collection observation follows the Activity lifecycle.
- Export lifecycle: format and navigation survive file-picker rotation. The callback rechecks Premium, reads all four record collections in a Room transaction and writes UTF-8 on IO. The real Android DocumentsUI test produced complete Unicode/quoted JSON. Test entitlement is explicitly controlled QA sandbox bookkeeping, never claimed as an Amazon purchase.
- Room v1→v2 migration preserves Unicode records, relationships, measurements and private image files, adds three sorting indexes and removes the observed TEMP B-TREE plans. Real application DB rejects main-thread queries. Both exported schemas remain committed.
- Kotlin/JVM `:core` now contains recurrence, decimal rules and voice parsing/dictionaries without Android imports; Android services, UI, storage and billing remain in `:app`.
- Korean, Simplified Chinese and Traditional Chinese bring the app to 22 packaged draft bundles/dictionaries, each with 172 translatable resources and zero structural/placeholder validation errors. Human approvals remain zero.
- Spoken feedback is optional. A real installed TTS session stopped on disable, stayed off for later speak calls and retained the setting after recreation.
- Android speech callback/permission tests passed: real contextual OS denial, synthetic binder results/errors, review before command execution, listening state and background cancellation. Synthetic provider output does not demonstrate microphone recognition. The fixture service is QA-only and disabled after testing.
- Plant/journal editors import content-URI photographs through actual callbacks/ContentResolver, downsample3200×2400 to private 1600×1200 files, persist and decode after deleting the selected source. The picker result is controlled; this does not certify an external camera or OS URI grant.
- Offline force-stop changed PID19404→19619 with no default network; records, ordered journal data and two distinct private image pixels survived.
- Primary targets meet 48dp; 7/8-inch portrait/landscape configurations at180% font remain reachable. These are size overrides on physical HD10/API28. Zero animation-scale checks are recorded separately; additional hardware/screen-reader certification remains open.
- Ten original editable botanical vectors now supply eight category illustrations, launch artwork and Premium artwork; all native resources rendered and their contact sheet was inspected.
- A 120-photo collection retained all files without OOM or decode failure. Instrumented frame timing was slow; controlled tracing compared software/hardware bitmaps, serialized decoding, interceptor scheduling, thumbnails and full ART compilation. Thumbnail and loader experiments did not establish an app fix. With instrumentation detached and compile state reset, the normal non-debuggable candidate produced851frames at13ms p95 and0.35%jank. [Sanitized normal-app measurement](../evidence/release-next/performance-standalone.json) is the scoped scrolling result; raw traces remain private.

- All 15 listing drafts passed a freshly executed field/limit audit against official Amazon documentation. This confirms draft preparation and supported fields, not natural-language, naming or claims approval. New Loading/Error/Retry keys also passed extraction and all-bundle coverage checks.

## Evidence and interpretation

[Current generated matrix](../ACCEPTANCE_TEST_RESULTS.csv) is authoritative per criterion. [JVM totals](../evidence/release-next/jvm-results.json), [build/lint](../evidence/release-next/final-validation.log), [supporting tests](../evidence/release-next/supporting-tests-final.txt), and [native evidence folder](../evidence/release-next/) retain executed results.

Red app regressions are retained: decimal Save, translated category search, missing card identity label, slow-read false-empty state and ordered-query sorting. Separate test setup failures are labeled: schema asset build ordering (fixed), invalid JUnit cleanup signature (fixed), and ActivityScenario recreation while Android's picker held foreground (test harness wait; replaced with real picker rotation). No passing purchase or production performance result is inferred from these fixtures.

## Dependencies requiring input or external evidence

- Qualified language reviewers must approve the current source hash and actual UI, voice prompts, notification/paywall/accessibility wording, metadata and screenshot claims. Structural checks cannot establish natural translation or market terminology.
- Real multilingual recognition/TTS needs an appropriate device/installed engine. This Fire has no default ASR engine and supports only the tested English TTS tags. Native software callback tests do not replace that hardware evidence.
- Live identification needs a chosen provider account/private API key and permission/terms for sending selected images. Pl@ntNet documents a Free tier of 500 identification requests/day with a private key; no account, key, image upload or paid plan has been created. Source: https://my.plantnet.org/doc/getting-started/introduction and https://my.plantnet.org/terms_of_use . The identification engine/consent/result workflow remains implementation work pending that integration decision.
- A live translation-provider check needs developer-side credentials. No provider credentials belong in Git/APK.
- Public canonical/hreflang/sitemap and official production Appstore links require the publisher's approved public URL/listing and publication scope. The local site remains deliberately non-indexable. Publisher name/privacy/trademark/rights approval remains a publisher decision.
- Paid production receipts and dedicated store edge scenarios require appropriate genuine store evidence; free LAT is the only authorized purchase scope. No real charges, public promotion or paid hosting were attempted.

Code10 was submitted only to the existing private Test2 track, with the dedicated FloraTend group. The explicit LAT candidate differs from the production-default candidate only in verifier configuration. Genuine code10 Amazon delivery, checkout/purchase, Restore and offline restart remain PENDING. [Submission confirmation](../evidence/release-next/lat-code10-submitted.png).

The final QA suite passed39tests, followed by one real VoiceView/native-bridge test with service suppression disabled. Nine focus/click actions and editable plant persistence passed; VoiceView restored off. Earlier injected touch/DPAD attempts were inconclusive and are retained without claiming successful dialogs or an app defect. [Device scope matrix](DEVICE_MATRIX_20261101.md) separates declarations from execution.

Additional implementation/verification remains tracked as NOT TESTED, not relabeled as an external blocker. Store screenshots and new-candidate genuine LAT regression must be refreshed before that candidate can be approved. Secrets remain DPAPI-protected outside Git/APK; all retained public tests use controlled records.
