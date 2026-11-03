# Public submission preparation — 3 November 2026

The publisher requested completion and submission. This authorizes preparing and submitting FloraTend to Amazon; it does not supply missing test results, language reviews or compliance declarations.

## Current console readback

The public draft remains Incomplete. Its uploaded APK was still version code 5 when inspected, while the tested private Test2 release is code 15. The obsolete public APK has now been replaced by the verified production code 15 candidate; Amazon's upload and review pages both show version code 15, with DRM applied. Lifestyle > Home & Garden was selected and confirmed on the review page. The published support email and GitHub support/privacy URLs are present. The form initially displayed unloaded defaults; the later populated readback, rather than those defaults, is authoritative.

The draft targets 12 Fire tablets, no TV or automotive devices, all available countries/regions and ages 16+. Only English is selected in the binary language declaration; this is not evidence that the publisher approved narrowing the worldwide launch scope. Existing localized listings and screenshots still require their recorded qualified reviews.

Amazon's Submit App control is disabled with an unchecked export-compliance certification. That declaration has not been accepted for this public submission. Prior test-only certification is not treated as a new public-release certification.

## Release gates

The evidence-gated ledger remains **193/204**, with **11 incomplete**. The [remaining-input package](https://github.com/lanray07/FloraTend/blob/main/docs/RELEASE_FINAL_INPUTS_20261102.md) lists the outstanding language/asset review, separate DeepL API Free credential and actual speech-recognition test. The DeepL private configuration is still absent. The publisher's approval of the exact privacy-notice wording remains recorded and is not requested again.

The publisher explicitly retained worldwide scope and will provide a recognition-capable Android device. Qualified language/asset reviews and the actual speech-input test therefore remain required. The publisher requested help obtaining the separate translation credential.

DeepL's current [plan documentation](https://support.deepl.com/hc/en-us/articles/360021200939-DeepL-API-plans) says API Free is no longer available for new signup. The replacement free API Developer plan has a total allowance of 1,000,000 characters that does not reset. The official Developer signup page was opened with Developer selected and handed to the publisher for password entry, email verification and acceptance of DeepL's terms. No account was created by the agent, no key was captured and no paid plan was selected. Existing tooling is limited to the legacy Free endpoint; compatibility with a new Developer account must be verified before saving or using its key. See [official key management guidance](https://support.deepl.com/hc/en-us/articles/360020695820-API-key-for-DeepL-API).

Production source files match all 136 entries in the recorded code 15 source manifest. Source/configuration and metadata structural audits passed. These checks do not grant linguistic approval or replace native acceptance evidence. The existing genuine Amazon code 15 billing and scoped rendering evidence remains separately recorded.

The signed production build completed successfully, including release vital lint. Signature verification and APK inspection confirm package `com.floratend.app`, version `0.1.14`/15, minimum API 26, target API 36, no debug flag, the existing upload certificate, production receipt endpoint only and no configured photo-identification endpoint. SHA256: `28a25bb0813895885d1f962636f835b00442c6dd077dc162855b7a80640051d8`. The build emitted Amazon SDK stack-map warnings; these are retained in the private build log and are not represented as a clean warning-free build. The 16 supporting website/reporting regressions pass. See [artifact verification](https://github.com/lanray07/FloraTend/blob/main/evidence/public-submission/production15-artifact.json) and [console readback](https://github.com/lanray07/FloraTend/blob/main/evidence/public-submission/console-readback.json). This is not a genuine production-purchase result or an installation over the Amazon app.

No user plant records or installed Amazon package were changed. No private invitations, account identifiers, receipts or raw console logs are included in this report. A production APK must use the production receipt verifier; the LAT APK must not be promoted as the production configuration. The production default keeps optional photo identification disabled, consistently with the approved notice.

Public submission is not yet complete, and overall readiness is not claimed.
