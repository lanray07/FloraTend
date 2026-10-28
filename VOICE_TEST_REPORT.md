# Voice Test Report

Generated criterion-level verification report. No unrecorded result is inferred. For detailed test procedures consult [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md).

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-06.01 | Voice Input and Output | Microphone access is requested only when needed. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.02 | Voice Input and Output | Push-to-talk works on devices with supported speech recognition. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.03 | Voice Input and Output | A visible listening indicator appears. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.04 | Voice Input and Output | Recognised speech is displayed for review where appropriate. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.05 | Voice Input and Output | Supported commands trigger real application actions. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.06 | Voice Input and Output | Spoken responses use an available TextToSpeech engine. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.07 | Voice Input and Output | Destructive actions require confirmation. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.08 | Voice Input and Output | Recognition errors are handled. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.09 | Voice Input and Output | Permission denial does not crash the app. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.10 | Unavailable recognizer touch/text fallback | Missing recognition services trigger a clear fallback. | 2026-10-28 / Physical Amazon KFTRWI Fire HD 10 (2021), API 28, en-GB; separately named display overrides and no-network suite | This physical Fire reported no SpeechRecognizer service. The test asserted Push to talk is disabled, a clear unavailable message is displayed, microphone permission remains denied, and a typed Add a new plant command opens the real editor. This does not test recognition on a capable device. | PASS | evidence/device/physical-hd10.txt; evidence/device/offline-hd10.txt; docs/FIRE_TEST_PLAN.md; app/src/androidTest/java/com/floratend/app/UiSmokeTest.kt; evidence/device/physical-hd10/speech-engine-test.json; evidence/device/physical-hd10/voice-capabilities.json | Retain the tested-device scope and execute remaining locale/device/workflow scenarios separately. |
| AC-06.11 | Voice Input and Output | Users can perform all essential actions without voice. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.12 | Voice Input and Output | No continuous background listening occurs. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-06.13 | Voice Input and Output | Raw microphone recordings are not stored by default. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
