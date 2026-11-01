"""Measure only the isolated, seeded, nondebuggable .perf app with instrumentation detached."""
from pathlib import Path
import argparse
import hashlib
import json
import re
import subprocess
import struct
import time

ADB = Path.home() / 'AppData/Local/Android/Sdk/platform-tools/adb.exe'
PACKAGE = 'com.floratend.app.perf'
EXPECTED_SCREENS={'garden','calendar','journal','voice','insights','settings'}
BOUNDS={'frameP95Ms':32,'jankyPercent':10}

def assert_detached(processes):
    assert 'ACTIVITY MANAGER RUNNING PROCESSES' in processes,'Readable ActivityManager process inventory required'
    assert 'ActiveInstrumentation{' not in processes,'Detach instrumentation before measurement'

def frame_histogram(raw,frames):
    match=re.search(r'HISTOGRAM: ([^\r\n]+)',raw)
    assert match,'Observed frame-duration histogram required'
    pairs=[(int(duration),int(count)) for duration,count in re.findall(r'(\d+)ms=(\d+)',match.group(1))]
    assert pairs and len(dict(pairs))==len(pairs) and sum(count for _,count in pairs)==frames,'Histogram must account for every frame'
    return dict(pairs)

def frame_verdict(rows,manual_speed=False,focused_screen=None):
    expected={focused_screen} if focused_screen else EXPECTED_SCREENS
    diagnostic=manual_speed or focused_screen is not None
    passed=(len(rows)==len(expected) and {r['screen'] for r in rows}==expected
            and all(r['frames']>0 and r['frameP95Ms']<=BOUNDS['frameP95Ms'] and r['jankyPercent']<=BOUNDS['jankyPercent'] for r in rows))
    return {'result':('DIAGNOSTIC_THRESHOLDS_MET' if passed else 'DIAGNOSTIC_THRESHOLDS_MISSED') if diagnostic else ('PASS' if passed else 'FAIL'),
            'releaseGateApproved':passed and not diagnostic}


def adb(*args):
    return subprocess.check_output([str(ADB), *args])


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--populated',action='store_true',help='Require the controlled 120-row care/journal/history workload and exercise typed voice review')
    parser.add_argument('--manual-speed-diagnostic',action='store_true',help='Explicitly label a shell full-speed compilation experiment; never approves the release gate')
    parser.add_argument('--version-code',type=int,required=True,help='Exact isolated candidate version expected on the device')
    parser.add_argument('--output',type=Path,required=True,help='New report path; historical evidence is never overwritten')
    parser.add_argument('--only-screen',choices=sorted(EXPECTED_SCREENS),help='Focused diagnostic only; cannot approve the six-screen release gate')
    args=parser.parse_args()
    configuration={'fontScale':float(adb('shell','settings','get','system','font_scale').decode().strip()),
                   'accelerometerRotation':adb('shell','settings','get','system','accelerometer_rotation').decode().strip(),
                   'userRotation':adb('shell','settings','get','system','user_rotation').decode().strip()}
    assert configuration=={'fontScale':1.0,'accelerometerRotation':'0','userRotation':'1'},'Restore the declared physical landscape/font configuration first'
    metadata = adb('shell', 'dumpsys', 'package', PACKAGE).decode()
    assert re.search(r'\bversionCode='+str(args.version_code)+r'\b',metadata) and 'DEBUGGABLE' not in metadata
    apk_paths=adb('shell','pm','path',PACKAGE).decode().splitlines()
    assert len(apk_paths)==1 and apk_paths[0].startswith('package:/data/app/') and apk_paths[0].endswith('/base.apk')
    apk_hash=adb('shell','sha256sum',apk_paths[0].removeprefix('package:')).decode().split()[0]
    assert re.fullmatch('[a-f0-9]{64}',apk_hash)
    assert not args.output.exists(),'Choose a fresh evidence path'
    compiler=[{'abi':abi,'status':status,'reason':reason} for abi,status,reason in re.findall(r'^\s*(arm64|arm): \[status=(\S+)\] \[reason=(\S+)\]',metadata,re.M)]
    assert compiler,'Compiler state must be observable'
    if args.manual_speed_diagnostic:
        assert any(row['status']=='speed' for row in compiler),'Expected the explicit full-speed experiment'
    else:
        assert not any(row['status']=='speed' for row in compiler),'Full-speed compilation requires an explicitly labelled diagnostic run'
    processes=adb('shell','dumpsys','activity','processes').decode()
    assert_detached(processes)
    installed=adb('shell','pm','list','packages','com.floratend.app').decode().splitlines()
    assert 'package:com.floratend.app.qa' not in installed,'QA speech fixtures must be removed before standalone rendering measurements'
    workload=None
    if args.populated:
        workload=json.loads(adb('exec-out','cat','/sdcard/Android/data/'+PACKAGE+'/files/performance-workload.json'))
        assert workload['controlledFixture'] and all(workload[key]==120 for key in ['plants','schedules','journal','history'])
    folder = Path.home() / '.android' / ('floratend-core-frames-' + time.strftime('%Y%m%d-%H%M%S'))
    folder.mkdir()
    adb('shell', 'am', 'force-stop', PACKAGE)
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/com.floratend.app.MainActivity',
        '-a', 'android.intent.action.MAIN', '-c', 'android.intent.category.LAUNCHER')
    time.sleep(3)
    assert struct.unpack('>II',adb('exec-out','screencap','-p')[16:24])==(1920,1200),'Expected physical HD10 landscape dimensions'
    rows = []
    for name, y in [('garden', 230), ('calendar', 315), ('journal', 400), ('voice', 485), ('insights', 565), ('settings', 650)]:
        if args.only_screen and name!=args.only_screen: continue
        adb('shell', 'dumpsys', 'gfxinfo', PACKAGE, 'reset')
        adb('shell', 'input', 'tap', '78', str(y))
        time.sleep(1)
        for _ in range(16 if name == 'garden' else 4):
            adb('shell', 'input', 'swipe', '1550', '1000', '1550', '350', '350')
        if name=='voice' and args.populated:
            # Verified English landscape field bounds; review text only, never Run.
            adb('shell','input','tap','1000','550')
            time.sleep(1)
            adb('shell','input','text','Read%smy%sreminders')
            time.sleep(1)
            adb('shell','input','keyevent','4')
        time.sleep(1)
        capture = adb('exec-out', 'screencap', '-p')
        (folder / (name + '.png')).write_bytes(capture)
        raw = adb('shell', 'dumpsys', 'gfxinfo', PACKAGE).decode()
        (folder / (name + '-gfxinfo.txt')).write_text(raw, encoding='utf-8')
        frames = int(re.search(r'Total frames rendered: (\d+)', raw).group(1))
        janky, percent = re.search(r'Janky frames: (\d+) \(([\d.]+)%\)', raw).groups()
        p95 = int(re.search(r'95th percentile: (\d+)ms', raw).group(1))
        rows.append({'screen': name, 'frames': frames, 'jankyFrames': int(janky), 'jankyPercent': float(percent),
                     'frameP95Ms': p95, 'captureSha256': hashlib.sha256(capture).hexdigest(),
                     'frameHistogram':frame_histogram(raw,frames),
                     'thresholdPassed': frames > 0 and p95 <= 32 and float(percent) <= 10})
    report = {'versionCode': args.version_code, 'apkSha256':apk_hash, 'deviceConfiguration':configuration, 'package': PACKAGE, 'debuggable': False, 'instrumentationDetached': True,'qaPackageAbsent':True,
              'compileStateReset': False, 'compileState': 'Explicit shell full-speed diagnostic' if args.manual_speed_diagnostic else 'Natural install/update compiler state; Fire rejects shell profile reset as system-only',
              'compilerState':compiler,'manualSpeedDiagnostic':args.manual_speed_diagnostic,
              'seededPlantPhotos': 120, 'screens': rows,
              'populatedWorkload':workload,'voiceInteraction':'Typed review only; command not executed' if args.populated else 'Navigation and scroll attempt only',
              'thresholds': BOUNDS,
              'result': 'PASS' if all(row['thresholdPassed'] for row in rows) else 'FAIL',
              'scope': 'Controlled ordinary core navigation/scrolling on physical Fire HD10 API28. OS gesture input; raw traces/captures private. Not Amazon billing, other hardware, battery or worst-case performance certification.'}
    report.update(frame_verdict(rows,args.manual_speed_diagnostic,args.only_screen))
    report['focusedDiagnosticScreen']=args.only_screen
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + '\n',encoding='utf-8')
    print(json.dumps(report, indent=2))
    return 0 if report['result'] in {'PASS','DIAGNOSTIC_THRESHOLDS_MET'} else 1


if __name__ == '__main__':
    raise SystemExit(main())
