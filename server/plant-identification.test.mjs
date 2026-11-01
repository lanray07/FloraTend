import test from 'node:test';
import assert from 'node:assert/strict';
import {createIdentifier,safeCandidates,configuredDailyCap,PHOTO_CONSENT,MAX_IMAGE_BYTES} from './plant-identification.mjs';

// Synthetic transport contracts only: these are not live species-identification evidence.
const apiKey='synthetic-provider-key';
const jpeg=new Uint8Array([255,216,1,2,255,217]);
const ticket='00000000-0000-4000-8000-000000000000';
const candidate=(name='Ajuga genevensis',score=.9)=>({score,species:{scientificNameWithoutAuthor:name,commonNames:['Blue bugle']}});
const request=(options={})=>new Request('https://identifier.test/identify?lang=en',{method:'POST',headers:{'content-type':'image/jpeg','cf-connecting-ip':'192.0.2.1','X-FloraTend-Photo-Consent':PHOTO_CONSENT,...options.headers},body:jpeg,...options});
function fixture(options={}) {
  const calls=[],owners=[],released=[];
  const handler=createIdentifier({apiKey,rateSalt:'synthetic-only-salt-01234567890123456789',
    reserve:async owner=>{owners.push(owner);return {allowed:true,ticket};},release:async value=>{released.push(value);},
    fetcher:async(url,init)=>{
      calls.push({url:new URL(url),init});
      if(new URL(url).pathname==='/v2/quota/daily')return Response.json({quota:{identify:{total:500,remaining:499}}});
      if(new URL(url).pathname==='/v2/languages')return Response.json(['en','fr']);
      return Response.json({results:[candidate()],privateAccount:'must-not-escape',query:{images:['private-id']}});
    },...options});
  return {handler,calls,owners,released};
}
test('successful consented upload uses fixed origin, stripped filename, safe output and release',async()=>{
  const f=fixture();const response=await f.handler(request());
  assert.equal(response.status,200);assert.equal(response.headers.get('cache-control'),'no-store');
  assert.deepEqual(await response.json(),{state:'identified',provider:'plantnet',language:'en',results:[{scientificName:'Ajuga genevensis',commonNames:['Blue bugle'],score:.9}]});
  assert.equal(f.calls.length,3);assert.deepEqual(f.released,[ticket]);
  assert.match(f.owners[0],/^[a-f0-9]{64}$/);assert.ok(!f.owners[0].includes('192.0.2.1'));
  for(const {url,init} of f.calls){assert.equal(url.origin,'https://my-api.plantnet.org');assert.equal(url.searchParams.get('api-key'),apiKey);assert.equal(init.redirect,'manual');}
  const {url,init}=f.calls[2];assert.equal(url.pathname,'/v2/identify/all');assert.equal(url.searchParams.get('include-related-images'),'false');assert.equal(url.searchParams.get('no-reject'),'false');assert.equal(url.searchParams.get('nb-results'),'3');
  assert.equal(init.body.get('images').name,'plant.jpg');assert.equal(init.body.get('organs'),'auto');assert.deepEqual(new Uint8Array(await init.body.get('images').arrayBuffer()),jpeg);
});
for(const [label,options,status] of [
  ['missing consent',{headers:{'content-type':'image/jpeg','cf-connecting-ip':'192.0.2.1'}},403],
  ['browser origin',{headers:{origin:'https://site.test','content-type':'image/jpeg','cf-connecting-ip':'192.0.2.1','X-FloraTend-Photo-Consent':PHOTO_CONSENT}},403],
  ['wrong content type',{headers:{'content-type':'image/png','cf-connecting-ip':'192.0.2.1','X-FloraTend-Photo-Consent':PHOTO_CONSENT}},415],
  ['bad JPEG',{body:'not a photo'},400],
  ['too large',{body:new Uint8Array(MAX_IMAGE_BYTES+1)},413],
  ['missing edge IP',{headers:{'content-type':'image/jpeg','X-FloraTend-Photo-Consent':PHOTO_CONSENT}},503],
])test(label+' never calls provider or budget',async()=>{const f=fixture();assert.equal((await f.handler(request(options))).status,status);assert.equal(f.calls.length,0);assert.equal(f.owners.length,0);});
test('query destinations and invalid locales cannot influence upstream',async()=>{
  for(const query of ['lang=en&url=https://other.test','lang=en&lang=fr','lang=../en']){
    const f=fixture();const original=request();const req=new Request('https://identifier.test/identify?'+query,original);
    assert.equal((await f.handler(req)).status,400);assert.equal(f.calls.length,0);
  }
});
test('absent credentials/budget fail closed',async()=>{
  for(const options of [{apiKey:''},{apiKey:'x'.repeat(513)},{rateSalt:''},{reserve:undefined},{release:undefined}]){
    const f=fixture(options);assert.equal((await f.handler(request())).status,503);assert.equal(f.calls.length,0);
  }
});
test('exhaustion and concurrency return bounded errors before fetch',async()=>{
  for(const [reason,status] of [['limit',429],['busy',503]]){const f=fixture({reserve:async()=>({allowed:false,reason})});const response=await f.handler(request());assert.equal(response.status,status);assert.ok(Number(response.headers.get('retry-after'))>0);assert.equal(f.calls.length,0);}
});
test('missing reservation ticket fails closed',async()=>{const f=fixture({reserve:async()=>({allowed:true})});assert.equal((await f.handler(request())).status,503);assert.equal(f.calls.length,0);});
test('paid, malformed or exhausted quota cannot submit a photo',async()=>{
  for(const [quota,status] of [[{total:501,remaining:500},503],[{total:500,remaining:0},429],[{total:500,remaining:501},503],[{total:'500',remaining:2},503]]){
    let count=0;const f=fixture({fetcher:async()=>{count++;return Response.json({quota:{identify:quota}});}});assert.equal((await f.handler(request())).status,status);assert.equal(count,1);assert.deepEqual(f.released,[ticket]);
  }
});
test('redirects and provider errors never expose provider bodies or credentials',async()=>{
  for(const status of [302,401,403,404,429,500]){const f=fixture({fetcher:async()=>new Response(apiKey,{status})});const response=await f.handler(request());assert.equal(response.status,status===429?429:503);assert.ok(!(await response.text()).includes(apiKey));assert.deepEqual(f.released,[ticket]);}
});
test('unsupported common-name locale omits English names',async()=>{
  const f=fixture();const req=new Request('https://identifier.test/identify?lang=ja',request());const body=await (await f.handler(req)).json();assert.equal(body.language,'en');assert.deepEqual(body.results[0].commonNames,[]);
});
test('observed provider language tags do not disable all identification requests',async()=>{
  let count=0;const f=fixture({fetcher:async()=>++count===1?Response.json({quota:{identify:{total:500,remaining:500}}}):count===2?Response.json(['en','sat','zh-tw','pt-br','zh-hant','de-at','en-au']):Response.json({results:[candidate()]})});
  assert.equal((await f.handler(request())).status,200);
});
test('identify-route rejection is no-match, unlike quota-route 404',async()=>{
  let count=0;const f=fixture({fetcher:async()=>++count===1?Response.json({quota:{identify:{total:500,remaining:5}}}):count===2?Response.json(['en']):new Response('Species not found',{status:404})});
  const response=await f.handler(request());assert.equal(response.status,200);assert.deepEqual(await response.json(),{state:'no_match',provider:'plantnet',language:'en',results:[]});
});
test('timeout bounds even an uncooperative transport',async()=>{
  const f=fixture({timeoutMs:10,fetcher:()=>new Promise(()=>{})});const start=Date.now();assert.equal((await f.handler(request())).status,503);assert.ok(Date.now()-start<1000);assert.deepEqual(f.released,[ticket]);
});
test('bounded result stream and malformed JSON fail closed',async()=>{
  for(const raw of ['{',' '.repeat(65537)]){let count=0;const f=fixture({fetcher:async()=>++count===1?Response.json({quota:{identify:{total:500,remaining:5}}}):count===2?Response.json(['en']):new Response(raw)});assert.equal((await f.handler(request())).status,503);assert.deepEqual(f.released,[ticket]);}
});
test('release failures cannot override a safe response',async()=>{const f=fixture({release:()=>{throw Error(apiKey);}});assert.equal((await f.handler(request())).status,200);});
test('candidate bounds, sorting, deduplication and no-match',()=>{
  assert.deepEqual(safeCandidates({results:[]},apiKey),[]);
  assert.deepEqual(safeCandidates({results:[candidate('A',.1),candidate('A',.8),candidate('B',.7),candidate('C',.6),candidate('D',.5)]},apiKey).map(x=>x.scientificName),['A','B','C']);
  for(const row of [candidate(apiKey),candidate('https://bad.test'),candidate('bad\u202ename'),candidate('A',NaN),candidate('A',2),candidate('A','0.5')])assert.throws(()=>safeCandidates({results:[row]},apiKey));
});
test('configured cap cannot exceed free safety budget',()=>{assert.equal(configuredDailyCap(),480);assert.equal(configuredDailyCap('1000'),480);assert.equal(configuredDailyCap('0'),0);assert.equal(configuredDailyCap('bad'),0);assert.equal(configuredDailyCap('-1'),0);});
