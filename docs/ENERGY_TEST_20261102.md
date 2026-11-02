# Actual unplugged energy measurement - 2 November 2026

The release gate AC-18.05 remains NOT TESTED until a valid physical discharge interval and its background-work observations are reviewed. Valid recording is separate from acceptable energy use; parser fixtures cannot close the real-device gate.

## Preserved first attempt

The user reported32minutes unplugged. The actual collected recorder remained WAITING_FOR_PHYSICAL_UNPLUG and contained one USB-powered waiting baseline. Its detached process was still running. Cumulative Android statistics showed27.378seconds on battery and0seconds on-battery screen-off, not a qualifying30minute test. The cause is unisolated: the original recorder did not log waiting heartbeats, so it cannot distinguish uninterrupted powered readings from a scheduling gap. This does not establish an app battery defect or contradict the user report by assumption. Raw files remain private; sanitized facts are in `evidence/energy/attempt1.json`. The old recorder was stopped before the retry.

## Retry protocol, established before collection

- Actual installed Amazon0.1.14/code15, nondebug, installer com.amazon.venezia; no APK change or test-package installation.
- A detached native shell logger was armed at08:53UTC. The user confirmed cable removal directly from the tablet port; the host observed ADB absent at08:54UTC. Reconnect requested no earlier than09:26London time.
- The observer waits for physical AC/USB/wireless power-off, enters the background with HOME and turns the screen off normally, then requires at least1800seconds using device uptime. The app is never force-stopped, BatteryService is never simulated/reset and no wakelock is held.
- BatteryService and power snapshots are sampled approximately once per minute. Waiting heartbeats were added to the retry. Native start/end package-filtered batterystats are captured around the window. Observer work is shell activity; it is not attributed to FloraTend, and its device-level overhead must be acknowledged.
- A host watcher captures cumulative system statistics and the recorder files immediately when the same tablet reconnects, before any app/UI action. Raw files stay in the private .android directory.

## Interpretation rules

The normal recording path requires COMPLETE status, at least1800seconds between monotonic samples, external power false and screen Asleep at measurement samples, no stopped/simulated BatteryService updates, and corroborating cumulative on-battery/screen-off time. Short, powered, reconnected or uncorroborated runs are invalid. The analyzer never automatically approves the energy criterion.

Deep idle can defer shell polling. If the native observer misses a transition, independently retained before/after Android cumulative battery accounting and host connection timestamps can be reviewed as a separate evidence channel. Counter epochs/resets must be checked explicitly; never blindly subtract counters across a reset or invent missing samples. A valid system-accounting interval must independently demonstrate at least1800seconds of actual screen-off battery operation.

After validity is established, review actual device drain, app-attributed CPU/power/wakelock observations and source scheduling policy. Constant or integer-percent counters do not prove zero energy. The result is scoped to the actual device, configuration and idle interval; it is not a universal battery-life guarantee.

## Analyzer verification

Six synthetic regression tests cover valid recording versus energy acceptance, powered-only baseline rejection, short/reconnected intervals, frozen cumulative accounting, simulated/awake samples and duration/minimum validation. The complete supporting suite passed85tests before the additional explicit NOT_EVALUABLE assertion; the six targeted cases are rerun after that assertion. Actual attempt1 is rejected by the same analyzer. No synthetic sample is published as device evidence.
