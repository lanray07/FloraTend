// Negative fixtures only: no private account IDs or real receipts are supplied.
import assert from 'node:assert/strict';
const endpoint = process.argv[2];
assert.ok(endpoint?.startsWith('https://'), 'Pass the deployed HTTPS /verify URL');
const owner = `floratend-negative-fixture-${Date.now()}`;
async function send(body, suffix='') {
  const response = await fetch(endpoint+suffix,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
  return {status:response.status,body:await response.json(),cache:response.headers.get('cache-control')};
}
assert.equal((await fetch(endpoint)).status,405);
assert.equal((await send({})).status,400);
assert.equal((await send({userId:owner,receiptId:'invalid-fixture'},'?unexpected=1')).status,400);
const results = await Promise.all(Array.from({length:12},()=>send({userId:owner,receiptId:'invalid-fixture'})));
const allowed = results.filter(x=>x.status===200);
const limited = results.filter(x=>x.status===429);
assert.equal(allowed.length,10,'Durable owner cap must allow exactly ten concurrent fixture requests');
assert.equal(limited.length,2,'Remaining concurrent requests must be rate limited');
for (const result of allowed) {
  assert.deepEqual(result.body,{state:'invalid'});
  assert.equal(result.cache,'no-store');
}
console.log(JSON.stringify({endpoint,negativeFixtureOnly:true,methodRejected:true,malformedRejected:true,queryRejected:true,concurrentRequests:12,amazonRejectedInvalidPairs:allowed.length,durableRateLimited:limited.length,rawAccountOrReceiptIdentifiersUsed:false,status:'PASS'},null,2));
