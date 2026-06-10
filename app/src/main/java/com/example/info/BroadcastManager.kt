package com.example.info

import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.nio.charset.StandardCharsets

data class DiscoveredDevice(
    val id: String,
    val model: String,
    val manufacturer: String,
    val version: String,
    val lastSeen: Long = System.currentTimeMillis()
)

object BroadcastManager {
    private var PORT = 8888
    private const val TAG = "BroadcastManager"
    
    private var socket: DatagramSocket? = null
    private var broadcastJob: Job? = null
    private var listenJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _discoveredDevices = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())
    val discoveredDevices = _discoveredDevices.asStateFlow()
    
    private val _isBroadcasting = MutableStateFlow(false)
    val isBroadcasting = _isBroadcasting.asStateFlow()

    private var customBroadcastAddress: String? = null

    var gpuRenderer: String = "N/A"
    var gpuVendor: String = "N/A"

    fun setConfig(port: Int, address: String?) {
        this.PORT = port
        this.customBroadcastAddress = address
    }

    fun start(context: Context) {
        if (_isBroadcasting.value) return
        
        try {
            socket = DatagramSocket(PORT).apply {
                broadcast = true
                reuseAddress = true
            }
        } catch (e: SocketException) {
            Log.e(TAG, "Socket creation failed", e)
            return
        }
        
        _isBroadcasting.value = true
        
        broadcastJob = scope.launch {
            while (true) {
                try {
                    val memInfo = DeviceUtils.getMemoryInfo(context)
                    val internalStorage = Environment.getDataDirectory()
                    val batteryIntent = DeviceUtils.getBatteryIntent(context)
                    val screenInfo = DeviceUtils.getScreenInfo(context)
                    val hwDetails = DeviceUtils.getHardwareDetails(context)
                    val wifiInfo = DeviceUtils.getWifiInfo(context)
                    val btInfo = DeviceUtils.getBluetoothInfo(context)
                    val audioInfo = DeviceUtils.getAudioInfo(context)
                    
                    val batteryStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    val batteryHealth = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
                    val batteryVolt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
                    val batteryTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1

                    val cameras = DeviceUtils.getCameraInfo(context).joinToString(";") { "${it.id}(${it.facing}): ${it.megapixels}MP" }
                    val sensors = DeviceUtils.getSensors(context).take(20).joinToString(";") { "${it.name}: ${it.vendor}" }
                    val partitions = DeviceUtils.getPartitions().take(10).joinToString(";") { "${it.name}: ${DeviceUtils.formatStorageSize(it.size)}" }

                    val deviceInfo = listOf(
                        Build.ID,                              // 0
                        Build.MODEL,                           // 1
                        Build.MANUFACTURER,                    // 2
                        Build.VERSION.RELEASE,                 // 3
                        DeviceUtils.getSocInfo(),              // 4
                        DeviceUtils.checkBootloaderStatus(),   // 5
                        DeviceUtils.getSELinuxStatus(),        // 6
                        DeviceUtils.getRootModuleCount().toString(), // 7
                        DeviceUtils.formatStorageSize(DeviceUtils.getTotalStorageSize(internalStorage)), // 8
                        DeviceUtils.formatStorageSize(DeviceUtils.getUsedStorageSize(internalStorage)),  // 9
                        DeviceUtils.formatStorageSize(memInfo.totalMem), // 10
                        DeviceUtils.formatStorageSize(memInfo.totalMem - memInfo.availMem), // 11
                        "${DeviceUtils.getBatteryLevel(context)}%", // 12
                        batteryStatus.toString(),              // 13
                        batteryHealth.toString(),              // 14
                        "${batteryVolt}mV",                    // 15
                        "${batteryTemp / 10f}°C",              // 16
                        screenInfo["resolution"] ?: "N/A",      // 17
                        screenInfo["refresh_rate"] ?: "N/A",    // 18
                        screenInfo["size"] ?: "N/A",            // 19
                        DeviceUtils.getCpuCoreCount().toString(), // 20
                        DeviceUtils.getCurrentTime(),          // 21
                        Build.VERSION.SDK_INT.toString(),      // 22
                        System.getProperty("os.version") ?: "N/A", // 23
                        System.getProperty("java.vm.version") ?: "N/A", // 24
                        DeviceUtils.formatTime(Build.TIME),    // 25
                        Build.HARDWARE,                        // 26
                        DeviceUtils.getGlEsVersion(context),   // 27
                        DeviceUtils.getVulkanVersion(context), // 28
                        DeviceUtils.getOpenCLVersion(),        // 29
                        Build.BOARD,                           // 30
                        hwDetails["Platform"] ?: "N/A",        // 31
                        Build.PRODUCT,                         // 32
                        Build.DEVICE,                          // 33
                        hwDetails["Display ID"] ?: "N/A",      // 34
                        wifiInfo.entries.joinToString(";") { "${it.key}: ${it.value}" }, // 35
                        btInfo.entries.joinToString(";") { "${it.key}: ${it.value}" },   // 36
                        audioInfo.entries.joinToString(";") { "${it.key}: ${it.value}" }, // 37
                        gpuRenderer,                           // 38
                        gpuVendor,                             // 39
                        if (cameras.isEmpty()) "No Cameras Found" else cameras, // 40
                        if (sensors.isEmpty()) "No Sensors Found" else sensors, // 41
                        if (partitions.isEmpty()) "No Partitions Found" else partitions // 42
                    ).joinToString("|")
                    
                    val data = deviceInfo.toByteArray(StandardCharsets.UTF_8)
                    val broadcastAddress = if (!customBroadcastAddress.isNullOrBlank()) {
                        try { InetAddress.getByName(customBroadcastAddress) } catch (e: Exception) { getBroadcastAddress(context) }
                    } else {
                        getBroadcastAddress(context)
                    } ?: return@launch

                    val packet = DatagramPacket(data, data.size, broadcastAddress, PORT)
                    socket?.send(packet)
                } catch (e: Exception) {
                    Log.e(TAG, "Send failed", e)
                }
                delay(500) 
            }
        }
        
        listenJob = scope.launch {
            val buffer = ByteArray(8192) 
            while (true) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)
                    val received = String(packet.data, 0, packet.length, StandardCharsets.UTF_8)
                    val parts = received.split("|")
                    if (parts.size >= 4) {
                        if (parts[0] == Build.ID) continue
                        val device = DiscoveredDevice(
                            id = parts[0],
                            model = parts[1],
                            manufacturer = parts[2],
                            version = parts[3]
                        )
                        updateDevice(device)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Receive failed", e)
                    if (socket?.isClosed == true) break
                }
            }
        }
        
        scope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _discoveredDevices.value = _discoveredDevices.value.filter { it.value.lastSeen > now - 5000 }
                delay(2000)
            }
        }
    }

    private fun updateDevice(device: DiscoveredDevice) {
        val current = _discoveredDevices.value.toMutableMap()
        current[device.id] = device
        _discoveredDevices.value = current
    }

    fun stop() {
        _isBroadcasting.value = false
        broadcastJob?.cancel()
        listenJob?.cancel()
        socket?.close()
        socket = null
    }

    private fun getBroadcastAddress(context: Context): InetAddress? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp: DhcpInfo = wifiManager.dhcpInfo ?: return null
        val broadcast = (dhcp.ipAddress and dhcp.netmask) or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3) quads[k] = ((broadcast shr (k * 8)) and 0xFF).toByte()
        return try {
            InetAddress.getByAddress(quads)
        } catch (e: Exception) {
            InetAddress.getByName("255.255.255.255")
        }
    }
}
