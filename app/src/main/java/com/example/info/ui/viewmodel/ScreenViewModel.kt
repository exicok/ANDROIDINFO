package com.example.info.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.info.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val _screenInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val screenInfo: StateFlow<Map<String, String>> = _screenInfo.asStateFlow()

    private val _screenTimeout = MutableStateFlow(0)
    val screenTimeout: StateFlow<Int> = _screenTimeout.asStateFlow()

    init {
        refreshInfo()
    }

    fun refreshInfo() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val info = withContext(Dispatchers.IO) { DeviceUtils.getScreenInfo(context) }.toMutableMap()
            info["brightness_range"] = withContext(Dispatchers.IO) { DeviceUtils.getBrightnessRange(context) }
            _screenInfo.value = info
            _screenTimeout.value = DeviceUtils.getScreenOffTimeout(context)
        }
    }

    fun setResolution(width: Int, height: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setScreenSize(width, height) }
            refreshInfo()
        }
    }

    fun resetResolution() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.resetScreenSize() }
            refreshInfo()
        }
    }

    fun setDensity(density: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setScreenDensity(density) }
            refreshInfo()
        }
    }

    fun resetDensity() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.resetScreenDensity() }
            refreshInfo()
        }
    }

    fun setSmallestWidth(sw: Int) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            withContext(Dispatchers.IO) { DeviceUtils.setSmallestWidth(context, sw) }
            refreshInfo()
        }
    }

    fun setRefreshRate(rate: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setRefreshRate(rate) }
            refreshInfo()
        }
    }

    fun setScreenTimeout(timeoutMs: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DeviceUtils.setScreenOffTimeout(timeoutMs) }
            _screenTimeout.value = timeoutMs
        }
    }
}
