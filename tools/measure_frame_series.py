"""A predeclared five-launch comparison; includes every trial, never averages p95s."""
import argparse,json,math,subprocess,sys
from collections import Counter
from pathlib import Path
from measure_core_frames import EXPECTED_SCREENS,BOUNDS,PACKAGE

TRIALS=5
MIN_FRAMES=100
MAX_BUCKET_MS=250

def aggregate(reports):
    if len(reports)!=TRIALS:raise ValueError('Exactly five predetermined trials required')
    identity={(r['versionCode'],r['apkSha256'],r['package']) for r in reports}
    if len(identity)!=1:raise ValueError('Every trial must use the same installed artifact')
    if any(r.get('deviceConfiguration')!={'fontScale':1.0,'accelerometerRotation':'0','userRotation':'1'} for r in reports):raise ValueError('Declared physical configuration required in every trial')
    for report in reports:
        if report['package']!=PACKAGE or report['debuggable'] or not report['instrumentationDetached'] or not report['qaPackageAbsent'] or report['manualSpeedDiagnostic'] or report['focusedDiagnosticScreen'] is not None:
            raise ValueError('Standalone natural six-screen measurements required')
        if not report.get('compilerState') or any(row['status']=='speed' for row in report['compilerState']):raise ValueError('No forced full-speed compilation in a natural series')
        workload=report['populatedWorkload']
        if not workload.get('controlledFixture') or any(workload[k]!=120 for k in ['plants','schedules','journal','history']):raise ValueError('Same complete controlled workload required')
        if len(report['screens'])!=6 or {row['screen'] for row in report['screens']}!=EXPECTED_SCREENS:raise ValueError('Each trial must include all six screens')
    rows=[]
    for screen in sorted(EXPECTED_SCREENS):
        samples=[next(row for row in r['screens'] if row['screen']==screen) for r in reports]
        histogram=Counter()
        for sample in samples:
            hist={int(k):int(v) for k,v in sample['frameHistogram'].items()}
            if sample['frames']<=0 or any(k<0 or v<0 for k,v in hist.items()) or sum(hist.values())!=sample['frames'] or not 0<=sample['jankyFrames']<=sample['frames']:raise ValueError('Complete positive frame samples required')
            histogram.update(hist)
        frames=sum(sample['frames'] for sample in samples)
        janky=sum(sample['jankyFrames'] for sample in samples)
        target=math.ceil(frames*.95);cumulative=0;p95=None
        for duration,count in sorted(histogram.items()):
            cumulative+=count
            if cumulative>=target:p95=duration;break
        maximum=max(duration for duration,count in histogram.items() if count)
        percent=100*janky/frames
        rows.append({'screen':screen,'frames':frames,'jankyFrames':janky,'jankyPercent':round(percent,4),'frameP95Ms':p95,'maxDurationBucketMs':maximum,
                     'frameHistogram':dict(sorted(histogram.items())),
                     'individualTrials':[{'trial':i+1,'frames':s['frames'],'jankyPercent':s['jankyPercent'],'frameP95Ms':s['frameP95Ms'],'thresholdPassed':s['thresholdPassed']} for i,s in enumerate(samples)],
                     'thresholdPassed':frames>=MIN_FRAMES and p95<=BOUNDS['frameP95Ms'] and percent<=BOUNDS['jankyPercent'] and maximum<=MAX_BUCKET_MS})
    passed=all(row['thresholdPassed'] for row in rows)
    return {'protocol':'fixed-five-launch-histogram-v1','versionCode':reports[0]['versionCode'],'apkSha256':reports[0]['apkSha256'],'trials':TRIALS,'minimumFramesPerScreen':MIN_FRAMES,'thresholds':dict(BOUNDS,maxDurationBucketMs=MAX_BUCKET_MS),
            'compilerStatesByTrial':[r['compilerState'] for r in reports],
            'allIndividualTrialsPassed':all(s['thresholdPassed'] for r in reports for s in r['screens']),
            'screens':rows,'result':'PASS' if passed else 'FAIL','releaseGateApproved':passed,
            'scope':'Same six-screen workload across five cold process launches, all trials retained. Pooled observed duration buckets and jank counts, not average percentiles. Individual cold-entry misses remain visible. Controlled FireHD10 API28 evidence, not Amazon delivery, battery-energy or other-hardware certification.'}

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--version-code',type=int,required=True)
    parser.add_argument('--output-dir',type=Path,required=True)
    args=parser.parse_args()
    args.output_dir.mkdir(parents=True,exist_ok=False)
    plan={'protocol':'fixed-five-launch-histogram-v1','trials':TRIALS,'versionCode':args.version_code,'minimumFramesPerScreen':MIN_FRAMES,'thresholds':dict(BOUNDS,maxDurationBucketMs=MAX_BUCKET_MS),'selection':'All trials in order; no replacement or extra favourable trials'}
    (args.output_dir/'plan.json').write_text(json.dumps(plan,indent=2)+'\n',encoding='utf-8')
    reports=[]
    for trial in range(1,TRIALS+1):
        path=args.output_dir/f'trial-{trial}.json'
        result=subprocess.run([sys.executable,str(Path(__file__).with_name('measure_core_frames.py')),'--populated','--version-code',str(args.version_code),'--output',str(path)],stdout=subprocess.DEVNULL)
        if result.returncode not in {0,1} or not path.exists():raise RuntimeError('Measurement did not complete; partial series retained, cannot aggregate')
        reports.append(json.loads(path.read_text(encoding='utf-8')))
        print(f'Trial {trial}/{TRIALS}: {reports[-1]["result"]}',flush=True)
    report=aggregate(reports)
    (args.output_dir/'summary.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(report['result'])
    return 0 if report['result']=='PASS' else 1

if __name__=='__main__':raise SystemExit(main())
