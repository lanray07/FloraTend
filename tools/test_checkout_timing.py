"""Validate the presentation detector against retained actual Fire captures."""
from pathlib import Path
import unittest

from measure_lat_checkout import checkout_pixels


class CheckoutTimingTest(unittest.TestCase):
    def test_actual_checkout_and_waiting_states_are_distinguished(self):
        root = Path(__file__).resolve().parents[1] / 'evidence/lat-code7'
        for filename in ['post-reboot-landscape-delayed.png', 'post-reboot-portrait-checkout.png']:
            with self.subTest(filename=filename):
                self.assertGreaterEqual(checkout_pixels((root / filename).read_bytes()), 8000)
        for filename in ['landscape-initial-waiting.png', 'portrait-attempt.png']:
            with self.subTest(filename=filename):
                self.assertLess(checkout_pixels((root / filename).read_bytes()), 8000)


    def test_optimized_counter_preserves_exact_retained_frame_counts(self):
        root = Path(__file__).resolve().parents[1] / 'evidence/lat-code7'
        expected = {'post-reboot-landscape-delayed.png': 45294, 'post-reboot-portrait-checkout.png': 45305, 'landscape-initial-waiting.png': 0, 'portrait-attempt.png': 0}
        for filename, count in expected.items():
            with self.subTest(filename=filename):
                self.assertEqual(count, checkout_pixels((root / filename).read_bytes()))


if __name__ == '__main__':
    unittest.main()
