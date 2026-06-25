package com.example.info.battery

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.batteryDataStore: DataStore<Preferences> by preferencesDataStore(name = "battery_settings")

object BatteryKeys {
    val DUAL_BATTERY = booleanPreferencesKey("dual_battery")
    val MULTIPLY = booleanPreferencesKey("multiply")
    val MULTIPLIER_MAGNITUDE = intPreferencesKey("multiplier_magnitude")
    val ESTIMATED_FCC = intPreferencesKey("estimated_fcc")
    val SHOW_OPLUS_FIELDS = booleanPreferencesKey("show_oplus_fields")
    val CUSTOM_ENTRIES = stringPreferencesKey("custom_entries")
    val IS_CELSIUS = booleanPreferencesKey("is_celsius")
}
