"""Observe the real Fire checkout without attaching UiAutomation to its dialog.

Run after opening FloraTend Settings with the test entitlement reset. Saves raw
captures privately; emits no account/receipt identifiers. Never presses Pay.
"""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from io import BytesIO
from PIL import Image

ADB = str(Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platform-tools/adb.exe')


def adb(*args):
    return subprocess.check_output([ADB, *args], timeout=20)


def before_snapshot():
    for _ in range(3):
        result = adb('shell', 'uiautomator', 'dump', '/sdcard/floratend-checkout-before.xml').decode()
        if 'dumped to' in result:
            return ET.fromstring(adb('shell', 'cat', '/sdcard/floratend-checkout-before.xml'))
        time.sleep(.7)
    raise RuntimeError('No fresh pre-checkout UI; refusing stale selectors')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--confirm-lat-test-only', action='store_true', required=True)
    parser.add_argument('--orientation', choices=['portrait', 'landscape'], required=True)
    args = parser.parse_args()
    original = {name: adb('shell', 'settings', 'get', 'system', name).decode().strip()
                for name in ['accelerometer_rotation', 'user_rotation']}
    destination = Path.home() / '.android' / 'floratend-checkout-observer'
    destination.mkdir(exist_ok=True)
    try:
        package = adb('shell', 'dumpsys', 'package', 'com.floratend.app').decode()
        if 'installerPackageName=com.amazon.venezia' not in package:
            raise RuntimeError('Requires the actual Amazon-delivered test installation')
        policy = adb('shell', 'dumpsys', 'window', 'policy').decode()
        if 'mIsShowing=true' in policy or 'mInputRestricted=true' in policy:
            raise RuntimeError('Unlock the test tablet before observing checkout')
        adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
        adb('shell', 'settings', 'put', 'system', 'user_rotation', '0' if args.orientation == 'portrait' else '3')
        time.sleep(2)
        # Only inspect the application screen BEFORE opening checkout.
        root = before_snapshot()
        settings = [n for n in root.iter('node') if n.get('text') == 'Settings' or n.get('content-desc') == 'Settings']
        if len(settings) == 1:
            x1, y1, x2, y2 = map(int, re.findall(r'\d+', settings[0].get('bounds', '')))
            adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
            time.sleep(1)
            root = before_snapshot()
        if any('Premium is active' in n.get('text', '') for n in root.iter('node')):
            raise RuntimeError('BLOCKED: reset only the dedicated LAT test entitlement before checkout reproduction')
        buttons = [n for n in root.iter('node') if n.get('text', '').startswith('Unlock for ') and n.get('enabled') == 'true']
        if len(buttons) != 1:
            raise RuntimeError('Open Settings and wait for one enabled SDK-priced Unlock button')
        # Fire rotation requests can lag or be ignored; verify the displayed shape.
        width, height = Image.open(BytesIO(adb('exec-out', 'screencap', '-p'))).size
        observed = 'landscape' if width > height else 'portrait'
        if observed != args.orientation:
            raise RuntimeError('Requested orientation was not applied; refusing mislabeled evidence')
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', buttons[0].get('bounds', '')))
        adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
        samples = []
        started = time.monotonic()
        for second in [1, 5, 15]:
            time.sleep(max(0, second - (time.monotonic()-started)))
            capture = adb('exec-out', 'screencap', '-p')
            path = destination / f'{args.orientation}-{second}s.png'
            path.write_bytes(capture)
            picture = Image.open(BytesIO(capture)).convert('RGB')
            yellow = sum(r > 210 and 125 < g < 220 and b < 75 for r, g, b in picture.getdata())
            samples.append({'second': second, 'amazonButtonPixels': yellow,
                            'checkoutButtonVisible': yellow >= 8000})
        report = {'orientation': args.orientation, 'samples': samples,
                  'uiAutomationAttachedAfterPurchaseTap': False,
                  'payPressed': False, 'rawCapturesHeldPrivately': True,
                  'result': 'CHECKOUT_VISIBLE' if samples[-1]['checkoutButtonVisible'] else 'CHECKOUT_NOT_VISIBLE'}
        print(json.dumps(report, indent=2))
        if report['result'] != 'CHECKOUT_VISIBLE':
            raise SystemExit(1)
    finally:
        for name, value in original.items():
            adb('shell', 'settings', 'put', 'system', name, value)


if __name__ == '__main__':
    main()
