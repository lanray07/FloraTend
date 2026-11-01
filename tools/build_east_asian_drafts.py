"""Emit assistant-authored Korean/Chinese resource drafts. No API or approval.

The checked-in table is editable source; placeholder and source coverage checks
fail before emitting resources. Voice dictionaries are separately editable JSON.
"""
import hashlib
import json
from pathlib import Path
from localize import extract, emit_resources, validate

ROOT = Path(__file__).resolve().parents[1]
TAGS = ['ko-KR', 'zh-CN', 'zh-TW']


def main():
    source_path = ROOT / 'app/src/main/res/values/strings.xml'
    source = extract(source_path)
    rows = {}
    for line in (ROOT / 'localization/drafts/east-asian-translations.tsv').read_text(encoding='utf-8').splitlines():
        if not line or line.startswith('#'):
            continue
        fields = line.split('|')
        if len(fields) != 4 or fields[0] in rows:
            raise ValueError('Exactly one unique key and three translations required')
        rows[fields[0]] = fields[1:]
    if set(rows) != set(source):
        raise ValueError(f'Coverage mismatch: missing={set(source)-set(rows)}, extra={set(rows)-set(source)}')
    catalogue = json.loads((ROOT / 'localization/locales.json').read_text(encoding='utf-8'))
    glossary = json.loads((ROOT / 'localization/glossary.json').read_text(encoding='utf-8'))
    terms = glossary['protected_terms']
    report = {'source_sha256': hashlib.sha256(source_path.read_bytes()).hexdigest(),
              'authorship': 'assistant draft; no provider calls', 'release_approved': False, 'locales': []}
    for index, tag in enumerate(TAGS):
        raw = {key: ({'other': rows[key][index]} if value['type']=='plurals' else rows[key][index]) for key,value in source.items()}
        records = {key: {'type':value['type'], 'values':raw[key] if value['type']=='plurals' else {'value':raw[key]}} for key,value in source.items()}
        errors = validate(source, records, terms, tag, glossary.get('branded_values', []))
        if errors:
            raise ValueError(errors)
        config = next(item for item in catalogue if item['locale']==tag)
        output = ROOT / 'app/src/main/res' / config['android_folder'] / 'strings.xml'
        emit_resources(records, output)
        assert not validate(source, extract(output), terms, tag, glossary.get('branded_values', []))
        (ROOT / f'localization/drafts/global-{tag}.json').write_text(json.dumps(raw, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
        voice = json.loads((ROOT / f'localization/voice/{tag}.json').read_text(encoding='utf-8'))
        lines = ['# Assistant-generated command draft; linguistic/device review pending.']
        for key, examples in voice.items():
            for number, text in enumerate(examples):
                escaped = text.replace('\\','\\\\')
                if escaped.startswith(' '): escaped='\\ '+escaped[1:]
                lines.append(f'{key}.{number}={escaped}')
        (ROOT / f'app/src/main/assets/voice/{tag}.properties').write_text('\n'.join(lines)+'\n', encoding='utf-8')
        report['locales'].append({'locale':tag, 'resources':len(records), 'xml_placeholder_validation':'PASS', 'human_review':'PENDING'})
    (ROOT / 'localization/drafts/group-east-asian.json').write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
