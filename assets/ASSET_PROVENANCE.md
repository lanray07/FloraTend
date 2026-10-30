# FloraTend asset provenance

Created 2026-10-28 for this project. No competitor artwork, external stock photography, fabricated UI screenshots or ratings were used.

| Asset | Origin and use |
|---|---|
| `botanical_hero_master.png` | Original generated illustration, built-in `image_gen` tool; 1536 × 1024 PNG. Master retained unmodified. |
| `../app/src/main/res/drawable-nodpi/botanical_hero.png` | Exact copy of generated master for onboarding / empty garden. Kotlin usage is integrated separately. |
| `app_icon_master.svg` | Editable vector master derived directly from this project's original `ic_flora.xml` paths; 1024 × 1024 vector viewport export specification. |
| `store/floratend-icon-1024.png`, `store/floratend-icon-512.png`, `store/floratend-icon-114.png` | Deterministic raster exports of the original SVG paths, forest/sage/ivory palette retained, transparent rounded background corners. Created with `python tools/render_store_icon.py` using Pillow at 4× supersampling. No AI image edit or third-party asset used. Dimensions, RGBA mode, transparent corners, opaque logo centre and PNG integrity validated; hashes in `store/icon-validation.json`. |
| Android `ic_flora_foreground.xml`, `ic_flora_background.xml`, `ic_flora_monochrome.xml` | Native vector icon layers derived from the same original geometry. |
| Android `mipmap-anydpi-v26/ic_launcher.xml`, `mipmap-anydpi-v33/ic_launcher.xml` | Adaptive icon containers; v33 adds themed monochrome layer. Manifest activation is integrated separately. |

Illustration generation mode: built-in tool; no API key or CLI fallback used. Final generation prompt:

> Use case: illustration-story. Asset type: premium Android plant-care app onboarding and empty garden illustration. Create an original sophisticated botanical still-life illustration: one lush Monstera deliciosa in a restrained matte terracotta pot alongside a delicate fern in a warm ivory ceramic pot, a tiny watering can resting nearby. Painterly gouache with crisp elegant silhouettes, beautiful leaf detail, tactile quiet paper texture, soft morning light and subtle grounding shadows. Warm ivory #FAF8F2 background, forest green #315C45 leaves, sage #8FAE91, muted terracotta #C8795B. Airy centered composition with generous negative space, landscape 3:2 framing, plants fully contained and no clipping. Calm premium editorial botanical art for Fire tablets. No text, typography, letters, logos, badges, UI, mockup, borders or watermarks. This is a production app illustration asset, not a screenshot.

The image was inspected visually in the generation result: botanical scene, no visible letters or UI, subject contained. The generated and project copies are byte-identical. This provenance records the creation process; final app-name/trademark and release asset review remain separate. Required category icons, alternate care illustrations, banners, dark variants and verified app screenshot compositions are not supplied by this asset pass.

Store icon exports were visually inspected at 512×512. The original water-drop silhouette, sage leaf and forest-green stem are retained. `tools/render_store_icon.py` reads the actual SVG path geometry and palette, samples the source Bezier curves and circular arc, and exports antialiased RGBA PNGs. The three files also passed the standard-library PNG chunk/CRC and decoded scanline validation in `tools/screenshots.py`. These are icon assets, not app screenshots or evidence of release approval.
# Localized screenshot package — 30 October 2026

The 152 source captures in `evidence/store-captures/run-20261030-094326` are actual rendered native Fire UI with explicitly seeded demonstration records. The 120 PNGs under `assets/screenshots/localized` preserve the full screen and add original localized draft headlines. The imported demonstration image is the existing original botanical hero artwork. Native records are examples, not customer data or claimed real-world growth results. Hashes, regional mappings and review status are in `localized-manifest.json`. Windows-installed fonts were used for rendering; font files are not redistributed. Human linguistic/claims review remains pending. See `docs/RELEASE_VERIFICATION_20261030.md` for reproduction and console limitations.
