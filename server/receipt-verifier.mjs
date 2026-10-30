// No identifiers, shared secrets, upstream URLs or response bodies may be logged.
const SKU = 'floratend.premium';
const MAX_BODY = 8192;
const reply = (status, state) => Response.json({state}, {
  status, headers: {'Cache-Control': 'no-store', 'X-Content-Type-Options': 'nosniff'},
});

export function receiptDecision(data, receiptId, environment) {
  if (!data || data.receiptId !== receiptId || data.productId !== SKU ||
      data.productType !== 'ENTITLED' || !Number.isSafeInteger(data.purchaseDate) ||
      data.purchaseDate <= 0 || typeof data.betaProduct !== 'boolean' ||
      typeof data.testTransaction !== 'boolean' ||
      !(data.cancelDate === null || (Number.isSafeInteger(data.cancelDate) && data.cancelDate >= 0))) {
    return 'invalid';
  }
  if (environment === 'LAT' ? !data.betaProduct :
      environment !== 'PRODUCTION' || data.betaProduct || data.testTransaction) return 'invalid';
  return data.cancelDate === null ? 'verified' : 'revoked';
}

async function readLimited(response, limit) {
  const reader = response.body?.getReader();
  if (!reader) throw new Error('Missing body');
  let size = 0;
  const parts = [];
  try {
    for (;;) {
      const {done, value} = await reader.read();
      if (done) break;
      size += value.byteLength;
      if (size > limit) throw new Error('Body limit');
      parts.push(value);
    }
  } finally { await reader.cancel().catch(() => {}); }
  const bytes = new Uint8Array(size);
  let offset = 0;
  for (const part of parts) { bytes.set(part, offset); offset += part.length; }
  return JSON.parse(new TextDecoder('utf-8', {fatal: true}).decode(bytes));
}

/** Receipt+Amazon user ID are bearer credentials; Amazon validates their pairing.
 * No app-embedded shared API key is treated as authentication. Deploy only behind
 * the durable limiter. This endpoint grants no server account or other resources.
 */
export function createVerifier({secret, environment, limit, fetcher = fetch}) {
  return async function verify(request) {
    if (request.method !== 'POST') return reply(405, 'invalid');
    if (new URL(request.url).search ||
        request.headers.get('content-type')?.split(';')[0].trim() !== 'application/json') {
      return reply(400, 'invalid');
    }
    if (!secret || !['LAT', 'PRODUCTION'].includes(environment) || !limit) return reply(503, 'unavailable');
    if (Number(request.headers.get('content-length')) > MAX_BODY) return reply(413, 'invalid');
    let body;
    try { body = await readLimited(request, MAX_BODY); }
    catch { return reply(400, 'invalid'); }
    if (!body || Object.keys(body).sort().join(',') !== 'receiptId,userId' ||
        ![body.userId, body.receiptId].every(v => typeof v === 'string' &&
          v.length >= 1 && v.length <= 2048 && v.trim() === v && !/[\u0000-\u001f\u007f]/.test(v))) {
      return reply(400, 'invalid');
    }
    try {
      if (!(await limit(body.userId))) return reply(429, 'unavailable');
      const path = [secret, body.userId, body.receiptId].map(encodeURIComponent);
      const upstream = await fetcher(`https://appstore-sdk.amazon.com/version/1.0/verifyReceiptId/developer/${path[0]}/user/${path[1]}/receiptId/${path[2]}`, {
        // Workers supports manual/follow, not redirect:error. Fail closed below
        // on every 3xx response so the secret never follows a redirect.
        method: 'GET', redirect: 'manual', signal: AbortSignal.timeout(8000),
        headers: {Accept: 'application/json'},
      });
      if (upstream.status === 410) return reply(200, 'revoked');
      if ([400, 497].includes(upstream.status)) return reply(200, 'invalid');
      if (upstream.status !== 200) return reply(503, 'unavailable');
      const result = receiptDecision(await readLimited(upstream, 32768), body.receiptId, environment);
      return reply(200, result);
    } catch { return reply(503, 'unavailable'); }
  };
}
