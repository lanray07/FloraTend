import json
from pathlib import Path
import tempfile
import unittest
from add_identification_drafts import KEYS, main as regenerate
from localize import extract,extract_bundle,bundle_digest,placeholders

ROOT=Path(__file__).resolve().parents[1]

class IdentificationCopyTests(unittest.TestCase):
    def test_all_22_locales_have_identical_keys_and_score_placeholder(self):
        locales=json.loads((ROOT/'localization/locales.json').read_text())
        self.assertEqual(22,len(locales))
        for locale in locales:
            path=ROOT/'app/src/main/res'/locale['android_folder']/'identification.xml'
            records=extract(path)
            self.assertEqual(set(KEYS),records.keys(),locale['locale'])
            self.assertEqual({'%1$s':1},dict(placeholders(records['identification_score']['values']['value'])))
            self.assertIn('Pl@ntNet',records['identification_consent']['values']['value'])
            for record in records.values():self.assertTrue(record['values']['value'].strip('" ').strip())
    def test_modular_copy_changes_review_digest_and_rejects_duplicates(self):
        with tempfile.TemporaryDirectory() as folder:
            path=Path(folder)/'strings.xml';path.write_text('<resources><string name="base">Hello</string></resources>')
            old=bundle_digest(path)
            extra=path.with_name('identification.xml');extra.write_text('<resources><string name="send">Send</string></resources>')
            self.assertNotEqual(old,bundle_digest(path));self.assertEqual({'base','send'},extract_bundle(path).keys())
            extra.write_text('<resources><string name="base">Duplicate</string></resources>')
            with self.assertRaises(ValueError):extract_bundle(path)

if __name__=='__main__':unittest.main()
