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
    // Amazon callback is the only writer of this entitlement. No developer toggle.
    val premium = context.store.data.map { it[booleanPreferencesKey("premium")] ?: false }
    suspend fun set(key: String, value: Boolean) { context.store.edit { it[booleanPreferencesKey(key)] = value } }
}
