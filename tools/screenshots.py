"""Validate real Android PNG captures and compose editable en-GB preview SVGs.

No PNG screenshot or translated UI is synthesized. Final PNG exports, locale approval,
and per-image visual review are required for --production to succeed.
"""
from __future__ import annotations

import argparse
import base64
import hashlib
from html import escape
import json
from pathlib import Path
import struct
import sys
import zlib

ROOT = Path(__file__).resolve().parents[1]
SIGNATURE = b'\x89PNG\r\n\x1a\n'
AMAZON_LANDSCAPE = {(800, 480), (1024, 600), (1280, 720), (1280, 800),
                    (1920, 1080), (1920, 1200), (2560, 1600)}
SCENES = [
    ('01-hero', '01-garden', 'Plant Care & Watering Tracker', 'Your plants, beautifully organised.'),
    ('02-watering', '02-care', 'Plant Watering Reminders', 'A gentle reminder to check conditions first.'),
    ('03-library', '01-garden', 'Organise Your Houseplants', 'Keep your saved plants together.'),
    ('04-calendar', '09-calendar', 'Plant Care Schedule', 'Plan your next care checks.'),
    ('05-growth', '03-journal', 'Plant Growth Tracker', 'Record notes and your own photographs.'),
    ('06-voice', '04-voice', 'Voice-Powered Plant Care', 'Use supported commands, with a text fallback.'),
    ('07-offline', '08-offline', 'Offline Plant Care Planner', 'Access saved plant records without internet.'),
    ('08-insights', '05-insights', 'Track Your Plant Care Progress', 'Review records from your own collection.'),
]


def validate_png(path):
    """CRC, chunk order, dimensions and actual scanline-length validation; no Pillow."""
    data = Path(path).read_bytes()
    if not data.startswith(SIGNATURE):
        raise ValueError('Invalid PNG signature')
    offset, chunks, compressed = 8, [], bytearray()
    width = height = channels = depth = None
    ended = False
    while offset < len(data):
        if offset + 12 > len(data):
            raise ValueError('Truncated PNG chunk')
        length, = struct.unpack('>I', data[offset:offset+4])
        kind = data[offset+4:offset+8]
        finish = offset + 12 + length
        if finish > len(data):
            raise ValueError('Truncated PNG payload')
        payload = data[offset+8:offset+8+length]
        crc, = struct.unpack('>I', data[offset+8+length:finish])
        if zlib.crc32(kind + payload) & 0xffffffff != crc:
            raise ValueError('PNG CRC mismatch')
        if not chunks and kind != b'IHDR':
            raise ValueError('IHDR must be first')
        if kind == b'IHDR':
            if chunks or length != 13:
                raise ValueError('Invalid or duplicate IHDR')
            width, height, depth, colour, compression, filtering, interlace = struct.unpack('>IIBBBBB', payload)
            if not width or not height or width * height > 50_000_000:
                raise ValueError('Invalid or excessive dimensions')
            channels = {0: 1, 2: 3, 4: 2, 6: 4}.get(colour)
            if channels is None or depth != 8 or compression or filtering or interlace:
                raise ValueError('Only standard 8-bit noninterlaced capture PNGs are supported')
        elif kind == b'IDAT':
            compressed.extend(payload)
        elif kind == b'IEND':
            if length or not compressed or finish != len(data):
                raise ValueError('Invalid IEND or trailing PNG data')
            ended = True
            chunks.append(kind)
            break
        elif kind[0] & 32 == 0 and kind != b'PLTE':
            raise ValueError('Unsupported critical PNG chunk')
        chunks.append(kind)
        offset = finish
    if not ended:
        raise ValueError('Missing PNG IEND')
    row_size = width * channels + 1
    expected = row_size * height
    decompressor = zlib.decompressobj()
    decoded = decompressor.decompress(bytes(compressed), expected + 1)
    if len(decoded) != expected or not decompressor.eof or decompressor.unused_data:
        raise ValueError('Invalid PNG image data length')
    if any(decoded[index] > 4 for index in range(0, len(decoded), row_size)):
        raise ValueError('Invalid PNG scanline filter')
    return {'width': width, 'height': height, 'sha256': hashlib.sha256(data).hexdigest(),
            'bytes': len(data), 'store_dimensions_valid': (max(width, height), min(width, height)) in AMAZON_LANDSCAPE}


def compose_svg(png, headline, caption, width, height):
    embedded = base64.b64encode(Path(png).read_bytes()).decode('ascii')
    # Full-screen capture remains uncropped; the SVG document is an editable composition.
    return f'''<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="1200" height="1920" viewBox="0 0 1200 1920">
  <title>{escape(headline)} — en-GB preview</title>
  <desc>PREVIEW: actual app capture. Locale and claim review required before submission.</desc>
  <rect width="1200" height="1920" fill="#FAF8F2"/>
  <text x="70" y="90" font-family="sans-serif" font-size="30" fill="#315C45">FloraTend</text>
  <text x="70" y="170" font-family="sans-serif" font-size="46" font-weight="700" fill="#2D332E">{escape(headline)}</text>
  <text x="70" y="230" font-family="sans-serif" font-size="28" fill="#315C45">{escape(caption)}</text>
  <image x="150" y="300" width="900" height="1440" preserveAspectRatio="xMidYMid meet" xlink:href="data:image/png;base64,{embedded}"/>
  <text x="70" y="1850" font-family="sans-serif" font-size="24" fill="#315C45">Every plant deserves a little attention.</text>
</svg>'''


def build_manifest(capture_dir, output_dir, config):
    output_dir.mkdir(parents=True, exist_ok=True)
    manifest = []
    review = config.get('reviews', {})
    for scene, capture_name, headline, caption in SCENES:
        capture = capture_dir / (capture_name + '.png')
        row = {'scene': scene, 'locale': 'en-GB', 'device': 'tablet',
               'capture': str(capture), 'composition': None, 'production_png': None,
               'status': 'PENDING_CAPTURE', 'review': 'pending',
               'claims_review': 'pending', 'capture_locale_review': 'pending'}
        if capture.exists():
            try:
                row['capture_validation'] = validate_png(capture)
                if not row['capture_validation']['store_dimensions_valid']:
                    raise ValueError('Capture dimensions outside documented Amazon list')
                destination = output_dir / (scene + '-en-GB-tablet-preview.svg')
                destination.write_text(compose_svg(capture, headline, caption,
                    row['capture_validation']['width'], row['capture_validation']['height']), encoding='utf-8')
                row['composition'] = str(destination)
                row['status'] = 'PREVIEW_ONLY'
                reviewed = review.get(scene, {})
                if (reviewed.get('capture_sha256') == row['capture_validation']['sha256'] and
                        reviewed.get('reviewer') and reviewed.get('date')):
                    for key in ('review', 'claims_review', 'capture_locale_review'):
                        row[key] = reviewed.get(key, 'pending')
                final_png = output_dir / (scene + '-en-GB-tablet.png')
                if final_png.exists():
                    row['export_validation'] = validate_png(final_png)
                    row['production_png'] = str(final_png)
                    if (row['export_validation']['store_dimensions_valid'] and
                            reviewed.get('export_sha256') == row['export_validation']['sha256'] and
                            all(row[key] == 'approved' for key in ('review', 'claims_review', 'capture_locale_review')) and
                            'en-GB' in config.get('approved_locales', [])):
                        row['status'] = 'PASS'
            except (ValueError, OSError, zlib.error) as error:
                row['status'] = 'FAIL'
                row['error'] = str(error)
        manifest.append(row)
    (output_dir / 'screenshot-manifest.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
    return manifest


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--captures', type=Path, default=ROOT / 'evidence/device')
    parser.add_argument('--output', type=Path, default=ROOT / 'assets/screenshots')
    parser.add_argument('--config', type=Path, default=ROOT / 'assets/screenshot-review.json')
    parser.add_argument('--production', action='store_true')
    args = parser.parse_args(argv)
    config = json.loads(args.config.read_text(encoding='utf-8')) if args.config.exists() else {}
    manifest = build_manifest(args.captures, args.output, config)
    print(json.dumps({'scenes': len(manifest), 'passes': sum(x['status'] == 'PASS' for x in manifest),
                      'preview_compositions': sum(bool(x['composition']) for x in manifest),
                      'pending_captures': sum(x['status'] == 'PENDING_CAPTURE' for x in manifest)}, indent=2))
    if args.production and (config.get('approved_locales') != ['en-GB'] or any(x['status'] != 'PASS' for x in manifest)):
        print('BLOCKED: production needs every capture, approved locale, reviewed claims/UI language, and verified PNG export.', file=sys.stderr)
        return 1
    return 0


if __name__ == '__main__':
    sys.exit(main())
