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
