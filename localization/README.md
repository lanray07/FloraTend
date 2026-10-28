# Translation workflow

This development-only Python standard-library tool extracts Android resources, translates through DeepL when a developer configures credentials, protects format arguments and brand terms, caches per source content and locale, generates Android resource XML, and reports missing strings and review gates. No runtime translation SDK or secret is included in the app.

Run from the project root:

```
python tools/localize.py extract
python tools/localize.py translate --locale de-DE
python tools/localize.py validate
python -m unittest discover -s tools -p test_localize.py -v
```

Set `DEEPL_AUTH_KEY` in the process environment through your usual secret management. `DEEPL_ENDPOINT` accepts only `https://api-free.deepl.com` (default) or `https://api.deepl.com`. Do not commit keys or include them in Gradle resources. Provider requests use the [official DeepL translate endpoint](https://developers.deepl.com/api-reference/translate/request-translation), checked 2026-10-28. Live endpoint and language support remain unverified without credentials. Regional variants without an explicit suitable provider target remain blocked instead of silently substituting another region.

`locales.json` inventories every requested application locale. This inventory does not establish Amazon listing support, distribution availability, SpeechRecognizer support or TextToSpeech support. Those are separate runtime/store checks. `glossary.json` protects brand terms and documents care terminology. User records are never input to this pipeline.

`source.json` is regenerated on every command. Source-content cache keys identify new or changed strings without retranslating unchanged text. `cache.json` contains translations, never credentials. `validation-report.json` provides per-locale errors. A failed command exits nonzero. Android styled text is rejected for explicit handling rather than losing markup. Strings and plurals are supported; other resource types require extension before release.

After translation, `review.json` marks the locale `machine_pending_human_review`. Reviewers must record `human_approved`, reviewer identity, timestamp, and the exact source SHA-256 only after checking every string, terminology, regional phrasing, expanded layouts, dates, quantities, accessibility, notification copy and real screenshots. A changed source digest invalidates approval. No locale is approved initially. Arabic and Polish plural categories are checked explicitly; all other locales require at least `other` and human plural review. Translation cannot infer correct additional Arabic/Polish plural semantics from English alone; a reviewer must add these forms.

UI translation is not complete while Compose screens contain literal text outside resources. Review all source UI before release. Voice command dictionaries, marketing text and store metadata need separate reviewed source bundles; this first pipeline does not claim to translate these unimplemented bundles. Screenshot localization requires device capture after UI review and is not performed by this tool.

For RTL and expansion testing, use Android developer options Force RTL layout and pseudolocales on a test build, capture every screen at large font sizes in portrait and landscape, then repeat with reviewed Arabic resources on representative Fire devices. This document describes procedures, not executed device evidence.
