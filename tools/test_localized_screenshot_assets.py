"""Verify committed real-capture provenance and every localized PNG without Pillow."""
import hashlib
import json
from pathlib import Path
import unittest

from screenshots import validate_png

ROOT = Path(__file__).resolve().parents[1]
SCENES = {'01-hero', '02-watering', '03-library', '04-calendar',
          '05-growth', '06-voice', '07-offline', '08-insights'}


class LocalizedScreenshotAssets(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((ROOT / 'assets/screenshots/localized/localized-manifest.json').read_text(encoding='utf-8-sig'))

    def test_complete_unique_locale_scene_matrix(self):
        rows = self.manifest['screenshots']
        headlines = json.loads((ROOT / 'assets/screenshots/headlines.json').read_text(encoding='utf-8-sig'))
        locales = {r['listing_locale'] for r in rows}
        self.assertEqual(15, len(locales))
        self.assertEqual(120, len(rows))
        self.assertEqual(120, len({(r['listing_locale'], r['scene']) for r in rows}))
        self.assertEqual(set(headlines['locales']), locales)
        for locale in locales:
            self.assertEqual(SCENES, {r['scene'] for r in rows if r['listing_locale'] == locale})
        self.assertFalse(self.manifest['human_language_approval'])

    def test_png_integrity_and_native_capture_provenance(self):
        for row in self.manifest['screenshots']:
            with self.subTest(locale=row['listing_locale'], scene=row['scene']):
                capture = ROOT / row['capture']
                export = ROOT / row['export']
                for path in (capture, export):
                    self.assertTrue(path.resolve().is_relative_to(ROOT.resolve()))
                self.assertEqual(row['capture_sha256'], hashlib.sha256(capture.read_bytes()).hexdigest())
                info = validate_png(export)
                self.assertEqual(row['export_sha256'], info['sha256'])
                self.assertEqual((2560, 1600), (info['width'], info['height']))
                native = self.manifest['region_mapping'].get(row['listing_locale'], row['listing_locale'])
                self.assertEqual(native, row['native_app_locale'])
                self.assertIn('/' + native + '/', row['capture'])
                self.assertTrue(row['actual_rendered_ui'] and row['offline_capture'])
                self.assertEqual('DRAFT_PENDING_HUMAN_REVIEW', row['status'])

    def test_all_app_locales_captured_offline(self):
        source = ROOT / self.manifest['screenshots'][0]['capture']
        run = source.parent.parent
        manifest = json.loads((run / 'capture-manifest.json').read_text(encoding='utf-8-sig'))
        self.assertEqual(19, len(manifest['locales']))
        self.assertIn('none', (run / 'network-state.txt').read_text(encoding='utf-8-sig'))
        self.assertIn('OK (1 test)', (run / 'instrumentation.txt').read_text(encoding='utf-8-sig'))
        for locale in manifest['locales']:
            self.assertEqual(SCENES, {s['scene'] for s in locale['scenes']})
            for scene in locale['scenes']:
                self.assertTrue((run / scene['file']).is_file())


if __name__ == '__main__':
    unittest.main()
