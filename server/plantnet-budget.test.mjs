import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {Miniflare,convertV4MiniflareOptions} from 'miniflare';

// Real local workerd + SQLite actor, with a test-only RPC bridge. Never deployed.
test('SQLite actor atomically enforces per-owner/global/concurrency budgets',async()=>{
  const source=await readFile(new URL('./plantnet-worker.mjs',import.meta.url),'utf8');
  const library=await readFile(new URL('./plant-identification.mjs',import.meta.url),'utf8');
  const bridge=`export class TestBudget extends IdentificationBudget {
    async expireFixture() {
      this.ctx.storage.sql.exec('UPDATE leases SET expires=0');
      this.ctx.storage.sql.exec('INSERT INTO counters VALUES(0,?,1)','expired-fixture');
      await this.alarm();
      return {leases:[...this.ctx.storage.sql.exec('SELECT count(*) AS n FROM leases')][0].n,
        oldDays:[...this.ctx.storage.sql.exec('SELECT count(*) AS n FROM counters WHERE day < ?',Math.floor(Date.now()/86400000))][0].n};
    }
  }
  export default {async fetch(request,env) {
    const body=await request.json();const actor=env.IDENTIFICATION_BUDGET.get(env.IDENTIFICATION_BUDGET.idFromName(body.scenario));
    if(body.expire)return Response.json(await actor.expireFixture());
    return Response.json(body.release ? (await actor.release(body.release),{released:true}) : await actor.reserve(body.owner));
  }};`;
  const mf=new Miniflare(convertV4MiniflareOptions({workers:[{name:'budget-test',modules:[
    {type:'ESModule',path:'worker.mjs',contents:source.split('export default {')[0]+bridge},
    {type:'ESModule',path:'plant-identification.mjs',contents:library},
  ],compatibilityDate:'2026-10-30',bindings:{IDENTIFICATION_DAILY_CAP:'7'},
  durableObjects:{IDENTIFICATION_BUDGET:{className:'TestBudget',useSQLite:true}}}]}));
  const invoke=async body=>(await mf.dispatchFetch('https://budget.test/',{method:'POST',body:JSON.stringify(body)})).json();
  const owner=n=>n.toString(16).padStart(64,'0');
  try {
    for(let i=0;i<5;i++){const result=await invoke({scenario:'owner',owner:owner(1)});assert.equal(result.allowed,true);await invoke({scenario:'owner',release:result.ticket});}
    assert.deepEqual(await invoke({scenario:'owner',owner:owner(1)}),{allowed:false,reason:'limit'});
    const concurrent=await Promise.all(Array.from({length:15},(_,i)=>invoke({scenario:'concurrency',owner:owner(i+1)})));
    assert.equal(concurrent.filter(x=>x.allowed).length,5);
    assert.equal(concurrent.filter(x=>x.reason==='busy').length,10);
    for(const row of concurrent.filter(x=>x.allowed))await invoke({scenario:'concurrency',release:row.ticket});
    for(let i=0;i<2;i++){const row=await invoke({scenario:'concurrency',owner:owner(100+i)});assert.equal(row.allowed,true);await invoke({scenario:'concurrency',release:row.ticket});}
    assert.deepEqual(await invoke({scenario:'concurrency',owner:owner(200)}),{allowed:false,reason:'limit'});
    assert.equal((await invoke({scenario:'bad',owner:'raw-ip-not-permitted'})).allowed,false);
    for(let i=0;i<5;i++)assert.equal((await invoke({scenario:'expiry',owner:owner(i+1)})).allowed,true);
    assert.deepEqual(await invoke({scenario:'expiry',expire:true}),{leases:0,oldDays:0});
    assert.equal((await invoke({scenario:'expiry',owner:owner(100)})).allowed,true);
  } finally {await mf.dispose();}
});
