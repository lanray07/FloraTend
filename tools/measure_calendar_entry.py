"""Focused cold Calendar entry diagnostic; never certifies the six-screen gate."""
import argparse,json,re,time,hashlib
from pathlib import Path
from measure_core_frames import adb,assert_detached,PACKAGE,frame_histogram

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--version-code',type=int,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
 assert not a.output.exists()
 assert_detached(adb('shell','dumpsys','activity','processes').decode())
 meta=adb('shell','dumpsys','package',PACKAGE).decode();assert re.search(r'\bversionCode='+str(a.version_code)+r'\b',meta) and 'DEBUGGABLE' not in meta and '[status=speed]' not in meta
 assert 'package:com.floratend.app.qa' not in adb('shell','pm','list','packages','com.floratend.app').decode().splitlines()
 workload=json.loads(adb("exec-out","cat","/sdcard/Android/data/"+PACKAGE+"/files/performance-workload.json"));assert workload["controlledFixture"] and all(workload[k]==120 for k in ["plants","schedules","journal","history"])
 rows=[]
 for i in range(3):
  adb('shell','am','force-stop',PACKAGE);adb('shell','am','start','-W','-n',PACKAGE+'/com.floratend.app.MainActivity');time.sleep(3)
  adb('shell','dumpsys','gfxinfo',PACKAGE,'reset');adb('shell','input','tap','78','315');time.sleep(1)
  raw=adb('shell','dumpsys','gfxinfo',PACKAGE).decode();n=int(re.search(r'Total frames rendered: (\d+)',raw).group(1));h=frame_histogram(raw,n)
  row={'trial':i+1,'frames':n,'frameP95Ms':int(re.search(r'95th percentile: (\d+)ms',raw).group(1)),'maxDurationBucketMs':max(k for k,v in h.items() if v),'frameHistogram':h};rows.append(row);print({k:v for k,v in row.items() if k!='frameHistogram'},flush=True)
 apk=adb('shell','pm','path',PACKAGE).decode().strip().removeprefix('package:');sha=adb('shell','sha256sum',apk).decode().split()[0]
 result={'versionCode':a.version_code,'apkSha256':sha,'trials':rows,'controlledWorkload':workload,'result':'DIAGNOSTIC_THRESHOLDS_MET' if all(r['maxDurationBucketMs']<=250 for r in rows) else 'DIAGNOSTIC_THRESHOLDS_MISSED','releaseGateApproved':False,'scope':'Three cold process launches; direct Calendar navigation after3s. Natural compilation, no instrumentation. Entry-only workload; not full rendering approval.'}
 a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':main()
