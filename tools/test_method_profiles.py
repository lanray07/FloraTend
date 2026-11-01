"""ART diagnostic parsing must use thread CPU clocks and reject invalid captures."""
import struct
import unittest
from summarize_method_profile import parse_profile

def profile(*,overflow=False,version=3,tail=b''):
    header=('*version\n3\nclock=dual\ndata-file-overflow='+str(overflow).lower()+
            '\nelapsed-time-usec=9000\nnum-method-calls=4\n*threads\n1\tmain\n*methods\n'
            '0x4\tcom/floratend/app/Controlled\touter\t()V\n'
            '0x8\tandroid/view/Controlled\tinner\t()V\n*end\n').encode()
    binary=struct.pack('<4sHHQH14x',b'SLOW',version,32,0,14)
    records=[(1,4,10,100),(1,8,20,200),(1,9,50,500),(1,5,70,900)]
    return header+binary+b''.join(struct.pack('<HIII',*row) for row in records)+tail

class MethodProfileTests(unittest.TestCase):
    def test_exclusive_cpu_uses_thread_time_instead_of_wall_wait(self):
        result=parse_profile(profile())
        self.assertAlmostEqual(.06,result['mainThreadCpuMillis'])
        rows={r['method']:r['cpuMillis'] for r in result['topExclusiveCpuMethods']}
        self.assertAlmostEqual(.03,rows['com.floratend.app.Controlled.outer'])
        self.assertAlmostEqual(.03,rows['android.view.Controlled.inner'])
        self.assertEqual(0,result['unmatchedExits'])
        self.assertFalse(result['releaseFrameGateApproved'])
    def test_overflowed_capture_is_not_used(self):
        with self.assertRaises(ValueError):parse_profile(profile(overflow=True))
    def test_unknown_format_and_partial_records_are_not_used(self):
        with self.assertRaises(ValueError):parse_profile(profile(version=4))
        with self.assertRaises(ValueError):parse_profile(profile(tail=b'x'))

if __name__=='__main__':unittest.main()
