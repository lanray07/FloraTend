import json
from pathlib import Path
import unittest
from localize import extract,extract_bundle,placeholders

ROOT=Path(__file__).resolve().parents[1]

class PurchasePrivacyCopyTests(unittest.TestCase):
    def test_every_app_locale_discloses_both_verification_providers_without_placeholders(self):
        locales=json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8'))
        self.assertEqual(22,len(locales))
        for locale in locales:
            with self.subTest(locale=locale['locale']):
                folder=ROOT/'app/src/main/res'/locale['android_folder']
                records=extract(folder/'privacy.xml')
                self.assertEqual({'purchase_verification_privacy'},set(records))
                copy=records['purchase_verification_privacy']['values']['value']
                for provider in ['Amazon','Cloudflare','HTTPS']: self.assertIn(provider,copy)
                self.assertFalse(placeholders(copy))
                self.assertIn('purchase_verification_privacy',extract_bundle(folder/'strings.xml'))

if __name__=='__main__':unittest.main()
