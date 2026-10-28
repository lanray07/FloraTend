"""Development-only Android localisation pipeline. No credentials enter Android resources.

python tools/localize.py extract
python tools/localize.py translate --locale de-DE  # DEEPL_AUTH_KEY in environment
python tools/localize.py validate                 # nonzero for pending/invalid locales
"""
from __future__ import annotations

import argparse
from collections import Counter
import hashlib
import html
import json
import os
from pathlib import Path
import re
import sys
from urllib.request import Request, urlopen
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
PLACEHOLDER = re.compile(r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]|\{[a-zA-Z_][a-zA-Z_0-9]*\}")
QUANTITIES = {'zero', 'one', 'two', 'few', 'many', 'other'}


def read_json(path: Path, default):
    return json.loads(path.read_text(encoding='utf-8')) if path.exists() else default


def write_json(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def extract(path: Path):
    root = ET.parse(path).getroot()
    if root.tag != 'resources':
        raise ValueError('Android resources root is required')
    records = {}
    for entry in root:
        if entry.get('translatable') == 'false' or entry.tag not in ('string', 'plurals'):
            continue
        name = entry.get('name')
        if not name or name in records:
            raise ValueError(f'Duplicate or missing resource name: {name}')
        if entry.tag == 'string':
            if len(entry):
                raise ValueError(f'{name}: styled strings need explicit translation handling')
            records[name] = {'type': 'string', 'values': {'value': entry.text or ''}}
        else:
            vals = {}
            for item in entry:
                quantity = item.get('quantity')
                if item.tag != 'item' or quantity not in QUANTITIES or quantity in vals:
                    raise ValueError(f'{name}: invalid plural quantity')
                vals[quantity] = item.text or ''
            if 'other' not in vals:
                raise ValueError(f'{name}: plural other is required')
            records[name] = {'type': 'plurals', 'values': vals}
    return records


def placeholders(text):
    return Counter(PLACEHOLDER.findall(text))


def protect(text, terms):
    parts = sorted(set(terms), key=len, reverse=True)
    pattern = re.compile('|'.join([PLACEHOLDER.pattern] + [re.escape(x) for x in parts]))
    tokens = []
    def replace(match):
        tokens.append(match.group(0))
        return f'<x id="{len(tokens)-1}" />'
    chunks = []
    position = 0
    for match in pattern.finditer(text):
        chunks.extend([html.escape(text[position:match.start()]), replace(match)])
        position = match.end()
    chunks.append(html.escape(text[position:]))
    return ''.join(chunks), tokens


def restore(text, tokens):
    seen = []
    def replace(match):
        idx = int(match.group(1))
        if idx >= len(tokens):
            raise ValueError('Provider returned an unknown protected token')
        seen.append(idx)
        return tokens[idx]
    text = re.sub(r'<x\s+id=[\"\'](\d+)[\"\']\s*/>', replace, text)
    if sorted(seen) != list(range(len(tokens))):
        raise ValueError('Provider changed, omitted, or duplicated protected tokens')
    return html.unescape(text)


class DeepL:
    def __init__(self, key=None, endpoint=None):
        self.key = key or os.environ.get('DEEPL_AUTH_KEY')
        self.endpoint = endpoint or os.environ.get('DEEPL_ENDPOINT', 'https://api-free.deepl.com')
        if self.endpoint not in ('https://api-free.deepl.com', 'https://api.deepl.com'):
            raise ValueError('Only official HTTPS DeepL endpoints are allowed')

    def translate(self, text, target):
        if not self.key:
            raise RuntimeError('BLOCKED: DEEPL_AUTH_KEY is not configured')
        payload = {'text': [text], 'target_lang': target, 'source_lang': 'EN',
                   'tag_handling': 'xml', 'ignore_tags': ['x'], 'preserve_formatting': True}
        request = Request(self.endpoint + '/v2/translate',
                          data=json.dumps(payload).encode('utf-8'), method='POST',
                          headers={'Authorization': 'DeepL-Auth-Key ' + self.key,
                                   'Content-Type': 'application/json'})
        with urlopen(request, timeout=45) as response:
            return json.load(response)['translations'][0]['text']


def translate_records(source, locale, target, provider, cache, terms):
    translated = {}
    for name, record in source.items():
        values = {}
        for quantity, original in record['values'].items():
            digest = hashlib.sha256(json.dumps([locale, target, original, terms], ensure_ascii=False).encode()).hexdigest()
            if digest not in cache:
                protected, tokens = protect(original, terms)
                candidate = restore(provider.translate(protected, target), tokens)
                if placeholders(candidate) != placeholders(original):
                    raise ValueError(f'{name}: provider changed placeholders')
                cache[digest] = candidate
            values[quantity] = cache[digest]
        translated[name] = {'type': record['type'], 'values': values}
    return translated


def emit_resources(records, path):
    root = ET.Element('resources')
    for name, record in records.items():
        element = ET.SubElement(root, record['type'], name=name)
        for quantity, value in record['values'].items():
            # Quote values for Android apostrophe handling; preserve literal Android escapes.
            encoded = '"' + value.replace('"', '\\"') + '"'
            if record['type'] == 'string':
                element.text = encoded
            else:
                ET.SubElement(element, 'item', quantity=quantity).text = encoded
    ET.indent(root)
    path.parent.mkdir(parents=True, exist_ok=True)
    ET.ElementTree(root).write(path, encoding='utf-8', xml_declaration=True)


def validate(source, candidate, terms, locale):
    errors = []
    required = {'ar': {'zero', 'one', 'two', 'few', 'many', 'other'},
                'pl-PL': {'one', 'few', 'many', 'other'}}.get(locale, {'other'})
    for name, original in source.items():
        entry = candidate.get(name)
        if not entry:
            errors.append(f'{name}: missing translation')
            continue
        if entry['type'] != original['type']:
            errors.append(f'{name}: resource type changed')
            continue
        if entry['type'] == 'plurals' and not required.issubset(entry['values']):
            errors.append(f'{name}: locale plural categories missing {sorted(required - set(entry["values"]))}')
        for q, text in entry['values'].items():
            baseline = original['values'].get(q, original['values'].get('other', ''))
            clean = text[1:-1] if text.startswith('"') and text.endswith('"') else text
            clean = clean.replace('\\"', '"')
            if not clean.strip():
                errors.append(f'{name}/{q}: empty translation')
            if placeholders(clean) != placeholders(baseline):
                errors.append(f'{name}/{q}: placeholder mismatch')
            for term in terms:
                if baseline.count(term) != clean.count(term):
                    errors.append(f'{name}/{q}: protected term {term} changed')
            if clean == baseline and locale.split('-')[0] != 'en' and clean not in terms:
                errors.append(f'{name}/{q}: unchanged English requires review')
    errors.extend(f'{name}: obsolete resource' for name in candidate.keys() - source.keys())
    return errors


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['extract', 'translate', 'validate'])
    parser.add_argument('--locale')
    parser.add_argument('--source', type=Path, default=ROOT / 'app/src/main/res/values/strings.xml')
    args = parser.parse_args(argv)
    source = extract(args.source)
    directory = ROOT / 'localization'
    write_json(directory / 'source.json', source)
    config = read_json(directory / 'locales.json', [])
    terms = read_json(directory / 'glossary.json', {})['protected_terms']
    review = read_json(directory / 'review.json', {})
    if args.action == 'extract':
        print(f'Extracted {len(source)} translatable resources')
        return 0
    selected = [x for x in config if not args.locale or x['locale'] == args.locale]
    if not selected:
        parser.error('Unknown locale')
    if args.action == 'translate':
        cache_path = directory / 'cache.json'
        cache = read_json(cache_path, {})
        provider = DeepL()
        for locale in selected:
            if locale['locale'] == 'en-GB':
                continue
            if not locale['provider_target']:
                raise RuntimeError(f'BLOCKED: {locale["locale"]} needs a regional translator/provider configuration')
            try:
                records = translate_records(source, locale['locale'], locale['provider_target'], provider, cache, terms)
            finally:
                write_json(cache_path, cache)
            path = ROOT / 'app/src/main/res' / locale['android_folder'] / 'strings.xml'
            emit_resources(records, path)
            review[locale['locale']] = {'status': 'machine_pending_human_review', 'source_sha256': hashlib.sha256(args.source.read_bytes()).hexdigest(), 'reviewer': None, 'reviewed_at': None}
            write_json(directory / 'review.json', review)
        print('Generated resources; human linguistic and UI review required before approval')
        return 0
    results = []
    for locale in selected:
        path = ROOT / 'app/src/main/res' / locale['android_folder'] / 'strings.xml'
        try:
            errors = validate(source, extract(path), terms, locale['locale']) if path.exists() else ['Resource file does not exist']
        except (ValueError, ET.ParseError) as error:
            errors = [str(error)]
        status = review.get(locale['locale'], {}).get('status', 'pending')
        digest = hashlib.sha256(args.source.read_bytes()).hexdigest()
        review_record = review.get(locale['locale'], {})
        approved = (status == 'human_approved' and review_record.get('reviewer') and review_record.get('reviewed_at') and review_record.get('source_sha256') == digest)
        results.append({'locale': locale['locale'], 'errors': errors, 'review_status': status,
                        'status': 'PASS' if not errors and approved else 'NOT TESTED' if not errors else 'FAIL'})
    write_json(directory / 'validation-report.json', results)
    print(json.dumps(results, ensure_ascii=False, indent=2))
    return 0 if all(x['status'] == 'PASS' for x in results) else 1


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as error:
        # Do not print HTTP request headers or provider exception bodies containing secrets.
        print(f'{type(error).__name__}: {error if isinstance(error, (ValueError, RuntimeError, FileNotFoundError)) else "Provider or resource operation failed; inspect local configuration"}', file=sys.stderr)
        sys.exit(2)
