# Implemented data flow review — 1 November 2026

This is a technical review of the current source and retained execution evidence, not legal compliance or publisher-policy approval.

| Data | Implemented path | Execution/source evidence | Remaining decision |
| --- | --- | --- | --- |
| Plant names, notes, care history and journal | Room in private application storage; no required FloraTend account. | Actual no-network process restart and migration tests; Data.kt. | Publisher final privacy notice and supported-device review. |
| Selected photo | Contextual picker URI → bounded decode/orientation correction → private JPEG. No photo argument enters ReceiptVerifier. | Real OS picker rotation and eight EXIF transforms; Photos.kt/Editors.kt. | Real external camera/device scope remains separate. |
| Export | Explicit user destination URI; snapshot written as UTF-8. | Real DocumentsUI rotation/export test; Screens.kt. | The destination provider can have its own sync/retention. |
| Amazon app-scoped user and receipt identifiers | HTTPS client → Cloudflare Worker → Amazon RVS. JSON body permits exactly these two fields. No plant/photo data or payment-card fields. | ReceiptVerifier.kt; receipt-verifier.mjs; six server protocol tests. Genuine LAT purchase evidence separately scoped. | Legal basis, controller/provider details, rights and final retention notice require publisher review. |
| Rate-limit bookkeeping | HMAC owner hash and minute counters in Durable Object,10 requests/minute/owner and240global. Cleanup during traffic and alarms; provider backups can persist historical snapshots. | cloudflare-worker.mjs; checked-in LAT/production configs disable observability. | Provider retention/backups must be accounted for in final notice; no zero-retention promise. |
| Speech | Installed recognition/TTS services; selected device engine can use its provider/network. No app raw recording store. | Speech privacy text, native permission/error/background tests; engine availability report. | Real multilingual engine evidence and publisher review. |
| Identification | No configured identification provider, no active external-photo upload workflow. | Current implementation and explicit unavailable wording. | Provider account/key/terms, consent/result implementation and genuine live test. |

The local development privacy page previously omitted the active Amazon/Cloudflare receipt path and used overbroad future-cloud wording. Its factual development notice now includes that path, request contents, rate-limit hashes/counters and provider-processing caveats. It remains marked as a development notice with no invented legal identity, contact or compliance certification. The website is still local/non-indexable and has not been publicly published.
