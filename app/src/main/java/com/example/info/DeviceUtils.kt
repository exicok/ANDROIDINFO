package com.example.info

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import android.provider.Settings
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun getProcessCount(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "ps -A | wc -l"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            reader.close()
            process.waitFor()
            val count = line?.trim()?.toInt() ?: 0
            if (count > 0) (count - 1).toString() else "0"
        } catch (e: Exception) {
            "未知"
        }
    }

    fun getMemoryUsage(context: Context): Int {
        val info = getMemoryInfo(context)
        return if (info.totalMem > 0) {
            (((info.totalMem - info.availMem).toDouble() / info.totalMem.toDouble()) * 100).toInt()
        } else 0
    }

    fun getStorageUsagePercentage(): Int {
        val directory = android.os.Environment.getDataDirectory()
        val total = getTotalStorageSize(directory)
        val used = getUsedStorageSize(directory)
        return if (total > 0) ((used.toDouble() / total.toDouble()) * 100).toInt() else 0
    }

    fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    fun getMemoryBrand(): String {
        // 尝试从系统属性获取，不同 OEM 可能有不同 key
        return getSystemProperty("ro.boot.ddr_manuf") ?: getSystemProperty("ro.boot.cpuid") ?: "未知"
    }

    fun getUfsInfo(): String {
        return try {
            val modelFile = File("/sys/class/scsi_host/host0/model")
            if (modelFile.exists()) {
                modelFile.readText().trim()
            } else {
                getSystemProperty("ro.boot.storage_type") ?: "未知"
            }
        } catch (e: Exception) {
            "未知"
        }
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
