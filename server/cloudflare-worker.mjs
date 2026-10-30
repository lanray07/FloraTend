import {DurableObject} from 'cloudflare:workers';
import {createVerifier} from './receipt-verifier.mjs';

// One named actor provides a global, atomic limit across Workers locations.
// It stores short-lived keyed hashes, never raw IDs, receipts or IP addresses.
export class ReceiptRateLimit extends DurableObject {
  constructor(ctx, env) {
    super(ctx, env);
    this.ctx = ctx;
    ctx.storage.sql.exec('CREATE TABLE IF NOT EXISTS buckets (minute INTEGER, key TEXT, hits INTEGER NOT NULL, PRIMARY KEY (minute, key))');
  }

  async check(ownerHash) {
    if (!/^[a-f0-9]{64}$/.test(ownerHash)) return false;
    const minute = Math.floor(Date.now()/60000);
    const allowed = this.ctx.storage.transactionSync(()=>{
      const sql = this.ctx.storage.sql;
      sql.exec('DELETE FROM buckets WHERE minute < ?', minute-2);
      const count = key => [...sql.exec('SELECT hits FROM buckets WHERE minute = ? AND key = ?',minute,key)][0]?.hits ?? 0;
      if (count('global') >= 240 || count(ownerHash) >= 10) return false;
      for (const key of ['global',ownerHash]) {
        sql.exec('INSERT INTO buckets VALUES (?, ?, 1) ON CONFLICT(minute,key) DO UPDATE SET hits = hits + 1',minute,key);
      }
      return true;
    });
    if (await this.ctx.storage.getAlarm() === null) await this.ctx.storage.setAlarm(Date.now()+180000);
    return allowed;
  }

  async alarm() {
    this.ctx.storage.sql.exec('DELETE FROM buckets WHERE minute < ?',Math.floor(Date.now()/60000)-2);
    // Ensure counters disappear even when traffic stops just before the first alarm.
    const count = [...this.ctx.storage.sql.exec('SELECT count(*) AS n FROM buckets')][0].n;
    if (count > 0) await this.ctx.storage.setAlarm(Date.now()+180000);
  }
}

export default {
  async fetch(request, env) {
    if (new URL(request.url).pathname !== '/verify') return new Response(null,{status:404});
    const limit = async owner => {
      if (!env.RECEIPT_LIMIT || !env.FLORATEND_RATE_SALT || env.FLORATEND_RATE_SALT.length < 32) throw Error('Limiter unavailable');
      const key = await crypto.subtle.importKey('raw',new TextEncoder().encode(env.FLORATEND_RATE_SALT),{name:'HMAC',hash:'SHA-256'},false,['sign']);
      const digest = new Uint8Array(await crypto.subtle.sign('HMAC',key,new TextEncoder().encode(owner)));
      const hash = Array.from(digest,x=>x.toString(16).padStart(2,'0')).join('');
      const actor = env.RECEIPT_LIMIT.get(env.RECEIPT_LIMIT.idFromName('receipt-verification-global-v1'));
      return await actor.check(hash);
    };
    return createVerifier({secret:env.AMAZON_RVS_SHARED_SECRET,environment:env.FLORATEND_RVS_ENVIRONMENT,limit})(request);
  },
};
