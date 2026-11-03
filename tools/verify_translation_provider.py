"""One actual Free-endpoint translation, without modifying app resources or saving credentials.

Invoked through verify_deepl_key.ps1 after private key configuration. No request is
made without a configured key. Exceptions are reported by class, never HTTP bodies.
"""
import argparse
import json
import os
from datetime import datetime, timezone
from pathlib import Path
from localize import DeepL, placeholders, protect, restore


def verify():
    plan = os.environ.get('DEEPL_PLAN', 'free')
    if plan not in ('free', 'developer'):
        raise ValueError('Only free plans may be verified')
    source = 'FloraTend: Check %1$s before watering.'
    protected, tokens = protect(source, ['FloraTend'])
    translated = restore(DeepL(endpoint='https://api-free.deepl.com').translate(protected, 'DE'), tokens)
    if translated == source or placeholders(translated) != placeholders(source) or 'FloraTend' not in translated:
        raise ValueError('Translation/protected-token check failed')
    return {'execution_date': datetime.now(timezone.utc).isoformat(), 'provider': 'DeepL API ' + ('Developer' if plan == 'developer' else 'Free'),
            'endpoint': 'https://api-free.deepl.com', 'target': 'DE', 'source': source, 'translation': translated,
            'placeholderAndBrandPreserved': True, 'actualProviderRequest': True, 'credentialsExported': False,
            'appResourcesModified': False, 'humanApproved': False, 'result': 'PASS'}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=Path(__file__).resolve().parents[1] / 'evidence/final-release/deepl-live.json')
    args = parser.parse_args()
    try:
        report = verify()
    except Exception as error:
        print('Translation verification did not pass: ' + type(error).__name__ + '. Check private configuration; no response body or key is printed.')
        raise SystemExit(1)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('One genuine free-plan translation passed; protected brand/placeholder preserved. Human review is separate.')
