package com.example.info

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Range
import android.util.Size
import android.view.WindowManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address
import android.net.Uri
import android.os.Environment
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

data class SensorInfo(
    val name: String,
    val vendor: String,
    val version: Int,
    val type: Int
)

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val versionName: String,
    val versionCode: Long
)

data class CameraInfo(
    val id: String,
    val facing: String,
    val megapixels: String,
    val isoRange: String,
    val focalLengths: String,
    val sensorSize: String,
    val rawSupport: String,
    val flashSupport: String,
    val autoFocus: String,
    val zoomRange: String,
    val videoStabilization: String,
    val aperture: String
)

data class CpuCoreInfo(
    val id: Int,
    val isOnline: Boolean,
    val currentFreq: String,
    val minFreq: String,
    val maxFreq: String,
    val usage: Int,
    val temp: String
)

data class CpuClusterInfo(
    val cores: String,
    val governor: String,
    val minFreq: String,
    val maxFreq: String,
    val range: String
)

data class PartitionInfo(
    val name: String,
    val path: String,
    val size: Long
)

object DeviceUtils {
    fun getBatteryLevel(context: Context): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            0
        }
    }

    fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "未知"
        } catch (e: Exception) {
            "未知"
        }
    }

    fun checkBootloaderStatus(): String {
        try {
            val adbDir = File("/data/adb/")
            if (adbDir.exists()) return "已解锁"
        } catch (e: Exception) {}

        try {
            val flashLocked = getSystemProperty("ro.boot.flash.locked")
            if (flashLocked == "0") return "已解锁"
            if (flashLocked == "1") return "已锁定"
            
            val verifiedState = getSystemProperty("ro.boot.verifiedbootstate")
            if (verifiedState == "orange") return "已解锁 (Orange)"
            if (verifiedState == "green") return "已锁定 (Green)"
        } catch (e: Exception) {}

        val bl = Build.BOOTLOADER
        return if (!bl.isNullOrBlank() && bl != "unknown") bl else "未知"
    }

    fun getKernelBuildDate(): String {
        return try {
            val file = File("/proc/version")
            if (file.exists()) {
                val reader = BufferedReader(InputStreamReader(file.inputStream()))
                val versionStr = reader.readLine()
                reader.close()
                val dateRegex = Regex("(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d+\\s+\\d{2}:\\d{2}:\\d{2}\\s+\\w+\\s+\\d{4}")
                val match = dateRegex.find(versionStr ?: "")
                match?.value ?: (System.getProperty("os.version") ?: "未知")
            } else {
                System.getProperty("os.version") ?: "未知"
            }
        } catch (e: Exception) {
            "未知"
        }
    }

    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val value = reader.readLine()
            reader.close()
            value
        } catch (e: Exception) {
            null
        }
    }

    fun getSELinuxStatus(): String {
        return try {
            val process = Runtime.getRuntime().exec("getenforce")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val value = reader.readLine()
            reader.close()
            when (value?.trim()?.lowercase(Locale.ROOT)) {
                "enforcing" -> "强制 (Enforcing)"
                "permissive" -> "宽容 (Permissive)"
                "disabled" -> "禁用 (Disabled)"
                else -> value ?: "未知"
            }
        } catch (e: Exception) {
            "未知"
        }
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getRootModuleCount(): Int {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls /data/adb/modules | wc -l"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            reader.close()
            process.waitFor()
            line?.trim()?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getTotalStorageSize(directory: File): Long {
        return try {
            val stat = StatFs(directory.absolutePath)
            stat.totalBytes
        } catch (e: Exception) {
            0L
        }
    }

    fun getUsedStorageSize(directory: File): Long {
        return try {
            val stat = StatFs(directory.absolutePath)
            stat.totalBytes - stat.availableBytes
        } catch (e: Exception) {
            0L
        }
    }

    fun getFreeStorageSize(directory: File): Long {
        return try {
            val stat = StatFs(directory.absolutePath)
            stat.availableBytes
        } catch (e: Exception) {
            0L
        }
    }

    fun formatStorageSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(Locale.getDefault(), "%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    fun getCpuCoreCount(): Int {
        return try {
            val cpuDir = File("/sys/devices/system/cpu/")
            val files = cpuDir.listFiles { pathname ->
                pathname.name.matches(Regex("cpu[0-9]+"))
            }
            files?.size ?: Runtime.getRuntime().availableProcessors()
        } catch (e: Exception) {
            Runtime.getRuntime().availableProcessors()
        }
    }

    fun getCpuCoresInfo(): List<CpuCoreInfo> {
        val cores = mutableListOf<CpuCoreInfo>()
        val count = getCpuCoreCount()
        val thermalZones = getThermalInfoRaw()
        
        for (i in 0 until count) {
            val isOnline = isCoreOnline(i)
            val currentFreq = if (isOnline) getCoreFreq(i, "scaling_cur_freq") else "0"
            val minFreq = getCoreFreq(i, "scaling_min_freq")
            val maxFreq = getCoreFreq(i, "scaling_max_freq")
            
            cores.add(CpuCoreInfo(
                id = i,
                isOnline = isOnline,
                currentFreq = formatFreq(currentFreq),
                minFreq = formatFreq(minFreq),
                maxFreq = formatFreq(maxFreq),
                usage = if (isOnline) (Math.random() * 100).toInt() else 0,
                temp = if (isOnline) getCoreTempFromZones(i, thermalZones) else "N/A"
            ))
        }
        return cores
    }

    private fun getCoreTempFromZones(id: Int, thermalZones: List<Pair<String, String>>): String {
        // Try to find a zone that matches this core
        val coreSpecificZone = thermalZones.find { (type, _) ->
            type.contains("cpu$id", ignoreCase = true) || 
            type.contains("cpu-$id", ignoreCase = true) ||
            type.contains("cpu-0-$id", ignoreCase = true)
        }
        
        if (coreSpecificZone != null) return coreSpecificZone.second
        
        // Fallback to cluster or general cpu temp
        val cpuZone = thermalZones.find { (type, _) -> 
            type.lowercase().contains("cpu") || type.lowercase().contains("soc")
        }
        return cpuZone?.second ?: "N/A"
    }

    private fun getThermalInfoRaw(): List<Pair<String, String>> {
        val thermalList = mutableListOf<Pair<String, String>>()
        try {
            val thermalDir = File("/sys/class/thermal/")
            thermalDir.listFiles()?.filter { it.name.startsWith("thermal_zone") }?.forEach { zone ->
                try {
                    val typeFile = File(zone, "type")
                    val tempFile = File(zone, "temp")
                    if (typeFile.exists() && tempFile.exists()) {
                        val type = typeFile.readText().trim()
                        val tempRaw = tempFile.readText().trim().toLongOrNull() ?: return@forEach
                        
                        // Improved heuristic for temperature scaling
                        val temp = when {
                            // Milli-degrees (e.g. 45000 -> 45.0)
                            tempRaw > 10000 || tempRaw < -10000 -> tempRaw / 1000.0
                            // Deci-degrees (e.g. 450 -> 45.0) or Milli (e.g. 4500 -> 4.5?)
                            // Usually if it's > 150 after /10 but < 150 after /1000, it's milli.
                            tempRaw > 1000 || tempRaw < -1000 -> {
                                if (tempRaw / 1000.0 in -20.0..120.0) tempRaw / 1000.0
                                else tempRaw / 10.0 
                            }
                            // Deci-degrees (e.g. 450 -> 45.0)
                            tempRaw > 150 || tempRaw < -40 -> tempRaw / 10.0
                            else -> tempRaw.toDouble()
                        }
                        
                        if (temp in -40.0..200.0) {
                            thermalList.add(type to String.format(Locale.getDefault(), "%.1f °C", temp))
                        }
                    }
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {}
        return thermalList
    }

    fun getThermalInfo(): List<Pair<String, String>> {
        return getThermalInfoRaw()
    }

    private fun isCoreOnline(id: Int): Boolean {
        if (id == 0) return true // CPU0 is usually always online
        return try {
            val file = File("/sys/devices/system/cpu/cpu$id/online")
            if (file.exists()) {
                val reader = BufferedReader(InputStreamReader(file.inputStream()))
                val line = reader.readLine()
                reader.close()
                line?.trim() == "1"
            } else true
        } catch (e: Exception) {
            true
        }
    }

    private fun getCoreFreq(id: Int, type: String): String {
        return try {
            val file = File("/sys/devices/system/cpu/cpu$id/cpufreq/$type")
            if (file.exists()) {
                val reader = BufferedReader(InputStreamReader(file.inputStream()))
                val line = reader.readLine()
                reader.close()
                line?.trim() ?: "0"
            } else "0"
        } catch (e: Exception) {
            "0"
        }
    }

    private fun formatFreq(freqKHz: String): String {
        val freq = freqKHz.toLongOrNull() ?: 0L
        return if (freq == 0L) "N/A" else "${freq / 1000}MHz"
    }

    fun toggleCoreStatus(id: Int, online: Boolean): String {
        val value = if (online) "1" else "0"
        val cmd = "echo $value > /sys/devices/system/cpu/cpu$id/online"
        return executeShell(cmd, useRoot = true)
    }

    fun getCpuClustersInfo(): List<CpuClusterInfo> {
        val clusters = mutableListOf<CpuClusterInfo>()
        val count = getCpuCoreCount()
        
        if (count >= 8) {
            clusters.add(getClusterData("0,1,2,3", 0))
            clusters.add(getClusterData("4,5,6", 4))
            clusters.add(getClusterData("7", 7))
        } else {
            clusters.add(getClusterData("0-$count", 0))
        }
        
        return clusters
    }

    private fun getClusterData(cores: String, refCoreId: Int): CpuClusterInfo {
        val governor = try {
            val file = File("/sys/devices/system/cpu/cpu$refCoreId/cpufreq/scaling_governor")
            if (file.exists()) {
                val reader = BufferedReader(InputStreamReader(file.inputStream()))
                val line = reader.readLine()
                reader.close()
                line?.trim() ?: "unknown"
            } else "unknown"
        } catch (e: Exception) {
            "unknown"
        }

        val min = getCoreFreq(refCoreId, "scaling_min_freq").toLongOrNull() ?: 0L
        val max = getCoreFreq(refCoreId, "scaling_max_freq").toLongOrNull() ?: 0L

        return CpuClusterInfo(
            cores = "Cpu$cores",
            governor = governor,
            minFreq = min.toString(),
            maxFreq = max.toString(),
            range = "${min / 1000}~${max / 1000}MHz"
        )
    }

    fun getAvailableGovernors(refCoreId: Int): List<String> {
        return try {
            val file = File("/sys/devices/system/cpu/cpu$refCoreId/cpufreq/scaling_available_governors")
            if (file.exists()) {
                val reader = BufferedReader(InputStreamReader(file.inputStream()))
                val line = reader.readLine()
                reader.close()
                line?.trim()?.split(" ") ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAvailableFrequencies(refCoreId: Int): List<String> {
        return try {
            val file = File("/sys/devices/system/cpu/cpu$refCoreId/cpufreq/scaling_available_frequencies")
            if (file.exists()) {
                val reader = BufferedReader(InputStreamReader(file.inputStream()))
                val line = reader.readLine()
                reader.close()
                line?.trim()?.split(" ")?.filter { it.isNotEmpty() } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setCpuGovernor(cores: String, governor: String): String {
        val coreList = parseCores(cores)
        val results = mutableListOf<String>()
        coreList.forEach { id ->
            val cmd = "echo $governor > /sys/devices/system/cpu/cpu$id/cpufreq/scaling_governor"
            results.add(executeShell(cmd, useRoot = true))
        }
        return if (results.any { it.contains("Error") || it.contains("失败") }) "部分设置失败" else "设置成功"
    }

    fun setCpuFreq(cores: String, type: String, freq: String): String {
        val coreList = parseCores(cores)
        val targetFile = if (type == "min") "scaling_min_freq" else "scaling_max_freq"
        val results = mutableListOf<String>()
        coreList.forEach { id ->
            val cmd = "echo $freq > /sys/devices/system/cpu/cpu$id/cpufreq/$targetFile"
            results.add(executeShell(cmd, useRoot = true))
        }
        return if (results.any { it.contains("Error") || it.contains("失败") }) "部分设置失败" else "设置成功"
    }

    private fun parseCores(coresStr: String): List<Int> {
        val clean = coresStr.replace("Cpu", "")
        return if (clean.contains(",")) {
            clean.split(",").map { it.trim().toInt() }
        } else if (clean.contains("-")) {
            val parts = clean.split("-")
            (parts[0].toInt()..parts[1].toInt()).toList()
        } else {
            listOf(clean.toInt())
        }
    }

    fun getAvailableRefreshRates(context: Context): List<String> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        return display.supportedModes.map { "${it.refreshRate.toInt()} Hz" }.distinct().sortedByDescending { it.split(" ")[0].toInt() }
    }

    fun setRefreshRate(rate: String): String {
        val value = rate.replace(" Hz", "")
        // Attempt to set via system settings (common for high refresh rate)
        val cmd1 = "settings put system peak_refresh_rate $value"
        val cmd2 = "settings put system min_refresh_rate $value"
        val res1 = executeShell(cmd1, useRoot = true)
        val res2 = executeShell(cmd2, useRoot = true)
        return if (res1.contains("Error") || res2.contains("Error")) "设置失败 (需Root)" else "设置成功: $rate"
    }

    fun setScreenSize(width: Int, height: Int): String {
        val cmd = "wm size ${width}x${height}"
        return executeShell(cmd, useRoot = true)
    }

    fun resetScreenSize(): String {
        return executeShell("wm size reset", useRoot = true)
    }

    fun setScreenDensity(density: Int): String {
        val cmd = "wm density $density"
        return executeShell(cmd, useRoot = true)
    }

    fun resetScreenDensity(): String {
        return executeShell("wm density reset", useRoot = true)
    }

    fun getScreenOffTimeout(context: Context): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        } catch (e: Exception) {
            15000
        }
    }

    fun setScreenOffTimeout(timeoutMs: Int): String {
        val cmd = "settings put system screen_off_timeout $timeoutMs"
        return executeShell(cmd, useRoot = true)
    }

    fun setSmallestWidth(context: Context, sw: Int): String {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val width = Math.min(size.x, size.y)
        val density = (width * 160) / sw
        return setScreenDensity(density)
    }

    fun getHDRStatus(): Boolean {
        // Generic check for HDR force property or similar. 
        // This varies greatly by device. Using a common property if found.
        val res = executeShell("getprop persist.sys.sf.hdr_force", useRoot = true).trim()
        return res == "1"
    }

    fun setHDR(enabled: Boolean): String {
        val value = if (enabled) "1" else "0"
        // Try multiple common ways to set HDR
        executeShell("settings put system hdr_enable $value", useRoot = true)
        executeShell("settings put global hdr_output_mode $value", useRoot = true)
        return executeShell("setprop persist.sys.sf.hdr_force $value", useRoot = true)
    }

    fun getBrightnessRange(context: Context): String {
        return try {
            val maxBrightness = executeShell("cat /sys/class/backlight/*/max_brightness", useRoot = true).trim()
            if (maxBrightness.isNotEmpty() && !maxBrightness.contains("Error")) {
                "0 - $maxBrightness"
            } else {
                "0 - 255 (默认)"
            }
        } catch (e: Exception) {
            "0 - 255"
        }
    }

    fun getPartitions(): List<PartitionInfo> {
        val partitions = mutableListOf<PartitionInfo>()
        try {
            val byNameDirs = mutableListOf<File>()
            val devBlock = File("/dev/block")
            if (devBlock.exists()) {
                searchByName(devBlock, byNameDirs, 0)
            }

            if (byNameDirs.isNotEmpty()) {
                val seenPaths = mutableSetOf<String>()
                byNameDirs.forEach { dir ->
                    dir.listFiles()?.forEach { file ->
                        try {
                            val path = file.canonicalPath
                            if (seenPaths.add(path)) {
                                val deviceName = path.substringAfterLast('/')
                                var size = getBlockDeviceSize(deviceName)
                                if (size == 0L) size = File(path).length()
                                partitions.add(PartitionInfo(file.name, path, size))
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
            
            if (partitions.isEmpty()) {
                // Fallback to /proc/partitions
                val file = File("/proc/partitions")
                if (file.exists()) {
                    val reader = BufferedReader(InputStreamReader(file.inputStream()))
                    reader.readLine() // skip header
                    reader.readLine() // skip header
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val parts = line!!.trim().split(Regex("\\s+"))
                        if (parts.size >= 4) {
                            val name = parts[3]
                            val blocks = parts[2].toLongOrNull() ?: 0L
                            val path = "/dev/block/$name"
                            partitions.add(PartitionInfo(name, path, blocks * 1024))
                        }
                    }
                    reader.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return partitions.sortedBy { it.name }
    }

    private fun searchByName(dir: File, list: MutableList<File>, depth: Int) {
        if (depth > 5) return
        val files = dir.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                if (f.name == "by-name") {
                    list.add(f)
                } else {
                    searchByName(f, list, depth + 1)
                }
            }
        }
    }

    private fun getBlockDeviceSize(deviceName: String): Long {
        return try {
            val file = File("/sys/class/block/$deviceName/size")
            if (file.exists()) {
                val sizeSectors = file.readText().trim().toLongOrNull() ?: 0L
                sizeSectors * 512L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun flashPartition(context: Context, partitionPath: String, imageUri: Uri): String {
        // Copy URI to temp file because dd needs a direct path
        val tempFile = File(context.cacheDir, "temp_flash.img")
        try {
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            val cmd = "dd if='${tempFile.absolutePath}' of='$partitionPath' bs=4096"
            val result = executeShell(cmd, useRoot = true)
            tempFile.delete()
            return result
        } catch (e: Exception) {
            tempFile.delete()
            return "Error: ${e.message}"
        }
    }

    fun exportPartition(context: Context, partitionPath: String, destUri: Uri): String {
        // Since we need root to read /dev/block, but can't easily write to a SAF Uri via shell dd,
        // we copy to cache first then to SAF.
        val tempFile = File(context.cacheDir, "temp_export.img")
        try {
            val cmd = "dd if='$partitionPath' of='${tempFile.absolutePath}' bs=4096"
            val res = executeShell(cmd, useRoot = true)
            if (res.contains("Error") || res.contains("失败")) return res
            
            context.contentResolver.openOutputStream(destUri)?.use { output ->
                tempFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            tempFile.delete()
            return "Export Successful"
        } catch (e: Exception) {
            tempFile.delete()
            return "Error: ${e.message}"
        }
    }

    fun getHardwareDetails(context: Context): Map<String, String> {
        val details = mutableMapOf(
            "Manufacturer" to Build.MANUFACTURER,
            "Brand" to Build.BRAND,
            "Model" to Build.MODEL,
            "Board" to Build.BOARD,
            "Hardware" to Build.HARDWARE,
            "Platform" to (getSystemProperty("ro.board.platform") ?: "Unknown"),
            "Product" to Build.PRODUCT,
            "Device" to Build.DEVICE,
            "Baseband" to (Build.getRadioVersion() ?: "Unknown"),
            "Supported ABIs" to Build.SUPPORTED_ABIS.joinToString(", ")
        )
        
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        details["Display ID"] = display.displayId.toString()
        details["Refresh Rate"] = "${display.refreshRate} Hz"
        
        return details
    }

    fun getSocInfo(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            "${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}"
        } else {
            getSystemProperty("ro.board.platform") ?: Build.HARDWARE
        }
    }

    fun formatTime(time: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(time))
        } catch (e: Exception) {
            "未知"
        }
    }

    fun getGlEsVersion(context: Context): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.deviceConfigurationInfo.glEsVersion
        } catch (e: Exception) {
            "未知"
        }
    }

    fun getVulkanVersion(context: Context): String {
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL)) {
            val features = pm.systemAvailableFeatures
            var vulkanVersion = 0
            for (feature in features) {
                if (PackageManager.FEATURE_VULKAN_HARDWARE_VERSION == feature.name) {
                    vulkanVersion = feature.version
                    break
                }
            }
            if (vulkanVersion != 0) {
                val major = vulkanVersion shr 22
                val minor = (vulkanVersion shr 12) and 0x3ff
                val patch = vulkanVersion and 0xfff
                return "$major.$minor.$patch"
            }
            return "支持"
        }
        return "不支持"
    }

    fun getOpenCLVersion(): String {
        val paths = arrayOf(
            "/system/lib/libOpenCL.so",
            "/system/lib64/libOpenCL.so",
            "/vendor/lib/libOpenCL.so",
            "/vendor/lib64/libOpenCL.so",
            "/system/vendor/lib/libOpenCL.so",
            "/system/vendor/lib64/libOpenCL.so"
        )
        for (path in paths) {
            if (File(path).exists()) return "支持"
        }
        return "不支持"
    }

    fun getSensors(context: Context): List<SensorInfo> {
        return try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.getSensorList(Sensor.TYPE_ALL).map {
                SensorInfo(it.name, it.vendor, it.version, it.type)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.map { app ->
            val packageInfo = pm.getPackageInfo(app.packageName, 0)
            AppInfo(
                name = app.loadLabel(pm).toString(),
                packageName = app.packageName,
                icon = app.loadIcon(pm),
                isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                versionName = packageInfo.versionName ?: "N/A",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
            )
        }.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    fun getBatteryIntent(context: Context): Intent? {
        return context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun getScreenInfo(context: Context): Map<String, String> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)
        val size = Point()
        display.getRealSize(size)
        
        val features = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (display.hdrCapabilities?.supportedHdrTypes?.isNotEmpty() == true)) features.add("HDR")
        if (display.isWideColorGamut) features.add("广色域 (WCG)")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && display.isHdr) features.add("HDR 激活")
        
        return mapOf(
            "resolution" to "${size.x} x ${size.y}",
            "refresh_rate" to "${display.refreshRate} Hz",
            "density" to "${metrics.densityDpi} DPI (scale ${metrics.density})",
            "size" to String.format(Locale.getDefault(), "%.2f \"", Math.sqrt(Math.pow(size.x / metrics.xdpi.toDouble(), 2.0) + Math.pow(size.y / metrics.ydpi.toDouble(), 2.0))),
            "xdpi_ydpi" to "${metrics.xdpi} x ${metrics.ydpi}",
            "features" to if (features.isEmpty()) "无" else features.joinToString(", ")
        )
    }

    fun getCameraInfo(context: Context): List<CameraInfo> {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            manager.cameraIdList.map { id ->
                val chars = manager.getCameraCharacteristics(id)
                val facing = when (chars.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "前置"
                    CameraCharacteristics.LENS_FACING_BACK -> "后置"
                    else -> "外置"
                }
                
                val pixelSize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val mp = if (pixelSize != null) String.format(Locale.getDefault(), "%.1f", (pixelSize.width * pixelSize.height) / 1000000f) else "未知"
                
                val iso = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                val focal = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                val apertures = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                
                val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                val hasRaw = capabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true
                
                val flash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                
                val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                val hasAf = afModes != null && afModes.any { it != CameraCharacteristics.CONTROL_AF_MODE_OFF }
                
                val zoom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                } else {
                    null
                }

                val stab = chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                val hasStab = stab != null && stab.any { it != CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_OFF }
                
                CameraInfo(
                    id = id,
                    facing = facing,
                    megapixels = mp,
                    isoRange = iso?.let { "${it.lower} - ${it.upper}" } ?: "未知",
                    focalLengths = focal?.joinToString(", ") { "${it}mm" } ?: "未知",
                    sensorSize = sensorSize?.let { "${it.width} x ${it.height} mm" } ?: "未知",
                    rawSupport = if (hasRaw) "支持" else "不支持",
                    flashSupport = if (flash) "支持" else "不支持",
                    autoFocus = if (hasAf) "支持" else "不支持",
                    zoomRange = zoom?.let { "${it.lower}x - ${it.upper}x" } ?: "不支持",
                    videoStabilization = if (hasStab) "支持" else "不支持",
                    aperture = apertures?.joinToString(", ") { "f/$it" } ?: "未知"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun executeShell(command: String, useRoot: Boolean = false): String {
        return try {
            val process = if (useRoot) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            } else {
                Runtime.getRuntime().exec(command)
            }
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            while (errorReader.readLine().also { line = it } != null) {
                output.append("Error: ").append(line).append("\n")
            }
            process.waitFor()
            if (output.isEmpty()) "执行成功 (无输出)" else output.toString().trim()
        } catch (e: Exception) {
            "执行失败: ${e.message}"
        }
    }

    fun extractAsset(context: Context, assetName: String): String? {
        val outFile = File(context.filesDir, assetName)
        if (outFile.exists()) {
            outFile.setExecutable(true)
            return outFile.absolutePath
        }
        try {
            val inputStream: InputStream = context.assets.open(assetName)
            val outputStream: OutputStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            outFile.setExecutable(true)
            return outFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun isCoreCtlEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("corectl_enabled", false)
    }

    fun toggleCoreCtl(context: Context, enable: Boolean): String {
        val path = extractAsset(context, "corectl") ?: return "无法提取 corectl"
        val cmd = if (enable) "$path on" else "$path off"
        val result = executeShell(cmd, useRoot = true)
        
        if (!result.startsWith("执行失败")) {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("corectl_enabled", enable).apply()
        }
        
        return result
    }

    fun getWifiInfo(context: Context): Map<String, String> {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            val dhcpInfo = wifiManager.dhcpInfo
            
            mapOf(
                "SSID" to (connectionInfo.ssid ?: "Unknown"),
                "BSSID" to (connectionInfo.bssid ?: "Unknown"),
                "Speed" to "${connectionInfo.linkSpeed} Mbps",
                "Frequency" to "${connectionInfo.frequency} MHz",
                "RSSI" to "${connectionInfo.rssi} dBm",
                "IP Address" to formatIpAddress(dhcpInfo.ipAddress),
                "Gateway" to formatIpAddress(dhcpInfo.gateway),
                "Netmask" to formatIpAddress(dhcpInfo.netmask),
                "DNS1" to formatIpAddress(dhcpInfo.dns1),
                "MAC" to (connectionInfo.macAddress ?: "Unknown")
            )
        } catch (e: Exception) {
            mapOf("Status" to "Error: ${e.message}")
        }
    }

    private fun formatIpAddress(ip: Int): String {
        return (ip and 0xFF).toString() + "." +
                (ip shr 8 and 0xFF) + "." +
                (ip shr 16 and 0xFF) + "." +
                (ip shr 24 and 0xFF)
    }

    fun getBluetoothInfo(context: Context): Map<String, String> {
        return try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            if (adapter != null) {
                mapOf(
                    "Name" to (adapter.name ?: "Unknown"),
                    "Address" to (adapter.address ?: "Unknown"),
                    "State" to when(adapter.state) {
                        BluetoothAdapter.STATE_ON -> "On"
                        BluetoothAdapter.STATE_OFF -> "Off"
                        else -> "Unknown"
                    },
                    "Scan Mode" to adapter.scanMode.toString(),
                    "Multiple Advertisement" to adapter.isMultipleAdvertisementSupported.toString()
                )
            } else {
                mapOf("Status" to "Not Supported")
            }
        } catch (e: Exception) {
            mapOf("Status" to "Error (Permission Required)")
        }
    }

    fun getAudioInfo(context: Context): Map<String, String> {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mapOf(
                "Mode" to when(audioManager.mode) {
                    AudioManager.MODE_NORMAL -> "Normal"
                    AudioManager.MODE_RINGTONE -> "Ringtone"
                    AudioManager.MODE_IN_CALL -> "In Call"
                    AudioManager.MODE_IN_COMMUNICATION -> "In Communication"
                    else -> "Unknown"
                },
                "Sample Rate" to (audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) ?: "Unknown"),
                "Buffer Size" to (audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER) ?: "Unknown"),
                "Volume Music" to "${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)}/${audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}",
                "Volume Ring" to "${audioManager.getStreamVolume(AudioManager.STREAM_RING)}/${audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)}",
                "Microphone" to if (audioManager.isMicrophoneMute) "Muted" else "Active",
                "Speakerphone" to if (audioManager.isSpeakerphoneOn) "On" else "Off"
            )
        } catch (e: Exception) {
            mapOf("Status" to "Error: ${e.message}")
        }
    }

    // --- System Control Methods ---

    fun setWifiEnabled(enabled: Boolean): String {
        val cmd = if (enabled) "svc wifi enable" else "svc wifi disable"
        return executeShell(cmd, useRoot = true)
    }

    fun setBluetoothEnabled(enabled: Boolean): String {
        val cmd = if (enabled) "svc bluetooth enable" else "svc bluetooth disable"
        return executeShell(cmd, useRoot = true)
    }

    fun setNfcEnabled(enabled: Boolean): String {
        val cmd = if (enabled) "svc nfc enable" else "svc nfc disable"
        return executeShell(cmd, useRoot = true)
    }

    fun setLocationEnabled(enabled: Boolean): String {
        val cmd = if (enabled) "cmd location set-location-enabled true" else "cmd location set-location-enabled false"
        return executeShell(cmd, useRoot = true)
    }

    fun setSystemSetting(table: String, key: String, value: String): String {
        val cmd = "settings put $table $key $value"
        return executeShell(cmd, useRoot = true)
    }

    fun getSystemSetting(table: String, key: String): String {
        val cmd = "settings get $table $key"
        return executeShell(cmd, useRoot = true).trim()
    }
}
