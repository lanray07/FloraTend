# Amazon Billing Test Report

Generated criterion-level verification report. No unrecorded result is inferred. For detailed test procedures consult [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md).

| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |
|---|---|---|---|---|---|---|---|
| AC-12.01 | Amazon In-App Purchasing | Amazon-compatible billing is integrated. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.02 | Amazon configured product lookup prerequisite | Premium product information can be retrieved. | / Not executed | The app-specific public key, configured catalogue and Amazon test environment are not verified. No live product retrieval or store price is recorded. | BLOCKED | docs/BILLING_INTEGRATION.md | Configure the actual Amazon app and entitled product, then execute product lookup and all purchase/restore scenarios. |
| AC-12.03 | Amazon In-App Purchasing | The paywall displays store-provided pricing. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.04 | Amazon In-App Purchasing | Purchase success grants the correct entitlement. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.05 | Amazon In-App Purchasing | Purchase cancellation does not grant Premium. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.06 | Amazon In-App Purchasing | Failed purchases are handled. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.07 | Amazon In-App Purchasing | Purchase restoration works. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.08 | Amazon In-App Purchasing | Entitlements persist appropriately. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.09 | Amazon In-App Purchasing | Free-tier limits are enforced. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.10 | Amazon In-App Purchasing | Premium features unlock correctly. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.11 | Amazon In-App Purchasing | Development-only test billing cannot activate real entitlements in release builds. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
| AC-12.12 | Amazon In-App Purchasing | No Google Play Billing dependency exists in the Amazon build. | / Not executed | No verified test result recorded. | NOT TESTED |  | Execute the stated procedure and attach evidence. |
