# FloraTend static development website

Twelve English detail pages and22equivalent localized overview pages (34crawlable, responsive pages total), native light/dark colour schemes, no external scripts/fonts, and SoftwareApplication JSON-LD. Page content is intentionally explicit that the product is in development; it does not claim a released app, tested compatibility, working billing, identification, or completed localisation.

Build and check locally:

```powershell
python website/build.py
python website/validate.py
python -m http.server 8080 --directory website/dist
```

For a deployment, set `SITE_URL` to the actual verified HTTPS website base URL, then run `python website/build.py --production` and `python website/validate.py --production`. Do not use the test fixture `example.org` as a real domain. Without a configured origin, robots.txt disallows indexing, pages use noindex, and sitemap.xml has no URLs; this prevents an invented canonical from being published. With a real origin, the generator emits canonical URLs, en-GB alternate links and a complete absolute-URL sitemap. Each localized overview has a self canonical and the same reciprocal set of22language/region alternate URLs plus x-default. The12English detail/support/privacy pages remain separate; they are not falsely labelled translated. Language review is still pending.

`APPSTORE_URL` is optional. Set it only to a verified, existing Amazon product URL. No listing link is otherwise generated. This script does not publish or register anything.

Before publication: reconcile every product statement with verified release behaviour; add the publisher legal identity, monitored support address and hosting privacy details; complete privacy review; translate/review pages for approved locales; inspect the rendered site at phone/tablet/desktop widths. Technical validation alone does not complete visual, privacy, localisation or SEO release gates.


## GitHub-hosted URLs

GitHub Pages publishes the development website at https://lanray07.github.io/FloraTend/ through `.github/workflows/pages.yml`. The build uses the actual URL returned by configure-pages, including the case-sensitive project prefix. Privacy: `/FloraTend/privacy.html`; support: `/FloraTend/support.html`; sitemap: `/FloraTend/sitemap.xml`. Existing GitHub document URLs remain in `hosting.json` and in the Amazon draft. The app itself is not publicly available, no public Amazon download link is invented, and this deployment does not approve the publisher's final privacy policy or locale language review.

The website has no app-controlled analytics, forms, external fonts or media embeds. GitHub Pages logs visitor IP addresses for security purposes; see [GitHub Pages data collection](https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages#data-collection). Final publisher and language review remain separate gates.


## Localized overviews

The22pages under `locales/<BCP47-tag>/index.html` reuse current Android UI copy and add localized development notices/navigation. These are equivalent overview pages; English detail and legal pages are linked with an explicit English language label. Arabic uses RTL. The build report records normalized UTF-8/LF resource hashes, page hashes and `localized_human_approved: false`. Human linguistic approval is not inferred from generation, layout review, or technical validation. No auto-redirect hides languages.

Validation checks actual recursive HTML files, per-page canonical/language/direction, exact reciprocal hreflang sets and sitemap uniqueness/completeness. Regression tests reject a missing/misdirected alternate, a duplicate hiding a missing sitemap URL, and cross-platform provenance drift. The implementation follows [Google’s localized-page guidance](https://developers.google.com/search/docs/specialty/international/localized-versions).
