"""Validate generated static pages without relying on a deployed domain."""
from __future__ import annotations

import argparse
import json
import sys
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urlparse
from xml.etree import ElementTree as ET
sys.path.insert(0, str(Path(__file__).resolve().parent))
from localized import locale_inputs, overview_path, search_language


class PageParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.links = []
        self.ids = set()
        self.title_count = 0
        self.h1_count = 0
        self.description = ""
        self.canonicals = []
        self.script_type = ""
        self.structured = []
        self.language = ""
        self.direction = ""
        self.alternates = []

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "html":
            self.language = attrs.get("lang", "")
            self.direction = attrs.get("dir", "ltr")
        if "id" in attrs:
            self.ids.add(attrs["id"])
        if tag == "title":
            self.title_count += 1
        if tag == "h1":
            self.h1_count += 1
        if tag == "a":
            self.links.append(attrs.get("href", ""))
        if tag == "meta" and attrs.get("name") == "description":
            self.description = attrs.get("content", "")
        if tag == "link" and attrs.get("rel") == "canonical":
            self.canonicals.append(attrs.get("href"))
        if tag == "link" and attrs.get("rel") == "alternate":
            self.alternates.append((attrs.get("hreflang", ""), attrs.get("href", "")))
        if tag == "script":
            self.script_type = attrs.get("type", "executable")

    def handle_endtag(self, tag):
        if tag == "script":
            self.script_type = ""

    def handle_data(self, data):
        if self.script_type == "application/ld+json":
            self.structured.append(json.loads(data))


def validate(output: Path, production: bool = False) -> dict:
    errors = []
    files = sorted(output.rglob("*.html"))
    locales = locale_inputs()
    expected_count = 12 + len(locales)
    if len(files) != expected_count:
        errors.append(f"Expected {expected_count} pages, found {len(files)}")
    report = json.loads((output / 'build-report.json').read_text(encoding='utf-8'))
    origin = report.get('canonical_origin')
    paths = {file.relative_to(output).as_posix() for file in files}
    if paths != set(report['page_paths']):
        errors.append('Build manifest does not match generated pages')
    for file in files:
        parser = PageParser()
        parser.feed(file.read_text(encoding="utf-8"))
        if parser.title_count != 1 or parser.h1_count != 1 or not parser.description or not parser.language:
            errors.append(f"{file.name}: missing/duplicate title, h1, description or language")
        if production and len(parser.canonicals) != 1:
            errors.append(f"{file.name}: production canonical missing")
        relative = file.relative_to(output).as_posix()
        if production and parser.canonicals != [f'{origin}/{relative}']:
            errors.append(f'{relative}: incorrect canonical')
        if relative.startswith('locales/'):
            locale = relative.split('/')[1]
            if locale not in locales or parser.language != locale or parser.direction != locales[locale]['direction']:
                errors.append(f'{relative}: incorrect language/direction')
            expected_alternates = {search_language(tag): f'{origin}/{overview_path(tag)}' for tag in locales}
            expected_alternates['x-default'] = f'{origin}/{overview_path("en-GB")}'
            if production and (dict(parser.alternates) != expected_alternates or len(parser.alternates) != len(expected_alternates)):
                errors.append(f'{relative}: incomplete or incorrect reciprocal hreflang set')
        elif production and parser.alternates != [('en-GB', f'{origin}/{relative}')]:
            errors.append(f'{relative}: non-equivalent detail page has incorrect alternate')
        if not parser.structured or parser.structured[0].get("@type") != "SoftwareApplication":
            errors.append(f"{file.name}: invalid SoftwareApplication data")
        for href in parser.links:
            parsed = urlparse(href)
            if not parsed.scheme and not parsed.netloc:
                target = (file.parent / (parsed.path or file.name)).resolve()
                if not target.is_relative_to(output.resolve()) or not target.is_file():
                    errors.append(f"{file.name}: broken internal link {href}")
    if not (output / "robots.txt").is_file():
        errors.append("robots.txt missing")
    try:
        sitemap = ET.parse(output / "sitemap.xml")
        locations = [element.text for element in sitemap.iter() if element.tag.endswith("}loc")]
        if production and (set(locations) != {f'{origin}/{path}' for path in paths} or len(locations) != len(paths) or any(not (url or "").startswith("https://") for url in locations)):
            errors.append("Production sitemap must contain each page's absolute HTTPS URL exactly once")
    except (ET.ParseError, FileNotFoundError):
        errors.append("Sitemap missing or invalid")
    return {"checked_pages": len(files), "production": production, "errors": errors, "status": "FAIL" if errors else "PASS"}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path, nargs="?", default=Path(__file__).parent / "dist")
    parser.add_argument("--production", action="store_true")
    args = parser.parse_args()
    result = validate(args.output, args.production)
    print(json.dumps(result, indent=2))
    raise SystemExit(bool(result["errors"]))
