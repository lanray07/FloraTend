// No API keys, IPs, images, provider URLs or raw response/error bodies are logged.
export const PHOTO_CONSENT = 'plantnet-photo-v1';
export const MAX_IMAGE_BYTES = 2 * 1024 * 1024;
const UPSTREAM = 'https://my-api.plantnet.org';
const reply = (status, state, extra = {}, retryAfter) => Response.json({state, ...extra}, {
  status, headers: {'Cache-Control':'no-store','X-Content-Type-Options':'nosniff',
    ...(retryAfter ? {'Retry-After':String(retryAfter)} : {})},
});

export function configuredDailyCap(value) {
  if (value === undefined) return 480;
  const number = Number(value);
  return Number.isSafeInteger(number) && number >= 0 ? Math.min(480,number) : 0;
}

async function abortable(promise, signal) {
  if (signal.aborted) throw Error('Deadline');
  return new Promise((resolve,reject) => {
    const abort = () => reject(Error('Deadline'));
    signal.addEventListener('abort',abort,{once:true});
    Promise.resolve(promise).then(value => {signal.removeEventListener('abort',abort);resolve(value);},
      error => {signal.removeEventListener('abort',abort);reject(error);});
  });
}

async function bytesLimited(entity, maximum, signal) {
  const reader=entity.body?.getReader();
  if (!reader) throw Error('Missing body');
  const parts=[];let length=0;
  const abort=()=>{reader.cancel().catch(()=>{});};
  signal.addEventListener('abort',abort,{once:true});
  try {
    for (;;) {
      const {done,value}=await abortable(reader.read(),signal);
      if(done)break;
      length+=value.byteLength;
      if(length>maximum)throw Error('Body limit');
      parts.push(value);
    }
    if(signal.aborted)throw Error('Deadline');
    const result=new Uint8Array(length);let offset=0;
    for(const part of parts){result.set(part,offset);offset+=part.length;}
    return result;
  } finally {signal.removeEventListener('abort',abort);await reader.cancel().catch(()=>{});}
}

async function jsonLimited(response,signal,maximum=65536) {
  const bytes=await bytesLimited(response,maximum,signal);
  return JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(bytes));
}

function safeName(value, apiKey) {
  return typeof value==='string' && value.length>=1 && value.length<=180 && value===value.trim() &&
    !/[\u0000-\u001f\u007f\u202a-\u202e\u2066-\u2069]/.test(value) &&
    !/https?:\/\//i.test(value) && !value.includes(apiKey);
}

export function safeCandidates(data,apiKey,includeCommonNames=true) {
  if(!data || !Array.isArray(data.results) || data.results.length>100)throw Error('Invalid results');
  const candidates=[];
  for(const row of data.results) {
    const scientificName=row?.species?.scientificNameWithoutAuthor || row?.species?.scientificName;
    if(!safeName(scientificName,apiKey) || typeof row.score!=='number' || !Number.isFinite(row.score) || row.score<0 || row.score>1)throw Error('Invalid candidate');
    const names=row.species.commonNames;
    if(names!==undefined && !Array.isArray(names))throw Error('Invalid common names');
    const commonNames=includeCommonNames ? (names??[]).filter(name=>safeName(name,apiKey)).slice(0,3) : [];
    candidates.push({scientificName,commonNames,score:row.score});
  }
  const seen=new Set();
  return candidates.sort((a,b)=>b.score-a.score).filter(row=>{
    const key=row.scientificName.toLocaleLowerCase('en');
    if(seen.has(key))return false;seen.add(key);return true;
  }).slice(0,3);
}

/** One JPEG chosen by the user. No app-embedded secret is treated as authentication. */
export function createIdentifier({apiKey,rateSalt,reserve,release,fetcher=fetch,now=Date.now,timeoutMs=18000}) {
  return async function identify(request) {
    const url=new URL(request.url);
    if(url.pathname!=='/identify')return reply(404,'unavailable');
    if(request.method!=='POST')return reply(405,'unavailable');
    if(request.headers.has('origin') || request.headers.get('X-FloraTend-Photo-Consent')!==PHOTO_CONSENT)return reply(403,'consent_required');
    if([...url.searchParams.keys()].some(key=>key!=='lang') || url.searchParams.getAll('lang').length>1)return reply(400,'invalid_image');
    const language=url.searchParams.get('lang')??'en';
    if(!/^[a-z]{2}$/.test(language))return reply(400,'invalid_image');
    if(request.headers.get('content-type')?.split(';')[0].trim()!=='image/jpeg')return reply(415,'invalid_image');
    const declared=request.headers.get('content-length');
    if(declared!==null && (!/^\d+$/.test(declared) || Number(declared)>MAX_IMAGE_BYTES))return reply(413,'too_large');
    if(typeof apiKey!=='string' || apiKey.length<8 || apiKey.length>512 || /\s/.test(apiKey) || typeof rateSalt!=='string' || rateSalt.length<32 || typeof reserve!=='function' || typeof release!=='function')return reply(503,'unavailable');
    const ip=request.headers.get('cf-connecting-ip');
    if(!ip || !/^[0-9a-fA-F:.]{2,64}$/.test(ip))return reply(503,'unavailable');
    const controller=new AbortController();
    const timer=setTimeout(()=>controller.abort(),Math.min(20000,Math.max(1,timeoutMs)));
    let ticket;
    const retryAfter=()=>Math.max(1,Math.ceil((86400000-now()%86400000)/1000));
    try {
      let image;
      try {image=await bytesLimited(request,MAX_IMAGE_BYTES,controller.signal);} catch {return reply(controller.signal.aborted?503:413,controller.signal.aborted?'unavailable':'too_large');}
      if(image.length<4 || image[0]!==255 || image[1]!==216 || image[image.length-2]!==255 || image[image.length-1]!==217)return reply(400,'invalid_image');
      const key=await crypto.subtle.importKey('raw',new TextEncoder().encode(rateSalt),{name:'HMAC',hash:'SHA-256'},false,['sign']);
      const digest=new Uint8Array(await crypto.subtle.sign('HMAC',key,new TextEncoder().encode(Math.floor(now()/86400000)+'|'+ip)));
      const owner=Array.from(digest,x=>x.toString(16).padStart(2,'0')).join('');
      const permit=await abortable(reserve(owner),controller.signal);
      if(!permit?.allowed)return permit?.reason==='busy'?reply(503,'busy',{},30):reply(429,'daily_limit',{},retryAfter());
      ticket=permit.ticket;
      if(typeof ticket!=='string' || !/^[a-f0-9-]{36}$/.test(ticket))return reply(503,'unavailable');
      const upstream=(path)=>{const target=new URL(path,UPSTREAM);target.searchParams.set('api-key',apiKey);return target;};
      const get=(path)=>abortable(fetcher(upstream(path),{method:'GET',redirect:'manual',headers:{Accept:'application/json'},signal:controller.signal}),controller.signal);
      const quotaResponse=await get('/v2/quota/daily');
      if(quotaResponse.status!==200){await quotaResponse.body?.cancel().catch(()=>{});return quotaResponse.status===429?reply(429,'daily_limit',{},retryAfter()):reply(503,'unavailable');}
      const quota=(await jsonLimited(quotaResponse,controller.signal,32768))?.quota?.identify;
      if(!quota || !Number.isSafeInteger(quota.total) || quota.total<1 || quota.total>500 || !Number.isSafeInteger(quota.remaining) || quota.remaining<0 || quota.remaining>quota.total)return reply(503,'unavailable');
      if(quota.remaining===0)return reply(429,'daily_limit',{},retryAfter());
      const languagesResponse=await get('/v2/languages');
      if(languagesResponse.status!==200){await languagesResponse.body?.cancel().catch(()=>{});return reply(503,'unavailable');}
      const languages=await jsonLimited(languagesResponse,controller.signal,32768);
      if(!Array.isArray(languages) || languages.length>300 || !languages.includes('en') || !languages.every(code=>typeof code==='string' && /^[a-z]{2}(?:-[a-z]{2})?$/.test(code)))return reply(503,'unavailable');
      const selectedLanguage=languages.includes(language)?language:'en';
      const target=upstream('/v2/identify/all');
      target.searchParams.set('lang',selectedLanguage);target.searchParams.set('nb-results','3');target.searchParams.set('include-related-images','false');target.searchParams.set('no-reject','false');
      const form=new FormData();form.append('images',new Blob([image],{type:'image/jpeg'}),'plant.jpg');form.append('organs','auto');
      const result=await abortable(fetcher(target,{method:'POST',body:form,redirect:'manual',headers:{Accept:'application/json'},signal:controller.signal}),controller.signal);
      if(result.status!==200){await result.body?.cancel().catch(()=>{});
        // Pl@ntNet documents identify-route 404 as Species not found / non-plant rejection.
        if(result.status===404)return reply(200,'no_match',{provider:'plantnet',language:selectedLanguage,results:[]});
        return result.status===429?reply(429,'daily_limit',{},retryAfter()):reply(503,'unavailable');}
      const data=await jsonLimited(result,controller.signal);
      const candidates=safeCandidates(data,apiKey,selectedLanguage===language);
      return reply(200,candidates.length?'identified':'no_match',{provider:'plantnet',language:selectedLanguage,results:candidates});
    } catch {return reply(503,'unavailable');}
    finally {clearTimeout(timer);if(ticket) {try {await release(ticket);} catch { /* A lease expires independently. */ }}}
  };
}
