# FloraTend privacy notice

Last updated: 3 November 2026

FloraTend is maintained by Lanray. Contact: **banksmi@mail.com**. This notice describes the current FloraTend app; availability and features may vary by release and device.

## Your plant records

Plant names, species, locations you enter, notes, care schedules, care history, journal entries, measurements and imported photographs are stored in the app's private storage on your device. A separate FloraTend account is not required. FloraTend does not operate a cloud database or cloud synchronization service for these records.

Choosing a photograph copies an image into app storage. Taking a photograph uses an available camera application. FloraTend does not request precise device location or automatically upload plant photographs. Cloud backup is not available. Optional photo identification is enabled in the production candidate and sends the selected photo only after the separate Send photo consent described below. You can decline and enter a species manually.

## Speech and spoken feedback

The initial Fire release provides typed commands and optional spoken responses. Push-to-talk is deferred from this release: it does not request microphone access or capture microphone audio.

Your installed text-to-speech provider processes text when you use spoken feedback. Depending on the installed provider and voice, processing may use a network connection and be subject to that provider's privacy policy. Spoken feedback can include plant names or reminders you request. Touch controls and typed commands remain available. Installed voices and supported languages vary by device, and you can disable spoken feedback in Settings.

## Amazon purchases

Optional purchases and restoration use Amazon's Appstore SDK. Amazon processes store account, purchase and transaction information under its policies. FloraTend stores entitlement status and relevant Amazon account/receipt references locally to associate an unlock with its owner and support restoration. No payment-card details are collected by FloraTend itself. Purchasing may be unavailable until the store product is configured and offered in your marketplace.

The SDK also supplies marketplace or account-region information and includes operational diagnostics for receipt verification, parsing failures and exceptions. Connections to Amazon expose connection information, such as an IP address, to the receiving service. These provider operations support purchase functionality, troubleshooting and fraud prevention. FloraTend does not request an advertising identifier or use purchase information for advertising. Amazon controls retention of information processed by its services; clearing FloraTend's local data does not delete Amazon's records.

Premium purchases and restores use a Cloudflare-hosted server and Amazon's Receipt Verification Service. FloraTend sends the app-scoped Amazon user identifier and purchase receipt identifier over HTTPS; plant names, notes, photographs and payment-card details are not part of this request. The verifier stores short-lived keyed owner hashes and rate-limit counters rather than raw identifiers or receipts. Application request logging is disabled in the deployed configuration. Cloudflare and Amazon can process network/request metadata and retain provider backups under their own policies; this is not a promise of zero provider retention. Clearing local app data does not remove provider records.

## Optional photo identification

Choosing Send photo forwards the selected/imported JPEG and a language code through FloraTend's Cloudflare server to Pl@ntNet for identification suggestions and available localized names. Manual entry and consent cancellation remain available. Plant names, notes, receipt/account identifiers and GPS coordinates are not added to the identification request. The imported image is re-encoded before transmission; identification suggestions are reviewed or edited by you and saved only after an explicit Save action. Match scores are not calibrated probabilities, and suggestions can be wrong.

The application server forwards rather than stores the photo and has application request logging disabled. Pl@ntNet processes the image and retains request metadata under its own terms. Cloudflare receives ordinary network information. Free-service allowances use day-scoped keyed network-address hashes, daily counters and short-lived reservation leases. Provider logs/backups and processing are separate from the local plant database; local deletion does not delete provider metadata. Identification can be unavailable or reach its daily allowance; manual species entry remains available.

## Notifications, settings and analytics

Care-check notifications use locally saved schedules. You control reminders through the app and device settings. Language, appearance and measurement preferences are stored locally. FloraTend includes no third-party advertising SDK and operates no separate behavioral analytics service. Amazon and installed speech providers may process information needed for their services.

## Export, deletion and retention

Records remain until you remove them, clear the app's data or uninstall it. Delete all plant data removes the app's plant records, related care/journal records and saved plant photographs. App preferences and purchase bookkeeping may remain until app data is cleared; Amazon's own purchase records are governed by Amazon.

Where export is available, the destination you choose receives your records, including notes. The JSON record export does not embed photographs. Exported copies are outside the app's private storage and are not removed when you delete records in FloraTend. Manage those copies at their destination. Android automatic app backup is disabled for this version.

## Support and this public project

Emailing support shares the information you include in your message. Public GitHub issues and comments are visible to others; use email for private support details. GitHub hosts this notice and the public project under [GitHub's privacy statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement). GitHub Pages also hosts the development website at https://lanray07.github.io/FloraTend/. GitHub logs website visitor IP addresses for security purposes, whether or not visitors are signed in. The app does not automatically transmit plant records to GitHub.

For privacy questions or requests, contact **banksmi@mail.com**. Changes to actual data processing will be reflected in an updated notice.
