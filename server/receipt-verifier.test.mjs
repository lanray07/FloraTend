import test from 'node:test';
import assert from 'node:assert/strict';
import {createVerifier, receiptDecision} from './receipt-verifier.mjs';

const receipt = {receiptId:'test-receipt',productId:'floratend.premium',productType:'ENTITLED',purchaseDate:1234,cancelDate:null,betaProduct:true,testTransaction:false};
const request = (body = {userId:'test-owner',receiptId:'test-receipt'}) => new Request('https://verifier.test/verify', {method:'POST', headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
const fixture = (options = {}) => createVerifier({secret:'fixture-only',environment:'LAT',limit:async()=>true,fetcher:async()=>Response.json(receipt),...options});

test('Amazon fields bind exact SKU, type, receipt and environment', () => {
  assert.equal(receiptDecision(receipt,'test-receipt','LAT'),'verified');
  for (const field of ['receiptId','productId','productType','purchaseDate','cancelDate','betaProduct','testTransaction']) {
    const missing={...receipt};delete missing[field];
    assert.equal(receiptDecision(missing,'test-receipt','LAT'),'invalid',field);
  }
  for (const changed of [{productId:'other'},{productType:'CONSUMABLE'},{receiptId:'other'},{purchaseDate:0},{cancelDate:'0'},{betaProduct:false}]) {
    assert.equal(receiptDecision({...receipt,...changed},'test-receipt','LAT'),'invalid');
  }
  assert.equal(receiptDecision({...receipt,cancelDate:0},'test-receipt','LAT'),'revoked');
  assert.equal(receiptDecision(receipt,'test-receipt','PRODUCTION'),'invalid');
  assert.equal(receiptDecision({...receipt,betaProduct:false,testTransaction:false},'test-receipt','PRODUCTION'),'verified');
  assert.equal(receiptDecision({...receipt,betaProduct:false,testTransaction:true},'test-receipt','PRODUCTION'),'invalid');
});

test('successful HTTP flow returns minimal non-cacheable result', async()=>{
  let called=false;
  const handler=fixture({fetcher:async(url,options)=>{
    called=true;assert.equal(new URL(url).host,'appstore-sdk.amazon.com');
    assert.equal(options.redirect,'manual');return Response.json(receipt);
  }});
  const response=await handler(request());
  assert.equal(response.status,200);assert.equal(called,true);
  assert.deepEqual(await response.json(),{state:'verified'});
  assert.equal(response.headers.get('cache-control'),'no-store');
});

test('bad input, query strings, missing configuration and rate limits cannot call RVS',async()=>{
  let calls=0;const fetcher=async()=>{calls++;return Response.json(receipt);};
  for (const body of [{userId:'x'},{userId:'x',receiptId:'r',sku:'other'},{userId:'x\n',receiptId:'r'},{userId:'x',receiptId:'r'.repeat(9000)}]) {
    assert.equal((await fixture({fetcher})(request(body))).status,400);
  }
  assert.equal((await fixture({secret:'',fetcher})(request())).status,503);
  assert.equal((await fixture({limit:null,fetcher})(request())).status,503);
  assert.equal((await fixture({limit:async()=>false,fetcher})(request())).status,429);
  const query=new Request('https://verifier.test/verify?receiptId=private',{method:'POST',headers:{'Content-Type':'application/json'},body:'{}'});
  assert.equal((await fixture({fetcher})(query)).status,400);
  assert.equal((await fixture({fetcher})(new Request('https://verifier.test/verify'))).status,405);
  assert.equal(calls,0);
});

test('only definitive Amazon answers revoke; outages never fabricate success',async()=>{
  for (const [status,state,code] of [[410,'revoked',200],[400,'invalid',200],[497,'invalid',200],[496,'unavailable',503],[429,'unavailable',503],[500,'unavailable',503]]) {
    const response=await fixture({fetcher:async()=>new Response('',{status})})(request());
    assert.equal(response.status,code);assert.equal((await response.json()).state,state);
  }
  for (const fetcher of [async()=>{throw Error('private upstream URL must not escape');},async()=>new Response('not JSON'),async()=>new Response('x'.repeat(40000))]) {
    assert.deepEqual(await (await fixture({fetcher})(request())).json(),{state:'unavailable'});
  }
});

test('bearer identifiers are URL encoded, never interpreted as an upstream host',async()=>{
  const handler=fixture({fetcher:async url=>{
    assert.equal(new URL(url).host,'appstore-sdk.amazon.com');
    assert.ok(url.includes('a%2Fb%3F%23'));return new Response('',{status:497});
  }});
  assert.equal((await (await handler(request({userId:'a/b?#',receiptId:'r'}))).json()).state,'invalid');
});

test('Workers-compatible manual redirects never forward the shared secret',async()=>{
  let calls=0;
  const handler=fixture({fetcher:async(url,options)=>{
    calls++;
    if (!['manual','follow'].includes(options.redirect)) throw TypeError('Invalid redirect value');
    assert.equal(options.redirect,'manual');
    return new Response(null,{status:302,headers:{Location:'https://untrusted.test/secret'}});
  }});
  assert.deepEqual(await (await handler(request())).json(),{state:'unavailable'});
  assert.equal(calls,1);
});
