"""Validate generated static pages without relying on a deployed domain."""
from __future__ import annotations

import argparse
import json
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urlparse
from xml.etree import ElementTree as ET


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

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "html":
            self.language = attrs.get("lang", "")
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
    files = sorted(output.glob("*.html"))
    if len(files) != 12:
        errors.append(f"Expected 12 pages, found {len(files)}")
    for file in files:
        parser = PageParser()
        parser.feed(file.read_text(encoding="utf-8"))
        if parser.title_count != 1 or parser.h1_count != 1 or not parser.description or not parser.language:
            errors.append(f"{file.name}: missing/duplicate title, h1, description or language")
        if production and len(parser.canonicals) != 1:
            errors.append(f"{file.name}: production canonical missing")
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
        if production and (len(locations) != 12 or any(not (url or "").startswith("https://") for url in locations)):
            errors.append("Production sitemap must contain 12 absolute HTTPS URLs")
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
