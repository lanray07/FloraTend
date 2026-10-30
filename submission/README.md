# Amazon draft progress — 2026-10-30

The FloraTend draft is saved in the Lanray Amazon Developer account. **It has not been submitted or published.** IDs, contact links, locale enumeration and APK hash are in `console-draft.json`.

Saved and verified:

- Support email: banksmi@mail.com.
- Support: https://github.com/lanray07/FloraTend/blob/main/SUPPORT.md
- Privacy: https://github.com/lanray07/FloraTend/blob/main/PRIVACY.md
- Lifestyle / Home & Garden category, free download, SKU floratend.android.
- All 15 available description locales, verified by navigating/reloading and reading back title, short and long description. Text remains a draft pending human review.
- Both transparent tablet icons (512 and 114 pixels).
- Signed release APK, versionCode 2 / 0.1.1, target API 36: console displays **1 file(s) uploaded** and **DRM Applied: Yes**. Local apksigner validation passed (v2, RSA3072).
- Actual app-specific public Appstore SDK authentication key is bundled. This public key is distinct from the signing private key.

Outstanding: human translation approval, complete localized screenshot sets, detailed privacy labels, broader device/OS review, Amazon purchase/restore scenarios and final release gates. Audience 16-17 plus 18+ is saved. The content rating questionnaire shows Complete. No account creation is declared; the data-deletion link points to the GitHub policy instructions. The SDK/speech-provider privacy data mapping remains incomplete. No unknown declaration was answered speculatively. Only English is currently declared under binary language support until language verification is complete.

The current API 36 build, lint and 11 JVM tests pass. All 11 native tests passed on the unlocked physical Fire HD 10 (2021), including 19-bundle/dictionary loading and representative Spanish, German, Japanese and Arabic typed actions/screenshots. The first API 36 instrumentation run failed due to an already-registered UI Automation service; its log is retained, and the clean retry passed. See `../evidence/api36-verification.json` for artifact hashes and scope.

The minimum-target warning cleared after processing version code 2. Twelve existing Fire tablet selections remain. The six newer Alexa tablet entries are still disabled in the picker; API 36 runtime compatibility and those new models are not claimed verified. The older English display-override/offline matrix remains separate evidence under `evidence/device/`; it was not rerun wholesale in this phase.

The signing key and Windows DPAPI configuration are outside Git, at `C:\Users\User\.android\floratend-release.p12` and `floratend-signing.dpapi.json`. Preserve an independent secure backup before relying on this identity for future releases. DPAPI configuration is bound to the current Windows user; it is not a portable backup. Secrets were not uploaded to GitHub or the console.

Evidence: `github-support-console.png`, `github-privacy-console.png`, `api36-apk-console.png`, `targeting-console-20261030.png`, `listing-console-readback.json`, `../evidence/release-signature.txt`, `../evidence/signed-release-build.log`.
