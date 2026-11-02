# FloraTend static development website

Twelve crawlable, responsive pages, native light/dark colour schemes, no external scripts/fonts, and SoftwareApplication JSON-LD. Page content is intentionally explicit that the product is in development; it does not claim a released app, tested compatibility, working billing, identification, or completed localisation.

Build and check locally:

```powershell
python website/build.py
python website/validate.py
python -m http.server 8080 --directory website/dist
```

For a deployment, set `SITE_URL` to the actual verified HTTPS website base URL, then run `python website/build.py --production` and `python website/validate.py --production`. Do not use the test fixture `example.org` as a real domain. Without a configured origin, robots.txt disallows indexing, pages use noindex, and sitemap.xml has no URLs; this prevents an invented canonical from being published. With a real origin, the generator emits canonical URLs, en-GB alternate links and a complete absolute-URL sitemap. Other locale pages remain pending; there are no misleading hreflang links to untranslated English pages.

`APPSTORE_URL` is optional. Set it only to a verified, existing Amazon product URL. No listing link is otherwise generated. This script does not publish or register anything.

Before publication: reconcile every product statement with verified release behaviour; add the publisher legal identity, monitored support address and hosting privacy details; complete privacy review; translate/review pages for approved locales; inspect the rendered site at phone/tablet/desktop widths. Technical validation alone does not complete visual, privacy, localisation or SEO release gates.


## GitHub-hosted URLs

GitHub Pages publishes the development website at https://lanray07.github.io/FloraTend/ through `.github/workflows/pages.yml`. The build uses the actual URL returned by configure-pages, including the case-sensitive project prefix. Privacy: `/FloraTend/privacy.html`; support: `/FloraTend/support.html`; sitemap: `/FloraTend/sitemap.xml`. Existing GitHub document URLs remain in `hosting.json` and in the Amazon draft. The app itself is not publicly available, no public Amazon download link is invented, and this deployment does not approve the publisher's final privacy policy or untranslated locale pages.

The website has no app-controlled analytics, forms, external fonts or media embeds. GitHub Pages logs visitor IP addresses for security purposes; see [GitHub Pages data collection](https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages#data-collection). Final publisher and language review remain separate gates.
