# First physical-device launch observation

Date: 2026-10-28. Device observed by the primary agent: Amazon KFTRWI (Fire HD 10, 2021), Android API 28, landscape orientation. The debug APK was installed and launched; the primary agent visually inspected the actual captured screenshot [00-first-launch.png](00-first-launch.png).

The primary agent's observed `adb shell am start -W` timing fields for the first launch after installation were:

```text
ThisTime: 3955
TotalTime: 3779
WaitTime: 4031
```

Values are the Android activity manager's reported milliseconds and are preserved as observed, without normalising their relationship. They are a single first-install launch observation, not an instrumented app-level startup measurement or a repeatable performance baseline. No smooth-scrolling, responsiveness, memory, cold/warm startup budget, navigation coverage or general Fire-device compatibility pass follows from this record.

Evidence artifact: `evidence/device/00-first-launch.png`. Debug APK snapshot hash: `evidence/debug-apk-sha256.json`. Device workflow, persistence, accessibility, notification and speech-engine tests require their own executed evidence.
