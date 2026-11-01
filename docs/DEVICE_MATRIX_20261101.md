# Device coverage matrix — 1 November 2026

This matrix separates declared targets, physical execution and simulated dimensions. It does not certify all devices accepted by an APK manifest. Source candidate:0.1.9/code10, minAPI26, targetAPI36. Actual Amazon-installed build:0.1.7/code8; new code10 genuine store regression is pending delivery.

| Configuration | Executed scope | Result / evidence |
|---|---|---|
| Physical FireHD10 KFTRWI, API28, native1920×1200 landscape |39QA workflow tests; real VoiceView/native accessibility bridge;120-private-photo normal-app scroll |PASS in stated scope: [native suite](../evidence/release-next/native-final-suite.txt), [VoiceView](../evidence/release-next/voiceview-bridge.txt), [scrolling](../evidence/release-next/performance-standalone.json) |
| Same physical HD10, actual Amazon code8 |Cold-start checkout, genuine freeLAT purchase/automatic Premium, Restore, offline restart |Historical PASS only; [genuine code8 evidence](CHECKOUT_DELAY_DIAGNOSIS_20261101.md). Not a code10 pass. |
| Simulated7-inch portrait,600×1024px,160dpi,font1.8 |Primary control touch bounds and care/journal/voice/settings reachability |3tests PASS; [actual configuration/captures](../evidence/release-next/accessibility/fire7-portrait-font180/) |
| Simulated7-inch landscape,1024×600px,160dpi,font1.8, animation scales0 |Same primary controls and reachable flows |3tests PASS; [run](../evidence/release-next/layout-fire7-landscape-motion0-font180.txt), [captures](../evidence/release-next/accessibility/fire7-landscape-motion0-font180/) |
| Simulated8-inch portrait,800×1280px,160dpi,font1.8 |Same primary controls and reachable flows |3tests PASS; [captures/configuration](../evidence/release-next/accessibility/fire8-portrait-font180/) |
| Simulated8-inch landscape,1280×800px,160dpi,font1.8 |Same primary controls and reachable flows |3tests PASS; [captures/configuration](../evidence/release-next/accessibility/fire8-landscape-font180/) |
| Physical7/8-inch tablets |No physical model execution this pass |UNVERIFIED; size overrides are not hardware certification. |
| API31+ system splash and API33+ notification permission |Production resources/conditional permission handling compile |Runtime UNVERIFIED on newer supported hardware. |
| API36 / FireOS16 |Target configured and builds/lint pass |Runtime UNVERIFIED. |
| Extended unplugged battery behavior |12-hour periodic unique WorkManager job exists |NOT MEASURED; charging/USB workflow is not battery-drain evidence. |
| FireTV / Automotive |0selected in copied Test2 target configuration |Outside this tablet submission; remote/automotive compatibility not claimed. |
| iOS / GooglePlay / web app |No matching store/runtime port in this repository |Outside this Amazon tablet candidate. Static website is a separate development preview. |

The Test2 draft retained12 selected tablet models after APK replacement; those console declarations do not establish physical pass results. Default tablet font/size/density, Wi-Fi and rotation settings are restored after each test. VoiceView was restored to off. The original Amazon package was never uninstalled or cleared.

Public evidence excludes device serial, tester/account identifiers and raw logs. Additional locale/script/pronunciation, camera, newest-OS, hardware and battery tests remain individual gates in the generated matrix.
