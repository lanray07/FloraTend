"""Summarize main-thread ART-v3 dual-clock samples; never copy raw profiles to Git.

Format reference: https://android.googlesource.com/platform/art/+/master/runtime/trace.cc
Sampling and instrumentation add overhead. These are diagnostic CPU estimates only.
"""
from pathlib import Path
import argparse
import collections
import json
import struct

def parse_profile(data):
    text_end=data.index(b'*end\n')+5
    header=data[:text_end].decode('utf-8')
    start=data.index(b'SLOW',text_end)
    version,offset=struct.unpack_from('<HH',data,start+4)
    size=struct.unpack_from('<H',data,start+16)[0]
    if version!=3 or size!=14:raise ValueError('Only ART-v3 dual-clock records are supported')
    settings={};threads={};methods={};section=''
    for line in header.splitlines():
        if line.startswith('*'):section=line;continue
        if section=='*threads' and '\t' in line:
            key,name=line.split('\t',1);threads[int(key)]=name
        elif section=='*methods' and '\t' in line:
            parts=line.split('\t')
            if len(parts)>=3:methods[int(parts[0],16)]=parts[1].replace('/','.')+'.'+parts[2]
        elif '=' in line:
            key,value=line.split('=',1);settings[key]=value
    if settings.get('clock')!='dual' or settings.get('data-file-overflow')!='false':
        raise ValueError('An untruncated dual-clock profile is required')
    main_ids=[key for key,name in threads.items() if name=='main']
    if len(main_ids)!=1:raise ValueError('Exactly one main thread is required')
    records=data[start+offset:]
    if len(records)%size:raise ValueError('Incomplete trace record')
    if len(records)//size!=int(settings['num-method-calls']):raise ValueError('Record count differs from metadata')
    main=main_ids[0];stack=[];previous=None;first=None;last=None
    exclusive=collections.Counter();unmatched=0;main_records=0
    for tid,packed,cpu,wall in struct.iter_unpack('<HIII',records):
        if tid!=main:continue
        main_records+=1
        if first is None:first=cpu
        if previous is not None:
            if cpu<previous:raise ValueError('Non-monotonic thread CPU clock')
            if stack:exclusive[stack[-1]]+=cpu-previous
        previous=cpu;last=cpu
        method=packed&~3;action=packed&3
        if action==0:stack.append(method)
        elif action in (1,2):
            if stack and stack[-1]==method:stack.pop()
            elif method in stack:
                unmatched+=1;stack=stack[:len(stack)-1-stack[::-1].index(method)]
            else:unmatched+=1
        else:raise ValueError('Unknown method action')
    if main_records<2:raise ValueError('Insufficient main-thread samples')
    grouped=collections.Counter()
    allowed=('com.floratend.','androidx.','android.','com.android.','java.','kotlin.','kotlinx.','dalvik.','libcore.','sun.','coil.','com.amazon.')
    for method,cpu in exclusive.items():
        name=methods.get(method,'unresolved')
        grouped[name if name.startswith(allowed) else 'other']+=cpu
    total=(last-first)/1000
    return {'formatVersion':version,'samplingElapsedMillis':int(settings['elapsed-time-usec'])/1000,
            'mainThreadCpuMillis':total,'mainThreadRecords':main_records,'unmatchedExits':unmatched,
            'openSlicesAtEnd':len(stack),'topExclusiveCpuMethods':[{'method':name,'cpuMillis':round(cpu/1000,3),
                'percentOfObservedMainCpu':round(cpu/1000/total*100,2) if total else 0} for name,cpu in grouped.most_common(25)],
            'releaseFrameGateApproved':False,'scope':'Sampled main-thread CPU estimates with instrumentation attached; not ordinary frame latency or a release approval.'}

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--directory',type=Path,required=True);args=parser.parse_args()
    folder=args.directory.resolve()
    assert folder.is_relative_to((Path.home()/'.android').resolve()),'Raw method profiles must remain private'
    scope=json.loads((folder/'scope.json').read_text())
    assert scope['package']=='com.floratend.app.perf' and scope['controlledPlants']==120 and not scope['releaseFrameGateApproved']
    report={'controlledFixture':True,'rawProfilesPrivate':True,'phases':{name:parse_profile((folder/(name+'.trace')).read_bytes()) for name in ['garden','voice']}}
    Path('evidence/release-followup/code12-method-profile-summary.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,indent=2))

if __name__=='__main__':main()
