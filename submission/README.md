# Amazon draft progress — 2026-10-28

The FloraTend draft is saved in the Lanray Amazon Developer account. **It has not been submitted or published.** IDs, contact links, locale enumeration and APK hash are in `console-draft.json`.

Saved and verified:

- Support email: banksmi@mail.com.
- Support: https://github.com/lanray07/FloraTend/blob/main/SUPPORT.md
- Privacy: https://github.com/lanray07/FloraTend/blob/main/PRIVACY.md
- Lifestyle / Home & Garden category, free download, SKU floratend.android.
- All 15 available description locales, verified by navigating/reloading and reading back title, short and long description. Text remains a draft pending human review.
- Both transparent tablet icons (512 and 114 pixels).
- Signed release APK, versionCode 1 / 0.1.0: console displays **1 file(s) uploaded** and **DRM Applied: Yes**. Local apksigner validation passed (v2, RSA3072).
- Actual app-specific public Appstore SDK authentication key is bundled. This public key is distinct from the signing private key.

Outstanding: unlocked-tablet representative UI/action tests, human translation approval, required screenshots, privacy questionnaire, target audience, rating questionnaire, supported-device review, Amazon purchase/restore scenarios and final release gates. No unknown declaration was answered speculatively. Only English is currently declared under binary language support until language verification is complete.

The latest 11-test device attempt had 5 passing non-UI tests and 6 UI failures. The subsequent startup inspection showed the secure PIN lock screen. The runner now stops before instrumenting when keyguard is showing; it does not bypass the PIN. Unlock on the device before rerunning `tools/test_localized_fire.ps1 -SkipBuild`. Prior passing English Fire matrix evidence remains under `evidence/device/`; it does not establish the new localized UI tests passed.

The signing key and Windows DPAPI configuration are outside Git, at `C:\Users\User\.android\floratend-release.p12` and `floratend-signing.dpapi.json`. Preserve an independent secure backup before relying on this identity for future releases. DPAPI configuration is bound to the current Windows user; it is not a portable backup. Secrets were not uploaded to GitHub or the console.

Evidence: `github-support-console.png`, `github-privacy-console.png`, `signed-apk-console.png`, `listing-console-readback.json`, `../evidence/release-signature.txt`, `../evidence/signed-release-build.log`.
