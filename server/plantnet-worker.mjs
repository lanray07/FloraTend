import {DurableObject} from 'cloudflare:workers';
import {createIdentifier,configuredDailyCap} from './plant-identification.mjs';

// A single shared actor bounds every upstream image attempt, not each Worker location.
// Stores daily keyed IP hashes/counters and short leases, never images or raw IPs.
export class IdentificationBudget extends DurableObject {
  constructor(ctx,env) {
    super(ctx,env);this.ctx=ctx;this.cap=configuredDailyCap(env.IDENTIFICATION_DAILY_CAP);
    ctx.storage.sql.exec('CREATE TABLE IF NOT EXISTS counters (day INTEGER, key TEXT, hits INTEGER NOT NULL, PRIMARY KEY(day,key))');
    ctx.storage.sql.exec('CREATE TABLE IF NOT EXISTS leases (ticket TEXT PRIMARY KEY, expires INTEGER NOT NULL)');
  }
  async reserve(owner) {
    if(!/^[a-f0-9]{64}$/.test(owner))return {allowed:false,reason:'limit'};
    const now=Date.now(),day=Math.floor(now/86400000);
    const result=this.ctx.storage.transactionSync(()=>{
      const sql=this.ctx.storage.sql;
      sql.exec('DELETE FROM counters WHERE day < ?',day);
      sql.exec('DELETE FROM leases WHERE expires <= ?',now);
      const count=key=>[...sql.exec('SELECT hits FROM counters WHERE day=? AND key=?',day,key)][0]?.hits??0;
      if(count('global')>=this.cap || count(owner)>=5)return {allowed:false,reason:'limit'};
      if([...sql.exec('SELECT count(*) AS n FROM leases')][0].n>=5)return {allowed:false,reason:'busy'};
      for(const key of ['global',owner])sql.exec('INSERT INTO counters VALUES (?,?,1) ON CONFLICT(day,key) DO UPDATE SET hits=hits+1',day,key);
      const ticket=crypto.randomUUID();sql.exec('INSERT INTO leases VALUES (?,?)',ticket,now+45000);
      return {allowed:true,ticket};
    });
    const alarm=await this.ctx.storage.getAlarm(),next=Math.min(now+60000,(day+1)*86400000+1000);
    if(alarm===null || alarm>next)await this.ctx.storage.setAlarm(next);
    return result;
  }
  release(ticket) {
    if(typeof ticket==='string' && /^[a-f0-9-]{36}$/.test(ticket))this.ctx.storage.sql.exec('DELETE FROM leases WHERE ticket=?',ticket);
  }
  async alarm() {
    const now=Date.now(),day=Math.floor(now/86400000),sql=this.ctx.storage.sql;
    sql.exec('DELETE FROM counters WHERE day < ?',day);sql.exec('DELETE FROM leases WHERE expires <= ?',now);
    const leases=[...sql.exec('SELECT count(*) AS n FROM leases')][0].n;
    const counters=[...sql.exec('SELECT count(*) AS n FROM counters')][0].n;
    if(leases || counters)await this.ctx.storage.setAlarm(leases?Math.min(now+60000,(day+1)*86400000+1000):(day+1)*86400000+1000);
  }
}

export default {
  async fetch(request,env,ctx) {
    const actor=env.IDENTIFICATION_BUDGET?.get(env.IDENTIFICATION_BUDGET.idFromName('shared-identification-global-v1'));
    return createIdentifier({apiKey:env.PLANTNET_API_KEY,rateSalt:env.PLANTNET_RATE_SALT,
      reserve:actor ? owner=>actor.reserve(owner) : undefined,
      release:actor ? ticket=>{ctx.waitUntil(actor.release(ticket));} : undefined})(request);
  },
};
