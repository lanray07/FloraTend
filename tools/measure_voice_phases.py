"""Separate Voice navigation, keyboard focus, typing and dismissal; diagnostic only."""
import argparse,json,re,time,hashlib
from pathlib import Path
from measure_core_frames import adb,PACKAGE,assert_detached

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--version-code',type=int,required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    assert not args.output.exists()
    metadata=adb('shell','dumpsys','package',PACKAGE).decode()
    assert re.search(r'\bversionCode='+str(args.version_code)+r'\b',metadata) and 'DEBUGGABLE' not in metadata
    assert '[status=speed]' not in metadata
    processes=adb('shell','dumpsys','activity','processes').decode()
    assert_detached(processes)
    assert 'package:com.floratend.app.qa' not in adb('shell','pm','list','packages','com.floratend.app').decode().splitlines()
    folder=Path.home()/'.android'/('floratend-voice-phases-'+time.strftime('%Y%m%d-%H%M%S'))
    folder.mkdir()
    adb('shell','am','force-stop',PACKAGE)
    adb('shell','am','start','-W','-n',PACKAGE+'/com.floratend.app.MainActivity')
    time.sleep(3)
    rows=[]
    actions=[('navigation',['input','tap','78','485']),('focus',['input','tap','1000','550']),('typing',['input','text','Read%smy%sreminders']),('dismissal',['input','keyevent','4'])]
    for name,action in actions:
        adb('shell','dumpsys','gfxinfo',PACKAGE,'reset')
        adb('shell',*action)
        time.sleep(1)
        raw=adb('shell','dumpsys','gfxinfo',PACKAGE).decode()
        (folder/(name+'-gfxinfo.txt')).write_text(raw,encoding='utf-8')
        count=int(re.search(r'Total frames rendered: (\d+)',raw).group(1))
        janky,percent=re.search(r'Janky frames: (\d+) \(([\d.]+)%\)',raw).groups()
        rows.append({'phase':name,'frames':count,'jankyFrames':int(janky),'jankyPercent':float(percent),'frameP95Ms':int(re.search(r'95th percentile: (\d+)ms',raw).group(1))})
    capture=adb('exec-out','screencap','-p')
    (folder/'voice.png').write_bytes(capture)
    result={'versionCode':args.version_code,'phases':rows,'captureSha256':hashlib.sha256(capture).hexdigest(),'instrumentationDetached':True,'result':'DIAGNOSTIC_ONLY','releaseGateApproved':False,'scope':'Physical FireHD10 API28, isolated nondebuggable app; phase isolation changes the aggregate workload and cannot approve rendering.'}
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,indent=2))

if __name__=='__main__': main()
