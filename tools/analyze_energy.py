"""Validate a real unplugged BatteryService recording; never automatically approve energy use.

Raw samples and batterystats stay outside Git. This emits only aggregate observations.
The detached recorder writes COMPLETE only after1800seconds of sampled physical
power-off, without resetting BatteryService, force-stopping the app or a wakelock.
"""
from __future__ import annotations
import argparse
import json
from pathlib import Path
import re

COMPLETE = 'COMPLETE_30_MINUTES_PHYSICALLY_UNPLUGGED'
MARKER = re.compile(r'^@@SAMPLE (\d+) (\d+) (\d+) ([a-z]+)\s*$', re.MULTILINE)


def duration_seconds(value):
    matches = list(re.finditer(r'(\d+(?:\.\d+)?)(ms|d|h|m|s)', value))
    if not matches or re.sub(r'(\d+(?:\.\d+)?)(ms|d|h|m|s)', '', value).strip():
        raise ValueError('Unrecognised Android duration')
    scale = {'d': 86400, 'h': 3600, 'm': 60, 's': 1, 'ms': .001}
    return sum(float(match[1]) * scale[match[2]] for match in matches)


def stats_time(text, screen_off=False):
    label = 'Time on battery screen off' if screen_off else 'Time on battery'
    match = re.search(r'^\s*' + label + r': (.*?) \([^)]*\) realtime', text, re.MULTILINE)
    return duration_seconds(match[1]) if match else None


def parse_samples(text):
    markers = list(MARKER.finditer(text))
    samples = []
    for i, marker in enumerate(markers):
        body = text[marker.end():markers[i + 1].start() if i + 1 < len(markers) else len(text)]
        def number(label):
            value = re.search(r'^\s*' + re.escape(label) + r': (-?\d+)\s*$', body, re.MULTILINE)
            return int(value[1]) if value else None
        power = {}
        for source in ['AC', 'USB', 'Wireless']:
            value = re.search(r'^\s*' + source + r' powered: (true|false)\s*$', body, re.MULTILINE)
            power[source] = value[1] == 'true' if value else None
        screen = re.search(r'^\s*mWakefulness=(\w+)', body, re.MULTILINE)
        samples.append({'index': int(marker[1]), 'epochSeconds': int(marker[2]),
                        'uptimeSeconds': int(marker[3]), 'phase': marker[4], 'power': power,
                        'level': number('level'), 'scale': number('scale'),
                        'chargeCounterUah': number('Charge counter'),
                        'voltageMv': number('voltage'), 'temperatureTenthsC': number('temperature'),
                        'screen': screen[1] if screen else None,
                        'updatesStopped': bool(re.search(r'updates\s+stopped', body, re.IGNORECASE))})
    return samples


def analyze(status, sample_text, start_stats='', end_stats='', minimum_seconds=1800):
    if minimum_seconds < 1800:
        raise ValueError('The release protocol requires at least1800seconds')
    all_samples = parse_samples(sample_text)
    measured = [sample for sample in all_samples if sample['phase'] in ('start', 'measurement', 'invalid')]
    errors = []
    if status.strip() != COMPLETE:
        errors.append('Recorder did not report a complete physically unplugged window')
    if len(measured) < 2 or not measured or measured[0]['phase'] != 'start':
        errors.append('Missing start/end measurement samples')
    unplugged = [sample for sample in measured if sample['phase'] != 'invalid' and all(value is False for value in sample['power'].values())]
    observed_span = measured[-1]['uptimeSeconds'] - measured[0]['uptimeSeconds'] if len(measured) >= 2 else 0
    # A powered reconnection sample must never extend the claimed unplugged span.
    elapsed = unplugged[-1]['uptimeSeconds'] - unplugged[0]['uptimeSeconds'] if len(unplugged) >= 2 else 0
    if elapsed < minimum_seconds:
        errors.append('Measured uptime interval below the required duration')
    for sample in measured:
        if sample['phase'] == 'invalid' or any(value is not False for value in sample['power'].values()):
            errors.append('External power present, unknown, or reconnected during measurement')
        if sample['updatesStopped']:
            errors.append('BatteryService updates stopped; simulated state is not acceptable')
        if sample['screen'] != 'Asleep':
            errors.append('Screen-off idle state missing at a measurement sample')
        if sample['scale'] != 100 or sample['level'] is None or not 0 <= sample['level'] <= 100:
            errors.append('Battery percentage missing or invalid')
    if any(right['uptimeSeconds'] <= left['uptimeSeconds'] for left, right in zip(measured, measured[1:])):
        errors.append('Measurement timestamps are not strictly increasing')
    deltas = {}
    for key, off in [('onBatterySeconds', False), ('screenOffOnBatterySeconds', True)]:
        before, after = stats_time(start_stats, off), stats_time(end_stats, off)
        delta = after - before if before is not None and after is not None else None
        deltas[key] = delta
        if delta is None or delta < max(minimum_seconds - 5, elapsed - 5):
            errors.append('Cumulative ' + key + ' does not corroborate the sample interval')
    level_drop = measured[0]['level'] - measured[-1]['level'] if measured and all(s['level'] is not None for s in measured) else None
    counter_change = measured[0]['chargeCounterUah'] - measured[-1]['chargeCounterUah'] if measured and all(s['chargeCounterUah'] is not None for s in measured) else None
    return {'measurementValidity': 'INVALID' if errors else 'VALID_PHYSICALLY_UNPLUGGED_WINDOW',
            'errors': list(dict.fromkeys(errors)), 'recordedSamples': len(all_samples),
            'measurementSamples': len(measured), 'measuredSeconds': elapsed,
            'recordingObservedSpanSeconds': observed_span,
            'levelDropPercentagePoints': level_drop, 'chargeCounterDecreaseUah': counter_change,
            'chargeCounterWasConstant': counter_change == 0 if counter_change is not None else None,
            'systemStatsDeltas': deltas, 'energyAcceptanceDecision': 'NOT_EVALUABLE' if errors else 'REQUIRES_REVIEW',
            'scope': 'Recording validity and aggregate readings only. Constant/coarse counters do not prove zero energy use. App attribution, observer overhead and background-work behaviour require review. Raw system logs are not exported.'}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('private_folder', type=Path)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    def read(name):
        path = args.private_folder / name
        return path.read_text(encoding='utf-8-sig') if path.exists() else ''
    report = analyze(read('status.txt'), read('battery-samples.txt'), read('app-batterystats-start.txt'), read('app-batterystats-end.txt'))
    value = json.dumps(report, indent=2) + '\n'
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(value, encoding='utf-8')
    else:
        print(value)
    raise SystemExit(bool(report['errors']))
