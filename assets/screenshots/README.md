# Actual-app screenshot preparation

Run `python tools/screenshots.py` after device capture. This produces editable en-GB preview SVGs only for existing real PNG captures in `evidence/device/`. It never creates a replacement app UI or a missing screenshot. The eight requested compositions have entries in `screenshot-manifest.json`; absent captures remain pending. `template-source.svg` is an editable source template, visibly labelled as missing a capture, not a release screenshot.

Expected mapping:

| Composition | Capture |
|---|---|
| Hero | `01-garden.png` |
| Watering | `02-care.png` |
| Plant library | `01-garden.png` |
| Calendar | `09-calendar.png` |
| Growth | `03-journal.png` |
| Voice | `04-voice.png` |
| Offline | `08-offline.png` |
| Insights | `05-insights.png` |

Garden capture is deliberately reused for the hero and plant collection. A calendar screenshot is not substituted with a care-list screenshot. Capture filenames identify intended scenes; a reviewer still needs to verify the visible scene, language, readable typography, actual data, feature availability and accurate claims. Settings and plant detail captures are not substitutes for required calendar or offline evidence. Offline capture requires actual airplane-mode workflow evidence in the broader acceptance results.

The PNG validator uses Python standard-library CRC checks, chunk parsing, dimensions and decompressed row length verification. It supports ordinary noninterlaced 8-bit device PNG captures. It accepts documented Amazon screenshot dimensions and their portrait rotation; compositions are 1200×1920. See `docs/AMAZON_REQUIREMENTS.md` for the official submission reference. Final console validation remains required.

No application locale is approved initially. Preview captions use English only and must be placed over verified English UI. SVG preview file creation does not establish approval. The tool currently supports en-GB only and fails production if any other approved locale is requested. Do not translate headings over English captures.

For submission, render every reviewed SVG to a PNG named e.g. `01-hero-en-GB-tablet.png` with an SVG renderer, inspect it visually, then populate `assets/screenshot-review.json`. Record `approved_locales: ["en-GB"]` only after app translation review is also complete. Each scene review requires reviewer name, date, exact `capture_sha256`, exact `export_sha256`, and `approved` for `review`, `claims_review`, `capture_locale_review`. The exported PNG must match the reviewed hash and supported dimensions. Run `python tools/screenshots.py --production`; this must exit nonzero until every gate passes. Do not mark approval based only on the validator.

Phone variants, other locales, final raster rendering and human screenshot review remain pending. The six stdlib tests use synthetic temporary PNG fixtures solely to verify validator/tool behaviour; these fixtures are never exported as app evidence or marketing assets.
