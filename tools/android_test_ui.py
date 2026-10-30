"""Read and operate the Android test device UI through the official adb/uiautomator CLI.
Refuses to steal UiAutomation while instrumentation is active. Selectors use rendered text.
"""
import argparse,subprocess,os,time,xml.etree.ElementTree as ET,re
from pathlib import Path
ADB=str(Path(os.environ['LOCALAPPDATA'])/'Android/Sdk/platform-tools/adb.exe')
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,check=True).stdout

def snapshot():
    active=adb('shell','dumpsys','activity','processes')
    if 'ActiveInstrumentation{' in active:raise RuntimeError('Instrumentation is active; do not steal its UI Automation connection')
    adb('shell','input','keyevent','KEYCODE_WAKEUP')
    lock=adb('shell','dumpsys','window','policy')
    if 'mIsShowing=true' in lock or 'mInputRestricted=true' in lock:raise RuntimeError('Unlock the test tablet manually before UI control')
    for attempt in range(3):
        result=adb('shell','uiautomator','dump','/sdcard/floratend-current-ui.xml')
        if 'dumped to' in result:
            raw=adb('shell','cat','/sdcard/floratend-current-ui.xml')
            try:return ET.fromstring(raw)
            except ET.ParseError:pass
        time.sleep(.7)
    raise RuntimeError('Could not read the current Android UI')
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--tap-text');parser.add_argument('--back',action='store_true');args=parser.parse_args()
    root=snapshot()
    if args.tap_text:
        found=[n for n in root.iter('node') if n.get('text')==args.tap_text or n.get('content-desc')==args.tap_text]
        if len(found)!=1:raise RuntimeError(f'Expected one visible control for {args.tap_text!r}, found {len(found)}')
        numbers=list(map(int,re.findall(r'\d+',found[0].get('bounds',''))));assert len(numbers)==4
        adb('shell','input','tap',str((numbers[0]+numbers[2])//2),str((numbers[1]+numbers[3])//2));time.sleep(1.5);root=snapshot()
    if args.back:adb('shell','input','keyevent','KEYCODE_BACK');time.sleep(1.5);root=snapshot()
    for n in root.iter('node'):
        label=n.get('text') or n.get('content-desc')
        if label:
            if re.fullmatch(r'[A-Za-z0-9_\-=]{30,}',label):label='<sandbox identifier omitted>'
            print(label+' | '+n.get('bounds','')+' | checked='+n.get('checked','')+' | id='+n.get('resource-id',''))
if __name__=='__main__':main()
