package com.example.info.battery

import android.app.Application
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.info.Strings
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BatteryInfoRepository(application)
    private val dataStore = application.batteryDataStore
    
    private val _batteryInfos = MutableStateFlow<List<BatteryInfo>>(emptyList())
    val batteryInfos: StateFlow<List<BatteryInfo>> = _batteryInfos.asStateFlow()
    
    private val _isRoot = MutableStateFlow(false)
    val isRoot: StateFlow<Boolean> = _isRoot.asStateFlow()

    private val _powerDataPoints = MutableStateFlow<List<PowerDataPoint>>(emptyList())
    val powerDataPoints: StateFlow<List<PowerDataPoint>> = _powerDataPoints.asStateFlow()

    val isCelsius = dataStore.data.map { it[BatteryKeys.IS_CELSIUS] ?: true }
    val isDualBatt = dataStore.data.map { it[BatteryKeys.DUAL_BATTERY] ?: false }
    val showOplus = dataStore.data.map { it[BatteryKeys.SHOW_OPLUS_FIELDS] ?: true }
    val customEntries = dataStore.data.map {
        val json = it[BatteryKeys.CUSTOM_ENTRIES] ?: "[]"
        try { Json.decodeFromString<List<CustomEntry>>(json) } catch (e: Exception) { emptyList() }
    }

    private var chartStartTime = System.currentTimeMillis()

    init {
        checkRootAndStartRefreshing()
    }

    private fun checkRootAndStartRefreshing() {
        viewModelScope.launch {
            _isRoot.value = Shell.getShell().isRoot
            while (true) {
                val infos = repository.getAvailableBatteryInfo(_isRoot.value)
                _batteryInfos.value = infos
                collectPowerData(infos)
                delay(2000)
            }
        }
    }

    private fun collectPowerData(infos: List<BatteryInfo>) {
        val powerInfo = infos.find { it.type == BatteryInfoType.POWER }
        val tempInfo = infos.find { it.type == BatteryInfoType.TEMP }
        
        powerInfo?.let { p ->
            val powerValue = p.value.substringBefore(" ").toFloatOrNull() ?: 0f
            val tempValue = tempInfo?.value?.substringBefore("°")?.toFloatOrNull() ?: 0f
            val currentPoints = _powerDataPoints.value.toMutableList()
            currentPoints.add(PowerDataPoint(
                timestamp = System.currentTimeMillis() - chartStartTime,
                power = kotlin.math.abs(powerValue),
                temperature = tempValue
            ))
            if (currentPoints.size > 1000) currentPoints.removeAt(0)
            _powerDataPoints.value = currentPoints
        }
    }

    fun setIsCelsius(value: Boolean) = viewModelScope.launch {
        dataStore.edit { it[BatteryKeys.IS_CELSIUS] = value }
    }

    fun setDualBatt(value: Boolean) = viewModelScope.launch {
        dataStore.edit { it[BatteryKeys.DUAL_BATTERY] = value }
    }

    fun setShowOplus(value: Boolean) = viewModelScope.launch {
        dataStore.edit { it[BatteryKeys.SHOW_OPLUS_FIELDS] = value }
    }

    fun addCustomEntry(entry: CustomEntry) = viewModelScope.launch {
        val current = customEntries.first().toMutableList()
        current.removeAll { it.path == entry.path }
        current.add(entry)
        dataStore.edit { it[BatteryKeys.CUSTOM_ENTRIES] = Json.encodeToString(current) }
    }

    fun removeCustomEntry(path: String) = viewModelScope.launch {
        val current = customEntries.first().filterNot { it.path == path }
        dataStore.edit { it[BatteryKeys.CUSTOM_ENTRIES] = Json.encodeToString(current) }
    }
}
