# FloraTend privacy notice

Last updated: 30 October 2026

FloraTend is maintained by Lanray. Contact: **banksmi@mail.com**. This notice describes the current FloraTend app; availability and features may vary by release and device.

## Your plant records

Plant names, species, locations you enter, notes, care schedules, care history, journal entries, measurements and imported photographs are stored in the app's private storage on your device. A separate FloraTend account is not required. FloraTend does not operate a cloud database or cloud synchronization service for these records.

Choosing a photograph copies an image into app storage. Taking a photograph uses an available camera application. FloraTend does not request precise device location or automatically upload plant photographs. Photo-assisted identification and cloud backup are not available in this version.

## Speech and spoken feedback

Microphone access is requested only if you choose push-to-talk on a device with a compatible recognition service. FloraTend does not continuously listen or retain raw microphone recordings. Recognized text is displayed for review and used to interpret a command.

Your installed speech-recognition and text-to-speech providers process speech or text when you use assistant commands or spoken feedback. Depending on the installed provider and voice, processing may use a network connection and be subject to that provider's privacy policy. Spoken feedback can include plant names or reminders you request. Touch controls and typed commands remain available when speech recognition is unavailable. Installed voices and supported languages vary by device.

## Amazon purchases

Optional purchases and restoration use Amazon's Appstore SDK. Amazon processes store account, purchase and transaction information under its policies. FloraTend stores entitlement status and relevant Amazon account/receipt references locally to associate an unlock with its owner and support restoration. No payment-card details are collected by FloraTend itself. Purchasing may be unavailable until the store product is configured and offered in your marketplace.

The SDK also supplies marketplace or account-region information and includes operational diagnostics for receipt verification, parsing failures and exceptions. Connections to Amazon expose connection information, such as an IP address, to the receiving service. These provider operations support purchase functionality, troubleshooting and fraud prevention. FloraTend does not request an advertising identifier or use purchase information for advertising. Amazon controls retention of information processed by its services; clearing FloraTend's local data does not delete Amazon's records.

The 0.1.5 candidate adds server receipt verification. When configured, the app sends its app-scoped Amazon user ID and receipt ID over HTTPS to FloraTend's verification service, which checks the purchase with Amazon. Plant records, photographs, microphone recordings and payment-card details are not sent to that service. The verification service runs on Cloudflare Workers. It does not log receipt IDs or raw Amazon user IDs; short-lived rate-limit counters use a keyed hash of the user ID. Counters expire logically each minute and are normally removed from active storage within six minutes, including when traffic stops. Cloudflare may retain prior storage snapshots and connection/operational metadata under its [privacy policy](https://www.cloudflare.com/privacypolicy/). Provider retention and this processing change require publisher review before public release; the currently distributed Test 1 0.1.4 LAT build uses the previously described SDK-only flow.

## Notifications, settings and analytics

Care-check notifications use locally saved schedules. You control reminders through the app and device settings. Language, appearance and measurement preferences are stored locally. FloraTend includes no third-party advertising SDK and operates no separate behavioral analytics service. Amazon and installed speech providers may process information needed for their services.

## Export, deletion and retention

Records remain until you remove them, clear the app's data or uninstall it. Delete all plant data removes the app's plant records, related care/journal records and saved plant photographs. App preferences and purchase bookkeeping may remain until app data is cleared; Amazon's own purchase records are governed by Amazon.

Where export is available, the destination you choose receives your records, including notes. The JSON record export does not embed photographs. Exported copies are outside the app's private storage and are not removed when you delete records in FloraTend. Manage those copies at their destination. Android automatic app backup is disabled for this version.

## Support and this public project

Emailing support shares the information you include in your message. Public GitHub issues and comments are visible to others; use email for private support details. GitHub hosts this notice and the public project under [GitHub's privacy statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement). The app does not automatically transmit plant records to GitHub.

For privacy questions or requests, contact **banksmi@mail.com**. Changes to actual data processing will be reflected in an updated notice.
