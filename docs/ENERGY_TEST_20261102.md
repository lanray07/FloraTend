# Actual unplugged energy measurement - 2 November 2026

AC-18.05 passes for the actual default background configuration using independent Android accounting described below. The stricter planned all-screen-off recorder did not pass and is retained as INVALID; it is not relabelled successful. Valid recording is separate from acceptable energy use; parser fixtures cannot close the real-device gate.

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


## Actual collected result and scope

The host captured the same tablet at09:27:42UTC before any UI action. The first and last system-accounting snapshots share counter epoch08:53:47; no subtraction across a reset is used. On-battery time increased from7.956to2018.719seconds, giving2010.763seconds (33m30.763s). The target UID cached time increased by exactly the same2010.763seconds, and foreground-activity time did not increase. This independently establishes more than30minutes of real battery-powered background operation.

Whole-epoch reported UID CPU was156ms user+36ms system=192ms, about0.01%of the background window as a conservative reported-counter upper bound. No target UID wakelock was reported. FloraTend was not separately listed in modeled UID power, which is not interpreted as zero mAh. The model's whole-device drain increased by20.334mAh; that is modeled whole-device use, not measured FloraTend consumption. The100%gauge and6500000uAh charge counter were constant, so precision-limited readings do not establish zero energy.

The actual configuration had care reminders off, verified by read-only Settings UI after collection; the recorder never changes this preference. Battery-saver mode was enabled for virtually the whole battery epoch. The original WorkManager regression evidence separately checks disabled cancellation and one12hour periodic request when enabled. No excessive background activity was observed in the measured default configuration. This is the scope of the original background-work criterion pass, not certification of every reminder workload/power mode or an all-day battery-life claim.

## Strict recorder failure retained

The native recorder returned ABORTED_POWER_RECONNECTED_DURING_WINDOW. It recorded9samples: a waiting baseline, start and measurement samples, then a powered reconnection sample. Only1792seconds separate the first and last valid unplugged samples. The powered reconnection observation2029seconds after the first sample must not extend the claimed unplugged span; a regression now locks down that distinction.

The first samples were Awake despite the Sleep key request. A long polling gap is consistent with deep idle, and the next loop observed reconnection rather than completing. The independently measured window contains1707.087seconds (28m27.087s) screen-off. Therefore the planned30minute all-screen-off protocol was not completed. Its INVALID report is preserved separately. The original release criterion concerns background work, and the independently corroborated33m30s cached-background interval meets that scope; no missing samples, screen-off seconds or completed-recorder status were invented.

## Preservation and cleanup

The installed Amazon APK hash matched the prior verified candidate; its main process remained the same as at measurement start. Both observers' raw files were copied privately and every transported file hash checked before removing their temporary device directories. No main-app record was edited, no app was force-stopped/cleared/uninstalled, and no test APK was installed. Original Wi-Fi, rotation1/0, font1.0, physical display and disabled VoiceView states were verified. Settings were inspected read-only, Premium remained active, and the app returned to Garden. Raw logs/screenshots remain private.


## Final tooling checks

The full supporting suite passed86tests, including seven energy-parser regressions. The old analyzer was replayed against the actual retry inputs: it reported2029seconds because it included the powered reconnection sample; the corrected analyzer reports1792seconds and retains INVALID status. The independently reviewed Android background-accounting interval remains2010.763seconds. The reusable shell recorder matches the actual retry source after generalizing only its run-specific directory assignment, passes shell syntax checks, and is forced to LF on Windows/Linux checkouts. CI checks its syntax. No production app source or APK changed.
