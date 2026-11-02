"""Synthetic parser contracts only; fixtures are never genuine battery-test evidence."""
import unittest
from analyze_energy import analyze, COMPLETE, duration_seconds


def sample(index, uptime, phase, power='false', level=99, stopped=''):
    return f'''@@SAMPLE {index} {1793600000 + uptime} {uptime} {phase}
Current Battery Service state:
 {stopped}
 AC powered: false
 USB powered: {power}
 Wireless powered: false
 Charge counter: 6400000
 level: {level}
 scale: 100
 voltage: 4100
 temperature: 240
@@SCREEN {index}
 mWakefulness=Asleep
'''


def stats(duration):
    return f'Time on battery: {duration} (100.0%) realtime, 1s (100.0%) uptime\nTime on battery screen off: {duration} (100.0%) realtime, 1s (100.0%) uptime\n'


class EnergyAnalysisTests(unittest.TestCase):
    def test_valid_recording_is_not_automatic_energy_acceptance(self):
        result = analyze(COMPLETE, sample(1, 100, 'start') + sample(2, 1901, 'measurement', level=98), stats('1s'), stats('30m 2s'))
        self.assertEqual('VALID_PHYSICALLY_UNPLUGGED_WINDOW', result['measurementValidity'])
        self.assertEqual(1801, result['measuredSeconds'])
        self.assertEqual(1, result['levelDropPercentagePoints'])
        self.assertTrue(result['chargeCounterWasConstant'])
        self.assertEqual('REQUIRES_REVIEW', result['energyAcceptanceDecision'])

    def test_powered_waiting_baseline_cannot_pass(self):
        result = analyze('WAITING_FOR_PHYSICAL_UNPLUG', sample(0, 100, 'waiting', power='true'))
        self.assertEqual('INVALID', result['measurementValidity'])
        self.assertEqual(0, result['measuredSeconds'])
        self.assertEqual('NOT_EVALUABLE', result['energyAcceptanceDecision'])

    def test_short_window_or_reconnected_power_cannot_pass(self):
        for second in [sample(2, 1100, 'measurement'), sample(2, 1901, 'invalid', power='true')]:
            self.assertEqual('INVALID', analyze(COMPLETE, sample(1, 100, 'start') + second, stats('1s'), stats('30m 2s'))['measurementValidity'])

    def test_frozen_system_statistics_cannot_pass_even_if_sample_times_advance(self):
        self.assertEqual('INVALID', analyze(COMPLETE, sample(1, 100, 'start') + sample(2, 1901, 'measurement'), stats('27s 378ms'), stats('27s 378ms'))['measurementValidity'])

    def test_simulated_battery_or_awake_sample_cannot_pass(self):
        for second in [sample(2, 1901, 'measurement', stopped='UPDATES STOPPED'), sample(2, 1901, 'measurement').replace('Asleep', 'Awake')]:
            self.assertEqual('INVALID', analyze(COMPLETE, sample(1, 100, 'start') + second, stats('1s'), stats('30m 2s'))['measurementValidity'])

    def test_duration_and_minimum_are_strict(self):
        self.assertAlmostEqual(93784.125, duration_seconds('1d 2h 3m 4s 125ms'))
        with self.assertRaises(ValueError):
            duration_seconds('unknown')
        with self.assertRaises(ValueError):
            analyze(COMPLETE, '', minimum_seconds=60)


if __name__ == '__main__':
    unittest.main()
