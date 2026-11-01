# Pl@ntNet integration â€” code13, verification in progress

The user selected the Pl@ntNet free tier on1November2026. Development is now0.1.12/code13. A real private key was not present at the initial check. `tools/configure_plantnet_key.ps1` is ready; its CurrentUserDPAPI self-test passed. The user requested account/key help; the official developer signup page is open for their password/verification steps. No live identification, deployment or account creation is claimed.

The public identification endpoint defaults to empty. The new action remains disabled in normal builds until the privately configured server is verified. A developer may supply `-Pfloratend.plantIdentifierUrl=https://<verified-host>/identify`; no key belongs in that property. Existing receipt-verifier configuration and strict billing gating remain unchanged. Source12 signed candidates/captures are historical artifacts and do not certify source13. No new Amazon submission or main-package installation was performed.

Implemented: bounded cancellable JPEG transport, conservative JSON parsing, draft-scoped request state, generation guards against late responses, per-request consent, visible suggestions with locale-formatted scores, explicit species selection and ordinary editable-field/save flow. QA transport injection exists only in QA sources. Native consent/correction and private-file-boundary tests compile but remain unexecuted because ADB has no device. Neither those tests nor host fixtures establish live identification.

The acceptance ledger remains173/204 scoped passes,31 incomplete. Its earlier component evidence is retained; source13 still needs native regression, disclosures/listing review, refreshed relevant captures and a newly verified release artifact. The natural-install rendering gate remainsFAIL. Human linguistic approval remains0/22.

## Boundaries

- Keep the key and a separate rate-limit salt in private `floratend-plantnet-secrets.dpapi`, outsideGit/APK. Send the key only from a separate CloudflareFree Worker to the fixed Pl@ntNet HTTPS origin. Receipt verification remains separate and unchanged.
- One shared identification Worker/budget for both test and later production clients avoids separate480request allowances against the same500/day provider account. Hard app cap480UTC/day, per-IP cap5/day, bounded concurrent requests. A single SQLite Durable Object reserves attempts atomically. Never silently upgrade any account or sign a paid contract.
- Validate provider daily quota before sending the image. Refuse a reported identify allowance above500 instead of assuming a paid account is the selected free tier. Treat exhaustion/service errors explicitly. This technical check does not replace account-owner confirmation of the plan.
- Only a selected app-private normalized JPEG, at most2MiB, is eligible. No notes, plant records, local filename, receipt or Amazon identifier accompany identification. Images are not persisted by the Worker. Request logs remain disabled. Persist only short-lived daily keyed IP hashes/counters and expiring concurrency leases; disclose provider network processing accurately.

## Client flow

Plant editor photo selection stays local. An explicit Identify action opens a per-request disclosure and Cancel/Sendphoto choices. There is no upload from selection, opening an editor, saving a plant or background startup. Rotation preserves the draft and request/result state; changed photos and dismissed drafts cannot receive stale results.

Show actual cancellable loading, bounded service errors and manual entry. Results are suggestions with locale-formatted match scores, not confirmed identifications or health/safety advice. Never automatically change the species or save a plant. The user explicitly chooses a suggestion, can correct the species field, then separately saves. Provider attribution and policy links remain visible. Related provider images are not requested or redistributed.

Use a ViewModel/controller keyed by draft/photo to survive rotation, an asynchronous cancellable HTTP transport with time/body limits, and a narrow result DTO. A separate QA gateway may exercise UI/error contracts, clearly labelled synthetic. It cannot establish genuine provider recognition. All new interface/consent/error/accessibility strings must cover all22existing app locales and remain pending qualified review.

## Server contract under development

`POST /identify?lang=<ISO2>` accepts only `image/jpeg` and an explicit versioned photo-consent header. Reject missing consent, oversized/invalid input, missing configuration and unavailable limiter before upstream image submission. Fixed upstream endpoints; redirects disabled; no arbitrary destination, app-embedded secret or raw error echo.

Pl@ntNet `/v2/quota/daily` reports the current identify allowance/remaining count. `/v2/languages` supplies supported common-name languages; unsupported app languages keep localized interface copy and scientific names without English fallback common names. Forward one image as multipart with organauto, projectall, three results and related-imagesdisabled. Sanitize finite scores/names, cap and deduplicate candidates, and return only safe result fields. No provider request/account partition identifiers are returned.

## Verification required

1. Unit/edge tests: consent-before-fetch, input/response limits, fixed origins/no redirects, missing/bad credentials, free-only quota, HTTPfailure/timeout/nomatch, language fallback, bounded safe output, no private-key/error echo.
2. Local real Durable Object SQLite concurrency/budget/expiry tests; fixtures are not live provider evidence.
3. Android actual JSON/client/controller and native UI tests: no upload before consent, cancel, loading/error/retry/manual entry, result choice/correction/save, rotation/photo-change stale-response protection, all22locale surfaces.
4. With the privately supplied key and explicit UI consent, a genuine provider request using an appropriately licensed known test photo. Retain sanitized result/provenance; do not claim general accuracy from one image.
5. Update privacy/website/listing drafts and candidate provenance. Run relevant JVM/native/lint/security checks, push and await exact-headCI. If a new AmazonLAT candidate is necessary, use only the accepted Test2 channel and preserve the installed main app's records/certificate/installer.

The release remains blocked; selecting the provider or passing a fixture test does not close the live-engine gates.

## Host verification

Seven new Android JVM regressions execute real org.json parsing plus bounded reads, HTTPS configuration, no-send-before-explicit-action, duplicate-request suppression, cancellation/photo-change late-response rejection and current-photo-only selection. Both debug and release JVM suites each pass34tests, plus6core tests. A debug APK and debug instrumentation APK have been built. Lint and exact-headCI completion are tracked separately; no native runtime pass is inferred from compilation.

Twenty pure Node identification contracts plus one real local workerd/SQLite test and six unchanged receipt contracts pass. The SQLite test uses the actual production budget class with a test-only bridge/subclass; expiry cases seed old timestamps and invoke the actual cleanup alarm, without pretending a UTC day naturally elapsed. Initial Miniflare v4-style options failed against the pinned v5 beta; the explicit compatibility converter fixes that harness. The pinned runtime supports30October, so the new Worker uses2026-10-30 compatibility. These are local tests, not Cloudflare deployment proof.

The new Worker bundle passes Wrangler dry-run without secret values. Daily quota, language list, identification redirects/errors, no-match404, bounded output, safe-name/score filtering and synchronous release-failure handling are covered. Sixty supporting tests pass. Localization now measures modular XML resources too:183translatable entries per locale, zero structural/placeholder errors,22pending human reviews. Translation output preserves file partitions and includes all copy in the review digest. Existing source12 screenshot caption provenance remains bound to unchanged original resource files.

Reproduction:

```powershell
cd server
npm ci
npm test
npx wrangler deploy --config wrangler.plantnet.jsonc --dry-run --outdir ../build/identification-worker
cd ..
.\gradlew.bat --no-daemon :core:test :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug :app:lintRelease
python -m unittest discover -s tools -p "test_*.py" -v
python tools/localize.py validate
```

Localization validation intentionally exits nonzero until qualified reviews are recorded. Private key setup performs no network requests. Live tests require that key, the free-only server configuration and explicit selected-photo consent. Keep the actual Amazon code11 package/data/certificate intact; use an isolated QA package for new native contracts.

## Primary references

- [Pl@ntNet getting started](https://my.plantnet.org/doc/getting-started/introduction)
- [Single-species request/result contract](https://my.plantnet.org/doc/api/identify)
- [Quota contract](https://my.plantnet.org/doc/api/quota)
- [Supported-language route](https://my.plantnet.org/doc/api/other)
- [Image guidance](https://my.plantnet.org/doc/getting-started/faq)
- [Provider terms and attribution](https://my.plantnet.org/terms_of_use)
- [CloudflareFree SQLite Durable Objects](https://developers.cloudflare.com/durable-objects/platform/pricing/)

References were inspected1November2026. Publisher review must approve the final disclosures, attribution and use terms; no legal-compliance certification is made.

## Final client refinements

Full UI language tags are reduced to the requested ISO language for the provider. Traditional Chinese keeps scientific names and omits unverified common-name script fallbacks. A seventh JVM test covers these mappings. Consent text scrolls at large font sizes; the HTTP client is initialized only when needed and connection-failure replay is disabled for photo POSTs. Configured Settings copy now describes the explicit Sendphoto action. All22 bundles include that additional copy. Three QA native contracts cover consent/choice/correction, private-file boundaries and Android-platform JSON; none has executed on the disconnected tablet.

The earlier implementation commit865b9ea passed both local lints and exact CI41341762555. Final refinements require the latest-head CI again. Current lint results are tracked in the verification snapshot; a compile or fixture pass still cannot close the genuine provider/native criteria.
