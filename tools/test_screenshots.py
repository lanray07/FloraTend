from pathlib import Path
import struct
import tempfile
import unittest
import zlib

from screenshots import SIGNATURE, build_manifest, main, validate_png


def chunk(kind, payload):
    return struct.pack('>I', len(payload)) + kind + payload + struct.pack('>I', zlib.crc32(kind + payload) & 0xffffffff)


def png(width=2, height=3):
    return (SIGNATURE + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)) +
            chunk(b'IDAT', zlib.compress((b'\x00' + b'\x66' * width * 3) * height)) + chunk(b'IEND', b''))


class ScreenshotTests(unittest.TestCase):
    def test_valid_dimensions_and_crc(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / 'capture.png'
            path.write_bytes(png(1200, 1920))
            result = validate_png(path)
            self.assertEqual((result['width'], result['height']), (1200, 1920))
            self.assertTrue(result['store_dimensions_valid'])

    def test_corruption_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / 'capture.png'
            content = bytearray(png())
            content[20] ^= 1
            path.write_bytes(content)
            with self.assertRaisesRegex(ValueError, 'CRC'):
                validate_png(path)

    def test_truncated_and_zero_dimensions_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / 'capture.png'
            for content in (png()[:-5], png(0, 3)):
                path.write_bytes(content)
                with self.assertRaises(ValueError):
                    validate_png(path)

    def test_missing_captures_never_create_fake_compositions(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            result = build_manifest(directory / 'captures', directory / 'output', {})
            self.assertEqual(len(result), 8)
            self.assertTrue(all(row['status'] == 'PENDING_CAPTURE' for row in result))
            self.assertEqual(list((directory / 'output').glob('*.svg')), [])

    def test_actual_capture_creates_only_corresponding_previews(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            captures = directory / 'captures'
            captures.mkdir()
            (captures / '01-garden.png').write_bytes(png(1200, 1920))
            result = build_manifest(captures, directory / 'output', {})
            self.assertEqual(sum(row['status'] == 'PREVIEW_ONLY' for row in result), 2)
            self.assertTrue(all(row['production_png'] is None for row in result))
            self.assertIn('data:image/png;base64,', (directory / 'output/01-hero-en-GB-tablet-preview.svg').read_text())

    def test_unapproved_incomplete_production_fails(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            self.assertEqual(main(['--production', '--captures', str(directory / 'captures'),
                '--output', str(directory / 'output'), '--config', str(directory / 'absent.json')]), 1)


if __name__ == '__main__':
    unittest.main(verbosity=2)
