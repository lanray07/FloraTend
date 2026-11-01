"""Verify committed real-capture provenance and every localized PNG without Pillow."""
import hashlib
import json
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET
import tempfile
from unittest.mock import patch
from PIL import Image, ImageChops, ImageOps

from screenshots import validate_png
from compose_localized_screenshots import write_contact_sheets

ROOT = Path(__file__).resolve().parents[1]
SCENES = {'01-hero', '02-watering', '03-library', '04-calendar',
          '05-growth', '06-voice', '07-offline', '08-insights'}


class ContactSheetWriteTests(unittest.TestCase):
    def prepare(self,root):
        folder=root/'en-GB';folder.mkdir()
        for scene in SCENES:
            Image.new('RGB',(32,20),'green').save(folder/(scene+'.png'))
        destination=folder/'contact-sheet.jpg';destination.write_bytes(b'previous-complete-sheet')
        return destination

    def test_transient_replacement_denial_preserves_previous_sheet_then_succeeds(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);destination=self.prepare(root);original=Path.replace;attempts=[]
            def replace(path,target):
                attempts.append(path)
                if len(attempts)==1:
                    self.assertEqual(b'previous-complete-sheet',destination.read_bytes())
                    error=PermissionError('controlled temporary denial');error.winerror=5;raise error
                return original(path,target)
            with patch.object(Path,'replace',replace),patch('compose_localized_screenshots.time.sleep'):
                rows=write_contact_sheets(root,['en-GB'])
            self.assertEqual(1,rows[0]['replacementRetries'])
            with Image.open(destination) as sheet:self.assertEqual((1280,400),sheet.size)

    def test_persistent_denial_fails_and_preserves_previous_sheet(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);destination=self.prepare(root)
            error=PermissionError('controlled persistent denial');error.winerror=5
            with patch.object(Path,'replace',side_effect=error) as replace,patch('compose_localized_screenshots.time.sleep'):
                with self.assertRaises(PermissionError):write_contact_sheets(root,['en-GB'])
            self.assertEqual(6,replace.call_count)
            self.assertEqual(b'previous-complete-sheet',destination.read_bytes())


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
        self.assertEqual(self.manifest['raw_app_locales'], len(manifest['locales']))
        if manifest['versionCode']>=10:
            catalogue=json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8'))
            self.assertEqual({r['locale'] for r in catalogue},{r['locale'] for r in manifest['locales']})
        self.assertIn('none', (run / 'network-state.txt').read_text(encoding='utf-8-sig'))
        self.assertIn('OK (1 test)', (run / 'instrumentation.txt').read_text(encoding='utf-8-sig'))
        for locale in manifest['locales']:
            self.assertEqual(SCENES, {s['scene'] for s in locale['scenes']})
            for scene in locale['scenes']:
                self.assertTrue((run / scene['file']).is_file())

    def test_export_preserves_entire_native_capture_without_cropping(self):
        # Compare the actual exported pixels, not just compositor declarations.
        # Human wording/readability approval remains a separate release gate.
        for row in self.manifest['screenshots']:
            with self.subTest(locale=row['listing_locale'], scene=row['scene']):
                with Image.open(ROOT / row['capture']) as raw:
                    expected = ImageOps.contain(raw.convert('RGB'), tuple(row['native_frame_max_size']), Image.Resampling.LANCZOS)
                x, y, right, bottom = row['native_frame_bounds']
                self.assertEqual((expected.width, expected.height), (right-x, bottom-y))
                self.assertLessEqual(x + expected.width, 2560)
                self.assertLessEqual(y + expected.height, 1600)
                with Image.open(ROOT / row['export']) as exported:
                    actual = exported.convert('RGB').crop((x, y, x + expected.width, y + expected.height))
                self.assertIsNone(ImageChops.difference(expected, actual).getbbox())

    def test_all_supporting_captions_use_localized_app_resource(self):
        for row in self.manifest['screenshots']:
            with self.subTest(locale=row['listing_locale'], scene=row['scene']):
                source=ROOT / row['caption_resource_source']
                self.assertEqual(row['caption_resource_sha256'], hashlib.sha256(source.read_bytes()).hexdigest())
                strings={n.attrib['name']:''.join(n.itertext()).replace("\\'", "'").replace('\\"','"') for n in ET.parse(source).getroot() if n.tag=='string'}
                self.assertEqual(strings[row['caption_resource_key']],row['supporting_caption'])
                self.assertTrue(row['supporting_caption'].strip())
                self.assertLessEqual(len(row['caption_lines']),2)
                self.assertNotIn('%',row['supporting_caption'])
                self.assertEqual(36,row['caption_font_px'])
                self.assertEqual(len(row['caption_lines']),len(row['caption_line_bounds']))
                for left, top, right, bottom in row['caption_line_bounds']:
                    self.assertGreaterEqual(left,96)
                    self.assertGreaterEqual(top,350)
                    self.assertLessEqual(right,2456)
                    self.assertLess(bottom,450)


if __name__ == '__main__':
    unittest.main()
