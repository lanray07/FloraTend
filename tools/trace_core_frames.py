"""Diagnostic traces of the isolated populated .perf app; raw traces stay private.

This adds tracing overhead and cannot certify the release frame-time gate.
"""
from pathlib import Path
import json
import re
import subprocess
import time

ADB=Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe'
PACKAGE='com.floratend.app.perf'

def adb(*args):
    return subprocess.check_output([str(ADB),*args])

def summarize(raw,pid):
    stacks={};groups={};markers=0
    pattern=re.compile(r'-(\d+)\s+(?:\(\s*\d+\)\s+)?\[\d+\].*?(\d+\.\d+): tracing_mark_write: (.*)')
    for line in raw.splitlines():
        match=pattern.search(line)
        if not match:continue
        tid,stamp,event=match.groups();stamp=float(stamp)
        if event.startswith('B|'):
            parts=event.split('|',2)
            if len(parts)!=3 or parts[1]!=str(pid):continue
            markers+=1;stacks.setdefault(tid,[]).append((stamp,parts[2]))
        elif event.startswith('E') and stacks.get(tid):
            start,name=stacks[tid].pop();duration=(stamp-start)*1000
            # Publish only framework diagnostic categories, never arbitrary trace names.
            lower=name.lower()
            category=next((label for token,label in [('binder','binder'),('decode','decode'),('upload','upload'),('doframe','doFrame'),('traversal','traversals'),('measure','measure'),('layout','layout'),('draw','draw'),('gc','gc')] if token in lower),'other')
            key=('main' if int(tid)==pid else 'worker')+'/'+category
            row=groups.setdefault(key,{'count':0,'inclusiveMillis':0.,'maxMillis':0.})
            row['count']+=1;row['inclusiveMillis']+=duration;row['maxMillis']=max(row['maxMillis'],duration)
    return {'appMarkerCount':markers,'slices':groups,'scope':'Inclusive nested slice durations, not exclusive CPU time; trace overhead applies.'}

def main():
    metadata=adb('shell','dumpsys','package',PACKAGE).decode()
    assert 'versionCode=12' in metadata and 'DEBUGGABLE' not in metadata
    from measure_core_frames import assert_detached
    assert_detached(adb('shell','dumpsys','activity','processes').decode())
    assert 'package:com.floratend.app.qa' not in adb('shell','pm','list','packages','com.floratend.app').decode().splitlines()
    workload=json.loads(adb('exec-out','cat',f'/sdcard/Android/data/{PACKAGE}/files/performance-workload.json'))
    assert workload['controlledFixture'] and all(workload[k]==120 for k in ['plants','schedules','journal','history'])
    folder=Path.home()/'.android'/('floratend-atrace-'+time.strftime('%Y%m%d-%H%M%S'));folder.mkdir()
    adb('shell','am','force-stop',PACKAGE)
    adb('shell','am','start','-W','-n',PACKAGE+'/com.floratend.app.MainActivity','-a','android.intent.action.MAIN','-c','android.intent.category.LAUNCHER')
    time.sleep(3)
    pid=int(adb('shell','pidof',PACKAGE).decode().strip())
    rows=[]
    def phase(name,actions):
        adb('shell','dumpsys','gfxinfo',PACKAGE,'reset')
        adb('shell','atrace','--async_start','-b','16384','-a',PACKAGE,'gfx','view','sched','res','dalvik','binder_driver')
        try:actions();time.sleep(1)
        finally:
            raw=adb('shell','atrace','--async_stop').decode(errors='replace')
            (folder/(name+'.trace')).write_text(raw,encoding='utf-8')
        gfx=adb('shell','dumpsys','gfxinfo',PACKAGE).decode()
        (folder/(name+'-gfxinfo.txt')).write_text(gfx,encoding='utf-8')
        (folder/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
        frames=int(re.search(r'Total frames rendered: (\d+)',gfx).group(1))
        janky,percent=re.search(r'Janky frames: (\d+) \(([\d.]+)%\)',gfx).groups()
        rows.append({'phase':name,'sampleValid':frames>0,'frames':frames,'jankyFrames':int(janky),'jankyPercent':float(percent),'p95Millis':int(re.search(r'95th percentile: (\d+)ms',gfx).group(1)) if frames else None,**summarize(raw,pid)})
    def scroll(reverse=False):
        for _ in range(8):
            # Both endpoints stay inside the rendered grid, below its fixed header.
            adb('shell','input','swipe','1550','650' if reverse else '1000','1550','1000' if reverse else '650','350')
    phase('garden-first-visible-scroll',scroll)
    phase('garden-warm-reverse-scroll',lambda:scroll(True))
    adb('shell','input','tap','78','485');time.sleep(2)
    phase('voice-keyboard-focus',lambda:adb('shell','input','tap','1000','550'))
    phase('voice-typing',lambda:adb('shell','input','text','Read%smy%sreminders'))
    adb('shell','input','keyevent','4')
    report={'versionCode':12,'instrumentationDetached':True,'debuggable':False,'workload':workload,'phases':rows,'result':'DIAGNOSTIC_ONLY','releaseGateApproved':False,'rawTracesAndScreenshotsPrivate':True,'scope':'Controlled nondebug perf package; tracing overhead and natural compiler state. No Amazon purchase, no microphone activation and no main-package changes.'}
    Path('evidence/release-followup/code12-rendering-trace-diagnostics.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,indent=2))
    return 0

if __name__=='__main__':raise SystemExit(main())
