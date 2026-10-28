"""Deterministically rasterize FloraTend's original SVG paths with Pillow.

Run: python tools/render_store_icon.py
This small renderer supports the M/C/Q/semicircular-a/z paths used by the master,
not arbitrary SVG. Original colors and Bezier geometry are read from the SVG.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
from pathlib import Path
import re
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]


def sample_path(data, steps=128):
    tokens = re.findall(r'[A-Za-z]|[-+]?(?:\d*\.\d+|\d+)(?:[eE][-+]?\d+)?', data)
    position = 0j
    first = None
    points = []
    index = 0
    command = None
    while index < len(tokens):
        if tokens[index].isalpha():
            command = tokens[index]
            index += 1
        if command.lower() == 'z':
            points.append(first)
            position = first
            command = None
            continue
        count = {'m': 2, 'c': 6, 'q': 4, 'a': 7}.get(command.lower())
        if count is None:
            raise ValueError('SVG master changed: unsupported path command')
        values = list(map(float, tokens[index:index+count]))
        if len(values) != count:
            raise ValueError('Incomplete SVG path')
        index += count
        relative = command.islower()
        def coordinate(x, y):
            return complex(x, y) + (position if relative else 0j)
        if command.lower() == 'm':
            position = coordinate(*values)
            first = position
            points.append(position)
        elif command.lower() == 'c':
            start = position
            a, b, end = [coordinate(*values[k:k+2]) for k in (0, 2, 4)]
            points.extend((1-t)**3*start + 3*(1-t)**2*t*a + 3*(1-t)*t*t*b + t**3*end
                          for t in (k/steps for k in range(1, steps+1)))
            position = end
        elif command.lower() == 'q':
            start, control, end = position, coordinate(*values[:2]), coordinate(*values[2:])
            points.extend((1-t)**2*start + 2*(1-t)*t*control + t*t*end
                          for t in (k/steps for k in range(1, steps+1)))
            position = end
        else:
            rx, ry, rotation, large, sweep, x, y = values
            end = coordinate(x, y)
            # Current original icon has one exact circular semicircle.
            if rotation or rx != ry or not math.isclose(abs(end-position), 2*rx):
                raise ValueError('SVG master changed: unsupported elliptical arc')
            centre = (position + end)/2
            angle = math.atan2((position-centre).imag, (position-centre).real)
            delta = math.pi if sweep else -math.pi
            points.extend(centre + rx*complex(math.cos(angle+delta*k/steps), math.sin(angle+delta*k/steps))
                          for k in range(1, steps+1))
            position = end
    return points


def render(source, size, supersampling=4):
    svg = ET.parse(source).getroot()
    if svg.get('viewBox') != '0 0 108 108':
        raise ValueError('Unexpected icon viewport')
    actual = size * supersampling
    factor = actual/108
    image = Image.new('RGBA', (actual, actual), (0, 0, 0, 0))
    drawing = ImageDraw.Draw(image)
    ns = {'s': 'http://www.w3.org/2000/svg'}
    background = svg.find('s:rect', ns)
    drawing.rounded_rectangle((0, 0, actual-1, actual-1), radius=actual*0.20, fill=background.get('fill'))
    for path in svg.findall('s:path', ns):
        points = [(p.real*factor, p.imag*factor) for p in sample_path(path.get('d'))]
        if path.get('fill', 'black') != 'none':
            drawing.polygon(points, fill=path.get('fill', 'black'))
        if path.get('stroke'):
            stroke_width = float(path.get('stroke-width', '1'))*factor
            drawing.line(points, fill=path.get('stroke'), width=round(stroke_width), joint='curve')
            if path.get('stroke-linecap') == 'round':
                radius = stroke_width/2
                # Union of sampled discs avoids Pillow's tiny-segment miter gaps.
                for x, y in points:
                    drawing.ellipse((x-radius, y-radius, x+radius, y+radius), fill=path.get('stroke'))
    return image.resize((size, size), Image.Resampling.LANCZOS)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source', type=Path, default=ROOT/'assets/app_icon_master.svg')
    parser.add_argument('--output', type=Path, default=ROOT/'assets/store')
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    manifest = {'source': str(args.source), 'source_sha256': hashlib.sha256(args.source.read_bytes()).hexdigest(),
                'method': 'Original SVG cubic/quadratic Bezier and circular arc sampling; Pillow 4x antialiasing; transparent rounded background corners',
                'assets': []}
    for size in (1024, 512, 114):
        path = args.output/f'floratend-icon-{size}.png'
        render(args.source, size).save(path, format='PNG', optimize=True)
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            assert image.format == 'PNG' and image.mode == 'RGBA'
            assert image.size == (size, size)
            assert image.getpixel((0, 0))[3] == 0
            assert image.getpixel((size//2, size//2))[3] == 255
            manifest['assets'].append({'path': str(path), 'width': size, 'height': size,
                'format': image.format, 'mode': image.mode, 'transparent_corners': True,
                'opaque_logo_centre': True, 'integrity': 'PASS',
                'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size})
    (args.output/'icon-validation.json').write_text(json.dumps(manifest, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(manifest, indent=2))


if __name__ == '__main__':
    main()
