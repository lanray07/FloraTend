"""Verify actual phone-layout exports, complete native pixels and honest device scope."""
from pathlib import Path
import unittest,json,hashlib
from PIL import Image,ImageChops,ImageOps
from screenshots import validate_png
ROOT=Path(__file__).resolve().parents[1]

class PhoneScreenshotAssets(unittest.TestCase):
 @classmethod
 def setUpClass(cls):
  cls.report=json.loads((ROOT/'assets/screenshots/phone/localized-manifest.json').read_text(encoding='utf-8'))
 def test_complete_localized_phone_matrix_and_scope(self):
  r=self.report
  self.assertEqual('phone',r['device_kind']);self.assertGreaterEqual(r['raw_app_locales'],14);self.assertEqual(15,r['store_locales']);self.assertEqual(120,r['images'])
  self.assertIn(r['source_capture_suite_result'],['PASS','FAIL']);self.assertEqual(r['raw_app_locales']==22,r['full_app_locale_capture_complete'])
  self.assertFalse(r['human_language_approval']);self.assertTrue(r['device']['simulated_phone_configuration'])
  self.assertLess(r['capture_configuration']['screenWidthDp'],600)
  self.assertEqual(120,len({(x['listing_locale'],x['scene']) for x in r['screenshots']}))
  for x in r['screenshots']:
   self.assertEqual('DRAFT_PENDING_HUMAN_REVIEW',x['status']);self.assertTrue(x['actual_rendered_ui']);self.assertTrue(x['offline_capture'])
 def test_phone_images_are_valid_and_preserve_complete_native_frame(self):
  for x in self.report['screenshots']:
   native=ROOT/x['capture'];export=ROOT/x['export'];self.assertEqual(x['capture_sha256'],hashlib.sha256(native.read_bytes()).hexdigest());self.assertEqual(x['export_sha256'],hashlib.sha256(export.read_bytes()).hexdigest())
   check=validate_png(export);self.assertTrue(check['store_dimensions_valid']);self.assertEqual((1600,2560),(check['width'],check['height']))
   with Image.open(native) as image,Image.open(export) as result:
    expected=ImageOps.contain(image.convert('RGB'),tuple(x['native_frame_max_size']),Image.Resampling.LANCZOS)
    actual=result.convert('RGB').crop(tuple(x['native_frame_bounds']));self.assertEqual(expected.size,actual.size);self.assertIsNone(ImageChops.difference(expected,actual).getbbox())

if __name__=='__main__':unittest.main()
