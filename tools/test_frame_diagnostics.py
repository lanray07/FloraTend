"""Diagnostic tools must never promote forced compilation or missing frame samples."""
import json
import unittest
from measure_core_frames import frame_verdict, EXPECTED_SCREENS,assert_detached
from trace_core_frames import summarize

class FrameEvidenceTests(unittest.TestCase):
    def test_detachment_guard_rejects_unsupported_output_and_running_instrumentation(self):
        for text in ['', 'Unknown command: instrumentation', 'ACTIVITY MANAGER RUNNING PROCESSES\nActiveInstrumentation{123}']:
            with self.assertRaises(AssertionError): assert_detached(text)
        assert_detached('ACTIVITY MANAGER RUNNING PROCESSES\nAll known processes:')
    def rows(self):
        return [{'screen':name,'frames':100,'frameP95Ms':16,'jankyPercent':1.0} for name in EXPECTED_SCREENS]
    def test_forced_compilation_cannot_approve_release_even_when_all_bounds_pass(self):
        result=frame_verdict(self.rows(),True)
        self.assertEqual('DIAGNOSTIC_THRESHOLDS_MET',result['result'])
        self.assertFalse(result['releaseGateApproved'])
    def test_missing_screen_or_zero_frame_sample_cannot_pass(self):
        self.assertEqual('FAIL',frame_verdict(self.rows()[:-1])['result'])
        rows=self.rows();rows[0]['frames']=0
        self.assertFalse(frame_verdict(rows)['releaseGateApproved'])
    def test_one_failed_screen_fails_combined_gate(self):
        rows=self.rows();rows[0]['frameP95Ms']=33
        self.assertEqual('FAIL',frame_verdict(rows)['result'])
    def test_all_valid_natural_samples_can_pass(self):
        self.assertEqual({'result':'PASS','releaseGateApproved':True},frame_verdict(self.rows()))
    def test_focused_diagnostic_cannot_approve_release_or_accept_wrong_screen(self):
        rows=[row for row in self.rows() if row['screen']=='voice']
        self.assertEqual({'result':'DIAGNOSTIC_THRESHOLDS_MET','releaseGateApproved':False},frame_verdict(rows,focused_screen='voice'))
        self.assertEqual('DIAGNOSTIC_THRESHOLDS_MISSED',frame_verdict(rows,focused_screen='garden')['result'])
        rows[0]['frames']=0
        self.assertEqual('DIAGNOSTIC_THRESHOLDS_MISSED',frame_verdict(rows,focused_screen='voice')['result'])
    def test_trace_filters_other_processes_and_never_exports_arbitrary_names(self):
        raw='\n'.join([
            'app-42 [000] ...1 1.000: tracing_mark_write: B|42|Choreographer#doFrame',
            'worker-43 [001] ...1 1.002: tracing_mark_write: B|42|BitmapDecode',
            'other-99 [002] ...1 1.003: tracing_mark_write: B|99|DO_NOT_EXPORT',
            'app-42 [000] ...1 1.010: tracing_mark_write: E',
            'worker-43 [001] ...1 1.022: tracing_mark_write: E',
            'app-42 [000] ...1 1.030: tracing_mark_write: B|42|DO_NOT_EXPORT',
            'app-42 [000] ...1 1.035: tracing_mark_write: E'])
        result=summarize(raw,42)
        self.assertEqual(3,result['appMarkerCount'])
        self.assertAlmostEqual(10,result['slices']['main/doFrame']['maxMillis'])
        self.assertAlmostEqual(20,result['slices']['worker/decode']['maxMillis'])
        self.assertEqual(1,result['slices']['main/other']['count'])
        self.assertNotIn('DO_NOT_EXPORT',json.dumps(result))
    def test_unclosed_trace_slice_does_not_invent_duration(self):
        result=summarize('app-42 [000] ...1 1.000: tracing_mark_write: B|42|draw',42)
        self.assertEqual({},result['slices'])

if __name__=='__main__':unittest.main()
