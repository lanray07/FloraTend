# Amazon draft progress — 30 October 2026

The FloraTend draft is saved in the Lanray Amazon Developer account. **It has not been submitted or published.** IDs, links and current signed APK hash are in `console-draft.json`.

Saved and verified:

- Support email banksmi@mail.com, GitHub SUPPORT.md and PRIVACY.md URLs, Lifestyle / Home & Garden, free download and app SKU floratend.android.
- All 15 description locales. Text remains an assistant draft pending human review.
- Both transparent tablet icons and 88 actual-UI screenshot compositions across all 11 image-language channels, read back after reload. All 15 locale packs (120 PNGs) are in Git. Dutch, Polish, Portuguese (Portugal) and Swedish have no dedicated image channel in this console; it uses English (U.S.) fallback.
- Signed release 0.1.2/code 3, target API 36, accepted by the console: **1 file(s) uploaded**, **DRM Applied: Yes**. Local apksigner verifies RSA3072/v2. Missing IAP package queries and DRM receiver are fixed.
- Audience 16–17 plus 18+, rating questionnaire Complete, 12 Fire tablet selections, no account creation, GitHub data-deletion instructions.
- Privacy questionnaire: all 39 types answered and **Saved for review**. See `privacy-label-draft.json` and the dated rationale below. No legal approval is claimed.
- Real Amazon App Tester catalogue/price, purchase, cancellation, restore, revocation, account switch and failed-refresh cache tests passed. This does not configure a live product or prove production billing.

Verification, reproducing tests, environment cleanup and remaining limits: [dated report](../docs/RELEASE_VERIFICATION_20261030.md). Human review of translations/assets, publisher privacy assessment, live IAP/pricing/LAT, remaining billing edge cases, broader devices/accessibility and the mandatory release matrix remain open. Only English is declared under binary language support until human language verification is complete. Six newer Alexa tablet entries remain disabled; runtime compatibility with them is not claimed.

Signing keys and Windows DPAPI configuration remain outside Git. Preserve an independent secure backup; DPAPI configuration is bound to the current Windows user. No private signing key or RVS secret was uploaded.

Evidence: `screenshot-console-upload.json`, `localized-screenshots-console.png`, `privacy-labels-console.png`, `version3-apk-console.png`, `listing-console-readback.json`, `../evidence/release-signature-20261030.txt`, and `../evidence/signed-release-build.log`.
