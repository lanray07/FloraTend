package com.floratend.app

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("preferences")
class FloraSettings(private val context: Context) {
    val onboarded = context.store.data.map { it[booleanPreferencesKey("onboarded")] ?: false }
    val dark = context.store.data.map { it[booleanPreferencesKey("dark")] ?: false }
    val imperial = context.store.data.map { it[booleanPreferencesKey("imperial")] ?: false }
    val reminders = context.store.data.map { it[booleanPreferencesKey("reminders")] ?: false }
    internal val premiumProjection = context.store.data.map { it[booleanPreferencesKey("premium")] ?: false }
    // A stale projection alone can never unlock a release build from sandbox state.
    val premium = context.store.data.map {
        val cache = context.getSharedPreferences("amazon_entitlement", Context.MODE_PRIVATE)
        (it[booleanPreferencesKey("premium")] ?: false) && BillingPolicy.cachedGrant(BuildConfig.DEBUG,
            cache.getString("mode", null), cache.getString("owner", null), cache.getString("receipt", null), cache.getBoolean("active", false))
    }
    suspend fun set(key: String, value: Boolean) { context.store.edit { it[booleanPreferencesKey(key)] = value } }
}
