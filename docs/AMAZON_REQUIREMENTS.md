# Amazon requirements research

Verified against public primary sources on 2026-10-28. This is research evidence, not evidence that the application passed these requirements. Developer Console configuration was not inspected. No submission or publication was performed.

## Submission and metadata

Physical-device testing is required before submission; emulator checks are initial verification only. Privacy labels must describe actual collection/use. Retain rights documentation for third-party intellectual property. [Amazon presubmission checklist](https://developer.amazon.com/docs/app-submission/presubmission-checklist.html).

The internal App title differs from the public Display title. The public docs reviewed do not state a numeric Display title maximum: validate the proposed title in Developer Console before approval. Binary language declarations do not translate the application. Amazon supports APK and AAB submission. [Upload your app file](https://developer.amazon.com/docs/app-submission/upload-app-file.html).

For tablet listings: transparent PNG icons at 114×114 and 512×512; 3–10 PNG/JPEG screenshots. Supported landscape sizes: 800×480, 1024×600, 1280×720, 1280×800, 1920×1080, 1920×1200, 2560×1600; portrait is allowed. FloraTend's eight-composition requirement fits the count. Long description: 4,000 characters maximum; short description: 2,000 bytes. Supply 3–5 feature bullets. An optional comma-separated Keywords field exists. Listing inputs are plain text. IAP prices displayed inside the app must come from getProductData. [Appstore details](https://developer.amazon.com/docs/app-submission/appstore-details.html).

Do not put IAP prices in store metadata. Avoid repetitive terms, emojis, misleading assets or claims. [Listing and promotion policy](https://developer.amazon.com/docs/policy-center/listing-promo.html).

## Marketplace and locale matrix

Marketplaces, distribution countries, binary languages, recognition languages and TTS languages are separate. Listing languages: Dutch; English AU/India/US; French; German; Italian; Japanese; Polish; Portuguese BR/PT; Spanish; Swedish. The table below records major marketplace mappings; it is not the complete worldwide distribution list. Outside supported marketplaces, English is the documented fallback. [International distribution](https://developer.amazon.com/docs/app-submission/distribute-your-app-internationally.html).

| Marketplace | Primary region | Listing language |
|---|---|---|
| amazon.com | US/other territories | English US |
| amazon.ca | Canada | English/French |
| amazon.com.mx | Mexico | Spanish |
| amazon.com.br | Brazil | Portuguese |
| amazon.co.uk | UK/Ireland* | English UK |
| amazon.de | Germany/Austria/Switzerland/others | German |
| amazon.fr | France/Belgium/Luxembourg/others | French |
| amazon.it | Italy/others | Italian |
| amazon.es | Spain/Andorra/Portugal | Spanish |
| amazon.co.jp | Japan | Japanese |
| amazon.in | India | English India |
| amazon.com.au | Australia/New Zealand/territories | English AU |

*Ireland Fire tablets normally route through UK; accounts created on amazon.com can route there. Do not infer listing tabs from marketplace names: the documented listing-localization enumeration omits a separate English UK tab despite describing UK marketplace language.

FloraTend release rule: all 19 requested app locales remain pending until complete resources, UI verification and human review exist. Map regional variants to available console tabs only after console verification. In particular ar, hi-IN, tr-TR and id-ID are requested app locales, not separately documented store listing tabs. es-419 and fr-CA must not be assumed to have dedicated listing tabs. Recognition/TTS availability must be detected on each device and locale; listing availability proves neither.

## Amazon IAP integration

Use Appstore SDK, not the legacy standalone IAP v2. The documented pinned dependency is `com.amazon.device:amazon-appstore-sdk:3.0.9` from Maven Central. Add the app-specific public `AppstoreAuthenticationKey.pem` under `app/src/main/assets`; download it from the app's Developer Console record. This public authentication key is not a developer signing secret. [SDK integration](https://developer.amazon.com/docs/appstore-sdk/integrate-appstore-sdk.html). Version 3.0.9 was released May 20, 2026. [SDK release notes](https://developer.amazon.com/docs/appstore-sdk/release-notes.html).

Register a PurchasingListener and receiver; use PurchasingService. On resume refresh user data, product data and purchase updates. Restoration requests full history with getPurchaseUpdates(true); normal synchronization uses false. Handle pagination, duplicate receipts and canceled entitlements. Notify fulfillment after granting purchased content. Handle FAILED/NOT_SUPPORTED without unlocking Premium. [IAP implementation](https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html).

RVS has sandbox and production environments. Receipt verification must use the appropriate environment; do not place its shared secret in an APK. [Receipt verification](https://developer.amazon.com/docs/in-app-purchasing/iap-rvs-for-android-apps.html). Amazon's Billing Compatibility SDK stopped receiving maintenance/support September 15, 2026; new integration should use Appstore SDK. [Compatibility SDK notice](https://developer.amazon.com/docs/in-app-purchasing/billing-compatibility-troubleshoot.html).

Billing acceptance remains BLOCKED without app registration/public key, entitlement SKU configuration and executed App Tester/LAT purchase, cancellation, failure, restore, account-switch and revocation scenarios. A successful SDK compile does not prove purchase verification.

## Plant-care content boundaries

Use intervals to prompt a condition check. RHS recommends checking compost moisture rather than watering at fixed intervals. Wilting can also indicate overwatering, so symptoms alone do not establish a diagnosis. [RHS poorly-houseplant guidance](https://www.rhs.org.uk/plants/types/houseplants/how-to-help-a-poorly-houseplant). Allow drainage and avoid standing water; growing conditions and species affect care. [RHS growing guide](https://www.rhs.org.uk/plants/types/houseplants/growing-guide). These sources support general educational copy, not universal per-species intervals or chemical dosage instructions. No article text or third-party photographs were copied into application assets.

## Outstanding research and verification

- Confirm public title length and actual listing locale tabs in the account's current Developer Console.
- Select distribution countries explicitly; review rights/export/privacy obligations for that selection.
- Validate Fire model/OS support with the built APK and physical devices.
- Verify all final asset uploads against console validation; eight localized screenshots are required by FloraTend for each approved locale.
- Trademark/name clearance, privacy-policy legal approval and horticultural translation review remain outstanding.
