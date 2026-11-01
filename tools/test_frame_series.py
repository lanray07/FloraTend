import copy,unittest
from measure_core_frames import EXPECTED_SCREENS,frame_histogram
from measure_frame_series import aggregate,TRIALS

class FrameSeriesTests(unittest.TestCase):
    def reports(self):
        row={'versionCode':14,'apkSha256':'a'*64,'package':'com.floratend.app.perf','debuggable':False,'instrumentationDetached':True,'qaPackageAbsent':True,'manualSpeedDiagnostic':False,'focusedDiagnosticScreen':None,
             'deviceConfiguration':{'fontScale':1.0,'accelerometerRotation':'0','userRotation':'1'},
             'compilerState':[{'abi':'arm64','status':'speed-profile','reason':'install'}],
             'populatedWorkload':dict(controlledFixture=True,plants=120,schedules=120,journal=120,history=120),
             'screens':[dict(screen=name,frames=100,jankyFrames=1,jankyPercent=1.0,frameP95Ms=16,frameHistogram={'16':100},thresholdPassed=True) for name in sorted(EXPECTED_SCREENS)]}
        return [copy.deepcopy(row) for _ in range(TRIALS)]
    def test_observed_histogram_must_account_for_all_frames(self):
        self.assertEqual({5:1,16:2},frame_histogram('HISTOGRAM: 5ms=1 16ms=2',3))
        for raw,n in [('no histogram',3),('HISTOGRAM: 5ms=1',3),('HISTOGRAM: 5ms=1 5ms=1',2)]:
            with self.assertRaises(AssertionError):frame_histogram(raw,n)
    def test_pools_counts_and_histograms_including_a_failing_trial(self):
        reports=self.reports();bad=reports[0]['screens'][0]
        bad.update(frameHistogram={'16':80,'50':20},jankyFrames=20,jankyPercent=20,frameP95Ms=50,thresholdPassed=False)
        result=aggregate(reports);row=result['screens'][0]
        self.assertEqual('PASS',result['result']);self.assertFalse(result['allIndividualTrialsPassed'])
        self.assertEqual((500,24,16),(row['frames'],row['jankyFrames'],row['frameP95Ms']))
        self.assertEqual(20,row['frameHistogram'][50]);self.assertFalse(row['individualTrials'][0]['thresholdPassed'])
    def test_single_critical_outlier_cannot_be_hidden_in_pooled_p95(self):
        reports=self.reports();reports[0]['screens'][0]['frameHistogram']={'16':99,'300':1}
        self.assertEqual('FAIL',aggregate(reports)['result'])
    def test_rejects_incomplete_or_changed_artifacts_and_diagnostic_inputs(self):
        with self.assertRaises(ValueError):aggregate(self.reports()[:-1])
        changes=[('apkSha256','b'*64),('versionCode',13),('manualSpeedDiagnostic',True),('debuggable',True),('instrumentationDetached',False),('focusedDiagnosticScreen','voice')]
        for key,value in changes:
            reports=self.reports();reports[0][key]=value
            with self.subTest(key=key),self.assertRaises(ValueError):aggregate(reports)
        reports=self.reports();reports[0]['compilerState'][0]['status']='speed'
        with self.assertRaises(ValueError):aggregate(reports)
        for change in ['missing','zero','histogram','workload']:
            reports=self.reports()
            if change=='missing':reports[0]['screens'].pop()
            elif change=='zero':reports[0]['screens'][0]['frames']=0
            elif change=='histogram':reports[0]['screens'][0]['frameHistogram']={'16':99}
            else:reports[0]['populatedWorkload']['history']=0
            with self.subTest(change=change),self.assertRaises(ValueError):aggregate(reports)
    def test_does_not_approve_tiny_samples(self):
        reports=self.reports()
        for report in reports:
            for row in report['screens']:row.update(frames=5,frameHistogram={'16':5})
        self.assertEqual('FAIL',aggregate(reports)['result'])

if __name__=='__main__':unittest.main()
