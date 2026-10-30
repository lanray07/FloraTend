# FloraTend receipt verifier

The Android client sends only the app-scoped Amazon user ID and receipt ID over HTTPS. The server validates that pair with Amazon RVS and checks the exact `floratend.premium` entitled product, receipt ID, purchase date, cancellation and deployment environment. Responses contain only `verified`, `revoked`, `invalid` or `unavailable`; identifiers and upstream URLs must never be logged. The shared secret is server-only.

`LAT` accepts only beta products. `PRODUCTION` rejects beta products and test transactions. Do not expose a client-selected environment or upstream URL. Missing configuration, rate-limit failures, timeout, throttling and invalid shared secrets fail closed. Definitive invalid/cancelled receipts revoke the local grant; a transient failure preserves an already server-verified offline cache but grants nothing new. Old SDK-only caches require an online full-history revalidation.

Run the protocol and HTTP tests with `node --test server/receipt-verifier.test.mjs` (Node 22+). They use explicit fixtures and are not evidence of a real Amazon receipt validation.

## Cloudflare Workers Free deployment

Supabase quoted $0 but rejected a dedicated project because the account has reached its active-free-project limit. No Supabase resources were created or other projects modified. The selected alternative is Cloudflare Workers with a SQLite-backed Durable Object, which is supported on Workers Free. Confirm the actual account plan before deploying; do not upgrade or enable paid services.

`cloudflare-worker.mjs` exposes only `/verify`. Its one named Durable Object atomically enforces 10 requests per minute per HMAC owner and 240 globally, across locations and restarts. Owner hashes and global counters are the only stored data. Minute buckets expire logically every minute; old rows are removed during traffic and by a cleanup alarm after traffic stops. Provider backups may retain previous storage snapshots under Cloudflare's policies. No raw Amazon account IDs, receipts, plant records or photographs are stored. Raw application logging and automatic request observability are disabled. Platform ingress protections and free-tier availability limits still apply.

The deployed LAT configuration is `wrangler.jsonc`; `wrangler.production.jsonc` deploys the separate production verifier, which rejects beta/test receipts. The Android default points to the production URL; LAT builds must explicitly supply the LAT URL. Cached grants are bound to the exact verifying endpoint.

Use pinned Wrangler 4.154.0. Run `npx wrangler@4.154.0 deploy --config server/wrangler.jsonc --dry-run` before deployment. Secrets must be supplied from a private JSON file outside Git using `--secrets-file`; never place literal secret values in shell commands. Configure `AMAZON_RVS_SHARED_SECRET` and a separately generated random `FLORATEND_RATE_SALT` (at least 32 characters). `FLORATEND_RVS_ENVIRONMENT` defaults to `LAT`. The SQLite class migration is in the checked-in configuration. Workers Free enforces its fixed 10ms CPU limit; a custom CPU-limit field is deliberately omitted because Free rejects that setting. Free-tier limit exhaustion must fail requests rather than authorize a paid upgrade.

Build the app with `FLORATEND_RECEIPT_VERIFIER_URL=https://<worker>.<subdomain>.workers.dev/verify` or Gradle `-Pfloratend.receiptVerifierUrl=...`. An empty value deliberately disables new production checkout and grants. A development QA build uses the separate `com.floratend.app.qa` package, preserving the Amazon-installed app. Deployment instructions do not themselves establish a live deployment or successful real receipt verification; consult current release evidence.

Deployment acceptance: real owned LAT receipt returns verified; a changed receipt/user pair is rejected; the concurrent rate-limit boundary is enforced across requests; server outages retain an already verified cache; account changes and newer receipt decisions supersede in-flight verification; the production endpoint rejects sandbox/beta receipts. Capture sanitized booleans/statuses only, never receipt IDs or secrets.

References: [Amazon RVS fields/status codes](https://developer.amazon.com/docs/in-app-purchasing/iap-rvs-for-android-apps.html), [Amazon server setup](https://developer.amazon.com/docs/in-app-purchasing/iap-rvs-setup-prod.html), [Workers pricing](https://developers.cloudflare.com/workers/platform/pricing/), [Durable Objects Free pricing and failure limits](https://developers.cloudflare.com/durable-objects/platform/pricing/).
