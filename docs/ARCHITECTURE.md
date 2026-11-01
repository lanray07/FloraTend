# Current architecture

This describes the source currently present, not a claim that every original architectural or acceptance requirement is complete. The project is one Gradle Android application module, `:app`, with package `com.floratend.app`; the master brief's separately packaged feature modules and strict Clean Architecture layers are not yet implemented.

## Runtime and data flow

`FloraApp` lazily creates a Room database, settings and billing service. `MainActivity` synchronously registers the Amazon SDK listener in onCreate before asynchronous entitlement recovery, hosts Compose and owns Android SpeechRecognizer/TextToSpeech lifecycle. `FloraRoot` chooses onboarding or the application surface, keeps navigation/edit-dialog state in Compose, and obtains `PlantViewModel`. Wide layouts use a rail and adaptive plant grid; compact layouts use bottom navigation. Material colours, serif headings and the botanical artwork form the current design treatment.

`PlantViewModel` wraps the DAO and exposes Room Flows as StateFlows. Compose collects plant, schedule, journal and history snapshots. Mutations run in `viewModelScope`; Room suspend operations perform database work off the UI thread. Photo import and export IO use `Dispatchers.IO`. There is no separate domain-repository interface, Hilt graph or navigation library at present.

## Source responsibilities

| Source | Responsibility |
|---|---|
| `Data.kt` | Room entities, DAO, schema v1, transaction for care completion, recurrence helper, application composition root |
| `PlantViewModel.kt` | Collection flows, plant limit check and mutation commands |
| `MainActivity.kt` | Speech/TTS lifecycle, theme, adaptive app navigation, collection/journal/insights and shared Compose widgets |
| `Editors.kt` | Plant/schedule/journal dialogs, photo selection/capture and onboarding |
| `Screens.kt` | Calendar list modes, reviewed voice/text command execution, settings, purchase/restore and export entry points |
| `Settings.kt` | DataStore preferences for onboarding, theme, units and reminders; Premium is projected only from the verified entitlement store |
| `Photos.kt` | Bounded image decode, JPEG conversion and private local photo copy |
| `Reminders.kt` | Unique 12-hour periodic WorkManager job and aggregate due-task notification |
| `Voice.kt` | Resource-backed structured command parsing for 19 draft locale dictionaries; unsupported locale returns Unknown |
| `AmazonBilling.kt` | Native Appstore SDK listener, user/product/update requests, purchase/restore callbacks and strict server-verified cached entitlement |
| `Export.kt` | JSON record export and quoted/formula-prefixed CSV plant export |

These are file-level responsibilities inside one package, not independently deployable feature modules. Deeper layering should follow actual interface needs and preserve the currently tested data semantics.

## Persistence and recurrence

Room tables: `plants`, `schedules`, `journal`, `history`. Child rows reference plant UUIDs with indexed cascading foreign keys. Saving a plant uses `@Upsert`; completing care records an observation and advances its schedule in one DAO transaction. Journal and history are ordered by ISO local date strings. Deleting a plant cascades to that plant's child rows. App-wide deletion clears plant rows and the private photos directory; settings and Amazon entitlement are separate storage and need an explicit deletion-policy decision.

Dates represent local calendar days, not instants. `Recurrence.next` validates intervals from 1 to 3,650 days and adds calendar days. `Recurrence.reminder` maps a date to 09:00 in a supplied ZoneId; current WorkManager delivery does not schedule that exact instant. The periodic worker queries unpaused due schedules against the current local date and updates one stable notification ID. WorkManager may defer execution under OS power restrictions. Exact timing and timezone/device behaviour require device testing.

Schema v1 is exported under `app/schemas`. No destructive fallback is configured and no previous-version migration exists. Before any schema version increment, add a migration and preservation tests against the exported prior schema.

## Photos, export and optional network features

Selected content is decoded in two passes and downsampled to a bounded size before JPEG storage in `filesDir/photos`. URI grants are not retained as the source of truth. Camera integration launches an external preview activity; it does not request the app's own camera permission or provide full-resolution CameraX capture. UI handles missing journal photo files with explanatory text. Per-record unused-photo cleanup and large-collection memory behaviour require further verification.

JSON export includes all four record collections and schemaVersion, but excludes image binaries and does not implement restoration. CSV is a plant table, with quoted fields and a prefix for formula-leading cells. Android's document creation picker chooses the export destination. Premium is required for the export buttons. Cloud backup, encrypted backup and photo-identification engine integration are unavailable rather than silently simulated.

The app requests Internet access for Amazon/service capabilities. Core DAO operations do not require a network or account. Android speech engines may use their own network services; current voice input makes no offline-recognition guarantee. Release network auditing must substantiate privacy disclosures.

## Voice and purchase boundaries

Push-to-talk requests microphone access contextually, checks for recognition service availability, displays listening/transcription state, and allows typed command review. Installed locale dictionaries map reviewed text commands to app actions; actual recognition requires a compatible device engine. Water-record commands resolve a unique plant/schedule and require confirmation. TTS selects the app locale if supported, reports unavailable languages, and stops playback when the Activity stops. A separate spoken-feedback preference is not implemented. No app code persists raw microphone recordings or runs a continuous listener.

Billing retrieves store prices rather than embedding a purchase price. It checks request IDs, expected Amazon account, entitlement SKU/type, deferred/cancelled receipts and SDK mode. Full-history restore is paginated; cached entitlement supports offline use. Release builds reject sandbox mode. New release grants require the strict HTTPS server verifier backed by Amazon RVS. Source defaults to the production verifier; LAT artifacts explicitly select the separate LAT service. The receipt secret exists only in the backend. Account, receipt, mode and endpoint binding guard cached grants. Genuine code7 automatic purchase/Restore/offline tests passed; code8's cold-start checkout regression is tracked separately in [the diagnosis](CHECKOUT_DELAY_DIAGNOSIS_20261101.md). See [BILLING_INTEGRATION.md](BILLING_INTEGRATION.md).

## Development tooling and verification boundaries

Python translation tooling extracts XML string/plural resources, protects placeholders/brand terms, caches translations by source content, validates missing resources and tracks review hashes. The live DeepL provider is not configured or verified; credentials must remain in the developer environment. Nineteen voice dictionaries, 15 store metadata drafts and 120 localized screenshot compositions exist, with human linguistic approval pending. The website is English only. Korean and Simplified/Traditional Chinese from the standing worldwide-language priorities are not packaged in the current 19-locale set.

The static website generator emits twelve English pages. Its local build deliberately blocks indexing until a real SITE_URL is configured. Acceptance reporting has 204 source-checkbox criteria and refuses PASS/FAIL records without dated environment/result/evidence fields. It does not establish that the supplied evidence substantively proves the criterion; that remains reviewer responsibility.

JUnit covers recurrence/parser behaviour; instrumented tests exercise Room and Compose flows when executed on a connected device. Supporting Python tests exercise pipeline and website/reporting failure paths. Execution logs, not test-source presence, determine acceptance status.
