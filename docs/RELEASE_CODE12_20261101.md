# Source 12 release follow-up — 1 November 2026

Development is **0.1.11 / code 12**, Room schema 2. The generated checklist records **164/204 PASS, 40 incomplete**: 64 of the 104 criteria open at the start of this continuation have scoped passing evidence. Overall release remains blocked. The actual Amazon-delivered app stays code 11; its genuine free LAT purchase, automatic strict server-gated Premium activation, owned Restore and offline/final online Restore passed. Source 12 has not been submitted or installed over that package.

## Implemented and tested

- Actual Arabic screenshots exposed card-detail chevrons pointing right after the rest of the layout mirrored. The actual card vector failed a native `autoMirror` regression. `FloraIcons.Detail` now uses the Material automatically mirrored vector; the native check and Arabic rendering passed. Text/direction observations do not approve draft Arabic wording.
- A 120-plant Calendar composed 122 matching filter/card nodes. The native scale regression failed on the eager row. The plant filters now use a lazy row. The corrected implementation passed the visible-composition bound and selecting the last plant correctly filtered the actual care cards.
- An empty PowerShell verifier override produced a signed APK with neither endpoint. It was rejected before installation/submission. The signing helper now defaults an empty/absent override to production, restores missing environment variables correctly, and inspects the built DEX for the requested exclusive endpoint. Both environment states were exercised. A real conflicting empty Gradle property was then deliberately built and rejected by the output guard. No server gate was weakened.
- First-party audio inspection confirms no recorder API/background microphone service and an empty byte-buffer callback; prior Android binder permission/result/error/stop tests retain their synthetic-provider scope. Installed provider processing is separate from this first-party storage property.

The final native functional sweep passed **12 tests**: Calendar scale/last-item selection, directional icon, four smoke workflows, two release workflows, all-locale paywall/voice/accessibility resource surfaces and all-locale date/month references. Core JVM and both app JVM variants plus debug/release lint passed. [Native run](../evidence/release-followup/code12-native-final.txt), [build/JVM/lint](../evidence/release-followup/code12-final-validation.txt).

## Performance remains a failed gate

Nondebuggable isolated rendering measurements ran with instrumentation detached and OS gesture input. The controlled populated workload contained 120 plants/photos and 120 rows in each of care schedules, journal and history. Calendar, Journal, Insights and Settings met the declared 32 ms p95 / 10% jank bounds. Garden returned 22 ms / 53.06% jank; typed Voice returned 48 ms / 36.84%. The combined gate remains **FAIL**. [Workload](../evidence/release-followup/code12-performance-workload.json), [results](../evidence/release-followup/code12-core-frames-populated.json).

The initial plant-only probe observed Calendar p95 around 700 ms with few samples. A lazy-filter build observed 400 ms, but natural compiler state was not fully controlled. These measurements are not a causal latency guarantee. Synchronous-to-asynchronous empty-artwork loading was separately tried; mixed results did not establish reliable improvement, so that production experiment was reverted. Eager, lazy and artwork-experiment failures remain retained. The shell profile-reset command was refused as system-only; no reset or bypass is claimed. The latest probe records natural install/update state. Earlier source-10 scrolling numbers do not certify current core performance.

The source change retained from that diagnosis is independently justified by the deterministic excessive-composition regression and real final-item selection test. Performance follow-up remains engineering work, not relabeled as a user-input blocker.

## Candidate provenance and privacy

Developer-signed production and explicit LAT candidates were built and checked separately. Both are nondebuggable code 12, min API 26 / target 36, signed with the existing upload certificate and contain the exclusive intended endpoint. QA test classes are absent. Both configured private values were decrypted only in memory and checked against every uncompressed final APK entry; neither was present. APKs and encrypted signing/server secrets remain outside Git.

[Production artifact](../evidence/release-followup/code12-production-artifact.json), [LAT artifact](../evidence/release-followup/code12-lat-artifact.json), [source input manifest](../evidence/release-followup/code12-source-manifest.json), [private-value scan](../evidence/release-followup/code12-protected-secret-check.json), [builder conflict rejection](../evidence/release-followup/code12-builder-property-conflict-rejected.json).

The original unconfigured and conflicting-property APKs remain private rejected evidence. The performance seeding helper initially used the wrong DAO method for history; its compilation failure was corrected and retained separately. None of these setup failures is presented as a passing app test or a genuine receipt.

Source 12 changes rendering and candidate tooling. The billing implementation is unchanged from the genuinely tested Amazon 11 source. Actual Amazon 11 evidence remains scoped to that installed artifact; no genuine Amazon 12 purchase or paid production receipt is claimed. No new public release, charge, account or paid hosting was attempted.

## Outstanding input dependencies

The earlier identification-provider choice/private-credentials question remains pending. Qualified language/claims reviewers must approve the current 22 UI/voice bundles and 15 listing/screenshot drafts. Publisher identity, approved privacy/rights wording and publication URL/scope remain required. Additional recognition-capable/newer-OS devices and genuine unplugged battery evidence remain separate coverage gates. Other untested engineering criteria remain individually listed in the matrix.

Final source-12 captures regenerated176 actual offline scenes across22 app locales, followed by120 draft compositions for15 listing locales; hashes were checked. Human approvals remain zero. The final QA package was backed up privately, its four record tables verified empty, then removed. The nondebug performance fixture had a separate verified SQLite/photo backup before controlled cleanup; only then were its packages removed. Wi-Fi, font/size/density and original rotation were restored; only actual Amazon11 remains installed. [Final state](../evidence/release-followup/code12-final-device-state.json), [performance backup verification](../evidence/release-followup/code12-perf-cleanup-verified.json).

The final supporting suite passed43 tests. No raw SDK logs, receipt/account identifiers, private invitation, APK or decrypted signing/server secret was added to Git. The latest evidence commit must pass its own exact-head CI; earlier4376141 / CI41326017146 succeeded before these source12 changes.
