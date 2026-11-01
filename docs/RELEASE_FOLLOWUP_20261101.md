# Release follow-up — 1 November 2026

Source0.1.10/code11 supersedes the frozen code10 candidate. Code10 entered Test2 in-progress; code11 was submitted to the same private Test2 track after local verification; console status Test submitted. Actual Amazon-delivered code11 was installed in place through Appstore Update, preserving records. The genuine code11 results below supersede the earlier distribution checkpoint; no developer build replaced the main package.

## Reproduced defects and results

- Explicit Simplified Chinese script on a Traditional-region locale incorrectly selected the Traditional voice dictionary. A JVM red/green test now gives an explicit Hans/Hant script precedence over regional fallback.
- Rotating the real Android DocumentsUI picker discarded a new plant editor and its unsaved text. Root editor identities and primitive drafts now survive recreation. Pending content URI selection survives recreation too. Both real plant/journal picker workflows passed, preserving text, measurements and private downsampled photos without broad storage permission.
- Save could complete while private photo import was pending, losing the selected attachment. Editors now disable Save during import. The original fast-save failure is retained in the intermediate run; later actual picker workflows passed after private copy.
- Imported JPEGs ignored EXIF orientation. The controlled test failed at mirrored orientation2. Pixel correction now passes all8 normal/mirrored/rotated orientations through ContentResolver, retains private storage and drops orientation metadata after normalising pixels. This is not a camera-hardware or identification-engine claim.
- Real Android notifications passed selected-language text/channel checks in all22 packaged locales. Existing English notification then failed to update after actual UI language selection to French. Language selection now requests a worker refresh; the real UI English→French transition passed afterward.

## Failure interpretation

`photo-rotation-red.txt` and `photo-rotation/red-app.png` demonstrate the real lost editor. `photo-rotation-green.txt` is an intermediate failed run: plant draft restored but photo was saved too early; journal setup used an outdated field label. `photo-fixes-native.txt` passed journal+EXIF but its plant test ambiguously matched two Herbs controls. The selector was scoped to the editor dialog; `photo-rotation-final.txt` then passed2tests. These failures are retained, not overwritten or counted as passes.

`photo-orientation-red.txt` demonstrates the real mirrored-image defect. `notification-language-red.txt` reached the locale switch and timed out awaiting updated French notification text. No fixture is claimed as a genuine Amazon receipt or recognized speech.

## Scope

Twenty-two app bundles and15 store drafts still require qualified human linguistic/claims review. English search terminology now has dated primary-source evidence with explicit inference and no traffic/ranking promises. Metadata-audit execution dates are computed at run time. Production publishing, paid purchase, new accounts/keys and public hosting were not performed.

The immutable code10 artifact records reference source commit b64a62c6ba24f7b76efb67ecc653d81916372dd8; code11 has separately signed production/LAT artifacts; genuine Amazon delivery/regression and exact-head CI remain pending. The strict server receipt gate and production-default endpoint remain unchanged. Secrets remain private CurrentUser DPAPI files outside Git/APK.

Arabic date formatting initially differed from Android ICU only in digit shaping. The formatter now uses the locale decimal style; all22 packaged locale date outputs match the independent Android medium-date reference. French Insights also exposed a raw ISO month key; its native red test explicitly found2011-03. Localized month/year output is under final green verification.

High-contrast text and180%font/zero-animation configuration passed3native checks on physical HD10. Captures were visually inspected and all OS preferences restored. Prior simulated7/8-inch configurations are recorded as configurations on HD10, not additional physical hardware.

The new month test initially omitted its JUnit Rule import; `date-digits-build.log` is a test-compilation failure, not an app runtime failure. Corrected test build passed. Its initial month assertion was strengthened to wait for either ISO or localized output, then explicitly reject ISO; the confirmed red retains the actual raw-key node.

The first combined follow-up suite started with a persisted French QA preference left by a focused locale test. Older English-only privacy/reminder/smoke assertions consequently failed on translated labels/text. The run was stopped on the disposable QA client, retained as setup-contaminated evidence, and the empty-data privacy suite now explicitly selects English before testing. No main-package or protected Appstore process was stopped.

The controlled-language combined suite ran47cases with one PhotoAttachmentTest failure: its filesystem-only readiness check was followed by a Save callback timeout. That check can observe a newly created file before the preview and enabled Save control are ready. A focused repeat passed, showing the combined failure was timing-dependent. The test now awaits the actual preview and enabled Save control, as the real picker tests already do. No additional production change or bypass of import gating was made.

The fixed month formatter uses the platform locale skeleton instead of a fixed month/year order. Its first fixed-order comparison failed8locales (Spanish/Portuguese joining words and Japanese/Korean/Chinese ordering/forms). The platform-pattern correction passed all22 date and month reference comparisons plus the actual French Insights screen.

## Code11 distribution

The developer-signed production candidate SHA256 is292a097d5f9f719d36fb13daa36515340c76f0ec9bf874f59de82029192d383e; explicit LAT upload SHA256 isf4c549e09be5a8bb5852f0bb6e043a5ca100116c81f22aab9fb4d15bf40665c7. Nondebuggable code11, correct exclusive endpoint, upload certificate and absent QA classes were verified. The two configured private values were checked in memory against every uncompressed APK entry and were absent. Neither developer candidate replaced the installed Amazon code8.

Amazon read back code11, DRMYes and unchanged12tablet/0TV/0Auto support. Only FloraTend Premium LAT was selected; English-only declaration remains because22translated bundles are unapproved drafts. The existing copied export declaration remained checked under prior authorization. Console confirmed Test submitted; [proof](../evidence/release-followup/lat-code11-submitted.png). No public promotion or real charge occurred.

## Current verification checkpoint

The generated matrix now records159/204PASS and45incomplete. This closes59of the104criteria open at the start of this continuation. Camera launch/cancel passed1additional native case without capturing a photo. The47-case combined run had46passes and one timing-dependent fixture readiness failure; the corrected2-case attachment rerun passed. Do not describe this as a single clean47-case run. All actual app failures and setup failures remain separately labeled.

Code11captured176actual offline scenes across22app locales and rebuilt120draft exports for15store locales. Hash pairs were verified. Headline/listing fact checks found no invented reviews, ratings or awards; keyword lists have5distinct task phrases each. Asset provenance/native rendering was reviewed. These technical checks do not approve human translation, market terminology, readability, rights or final store compliance.

The development privacy notice now documents the actual Amazon/Cloudflare receipt path and counters; publisher identity, legal basis, retention/rights and publication scope remain external decisions. Identification-provider selection/private credentials are pending user input. No public publication, real payment or paid hosting was attempted. Genuine Amazon code11delivery and regression plus exact latest-head CI remain pending.

A first-party data-flow review combined with actual offline records/private-image execution verifies the current local-record/local-photo boundary. The only explicit first-party HTTP path carries Amazon user/receipt identifiers; optional speech/export and provider processing are disclosed separately. This is not a packet capture or legal-compliance claim, and any future identification upload must receive a new consent/privacy review.

The export host read initially used the test filename entered before DocumentsUI rotation; that path did not exist even though the app reported Save success. The harness now chooses the final unique destination name after picker rotation, then separately verifies the actual UTF-8 payload. This does not assert that external DocumentsUI preserves its filename edits across rotation.

The final real DocumentsUI export rerun passed; a separate private host read verified all4collections, the exact controlled Unicode name and quoted note in UTF-8. The destination payload stays private; only counts/hash/booleans are retained in `evidence/release-followup/export-destination-verification.json`.

## Genuine Amazon code11 regression completed

Actual nondebuggable Amazon code11 installed in place, installer `com.amazon.venezia`, APK SHA256 `fb765d78dfef3f7ea67482d456e44e90a8f1d086bd640c34a25d16b359dfbf86`, matching Amazon certificate. Both uploaded DEX files are byte-identical in the installed APK; the LAT endpoint is present and production endpoint absent. [Installed provenance](../evidence/release-followup/amazon-code11-installed.json), [DEX comparison](../evidence/release-followup/amazon-code11-dex-provenance.json).

Amazon acknowledged the authorized entitlement reset for exactly one tester in FloraTend Premium LAT. The app returned to inactive status. Warm landscape checkout appeared at3.250s and cold landscape at3.266s; both cancelled. First cold portrait appeared at5.844s, exceeding the internal5s investigation target: this FAIL remains retained. After reducing host detector overhead without changing its pixel predicate, a fresh cold portrait repeat appeared at2.735s. Measurements are screenshot-return upper bounds, not an Amazon SLA or isolated store latency; the original intermittent delay cause remains unisolated. The timing tool now records capture/detector costs separately and raw logcat is opt-in private diagnostics. All43 supporting Python tests passed.

The authorized genuine free LAT purchase then completed. Premium became active with Amazon purchase-confirmed text **before any Restore**. Explicit owned Restore passed. Wi-Fi was disabled, no default network was verified, process PID changed7746→8163, and Premium remained active after settling; unavailable refresh correctly reported that verification required connection. Wi-Fi was restored and final online Restore passed. [Scoped results](../evidence/release-followup/code11-genuine-results.json), [automatic grant](../evidence/release-followup/code11-automatic-premium.png), [owned Restore](../evidence/release-followup/code11-owned-restore.png), [offline state](../evidence/release-followup/code11-offline-network-process.json), [offline UI](../evidence/release-followup/code11-offline-premium.png), [final online Restore](../evidence/release-followup/code11-final-online-restore.png).

Positive evidence is the actual installed strict client gate plus real UI and unchanged uploaded DEX. No raw RVS response was captured; no developer instrumentation attached to Amazon's signed app. No UiAutomator attached during checkout. Historical code6 automatic-grant failure and later presentation/setup failures remain retained. An initial Restore immediately after server reset reported unavailable verification; normal cold history refresh recovered before checkout. Its cause is not isolated and it is not counted as a successful purchase.

Source/evidence head69f8d3a passed exact CI41323215215. Subsequent evidence/tool-only changes require their own exact-head CI. Full release remains incomplete; genuine LAT success does not certify paid production receipts or unresolved human/provider/publication requirements.

## Additional native localization contracts

The isolated code11 QA package passed2new native cases:572plural comparisons across all22locales, using Android ICU category selection against source forms, plus actual selected-language rendering of3Premium texts,4voice prompts and4toggle accessibility labels per locale. This verifies compiled resource selection/rendering, not human grammar approval or real speech recognition. The first harness run tried to scroll a fixed heading; its failure is retained as `localized-surfaces-harness-scroll-failure.txt`; only the test was corrected. Initial Gradle invocation also needed its dotted property quoted in PowerShell; that setup log is retained. The successful run is `localized-surface-contracts.txt`. The matrix now records162/204PASS,42incomplete:62of the original104closed.

Final device readback confirms Wi-Fi on/default network present, font1.0, physical size/density without overrides, original rotation1/0 and only the actual Amazon package installed. A fresh private QA tar backup was validated and all4record tables were empty before uninstalling the disposable QA packages. The original main package and records were not cleared. [Final state](../evidence/release-followup/code11-final-device-state.json).
