package com.example.info.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.info.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _wifiEnabled = MutableStateFlow(false)
    val wifiEnabled: StateFlow<Boolean> = _wifiEnabled.asStateFlow()

    private val _btEnabled = MutableStateFlow(false)
    val btEnabled: StateFlow<Boolean> = _btEnabled.asStateFlow()

    private val _nfcEnabled = MutableStateFlow(false)
    val nfcEnabled: StateFlow<Boolean> = _nfcEnabled.asStateFlow()

    private val _gpsEnabled = MutableStateFlow(false)
    val gpsEnabled: StateFlow<Boolean> = _gpsEnabled.asStateFlow()

    // Tweaks
    private val _battPercent = MutableStateFlow(false)
    val battPercent: StateFlow<Boolean> = _battPercent.asStateFlow()

    private val _networkTraffic = MutableStateFlow(false)
    val networkTraffic: StateFlow<Boolean> = _networkTraffic.asStateFlow()

    private val _doubleTapSleep = MutableStateFlow(false)
    val doubleTapSleep: StateFlow<Boolean> = _doubleTapSleep.asStateFlow()

    private val _adbNetwork = MutableStateFlow(false)
    val adbNetwork: StateFlow<Boolean> = _adbNetwork.asStateFlow()

    init {
        refreshStates()
    }

    fun refreshStates() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            
            // Standard APIs for status reading where possible
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            _wifiEnabled.value = wifiManager.isWifiEnabled

            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            _btEnabled.value = bluetoothManager.adapter?.isEnabled == true

            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            _nfcEnabled.value = nfcAdapter?.isEnabled == true

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            _gpsEnabled.value = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // Shell for tweaks
            withContext(Dispatchers.IO) {
                _battPercent.value = DeviceUtils.getSystemSetting("system", "status_bar_show_battery_percent") == "1"
                _networkTraffic.value = DeviceUtils.getSystemSetting("system", "network_traffic_state") == "1"
                _doubleTapSleep.value = DeviceUtils.getSystemSetting("system", "status_bar_double_tap_sleep_gesture") == "1"
                _adbNetwork.value = DeviceUtils.executeShell("getprop service.adb.tcp.port").contains("5555")
            }
        }
    }

    fun toggleWifi(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setWifiEnabled(enabled) }
            _wifiEnabled.value = enabled
        }
    }

    fun toggleBluetooth(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setBluetoothEnabled(enabled) }
            _btEnabled.value = enabled
        }
    }

    fun toggleNfc(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setNfcEnabled(enabled) }
            _nfcEnabled.value = enabled
        }
    }

    fun toggleGps(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setLocationEnabled(enabled) }
            _gpsEnabled.value = enabled
        }
    }

    fun toggleBattPercent(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setSystemSetting("system", "status_bar_show_battery_percent", if(enabled) "1" else "0") }
            _battPercent.value = enabled
        }
    }

    fun toggleNetworkTraffic(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setSystemSetting("system", "network_traffic_state", if(enabled) "1" else "0") }
            _networkTraffic.value = enabled
        }
    }

    fun toggleDoubleTapSleep(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setSystemSetting("system", "status_bar_double_tap_sleep_gesture", if(enabled) "1" else "0") }
            _doubleTapSleep.value = enabled
        }
    }

    fun toggleAdbNetwork(enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (enabled) {
                    DeviceUtils.executeShell("setprop service.adb.tcp.port 5555", useRoot = true)
                    DeviceUtils.executeShell("stop adbd", useRoot = true)
                    DeviceUtils.executeShell("start adbd", useRoot = true)
                } else {
                    DeviceUtils.executeShell("setprop service.adb.tcp.port -1", useRoot = true)
                    DeviceUtils.executeShell("stop adbd", useRoot = true)
                    DeviceUtils.executeShell("start adbd", useRoot = true)
                }
            }
            _adbNetwork.value = enabled
        }
    }
}
