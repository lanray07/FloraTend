# Declared permissions and platform capabilities

Source manifest audit date: 2026-10-28. This inventory describes `app/src/main/AndroidManifest.xml`; it does not substitute for auditing the merged release manifest, runtime behaviour or external providers.

| Declaration | Purpose | User control / verification boundary |
|---|---|---|
| `android.permission.RECORD_AUDIO` | Explicit push-to-talk speech recognition | Requested contextually from the voice screen; denial and engine availability require runtime verification. No continuous microphone service is declared. |
| `android.permission.POST_NOTIFICATIONS` | Optional care-check notifications on Android 13+ | Requested when enabling reminders where required; reminders are opt-in. Older devices use their platform notification controls. |
| `android.permission.INTERNET` | Amazon SDK/service connectivity and optional engine capabilities | Normal Android permission, no runtime permission dialog. Core plant records do not require an account or network. Actual release traffic still requires review. |
| Optional microphone hardware feature | Keeps devices without a microphone eligible for touch/text use | Voice is conditional on hardware, engines and supported languages; declaration alone does not prove compatibility. |
| RecognitionService and TTS_SERVICE package queries | Allows capability discovery of installed speech engines | These queries do not grant microphone access or establish offline-language availability. |
| Amazon response receiver notification permission | Restricts Amazon IAP notification receiver broadcasts | Defined on the exported SDK receiver; not a user runtime permission. Live SDK callback verification remains incomplete. |

The app currently declares no precise/coarse location permission, broad media/storage permission or app-owned camera permission. Image selection uses a document/content picker and copies an image into private storage. Camera preview uses an external camera activity, whose provider controls its permission and capture behaviour. Full-resolution CameraX capture is not implemented.

Android automatic backup is disabled (`allowBackup=false`), and cleartext network traffic is disabled. Optional cloud backup and encrypted backup are unavailable. Selected export destinations can leave app-private storage by explicit user action; export privacy and deletion behaviour require their own tests.
