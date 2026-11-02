"""Equivalent localized overview pages; English support/legal links are explicitly labelled.

Existing Android copy is reused with provenance, not replaced with English placeholders.
These are development previews, not linguistic approval or complete localized legal pages.
"""
from __future__ import annotations
import hashlib
import html
import json
from pathlib import Path
import re
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def source_digest(path):
    # Git checkouts use platform-specific line endings; translation provenance must not.
    return hashlib.sha256(path.read_text(encoding='utf-8-sig').encode('utf-8')).hexdigest()


def locale_inputs():
    matrix = json.loads((ROOT / 'localization/locales.json').read_text(encoding='utf-8'))
    copy = json.loads((ROOT / 'website/locale-copy.json').read_text(encoding='utf-8'))
    assert copy['review_status'] == 'draft_pending_human_review'
    assert {item['locale'] for item in matrix} == set(copy['locales'])
    result = {}
    for item in matrix:
        strings, hashes = {}, {}
        for source in sorted((ROOT / 'app/src/main/res' / item['android_folder']).glob('*.xml')):
            hashes[source.name] = source_digest(source)
            for node in ET.parse(source).getroot():
                if node.tag == 'string':
                    value = node.text or ''
                    if value.startswith('"') and value.endswith('"'):
                        value = value[1:-1]
                    strings[node.get('name')] = value.replace("\\'", "'").replace('\\"', '"').replace('\\n', '\n')
        keys = ['your_garden', 'tagline', 'garden', 'empty_garden_body', 'care_calendar', 'check_first',
                'growth_journal', 'journal_intro', 'voice', 'voice_privacy']
        for key in keys:
            assert strings.get(key), (item['locale'], key)
            assert not re.search(r'%\d*\$?[sdf]', strings[key]), (item['locale'], key)
        result[item['locale']] = {'copy': copy['locales'][item['locale']], 'strings': strings,
                                 'direction': item['direction'], 'resource_sha256': hashes}
    return result


def overview_path(locale):
    return f'locales/{locale}/index.html'


def search_language(locale):
    # HTML accepts the UN M49 region 419; Google's hreflang regions require ISO 3166-1.
    # Use this page as the catch-all Spanish alternate, alongside the Spain-specific page.
    return 'es' if locale == 'es-419' else locale


def build_overviews(output: Path, origin: str):
    locales = locale_inputs()
    paths, provenance = [], {}
    escape = html.escape
    for locale, item in locales.items():
        path = overview_path(locale)
        strings, copy = item['strings'], item['copy']
        canonical = f'{origin}/{path}' if origin else ''
        metadata = f'<link rel="canonical" href="{escape(canonical)}">' if origin else '<meta name="robots" content="noindex,nofollow">'
        if origin:
            metadata += ''.join(f'<link rel="alternate" hreflang="{search_language(tag)}" href="{escape(origin)}/{overview_path(tag)}">' for tag in locales)
            metadata += f'<link rel="alternate" hreflang="x-default" href="{escape(origin)}/{overview_path("en-GB")}">'
        sections = ''.join(f'<section><h2>{escape(strings[heading])}</h2><p>{escape(strings[body])}</p></section>'
                           for heading, body in [('garden', 'empty_garden_body'), ('care_calendar', 'check_first'),
                                                 ('growth_journal', 'journal_intro'), ('voice', 'voice_privacy')])
        languages = ''.join(f'<li><a href="../{tag}/index.html" lang="{tag}" hreflang="{tag}"'
                            + (' aria-current="page"' if tag == locale else '')
                            + f'>{escape(value["copy"]["name"])}</a></li>' for tag, value in locales.items())
        schema = {'@context': 'https://schema.org', '@type': 'SoftwareApplication', 'name': 'FloraTend',
                  'applicationCategory': 'LifestyleApplication', 'operatingSystem': 'Android',
                  'description': strings['tagline'], 'inLanguage': locale}
        if origin:
            schema['url'] = canonical
        structured = json.dumps(schema, ensure_ascii=False).replace('<', '\\u003c')
        document = f'''<!doctype html><html lang="{locale}" dir="{item['direction']}"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{escape(strings['your_garden'])} | FloraTend</title><meta name="description" content="{escape(strings['tagline'])}">{metadata}<link rel="stylesheet" href="../../style.css"><script type="application/ld+json">{structured}</script></head><body><header><a class="brand" href="index.html">FloraTend</a><nav><a href="#languages">{escape(copy['languages'])}</a><a href="../../support.html" lang="en-GB">{escape(copy['support'])}</a></nav></header><main id="main"><div class="hero"><h1>{escape(strings['your_garden'])}</h1><p class="intro">{escape(strings['tagline'])}</p><p class="notice">{escape(copy['notice'])}</p></div><div class="content">{sections}</div></main><footer><div><p>{escape(copy['notice'])}</p><p><a href="../../privacy.html" lang="en-GB">{escape(copy['support'])}</a></p><h2 id="languages">{escape(copy['languages'])}</h2><ul>{languages}</ul></div></footer></body></html>'''
        destination = output / path
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_text(document, encoding='utf-8')
        paths.append(path)
        provenance[locale] = {'path': path, 'direction': item['direction'], 'resource_sha256': item['resource_sha256'],
                              'page_sha256': hashlib.sha256(document.encode('utf-8')).hexdigest()}
    return paths, provenance
