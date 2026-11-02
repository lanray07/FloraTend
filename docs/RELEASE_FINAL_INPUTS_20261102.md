# Remaining release work - 2 November 2026

Within the10minute window, the15listing drafts were reviewed against implemented features and retained runtime evidence. AC-14.06 now passes for technical claim accuracy. This is AI technical review, not qualified language or rights approval. The exact15draft hashes and claim-to-source/test/evidence mapping are in `evidence/final10/listing-claim-review.json`.

Metadata validation now rejects HTML markup, decorative pictographs/emojis, an all-caps title/description field and Unicode replacement characters, alongside existing price/ASIN and length checks. Two regression tests passed, including6invalid-input cases and all15current drafts. Verified source requirements: [Appstore fields](https://developer.amazon.com/docs/app-submission/appstore-details.html) and [listing policy](https://developer.amazon.com/docs/policy-center/listing-promo.html). AC-14.07 remains open for complete metadata/asset/publisher approval; these technical checks do not certify Amazon policy acceptance.

## What is needed from the publisher

- Review and sign off the current [privacy notice](https://github.com/lanray07/FloraTend/blob/main/PRIVACY.md), provider/photo/receipt disclosures and final Amazon privacy declarations. Existing maintainer/contact details were reused; legal approval is not inferred.
- Provide qualified language/market review tied to the current22UI bundles and15listing/screenshot locales. Choose a narrower initial launch language set explicitly if desired; no languages were silently dropped. Review inputs: [UI review manifest](https://github.com/lanray07/FloraTend/blob/main/localization/review.json), [listing drafts](https://github.com/lanray07/FloraTend/tree/main/submission/listings), [tablet screenshots](https://github.com/lanray07/FloraTend/tree/main/assets/screenshots/localized) and [phone screenshots](https://github.com/lanray07/FloraTend/tree/main/assets/screenshots/phone).
- Supply a separate private DeepL Free credential for the existing translation pipeline (`DEEPL_AUTH_KEY`). The saved Pl@ntNet credential is for identification and does not satisfy this dependency. Keep keys out of chat and Git; no paid API was used.
- Provide a device with a working recognition service for real push-to-talk testing. The Fire tablet fallback is tested; absence of a compatible engine cannot establish successful recognition.

## Work still owned by the agent or dependent on Amazon

Real VoiceView verification of optimized Amazon15, sustained unplugged energy measurement, translated website pages/reciprocal hreflang, remaining receipt edge cases under the no-real-charge constraint, and final submission-checklist reconciliation remain unverified. These are not reassigned to the publisher merely because the time window ended. A public Appstore URL can be verified only once Amazon exposes the public listing; the private Test2 invitation is not substituted.

The actual Amazon15 purchase/automatic Premium grant/Restore/offline regression, both checkout orientations, source15rendering pooled gate,22locale native phone/tablet captures and GitHub Pages canonical/sitemap checks retain their existing evidence and limitations. No APK, billing gate, production service or active Test2 release was changed in this window. Public production readiness is not claimed.
