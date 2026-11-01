"""Measure actual Amazon checkout presentation without attaching UiAutomation.

Open Settings, verify an inactive LAT entitlement and its Amazon price, then
provide the observed Unlock button centre. Never presses Pay or clears data.
Raw screenshots/logcat stay in the private Android directory.
"""
import argparse
from io import BytesIO
import json
import os
from pathlib import Path
import subprocess
import time

from PIL import Image

ADB = str(Path(os.environ.get('LOCALAPPDATA', '')) / 'Android/Sdk/platform-tools/adb.exe')


def adb(*args):
    return subprocess.check_output([ADB, *args], timeout=20)


def checkout_pixels(capture):
    picture = Image.open(BytesIO(capture)).convert('RGB')
    # Screenshots are retained for human review; this is a presentation signal,
    # not receipt verification or proof that a payment completed.
    pixels = getattr(picture, 'get_flattened_data', picture.getdata)()
    return sum(r > 210 and 125 < g < 220 and b < 75 for r, g, b in pixels)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--confirm-priced-inactive-lat-screen', action='store_true', required=True)
    parser.add_argument('--tap', nargs=2, type=int, required=True)
    parser.add_argument('--timeout', type=int, default=60)
    parser.add_argument('--limit', type=float, default=5)
    parser.add_argument('--cancel-visible', action='store_true')
    parser.add_argument('--name', required=True)
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
        capture = adb('exec-out', 'screencap', '-p')
        elapsed = round(time.monotonic() - started, 3)
        pixels = checkout_pixels(capture)
        (folder / f'{len(samples):03d}.png').write_bytes(capture)
        samples.append({'elapsedSeconds': elapsed, 'amazonButtonPixels': pixels})
        if pixels >= 8000:
            activity = adb('shell', 'dumpsys', 'activity', 'activities').decode()
            resumed = next((line for line in activity.splitlines() if 'mResumedActivity:' in line), '')
            if 'com.amazon.mas.client.iap.purchase.PurchaseActivity' in resumed:
                visible = elapsed
                break
        time.sleep(.5)
    (folder / 'logcat.txt').write_bytes(adb('logcat', '-d', '-v', 'threadtime'))
    if visible is not None and args.cancel_visible:
        adb('shell', 'input', 'keyevent', '4')
    report = {'presentationSeconds': visible, 'limitSeconds': args.limit,
              'observationTimeoutSeconds': args.timeout, 'samples': samples,
              'result': 'PASS' if visible is not None and visible <= args.limit else 'FAIL',
              'scope': 'Actual LAT checkout presentation only; no payment or receipt verification',
              'uiAutomationAttached': False, 'payPressed': False,
              'canceled': visible is not None and args.cancel_visible,
              'rawArtifactsPrivate': True}
    (folder / 'result.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report, indent=2))
    return 0 if report['result'] == 'PASS' else 1


if __name__ == '__main__':
    raise SystemExit(main())
