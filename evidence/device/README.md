# Device evidence provenance

`00-first-launch.png`, root-level `01-garden.png` through `06-settings.png`, `instrumentation-first.txt`, `startup.txt` and `memory.txt` are earlier development snapshots. In particular, earlier screen captures could occur before the final compositor frame; these root screenshot files are **superseded timing snapshots**, not the latest layout or marketing evidence. Retain them for provenance; use the current named suite directory and its matching current log for review.

`first-extended/` contains an earlier failed extended run. The matching first failure log is `physical-hd10-first-extended-failures.txt`; its existence must not be hidden by a later passing rerun. Capability reports inside that directory are actual individual observations but do not turn the overall failed suite into a pass.

Canonical current logs are `physical-hd10.txt`, `layout-7in-large-font.txt`, `layout-8in.txt` and `offline-hd10.txt`. They are test results only after their content and freshness have been verified; a filename or screenshot directory alone is not a pass. Their expected counts are respectively 8, 4, 4 and 4. Named suite folders contain captures pulled after those runs; files not recreated by a selected subset test can be stale, so review only the cases the corresponding suite actually executes.

The actual hardware is a physical Fire HD10. The smaller-display suites reuse that device with display overrides and are not physical 7/8-inch hardware validation. See [FIRE_TEST_PLAN.md](../../docs/FIRE_TEST_PLAN.md) for precise coverage and the guarded observation updater.
