"""Measure actual Amazon checkout presentation without attaching UiAutomation.

Open Settings, verify an inactive LAT entitlement and its Amazon price, then
provide the observed Unlock button centre. Never presses Pay or clears data.
Raw screenshots and optional diagnostic logcat stay in the private Android directory.
"""
import argparse
from io import BytesIO
import json
import os
from pathlib import Path
import subprocess
import time

from PIL import Image, ImageChops

ADB = str(Path(os.environ.get('LOCALAPPDATA', '')) / 'Android/Sdk/platform-tools/adb.exe')


def adb(*args):
    return subprocess.check_output([ADB, *args], timeout=20)


def checkout_pixels(capture):
    picture = Image.open(BytesIO(capture)).convert('RGB')
    # Screenshots are retained for human review; this is a presentation signal,
    # not receipt verification or proof that a payment completed.
    red, green, blue = picture.split()
    red_mask = red.point([255 if value > 210 else 0 for value in range(256)])
    green_mask = green.point([255 if 125 < value < 220 else 0 for value in range(256)])
    blue_mask = blue.point([255 if value < 75 else 0 for value in range(256)])
    # C-backed masks preserve the exact original predicate without a Python loop per pixel.
    return ImageChops.multiply(ImageChops.multiply(red_mask, green_mask), blue_mask).histogram()[255]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--confirm-priced-inactive-lat-screen', action='store_true', required=True)
    parser.add_argument('--tap', nargs=2, type=int, required=True)
    parser.add_argument('--timeout', type=int, default=60)
    parser.add_argument('--limit', type=float, default=5)
    parser.add_argument('--cancel-visible', action='store_true')
    parser.add_argument('--name', required=True)
    parser.add_argument('--capture-private-logcat', action='store_true', help='Opt in only when raw diagnostic logs are needed; never publish them')
    args = parser.parse_args()
    if not args.name.replace('-', '').isalnum() or not 5 <= args.timeout <= 120:
        parser.error('Use an alphanumeric run name and a timeout from 5 to 120 seconds')
    package = adb('shell', 'dumpsys', 'package', 'com.floratend.app').decode()
    if 'installerPackageName=com.amazon.venezia' not in package or 'DEBUGGABLE' in package:
        raise RuntimeError('Actual nondebug Amazon-delivered build required')
    activity = adb('shell', 'dumpsys', 'activity', 'activities').decode()
    resumed = next((line for line in activity.splitlines() if 'mResumedActivity:' in line), '')
    if 'com.floratend.app/.MainActivity' not in resumed:
        raise RuntimeError('FloraTend must be foreground before timing')
    folder = Path.home() / '.android/floratend-checkout-timing' / args.name
    folder.mkdir(parents=True, exist_ok=False)
    (folder / 'before.png').write_bytes(adb('exec-out', 'screencap', '-p'))
    started = time.monotonic()
    adb('shell', 'input', 'tap', *map(str, args.tap))
    samples = []
    visible = None
    while time.monotonic() - started < args.timeout:
        capture_started = round(time.monotonic() - started, 3)
        capture = adb('exec-out', 'screencap', '-p')
        elapsed = round(time.monotonic() - started, 3)
        detector_started = time.monotonic()
        pixels = checkout_pixels(capture)
        detector_seconds = round(time.monotonic() - detector_started, 3)
        (folder / f'{len(samples):03d}.png').write_bytes(capture)
        samples.append({'elapsedSeconds': elapsed, 'captureStartedSeconds': capture_started,
                        'captureDurationSeconds': round(elapsed-capture_started,3),
                        'detectorSeconds': detector_seconds, 'amazonButtonPixels': pixels})
        if pixels >= 8000:
            activity = adb('shell', 'dumpsys', 'activity', 'activities').decode()
            resumed = next((line for line in activity.splitlines() if 'mResumedActivity:' in line), '')
            if 'com.amazon.mas.client.iap.purchase.PurchaseActivity' in resumed:
                visible = elapsed
                break
        time.sleep(.5)
    if args.capture_private_logcat:
        (folder / 'logcat.txt').write_bytes(adb('logcat', '-d', '-v', 'threadtime'))
    if visible is not None and args.cancel_visible:
        adb('shell', 'input', 'keyevent', '4')
    report = {'presentationSeconds': visible, 'limitSeconds': args.limit,
              'observationTimeoutSeconds': args.timeout, 'samples': samples,
              'result': 'PASS' if visible is not None and visible <= args.limit else 'FAIL',
              'scope': 'Actual LAT checkout presentation only; no payment or receipt verification',
              'uiAutomationAttached': False, 'payPressed': False,
              'canceled': visible is not None and args.cancel_visible,
              'rawArtifactsPrivate': True, 'rawLogcatCaptured': args.capture_private_logcat,
              'measurement': 'Screenshot-return upper bound; capture and detector costs recorded separately'}
    (folder / 'result.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report, indent=2))
    return 0 if report['result'] == 'PASS' else 1


if __name__ == '__main__':
    raise SystemExit(main())
