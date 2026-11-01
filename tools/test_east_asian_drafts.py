import json
from pathlib import Path
import unittest
from localize import extract, validate

ROOT = Path(__file__).resolve().parents[1]


class EastAsianDraftTests(unittest.TestCase):
    def test_complete_resources_preserve_placeholders_and_brand(self):
        source = extract(ROOT/'app/src/main/res/values/strings.xml')
        glossary = json.loads((ROOT/'localization/glossary.json').read_text(encoding='utf-8'))
        catalogue = json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8'))
        for tag in ['ko-KR','zh-CN','zh-TW']:
            item=next(x for x in catalogue if x['locale']==tag)
            candidate=extract(ROOT/'app/src/main/res'/item['android_folder']/'strings.xml')
            self.assertEqual(set(source),set(candidate))
            self.assertEqual([],validate(source,candidate,glossary['protected_terms'],tag,glossary['branded_values']))
            voice=json.loads((ROOT/f'localization/voice/{tag}.json').read_text(encoding='utf-8'))
            self.assertTrue({'add','due','calendar','read','water_prefix','water_suffix','location_prefix'}.issubset(voice))

    def test_no_human_approval_is_inferred(self):
        report=json.loads((ROOT/'localization/drafts/group-east-asian.json').read_text(encoding='utf-8'))
        self.assertFalse(report['release_approved'])
        self.assertTrue(all(item['human_review']=='PENDING' for item in report['locales']))
