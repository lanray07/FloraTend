# Remaining release work — source14, 1 November 2026

The ledger advances from178/204 to182/204 scoped passes;22criteria remain incomplete. Production release remains BLOCKED. Four new closures cover production placeholder/callback inspection, enlarged-text layout, local website product facts and the tested critical-crash regression scope. None grants linguistic, legal, new-hardware or store approval.

## Rendering: substantial improvement, remaining entry failure

All measurements use the isolated nondebuggable `.perf` package on physical Fire HD10/API28,120plants with120photos and120rows in each care/journal/history table. Instrumentation and QA speech fixtures are absent during measurements. Actual Amazon11/data are preserved. No forced full-speed compilation is promoted to a pass.

A fixed five-launch protocol was saved before each series. All trials remain, including failures; no replacement runs or averages of percentiles. Histograms and jank counts are pooled, each screen needs100frames across the series, and a stricter250ms maximum-duration bucket guard is included. Raw traces/images stay private. Reproduce with `python tools/measure_frame_series.py --version-code 14 --output-dir <new-directory>` after preparing the declared isolated fixture/configuration. This command does not seed data or modify the Amazon package.

| Screen | Original13 pooled p95 / jank | Retained14 pooled p95 / jank | Retained14 maximum bucket |
|---|---:|---:|---:|
| Garden |20ms /11.08% |19ms /7.21% |150ms |
| Voice, including typed review |38ms /30.03% |23ms /8.11% |97ms |
| Calendar |14ms /4.04% |15ms /4.10% |350ms |

All30individual source14 screen samples meet the original32ms p95/10%jank targets. However, the new maximum-duration guard FAILS Calendar. AC-18.01 therefore remains FAIL. Calendar phase tracing locates300–350ms stalls in first navigation; subsequent scrolling in that diagnostic has no frame over200ms. Inclusive trace summaries are diagnostic, not exclusive CPU attribution. Caching Calendar names/date formatting did not resolve entry stalls in three focused probes; reverted. This is unfinished engineering work, not a request for a new key or permission.

Retained changes replace the animated Compose Voice field with a native EditText and static localized label, preserving transcript editing, accessibility and explicit Run confirmation. Garden care summaries and distinct date strings are computed once per schedule/locale change rather than scanning/formatting per card in the scroll path. Native regression verifies paused/earliest/completed/resumed care dates. App schema remains2.

### Failed experiments retained

The initial source13 baseline reproduces Voice42ms/32.89%jank. Caching speech-service availability measured69ms/50.77% in the focused probe; BasicTextField measured53ms/29.69%; both were reverted. The native editor focused probe still missed the original limits, while its full workload and later five-run comparison improve them; a focused result is never used to approve the release. Source14's earlier full run failed Garden/Voice, and its repeat passed; neither replaces the final fixed five-run series. Historical source12 forced-compiler/R8/editor experiments remain diagnostic or failed. See the JSON files under `evidence/rendering/` for individual trials, patches, artifact hashes and verdicts.

## Native, build and artifact verification

- Source14 core/debug/release JVM reports:6/34/34tests, zero failures/errors/skips. Final build/lint verification completes after reverting the Calendar experiment; cached tasks are not described as reexecuted. Debug and release lint:0errors,46warnings each.
- Native core workflows/speech contracts:9tests PASS. Recognition callbacks in the speech contract are synthetic fixtures; genuine microphone recognition on supported hardware remains unverified.
- Actual WorkManager state/interval tests pass: disabled reminders retain no live periodic/refresh jobs; enabled reminders keep one12-hour periodic request; disabled state remains stable after real process restart. Scheduling is serialized and disabled work/notification cancelled. No unplugged energy measurement is claimed.
- Native enlarged-text checks pass242surfaces per configuration across22locales at180%font, including638unbounded paragraphs remeasured against actual Text size per run. Zero overflows/unassessed widths. A real Arabic chip and deliberately clipped control prove the bounds test can distinguish fitting from overflowing text. Native editor passes English/Arabic/Japanese editing/accessibility/bounds in the compact configuration.
- Captured176real offline scenes across22locales; regenerated120draft compositions for15listing locales. Every composition retains complete native pixels, hashes and localized resource provenance. Human approvals remain0.
- A scheduling test initially raced ENQUEUED→RUNNING; it now checks live job count and actual interval. A screenshot checker initially expected a public raw log; it now checks the sanitized summary. Both failures are retained in `verification.json`; the full68-test supporting suite passes after correction.
- Original13 was rebuilt from immutable c3364c9 in an isolated sparse checkout;111canonical production inputs matched. In-place isolated13→14 update preserved480records and120photo hashes. Fresh final DB/photo backup passed native and host integrity/transport checks before empty-data-guarded isolated cleanup.
- Signed production14 was verified separately, without installing over Amazon. It uses production receipt verification and leaves optional photo identification disabled pending release configuration. No new Amazon14 delivery, LAT submission or production purchase is claimed. Signed artifact/hash/certificate and current134source-input hashes are recorded separately. The post-reversion benchmark rebuild has a different APK hash from the measured artifact; both identities are retained, with no byte-identical or new device-execution claim.

The protected Amazon11 installation still matches its recorded SHA256, Amazon certificate and installer. QA/perf/test packages are removed; physical size/density, font1.0, original rotation1/0 and Wi-Fi are restored. App data and library sort were not cleared or altered. Private test backups remain outside Git.

## Website and privacy reconciliation

Engineering review maps12local English pages to executed collection, care, journal, voice, billing and identification evidence. FAQ/privacy distinguish tested optional Pl@ntNet from the disabled default endpoint, explicit Send photo consent, manual correction, Cloudflare forwarding, provider processing and daily hashed-address counters. No calibrated identification probability, universal recognition, public availability or zero provider retention is promised.12page build/validation passes; the site remains local and non-indexable.

Settings now shows a receipt-verification disclosure before Purchase in all22locale resources: app-scoped Amazon user identifier and receipt identifier pass overHTTPS through the FloraTend Cloudflare verifier to Amazon; plant notes/photos are excluded from that verification. These are translation drafts. Publisher identity, rights/contact process, approved production origin and final privacy review remain outstanding. AC-16.09 is not closed by adding copy.

## Store requirements and remaining input dependencies

Official Amazon [Appstore details](https://developer.amazon.com/docs/app-submission/appstore-details.html) and [listing policy](https://developer.amazon.com/docs/policy-center/listing-promo.html) were consulted on1November. Numeric field checks cover documented short-description bytes, long-description characters and bullet count. Plain text, rights, accurate local claims, terminology and readable screenshots need review; field checks cannot establish policy approval. No numeric display-title limit is invented. The preserved specification also requires phone variants; simulated compact text bounds do not satisfy actual phone screenshot/hardware approval.

Inputs still needed are qualified UI/store language and market reviewers; recognition-capable hardware/engine for genuine microphone tests; private translation-provider credentials if that integration is to be verified; and publisher/contact/privacy approval plus production origin and public Appstore link. The saved Pl@ntNet key is available and is not a blocker. Production billing-edge verification requires the appropriate real-store environment and authorization; no real charges are made. Remaining Calendar engineering and unplugged energy measurement are explicitly unverified, not represented as approvals awaiting the user.

The generated acceptance matrix lists all22remaining criteria and their individual corrective actions. Do not claim whole-release readiness from these development results.
