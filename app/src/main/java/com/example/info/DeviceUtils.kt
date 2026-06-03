package com.example.info

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

    fun getIpAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            var ip = "未知"
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        ip = address.hostAddress ?: "未知"
                        break
                    }
                }
                if (ip != "未知") break
            }
            ip
        } catch (e: Exception) {
            "未知"
        }
    }

    fun getNetworkType(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = connectivityManager?.activeNetwork ?: return "无网络"
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "未知"
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "移动数据"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "以太网"
                else -> "其他"
            }
        } catch (e: Exception) {
            "未知"
        }
    }

    fun getRootModuleCount(): Int {
        return try {
            val modulesDir = File("/data/adb/models")
            if (modulesDir.exists() && modulesDir.isDirectory) {
                val modules = modulesDir.listFiles { file -> file.isDirectory }
                modules?.size ?: 0
            } else {
                0
            }
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

    fun formatTime(time: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(time))
        } catch (e: Exception) {
            "未知"
        }
    }
}
