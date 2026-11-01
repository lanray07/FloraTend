# Pure application logic

`:core` contains locale-aware number input, reviewed command parsing and calendar
recurrence. It has no Android, Amazon, UI, database or network dependency. The app
passes text/locale/dictionaries into it and performs platform actions only after
review and confirmation. Existing class names stay stable for the callers.

Run `gradlew :core:test`. Dictionary contract tests read the actual app assets;
this test-data path does not create a production dependency on `:app`.

The Android module retains Room, SDK adapters, Compose and lifecycle integration.
Billing and strict server verification were not moved or relaxed in this change.
