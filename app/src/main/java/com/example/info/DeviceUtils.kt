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
import java.net.Inet4Address
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
}
